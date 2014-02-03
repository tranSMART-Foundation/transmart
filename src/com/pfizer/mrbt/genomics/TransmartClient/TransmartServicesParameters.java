package com.pfizer.mrbt.genomics.TransmartClient;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.state.State;
import java.util.Map;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author henstockpv
 */
public class TransmartServicesParameters {
    
    public final static int PORT = 8080;
    /*public final static String HOST = "amre1al336.pcld.pfizer.com";
    public final static String SERVER_URL = "http://" + HOST + ":" + PORT + "/transmartPfizer/webservice/";*/

    public final static String STAGE_HOST = "amre1al306.pcld.pfizer.com";
    //public final static String STAGE_HOST = "transmart.pfizer.com";
    public final static String DEV_HOST   = "amre1al336.pcld.pfizer.com";
    public final static String STAGE_URL  = "http://" + STAGE_HOST + ":" + PORT + "/transmart/webservice/";
    public final static String STAGE_URL2  = "http://" + STAGE_HOST + ":" + PORT + "/transmartPfizer/webservice/";
    public final static String DEV_URL    = "http://" + DEV_HOST   + ":" + PORT + "/transmart/webservice/";  // yes there are 2 dev servers
    public final static String DEV_URL2    = "http://" + DEV_HOST   + ":" + PORT + "/transmartPfizer/webservice/";
    //public final static String SERVER_URL = "http://" + STAGE_HOST + ":" + PORT + "/transmartPfizer/webservice/";

    public final static String MODEL_FETCH_METHOD = "getModelInfoByDataType";
    //public final static int MODEL_FETCH_SERVICE_ID = 775;
    public final static int MODEL_FETCH_GWAS_DATA_TYPE      = 1;
    public final static int MODEL_FETCH_EQTL_DATA_TYPE      = 2;
    public final static int MODEL_FETCH_METABOLIC_DATA_TYPE = 3;
    
    public final static int MODEL_FETCH_ID_COL = 0;
    public final static int MODEL_FETCH_MODEL_NAME_COL = 1;
    public final static int MODEL_FETCH_STUDY_SET_MODEL_NAME_COL = 2;
    public final static int MODEL_FETCH_STUDY_NAME_COL = 3;
    public final static int MODEL_FETCH_SET_NAME_COL = 2;

    public final static int OLD_MODEL_FETCH_SERVICE_ID = 724;
    public final static int OLD_MODEL_FETCH_STUDY_NAME_COL = 0;
    public final static int OLD_MODEL_FETCH_SET_NAME_COL = 1;

    public final static String DB_SNP_SOURCE_FETCH_METHOD = "getSnpSources";
    //public final static int DB_SNP_FETCH_SERVICE_ID = 725;
    public final static int DB_SNP_FETCH_SOURCE_ID_COL   = 0;
    public final static int DB_SNP_FETCH_SOURCE_NAME_COL = 1;
    public final static int DB_SNP_FETCH_SOURCE_DATE_COL = 3;
    public final static int DB_SNP_FETCH_SOURCE_UML_COL  = 4;

    public final static String GENE_SOURCE_FETCH_METHOD = "getGeneSources";    
    public final static int GENE_SOURCE_FETCH_SERVICE_ID = 726;
    public final static int GENE_SOURCE_FETCH_ID_COL = 0;
    public final static int GENE_SOURCE_FETCH_NAME_COL = 1;
    public final static int GENE_SOURCE_FETCH_DATE_COL = 3;
    public final static int GENE_SOURCE_FETCH_UML_COL  = 4;

