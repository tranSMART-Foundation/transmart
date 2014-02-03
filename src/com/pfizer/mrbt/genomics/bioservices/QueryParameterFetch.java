/*
 * This class populates the range of options available for the Query tab.
 * This includes the models, SNP or Gene Annotation source, etc.
 */
package com.pfizer.mrbt.genomics.bioservices;

import com.pfizer.tnb.api.server.util.BioServicesInitParams;
import com.pfizer.tnb.api.server.util.QueryResult;
import com.pfizer.tnb.bsutil.BsServiceClientImpl;
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
public class QueryParameterFetch  extends BsServiceClientImpl { 
    protected static Logger log = Logger.getLogger(com.pfizer.mrbt.genomics.bioservices.AmiDemo.class.getName());    

    /**
     * Initializes the logger and the host/port
     */
    public QueryParameterFetch() {
        super();
        log.setLevel(Level.DEBUG);
        BioServicesInitParams initParams = new BioServicesInitParams();
        initParams.setBioServicesServer(BioservicesParameters.SERVER_URL);
        initParams.setServer(BioservicesParameters.HOST);
        initParams.setPort(BioservicesParameters.PORT);
        setInitParams(initParams);
    }
    
    /**
     * Receives a list of ModelOption that contain the study name, set name, and
     * the id associated with them for inclusion in the query pane
     * @return 
     */
    public List<ModelOption> fetchModelOptions() {
        int service_id = BioservicesParameters.MODEL_FETCH_SERVICE_ID;
        String queryStr = BioservicesParameters.SERVER_URL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("DATA_TYPE", BioservicesParameters.MODEL_FETCH_DATA_TYPE);
        String queryStrWithParams = BioservicesParameters.addParametersToUrl(queryStr, paramMap);        
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        //QueryResult queryResults = getData(queryStr, service_id, -1, -1, true);
        List<ModelOption> modelOptions = new ArrayList<ModelOption>();
        int klugeStudyIdIndex = 1;
        for(List<String> row : queryResults.getData()) {
            String studyName = row.get(BioservicesParameters.MODEL_FETCH_STUDY_NAME_COL);
            String setName = row.get(BioservicesParameters.MODEL_FETCH_SET_NAME_COL);
            String modelName = row.get(BioservicesParameters.MODEL_FETCH_MODEL_NAME_COL);
            int studyId = Integer.parseInt(row.get(BioservicesParameters.MODEL_FETCH_ID_COL));
            modelOptions.add(new ModelOption(studyName, setName, modelName, studyId));
            //int studyId = Integer.parseInt(row.get(BioservicesParameters.MODEL_FETCH_ID_COL));
            ////int studyId = getStudyId(studyName, setName);
            //modelOptions.add(new ModelOption(studyName, setName, studyId));
            //modelOptions.add(new ModelOption(studyName, setName, klugeStudyIdIndex++));
        }
        Collections.sort(modelOptions);
        return modelOptions;
    }
    
    /**
     * Receives a list of ModelOption that contain the study name, set name, and
     * the id associated with them for inclusion in the query pane
     * @return 
     * @deprecated
     */
    public List<ModelOption> oldFetchModelOptions() {
        int service_id = BioservicesParameters.MODEL_FETCH_SERVICE_ID;
        String queryStr = BioservicesParameters.SERVER_URL + "service=" + service_id + "&SERVICE_RENDERID=7";
        System.out.println("fetching query Result for " + service_id);
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("DATA_TYPE", BioservicesParameters.MODEL_FETCH_DATA_TYPE);
        String queryStrWithParams = BioservicesParameters.addParametersToUrl(queryStr, paramMap);        
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        List<ModelOption> modelOptions = new ArrayList<ModelOption>();
        int klugeStudyIdIndex = 1;
        for(List<String> row : queryResults.getData()) {
            String studyName = row.get(BioservicesParameters.MODEL_FETCH_STUDY_NAME_COL);
            String setName = row.get(BioservicesParameters.MODEL_FETCH_SET_NAME_COL);
            String modelName = row.get(BioservicesParameters.MODEL_FETCH_MODEL_NAME_COL);
            int studyId = Integer.parseInt(row.get(BioservicesParameters.MODEL_FETCH_ID_COL));
            ////int studyId = getStudyId(studyName, setName);
            modelOptions.add(new ModelOption(studyName, setName, modelName, studyId));
            //modelOptions.add(new ModelOption(studyName, setName, klugeStudyIdIndex++));
        }
        return modelOptions;
    }
    
