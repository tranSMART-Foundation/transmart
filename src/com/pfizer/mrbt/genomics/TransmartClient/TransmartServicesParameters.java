package com.pfizer.mrbt.genomics.TransmartClient;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.state.State;
import com.pfizer.mrbt.genomics.webservices.Environment;
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
	/*
	 * public final static String HOST = "amre1al336.pcld.pfizer.com"; public
	 * final static String SERVER_URL = "http://" + HOST + ":" + PORT +
	 * "/transmartPfizer/webservice/";
	 * 
	 * 
	 */
	
	public static String SESSION_ID = "";
	public final static String WEBSERVICE_FOLDER = "gwasWeb";//"webservice";
	
	public static String PRODUCTION_HOST = "amrndhl1038.pfizer.com:8000";
	// public final static String STAGE_HOST = "transmart.pfizer.com";
	public static String STAGE_HOST = "amrndhl1040.pfizer.com:8000";
	public static String TEST_HOST = "amre1al306.pcld.pfizer.com";
	// public final static String STAGE_HOST = "transmart.pfizer.com";
	public static String DEV_HOST = "amre1al336.pcld.pfizer.com";
	public static String TEST_URL = "http://" + TEST_HOST + ":" + PORT
			+ "/transmart/webservice/";
	public static String STAGE_URL = "http://" + STAGE_HOST
			+ "/transmart/webservice/";
	public static String DEV_URL = "http://" + DEV_HOST + ":" + PORT
			+ "/transmart/webservice/"; // yes there are 2 dev servers
	public static String PROD_URL = "http://" + PRODUCTION_HOST
			+ "/transmart/webservice/";
	// public final static String SERVER_URL = "http://" + STAGE_HOST + ":" +
	// PORT + "/transmartPfizer/webservice/";

	// public final static String MODEL_FETCH_METHOD = "getModelInfoByDataType";
	public final static String MODEL_FETCH_METHOD = "getSecureModelInfoByDataType";
	// public final static int MODEL_FETCH_SERVICE_ID = 775;
	public final static int MODEL_FETCH_GWAS_DATA_TYPE = 1;
	public final static int MODEL_FETCH_EQTL_DATA_TYPE = 2;
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
	// public final static int DB_SNP_FETCH_SERVICE_ID = 725;
	public final static int DB_SNP_FETCH_SOURCE_ID_COL = 0;
	public final static int DB_SNP_FETCH_SOURCE_NAME_COL = 1;
	public final static int DB_SNP_FETCH_SOURCE_DATE_COL = 3;
	public final static int DB_SNP_FETCH_SOURCE_UML_COL = 4;

	public final static String GENE_SOURCE_FETCH_METHOD = "getGeneSources";
	public final static int GENE_SOURCE_FETCH_SERVICE_ID = 726;
	public final static int GENE_SOURCE_FETCH_ID_COL = 0;
	public final static int GENE_SOURCE_FETCH_NAME_COL = 1;
	public final static int GENE_SOURCE_FETCH_DATE_COL = 3;
	public final static int GENE_SOURCE_FETCH_UML_COL = 4;

	public final static String GENE_SEARCH_METHOD = "resultDataForFilteredByModelIdGeneAndRangeRev";
	public final static int GENE_SEARCH_SERVICE_ID = 779;
	public final static int GENE_SEARCH_RSID_COL = 0;
	public final static int GENE_SEARCH_RESULT_ID_COL = 1;
	public final static int GENE_SEARCH_MODEL_COL = 2;
	public final static int GENE_SEARCH_PVAL_COL = 3;
	public final static int GENE_SEARCH_LOG_PVAL_COL = 4;
	public final static int GENE_SEARCH_STUDY_COL = 5;
	public final static int GENE_SEARCH_STUDY_SET_MODEL_NAME_COL = 6;
	public final static int GENE_SEARCH_SET_COL = 6;
	public final static int GENE_SEARCH_DATA_TYPE_COL = 7;
	public final static int GENE_SEARCH_START_COL = 8;
	public final static int GENE_SEARCH_CHROMOSOME_COL = 9;
	public final static int GENE_SEARCH_SNP_GENE_COL = 10; // gene associated
															// with SNP
	public final static int GENE_SEARCH_INTRON_COL = 11;
	public final static int GENE_SEARCH_RECOMBINATION_RATE_COL = 12;
	public final static int GENE_SEARCH_REGULOME_COL = 13;
	public final static int GENE_SEARCH_NUM_FIELDS = 14;

	public final static String SNP_SEARCH_METHOD = "snpSearch";
	public final static int SNP_SEARCH_RSID_COL = 0;
	public final static int SNP_SEARCH_CHROMOSOME_COL = 1;
	// public final static int GENE_SEARCH_RESULT_ID_COL = 1;
	// public final static int GENE_SEARCH_MODEL_COL = 2;
	// public final static int GENE_SEARCH_PVAL_COL = 3;
	public final static int SNP_SEARCH_POSITION_COL = 2;
	public final static int SNP_SEARCH_LOG_PVAL_COL = 3;
	// public final static int GENE_SEARCH_STUDY_COL= 5;
	public final static int SNP_SEARCH_STUDY_SET_MODEL_NAME_COL = 4;
	public final static int SNP_SEARCH_SNP_GENE_COL = 5; // gene associated with
															// SNP
	public final static int SNP_SEARCH_INTRON_COL = 6;
	public final static int SNP_SEARCH_RECOMBINATION_RATE_COL = 7;
	public final static int SNP_SEARCH_REGULOME_COL = 8;
	public final static int SNP_SEARCH_NUM_FIELDS = 9;
	// public final static int SNP_SEARCH_STUDY_SET_MODEL_NAME_COL = 7;
	// public final static int SNP_SEARCH_STUDY_SET_MODEL_NAME_COL = 8;
	// public final static int GENE_SEARCH_SET_COL = 6;
	// public final static int GENE_SEARCH_DATA_TYPE_COL = 7;
	// public final static int GENE_SEARCH_INTRON_COL = 10;
	// public final static int GENE_SEARCH_RECOMBINATION_RATE_COL = 11;
	// public final static int GENE_SEARCH_REGULOME_COL = 12;

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
	// public final static int GENE_ANNOTATION_ENSEMBL_ID = 8;
	public final static int GENE_ANNOTATION_ENTREZGENE_ID_COL = 9;

	// public final static int RECOMBINATION_RATE_SERVICE_ID = 769;
	// public final static int RECOMBINATION_RATE_POSITION_COL = 0;
	// public final static int RECOMBINATION_GENE_RATE_COL = 1;

	public final static String GENE_LOCATION_METHOD = "computeGeneBounds";
	public final static int GENE_LOCATION_START_COL = 0;
	public final static int GENE_LOCATION_STOP_COL = 1;
	public final static int GENE_LOCATION_CHROMOSOME_COL = 2;

	public final static String RECOMBINATION_RATE_GENE_METHOD = "getRecombinationRatesForGene";
	public final static int RECOMBINATION_GENE_POSITION_COL = 0;
	public final static int RECOMBINATION_GENE_RATE_COL = 1;

	public final static String RECOMBINATION_RATE_SNP_METHOD = "recombinationRateBySnp";
	public final static int RECOMBINATION_SNP_CHROMOSOME_COL = 0;
	public final static int RECOMBINATION_SNP_POSITION_COL = 1;
	public final static int RECOMBINATION_SNP_RATE_COL = 2;
	public final static int RECOMBINATION_SNP_MAP_COL = 3;

	/**
	 * Adds in query parameters in the correct syntax to the URL string
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String addParametersToUrl(String url,
			Map<String, String> params) {
		StringBuilder sb = new StringBuilder();
		sb.append(url);
		int ampCount = params.keySet().size();
		for (Map.Entry<String, String> pairs : params.entrySet()) {
			String pname = (String) pairs.getKey();
			String pvalue = (String) pairs.getValue();
			if (ampCount > 0) {
				sb.append("&");
				ampCount--;
			}
			sb.append(pname);
			sb.append("=");
			sb.append(pvalue);
			// sb.append(pname+"="+pvalue);
		}
		return sb.toString();
	}

	/**
	 * Returns the server URL that will either be the dev or the production
	 * based on the passed environment variable
	 * 
	 * @param Environment
	 *            environment
	 * @return
	 */
	public static String getServerURL(Environment environment) {
		switch (environment) {
		case PRODUCTION:
			return PROD_URL;
		case STAGE:
			return STAGE_URL;
		case TEST:
			return TEST_URL;
		case DEV:
			return DEV_URL;
		default:
			return "Unknown server URL for " + environment.getDisplayStr();
		}
	}
	
	public static void updateUrlAndHosts(String host) {
		PRODUCTION_HOST = host;
        DEV_HOST = host;
        STAGE_HOST = host;
        TEST_HOST = host;
        PROD_URL =  PRODUCTION_HOST+"/"+WEBSERVICE_FOLDER+"/";
        DEV_URL =  DEV_HOST+"/"+WEBSERVICE_FOLDER+"/";
        STAGE_URL =  STAGE_HOST+"/"+WEBSERVICE_FOLDER+"/";
        TEST_URL =  TEST_HOST+"/"+WEBSERVICE_FOLDER+"/";
  
	}
}