    public final static String SNP_SEARCH_METHOD = "resultDataForFilteredByModelIdGeneAndRangeRev";
    public final static int SNP_SEARCH_SERVICE_ID = 779;
    public final static int SNP_SEARCH_RSID_COL = 0;
    public final static int SNP_SEARCH_RESULT_ID_COL = 1;
    public final static int SNP_SEARCH_MODEL_COL = 2;
    public final static int SNP_SEARCH_PVAL_COL = 3;
    public final static int SNP_SEARCH_LOG_PVAL_COL = 4;
    public final static int SNP_SEARCH_STUDY_COL = 5;
    public final static int SNP_SEARCH_STUDY_SET_MODEL_NAME_COL = 6;
    public final static int SNP_SEARCH_SET_COL = 6;
    public final static int SNP_SEARCH_DATA_TYPE_COL = 7;
    public final static int SNP_SEARCH_START_COL = 8;
    public final static int SNP_SEARCH_CHROMOSOME_COL = 9;
    
    public final static int OLD_SNP_SEARCH_SERVICE_ID = 744;
    public final static int OLD_SNP_SEARCH_STUDY_COL = 0;
    public final static int OLD_SNP_SEARCH_SET_COL = 1;
    public final static int OLD_SNP_SEARCH_RSID_COL = 2;
    public final static int OLD_SNP_SEARCH_PVAL_COL = 3;
    public final static int OLD_SNP_SEARCH_LOG_PVAL_COL = 4;
    public final static int OLD_SNP_SEARCH_ASSOCIATION_COL = 5;
    public final static int OLD_SNP_SEARCH_START_COL = 6;
    public final static int OLD_SNP_SEARCH_STOP_COL = 7;
    public final static int OLD_SNP_SEARCH_CHROMOSOME_COL = 8;
    
    public final static String GENE_ANNOTATION_METHOD = "getGeneByPosition";
    public final static int GENE_ANNOTATION_GENE_ID_COL = 0;
    public final static int GENE_SOURCE_ID_COL = 1;
    public final static int GENE_ANNOTATION_GENE_SYMBOL_COL = 2;
    public final static int GENE_ANNOTATION_GENE_NAME_COL = 3;
    public final static int GENE_ANNOTATION_CHROMOSOME_COL = 4;
    public final static int GENE_ANNOTATION_START_COL = 5;
    public final static int GENE_ANNOTATION_STOP_COL = 6;
    public final static int GENE_ANNOTATION_STRAND_COL = 7;
    //public final static int GENE_ANNOTATION_ENSEMBL_ID = 8;
    public final static int GENE_ANNOTATION_ENTREZGENE_ID_COL = 9;
    
    public final static int RECOMBINATION_RATE_SERVICE_ID = 769;
    public final static int RECOMBINATION_RATE_POSITION_COL = 0;
    public final static int RECOMBINATION_RATE_COL = 1;

    public final static String GENE_LOCATION_METHOD    = "computeGeneBounds";
    public final static int GENE_LOCATION_START_COL      = 0;
    public final static int GENE_LOCATION_STOP_COL       = 1;
    public final static int GENE_LOCATION_CHROMOSOME_COL = 2;

    
    /**
     * Adds in query parameters in the correct syntax to the URL string
     * @param url
     * @param params
     * @return 
     */
    public static String addParametersToUrl(String url,Map<String,String> params){
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        int ampCount = params.keySet().size();
        for(Map.Entry<String,String> pairs : params.entrySet()){
            String pname = (String)pairs.getKey();
            String pvalue = (String) pairs.getValue();
            if(ampCount > 0){
               sb.append("&");
               ampCount--;
            }
            sb.append(pname);
            sb.append("=");
            sb.append(pvalue);
            //sb.append(pname+"="+pvalue);
        }
        return sb.toString();
    }
    
    /**
     * Returns the SERVER_URL that will either be the dev or the production
     * based on the State.getDataMode() that will have the value of either
     * State.TRANSMART_SERVICES_MODE or State.TRANSMART_DEV_SERVICES_MODE.
     * @return 
     */
    public static String getServerURL() {
        if(Singleton.getState().getDataMode() == State.TRANSMART_SERVICES_MODE) {
            //System.out.println("ServerURL: " + STAGE_URL);
            return STAGE_URL;
        } else {
            //System.out.println("ServerURL: " + DEV_URL);
            return DEV_URL;
        }
    }
}
