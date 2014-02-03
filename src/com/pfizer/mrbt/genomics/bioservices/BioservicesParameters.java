package com.pfizer.mrbt.genomics.bioservices;

import java.util.Map;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author henstockpv
 */
public class BioservicesParameters {
    public final static int PORT = 443;
    public final static String HOST = "bioservicesdev.pfizer.com";
    public final static String SERVER_URL = "https://" + HOST + ":" + PORT + "/TouVis/user/DataServletFiltered?";
    
    public final static int MODEL_FETCH_SERVICE_ID = 775;
    public final static int MODEL_FETCH_ID_COL = 0;
    public final static int MODEL_FETCH_MODEL_NAME_COL = 1;
    public final static int MODEL_FETCH_SET_NAME_COL = 2;
    public final static int MODEL_FETCH_STUDY_NAME_COL = 3;
    public final static String MODEL_FETCH_DATA_TYPE = "1";

    public final static int OLD_MODEL_FETCH_SERVICE_ID = 724;
    public final static int OLD_MODEL_FETCH_STUDY_NAME_COL = 0;
    public final static int OLD_MODEL_FETCH_SET_NAME_COL = 1;

    public final static int DB_SNP_FETCH_SERVICE_ID = 725;
    public final static int DB_SNP_FETCH_SOURCE_ID_COL = 0;
    public final static int DB_SNP_FETCH_SOURCE_NAME_COL = 1;

    public final static int GENE_SOURCE_FETCH_SERVICE_ID = 726;
    public final static int GENE_SOURCE_FETCH_SOURCE_ID_COL = 0;
    public final static int GENE_SOURCE_FETCH_SOURCE_NAME_COL = 1;

    public final static int SNP_SEARCH_BY_GENE_SERVICE_ID = 779;
    public final static int SNP_SEARCH_BY_GENE_RSID_COL = 0;
    public final static int SNP_SEARCH_BY_GENE_MODEL_COL = 1;
    public final static int SNP_SEARCH_BY_GENE_MODEL_NAME_COL = 2;
    public final static int SNP_SEARCH_BY_GENE_RESULT_ID_COL = 3;
    public final static int SNP_SEARCH_BY_GENE_ASSOCIATION_COL = 4;
    public final static int SNP_SEARCH_BY_GENE_PVAL_COL = 5;
    public final static int SNP_SEARCH_BY_GENE_LOG_PVAL_COL = 6;
    public final static int SNP_SEARCH_BY_GENE_STUDY_COL = 7;
    public final static int SNP_SEARCH_BY_GENE_SET_COL = 8;
    public final static int SNP_SEARCH_BY_GENE_DATA_TYPE_COL = 9;
    public final static int SNP_SEARCH_BY_GENE_START_COL = 10;
    public final static int SNP_SEARCH_BY_GENE_STOP_COL = 11;
    public final static int SNP_SEARCH_BY_GENE_CHROMOSOME_COL = 12;
    
    public final static int SNP_SEARCH_BY_SNP_SERVICE_ID = 797;
    public final static int SNP_SEARCH_BY_SNP_RSID_COL = 0;
    public final static int SNP_SEARCH_BY_SNP_CHROMOSOME_COL = 1;
    public final static int SNP_SEARCH_BY_SNP_START_COL = 2;
    public final static int SNP_SEARCH_BY_SNP_LOG_PVAL_COL = 4;
    public final static int SNP_SEARCH_BY_SNP_STUDY_SET_MODEL_COL = 5;
    
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
    
    public final static int GENE_ANNOTATION_SERVICE_ID = 727;
    public final static int GENE_ANNOTATION_GENE_SYMBOL_COL = 2;
    public final static int GENE_ANNOTATION_GENE_NAME_COL = 3;
    public final static int GENE_ANNOTATION_CHROMOSOME_COL = 4;
    public final static int GENE_ANNOTATION_START_COL = 5;
    public final static int GENE_ANNOTATION_STOP_COL = 6;
    public final static int GENE_ANNOTATION_STRAND_COL = 7;
    public final static int GENE_ANNOTATION_ENSEMBL_ID = 8;
    public final static int GENE_ANNOTATION_ENTREZGENE_ID = 9;
    
    public final static int RECOMBINATION_RATE_SERVICE_ID = 769;
    public final static int RECOMBINATION_RATE_POSITION_COL = 0;
    public final static int RECOMBINATION_RATE_COL = 1;

    public final static int RECOMBINATION_RATE_BY_GENE_SERVICE_ID = 769;
    public final static int RECOMBINATION_RATE_BY_GENE_POSITION_COL = 0;
    public final static int RECOMBINATION_RATE_BY_GENE_COL = 1;

    public final static int RECOMBINATION_RATE_BY_SNP_SERVICE_ID = 781;
    public final static int RECOMBINATION_RATE_BY_SNP_POSITION_COL = 1;
    public final static int RECOMBINATION_RATE_BY_SNP_COL = 2;

    public final static int GENE_LOCATION_SERVICE_ID    = 785;
    public final static int GENE_LOCATION_CHROMOSOME_COL = 0;
    public final static int GENE_LOCATION_START_COL     = 1;
    public final static int GENE_LOCATION_STOP_COL       = 2;

    
    /**
     * Adds in query parameters in the correct syntax to the URL string
     * @param url
     * @param params
     * @return 
     */
    public static String addParametersToUrl(String url,Map<String,String> params){
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        int ampCount = params.keySet().size();
        for(Map.Entry<String,String> pairs : params.entrySet()){
            String pname = (String)pairs.getKey();
            String pvalue = (String) pairs.getValue();
            if(ampCount > 0){
               sb.append("&");
               ampCount--;
            }
            sb.append(pname+"="+pvalue);
        }
        return sb.toString();
    }
}
