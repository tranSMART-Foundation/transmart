/*
 * This class populates the range of options available for the Query tab.
 * This includes the models, SNP or Gene Annotation source, etc.
 */
package com.pfizer.mrbt.genomics.TransmartClient;

import com.pfizer.mrbt.genomics.webservices.GeneSourceOption;
import com.pfizer.mrbt.genomics.webservices.DbSnpSourceOption;
import com.pfizer.mrbt.genomics.webservices.ModelOption;
import com.pfizer.mrbt.genomics.webservices.Environment;
import com.pfizer.mrbt.genomics.webservices.RetrievalException;
import com.pfizer.mrbt.genomics.webservices.RetrievalMethod;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author henstockpv
 */
public class TransmartQueryParameterFetch {

    //public final static int MAX_STUDY_NAME_LENGTH = 26;
    public final static int MAX_STUDY_NAME_LENGTH = 70;
    protected static Logger log = Logger.getLogger(com.pfizer.mrbt.genomics.TransmartClient.TransmartQueryParameterFetch.class.getName());
    private Environment environment;

    /**
     * Initializes the logger and the host/port
     */
    public TransmartQueryParameterFetch(Environment environment) {
        this.environment = environment;
        log.setLevel(Level.DEBUG);
    }

    /**
     * Receives a list of ModelOption that contain the study name, set name, and
     * the id associated with them for inclusion in the query pane.  If any of
     * the models have an invalid modelID or wrong set of columns, then the model
     * is suppressed and a message sent to screen.  If the service fails, it throws
     * the exception below
     * @throws RetrievalException if web search for models fails
     * @return list of ModelOption containing the phenotype/models
     */
    public List<ModelOption> fetchModelOptions() throws RetrievalException {
        String queryStr = TransmartServicesParameters.getServerURL(environment) + TransmartServicesParameters.MODEL_FETCH_METHOD;
        HashMap<String, String> paramMap = new HashMap<String, String>();
        if(TransmartServicesParameters.MODEL_FETCH_METHOD.startsWith("getSecure")) {
            paramMap.put("user", getUserName()); //kluge for security
        }
        paramMap.put("dataType", TransmartServicesParameters.MODEL_FETCH_GWAS_DATA_TYPE + "");
        String queryStrWtihParams = TransmartUtil.addParametersToUrl(queryStr, paramMap);
        System.out.println("Fetch Model options query:" + queryStrWtihParams);
        List<ModelOption> modelOptions = new ArrayList<ModelOption>();
        try {
            String xmlResult = TransmartUtil.fetchResult(queryStrWtihParams);

            ArrayList<ArrayList<String>> queryResults = TransmartUtil.parseXml(xmlResult);
            for (ArrayList<String> row : queryResults) {
                try {
                    ModelOption modelOption = parseModelOption(row);
                    if (! modelOption.getAnalysisName().matches("^[tT][eE][sS][tT].*")) {
                        modelOptions.add(modelOption);
                    }
                } catch (NumberFormatException nfe) {
                    System.err.println("Failed to convert " + row.get(TransmartServicesParameters.MODEL_FETCH_ID_COL) + " into long");
                } catch(IndexOutOfBoundsException ioobe) {
                    System.err.println("Index out of bounds in parsing " + row + "\t" + ioobe.getMessage());
                }
            }
        } catch (UniformInterfaceException uiex) {
            throw new RetrievalException(uiex.getMessage(), RetrievalMethod.MODELS_SEARCH, paramMap);
        } catch (ClientHandlerException chex) {
            throw new RetrievalException(chex.getMessage(), RetrievalMethod.MODELS_SEARCH, paramMap);
        } catch (Exception ex) {
            throw new RetrievalException(ex.getMessage(), RetrievalMethod.MODELS_SEARCH, paramMap);
            //ex.printStackTrace();
        }

        Collections.sort(modelOptions);
        return modelOptions;
    }
    