    /**
     * Assumes that the return list contains Study_ID in column 0 and returns
     * the study_id from the given studyName and setName.  It also assumes
     * there is only one entry for the studyName/setName combination.  It
     * returns the first regardless.  If no return or invalid integer, it
     * returns -1
     * @param studyName
     * @param setName
     * @return 
     */
    public int getStudyId(String studyName, String setName) {
        int service_id = BioservicesParameters.MODEL_FETCH_SERVICE_ID;
        //System.out.println("Service " + service_id + "\tGetAllStudies");
        String queryStr = BioservicesParameters.SERVER_URL + "service=" + service_id + "&SERVICE_RENDERID=7";
        System.out.println("fetching query Result for " + service_id);
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("STUDY_NAME",studyName);
        paramMap.put("SET_NAME",setName);
        String queryStrWithParams = BioservicesParameters.addParametersToUrl(queryStr, paramMap);        
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        int FIRST_ROW = 0;
        int FIRST_COL = 0;
        String studyIdStr = queryResults.getData().get(FIRST_ROW).get(FIRST_COL);
        int studyId = -1;
        try {
            studyId = Integer.parseInt(studyIdStr);
        } catch(NumberFormatException nfe) {
            System.out.println("Error in parsing the study id [service=" + service_id + "]\t" + studyIdStr);
        }
        return studyId;
    }
    
    /**
     * Returns a list of DbSnpSourceOption that contains the id and the name of the
     * dbSnp available through the bioservice
     * @return 
     */
    public List<DbSnpSourceOption> getDbSnpSources() {
        int service_id = BioservicesParameters.DB_SNP_FETCH_SERVICE_ID;
        String queryStr = BioservicesParameters.SERVER_URL + "service=" + service_id + "&SERVICE_RENDERID=7";
        QueryResult queryResults = getData(queryStr, service_id, -1, -1, true);
        List<DbSnpSourceOption> dbSnpOptions = new ArrayList<DbSnpSourceOption>();
        int id = -1;
        for(List<String> row : queryResults.getData()) {
            id = -1;
            try {
                id = Integer.parseInt(row.get(BioservicesParameters.DB_SNP_FETCH_SOURCE_ID_COL));
            } catch(NumberFormatException nfe) {
                id = -1;
                System.out.println("Failed to get id for dbSnpSource: " + row.get(BioservicesParameters.DB_SNP_FETCH_SOURCE_ID_COL));
            }
            String name = row.get(BioservicesParameters.DB_SNP_FETCH_SOURCE_NAME_COL);
            dbSnpOptions.add(new DbSnpSourceOption(id, name));
        }
        return dbSnpOptions;
    }
    
    /**
     * Returns a list of DbSnpSourceOption that contains the id and the name of the
     * dbSnp available through the bioservice
     * @return 
     */
    public List<GeneSourceOption> getGeneSources() {
        int service_id = BioservicesParameters.GENE_SOURCE_FETCH_SERVICE_ID;
        String queryStr = BioservicesParameters.SERVER_URL + "service=" + service_id + "&SERVICE_RENDERID=7";
        QueryResult queryResults = getData(queryStr, service_id, -1, -1, true);
        List<GeneSourceOption> geneSourceOptions = new ArrayList<GeneSourceOption>();
        int id = -1;
        for(List<String> row : queryResults.getData()) {
            id = -1;
            try {
                id = Integer.parseInt(row.get(BioservicesParameters.GENE_SOURCE_FETCH_SOURCE_ID_COL));
            } catch(NumberFormatException nfe) {
                id = -1;
                System.out.println("Failed to get id for dbSnpSource: " + 
                                   row.get(BioservicesParameters.GENE_SOURCE_FETCH_SOURCE_NAME_COL));
            }
            String name = row.get(BioservicesParameters.GENE_SOURCE_FETCH_SOURCE_NAME_COL);
            geneSourceOptions.add(new GeneSourceOption(id, name));
        }
        return geneSourceOptions;
    }
    

}
