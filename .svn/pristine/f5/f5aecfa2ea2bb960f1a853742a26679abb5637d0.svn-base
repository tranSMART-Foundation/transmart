/*
 * This class populates the range of options available for the Query tab.
 * This includes the models, SNP or Gene Annotation source, etc.
 */
package com.pfizer.mrbt.genomics.TransmartClient;

import com.pfizer.mrbt.genomics.bioservices.*;
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
    protected static Logger log = Logger.getLogger(com.pfizer.mrbt.genomics.bioservices.AmiDemo.class.getName());    

    /**
     * Initializes the logger and the host/port
     */
    public TransmartQueryParameterFetch() {
        log.setLevel(Level.DEBUG);
    }
    
    /**
     * Receives a list of ModelOption that contain the study name, set name, and
     * the id associated with them for inclusion in the query pane
     * @return 
     */
    public List<ModelOption> fetchModelOptions() {
        String queryStr = TransmartServicesParameters.getServerURL() + TransmartServicesParameters.MODEL_FETCH_METHOD;
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("dataType", TransmartServicesParameters.MODEL_FETCH_GWAS_DATA_TYPE+"");
        String queryStrWtihParams = TransmartUtil.addParametersToUrl(queryStr, paramMap);
        String xmlResult = TransmartUtil.fetchResult(queryStrWtihParams);
        
        ArrayList<ArrayList<String>> queryResults = TransmartUtil.parseXml(xmlResult);
        List<ModelOption> modelOptions = new ArrayList<ModelOption>();
        for(ArrayList<String> row : queryResults) {
            String[] tokens = row.get(TransmartServicesParameters.MODEL_FETCH_STUDY_SET_MODEL_NAME_COL).split("\\s+\\-\\s+");
            String studyName;
            String setName;
            String modelName;
            if(tokens.length < 3) {
                // if the MODEL_FETCH_STUDY_SET_MODEL_NAME doesn't have the form x - y - z then
                // need to take the 3rd - 2nd - 1st line and re-represent it
                studyName = row.get(TransmartServicesParameters.MODEL_FETCH_STUDY_NAME_COL);
                setName   = row.get(TransmartServicesParameters.MODEL_FETCH_SET_NAME_COL);
                modelName = row.get(TransmartServicesParameters.MODEL_FETCH_MODEL_NAME_COL);
            } else if(tokens.length > 3) {
                // Probably have something in form of study - more study - more study - set - model
                // Need to join the left-most ones as study
                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < tokens.length-2; i++) {
                    if(i > 0) {
                        sb.append(" - ");
                    } 
                    sb.append(tokens[i]);
                }
                studyName = sb.toString();
                setName   = tokens[tokens.length-2];
                modelName = tokens[tokens.length-1];
            } else { // using the 2nd line of MODEL_FETCH_STUDY_SET_MODEL_NAME to represent the name
                studyName = tokens[0];
                setName   = tokens[1];
                modelName = tokens[2];
            }
            long studyId = -1;
            try {
                studyId      = Long.parseLong(row.get(TransmartServicesParameters.MODEL_FETCH_ID_COL));
            } catch(NumberFormatException nfe) {
                System.out.println("Failed to convert " + row.get(TransmartServicesParameters.MODEL_FETCH_ID_COL) + " into long");
                System.exit(0);
            }
            if(studyName.length() > MAX_STUDY_NAME_LENGTH) {
                studyName = studyName.substring(0, MAX_STUDY_NAME_LENGTH);
            }
            if(! setName.matches("^[tT][eE][sS][tT].*")) {
                modelOptions.add(new ModelOption(studyName, setName, modelName, studyId));
            }
        }
        Collections.sort(modelOptions);
        return modelOptions;
    }
    
    /**
     * Returns a list of DbSnpSourceOption that contains the id and the name of the
     * dbSnp available through the bioservice
     * @return 
     */
    public List<DbSnpSourceOption> getDbSnpSources() {
        String queryStr = TransmartServicesParameters.getServerURL() + TransmartServicesParameters.DB_SNP_SOURCE_FETCH_METHOD;
        HashMap<String,String> paramMap = new HashMap<String,String>();
        String xmlResult = TransmartUtil.fetchResult(queryStr);
        
        ArrayList<ArrayList<String>> queryResults = TransmartUtil.parseXml(xmlResult);
        List<DbSnpSourceOption> dbSnpOptions = new ArrayList<DbSnpSourceOption>();
        for(List<String> row : queryResults) {
            int id;
            try {
                id = Integer.parseInt(row.get(TransmartServicesParameters.DB_SNP_FETCH_SOURCE_ID_COL));
            } catch(NumberFormatException nfe) {
                id = -1;
                System.out.println("Failed to get id for dbSnpSource: " + row.get(TransmartServicesParameters.DB_SNP_FETCH_SOURCE_ID_COL));
            }
            String name = row.get(TransmartServicesParameters.DB_SNP_FETCH_SOURCE_NAME_COL);
            String date = row.get(TransmartServicesParameters.DB_SNP_FETCH_SOURCE_DATE_COL);
            dbSnpOptions.add(new DbSnpSourceOption(id, name + " " + date));
        }
        Collections.sort(dbSnpOptions);
        return dbSnpOptions;
    }
    
    /**
     * Returns a list of DbSnpSourceOption that contains the id and the name of the
     * dbSnp available through the bioservice
     * @return 
     */
    public List<GeneSourceOption> getGeneSources() {
        String queryStr = TransmartServicesParameters.getServerURL() + TransmartServicesParameters.GENE_SOURCE_FETCH_METHOD;
        HashMap<String,String> paramMap = new HashMap<String,String>();
        String xmlResult = TransmartUtil.fetchResult(queryStr);
        
        ArrayList<ArrayList<String>> queryResults = TransmartUtil.parseXml(xmlResult);
        List<GeneSourceOption> geneSourceOptions = new ArrayList<GeneSourceOption>();
        for(List<String> row : queryResults) {
            int id;
            try {
                id = Integer.parseInt(row.get(TransmartServicesParameters.GENE_SOURCE_FETCH_ID_COL));
            } catch(NumberFormatException nfe) {
                id = -1;
                System.out.println("Failed to get id for geneSourceOption: " + row.get(TransmartServicesParameters.GENE_SOURCE_FETCH_ID_COL));
            }
            String name = row.get(TransmartServicesParameters.GENE_SOURCE_FETCH_NAME_COL);
            String date = row.get(TransmartServicesParameters.GENE_SOURCE_FETCH_DATE_COL);
            geneSourceOptions.add(new GeneSourceOption(id, name + " " + date));
        }
        return geneSourceOptions;
    }

}