    /**
     * Parses a row from the query result and converts it into a ModelOption
     * class that it returns. 
     * @param row
     * @return
     * @throws IndexOutOfBoundsException if the expected row data is inconsistent with expected parameters
     * @throws NumberFormatException if the model id is not a long
     */
    protected ModelOption parseModelOption(ArrayList<String> row) throws NumberFormatException, IndexOutOfBoundsException {
        String[] tokens = row.get(TransmartServicesParameters.MODEL_FETCH_STUDY_SET_MODEL_NAME_COL).split("\\s+\\-\\s+");
        String studyName;
        String setName;
        String modelName;
        if (tokens.length < 3) {
                // if the MODEL_FETCH_STUDY_SET_MODEL_NAME doesn't have the form x - y - z then
            // need to take the 3rd - 2nd - 1st line and re-represent it
            studyName = row.get(TransmartServicesParameters.MODEL_FETCH_STUDY_NAME_COL);
            setName = row.get(TransmartServicesParameters.MODEL_FETCH_SET_NAME_COL);
            modelName = row.get(TransmartServicesParameters.MODEL_FETCH_MODEL_NAME_COL);
        } else if (tokens.length > 3) {
                // Probably have something in form of study - more study - more study - set - model
            // Need to join the left-most ones as study
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tokens.length - 2; i++) {
                if (i > 0) {
                    sb.append(" - ");
                }
                sb.append(tokens[i]);
            }
            studyName = sb.toString();
            setName = tokens[tokens.length - 2];
            modelName = tokens[tokens.length - 1];
        } else { // using the 2nd line of MODEL_FETCH_STUDY_SET_MODEL_NAME to represent the name
            studyName = tokens[0];
            setName = tokens[1];
            modelName = tokens[2];
        }
        long studyId = Long.parseLong(row.get(TransmartServicesParameters.MODEL_FETCH_ID_COL));
        if (studyName.length() > MAX_STUDY_NAME_LENGTH) {
            studyName = studyName.substring(0, MAX_STUDY_NAME_LENGTH);
        }
        return new ModelOption(studyName, setName, modelName, studyId);
    }
    
    /**
     * Returns a list of DbSnpSourceOption that contains the id and the name of the
     * dbSnp available through the bioservice
     * @throws RetrievalException if web service fails
     * @return 
     */
    public List<DbSnpSourceOption> fetchtDbSnpSources() throws RetrievalException {
        String queryStr = TransmartServicesParameters.getServerURL(environment) + TransmartServicesParameters.DB_SNP_SOURCE_FETCH_METHOD;
        List<DbSnpSourceOption> dbSnpOptions = new ArrayList<DbSnpSourceOption>();
        try {
            String xmlResult = TransmartUtil.fetchResult(queryStr);
            ArrayList<ArrayList<String>> queryResults = TransmartUtil.parseXml(xmlResult);
            for (List<String> row : queryResults) {
                int id;
                try {
                    id = Integer.parseInt(row.get(TransmartServicesParameters.DB_SNP_FETCH_SOURCE_ID_COL));
                } catch (NumberFormatException nfe) {
                    id = -1;
                    System.err.println("Failed to get id for dbSnpSource: " + row.get(TransmartServicesParameters.DB_SNP_FETCH_SOURCE_ID_COL));
                }
                String name = row.get(TransmartServicesParameters.DB_SNP_FETCH_SOURCE_NAME_COL);
                String date = row.get(TransmartServicesParameters.DB_SNP_FETCH_SOURCE_DATE_COL);
                dbSnpOptions.add(new DbSnpSourceOption(id, name + " " + date));
            }
        } catch (UniformInterfaceException uiex) {
            HashMap<String, String> paramMap = new HashMap<String, String>();
            throw new RetrievalException(uiex.getMessage(), RetrievalMethod.DB_SNP_SOURCES_SEARCH, paramMap);
        } catch (ClientHandlerException chex) {
            HashMap<String, String> paramMap = new HashMap<String, String>();
            throw new RetrievalException(chex.getMessage(), RetrievalMethod.DB_SNP_SOURCES_SEARCH, paramMap);
        } catch (Exception ex) {
            HashMap<String, String> paramMap = new HashMap<String, String>();
            throw new RetrievalException(ex.getMessage(), RetrievalMethod.DB_SNP_SOURCES_SEARCH, paramMap);
        }
        Collections.sort(dbSnpOptions);
        return dbSnpOptions;
    }
    
    /**
     * Returns a list of DbSnpSourceOption that contains the id and the name of the
     * dbSnp available through the bioservice
     * @throws RetrievalException if web service fails
     * @return 
     */
    public List<GeneSourceOption> getGeneSources() throws RetrievalException {
        String queryStr = TransmartServicesParameters.getServerURL(environment) + TransmartServicesParameters.GENE_SOURCE_FETCH_METHOD;
        
        List<GeneSourceOption> geneSourceOptions = new ArrayList<GeneSourceOption>();
        try {
            String xmlResult = TransmartUtil.fetchResult(queryStr);

            ArrayList<ArrayList<String>> queryResults = TransmartUtil.parseXml(xmlResult);
            for (List<String> row : queryResults) {
                int id;
                try {
                    id = Integer.parseInt(row.get(TransmartServicesParameters.GENE_SOURCE_FETCH_ID_COL));
                } catch (NumberFormatException nfe) {
                    id = -1;
                    System.err.println("Failed to get id for geneSourceOption: " + row.get(TransmartServicesParameters.GENE_SOURCE_FETCH_ID_COL));
                }
                String name = row.get(TransmartServicesParameters.GENE_SOURCE_FETCH_NAME_COL);
                String date = row.get(TransmartServicesParameters.GENE_SOURCE_FETCH_DATE_COL);
                geneSourceOptions.add(new GeneSourceOption(id, name + " " + date));
            }
        } catch (UniformInterfaceException uiex) {
            HashMap<String, String> paramMap = new HashMap<String, String>();
            throw new RetrievalException(uiex.getMessage(), RetrievalMethod.GENE_SOURCES_SEARCH, paramMap);
        } catch (ClientHandlerException chex) {
            HashMap<String, String> paramMap = new HashMap<String, String>();
            throw new RetrievalException(chex.getMessage(), RetrievalMethod.GENE_SOURCES_SEARCH, paramMap);
        } catch (Exception ex) {
            HashMap<String, String> paramMap = new HashMap<String, String>();
            throw new RetrievalException(ex.getMessage(), RetrievalMethod.GENE_SOURCES_SEARCH, paramMap);
        }
        return geneSourceOptions;
    }
    
    /**
     * Try to retrieve user name using different techniques.  
     * 
     * @return 
     */
    protected String getUserName() {
    	
    	// if session id is provided then this will be used for communicating with transmart
    	if (TransmartServicesParameters.SESSION_ID != null && TransmartServicesParameters.SESSION_ID.length() > 0)
    		return TransmartServicesParameters.SESSION_ID;
        
        String userName = System.getProperty("user.name");
        if(userName != null && ! userName.isEmpty()) {
            return userName;
        }
        return "";
    }

}
