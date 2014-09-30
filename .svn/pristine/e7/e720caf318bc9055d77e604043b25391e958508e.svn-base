package com.pfizer.mrbt.genomics.bioservices;

import com.pfizer.tnb.api.server.util.BioServicesInitParams;
import com.pfizer.tnb.api.server.util.QueryResult;
import com.pfizer.tnb.bsutil.BsServiceClientImpl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;




public class AmiDemo extends BsServiceClientImpl{
    protected static Logger log = Logger.getLogger(com.pfizer.mrbt.genomics.bioservices.AmiDemo.class.getName());    
    public final static int PORT = 443;
    public final static String HOST = "bioservicesdev.pfizer.com";
    public final static String serverURL = "https://" + HOST + ":" + PORT + "/TouVis/user/DataServletFiltered?";

    public AmiDemo() {
        log.setLevel(Level.DEBUG);
        BioServicesInitParams initParams = new BioServicesInitParams();
        initParams.setBioServicesServer(serverURL);
        initParams.setServer(HOST);
        initParams.setPort(PORT);
        setInitParams(initParams);
        
        /*
        //QueryResult queryResult753 = runQuery753();
        QueryResult queryResult725 = runQuery725();
        QueryResult queryResult726 = runQuery726();*/
        QueryResult queryResult724 = runQuery724();
        QueryResult queryResult726 = runQuery726();
        QueryResult queryResult729 = runQuery729();
        QueryResult queryResult729b = runQuery729b();
        QueryResult queryResult729c = runQuery729c();
        QueryResult queryResult785 = runQuery785();
        /*QueryResult queryResult721 = runQuery721();
        QueryResult queryResult720 = runQuery720();
        QueryResult queryResult737 = runQuery737();
        QueryResult queryResult718 = runQuery718();
        QueryResult queryResult727 = runQuery727();
        //QueryResult queryResult760 = runQuery760();
        QueryResult queryResult745 = runQuery745();
        QueryResult queryResult743 = runQuery743();
        QueryResult queryResult744 = runQuery744();
        System.out.println("All done");
        QueryResult query351 = runQuery351();
        //QueryResult q744a = runQuery744a();*/
        QueryResult q779 = runQuery779();
        //QueryResult q779 = runQuery779Big();
        //QueryResult q779p1 = runQuery779Big1();
        //QueryResult q779p2 = runQuery779Big2();
        /*//QueryResult q744b = runQuery744b();
        //QueryResult q744c = runQuery744c();*/
        //QueryResult q775 = runQuery775();
        /*QueryResult q769 = runQuery769();
        QueryResult q16 = runQuery16();*/
        QueryResult q797 = runQuery797();
        
    } 
    
    /**
     * Query Service 753 
     * @return 
     */
    protected QueryResult runQuery753() {
        int service_id = 753;
        System.out.println("Service " + service_id + "\tList of Genes");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStr, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        System.out.println("query Result: " + queryResults);
        return queryResults;
    }
      
    
    /**
     * Query Service 753 
     * @return 
     */
    protected QueryResult runQuery725() {
        int service_id = 725;
        System.out.println("Service " + service_id + "\tGetSNPSources");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStr, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        System.out.println("query Result: " + queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 753 
     * @return 
     */
    protected QueryResult runQuery726() {
        int service_id = 726;
        System.out.println("Service " + service_id + "\tGetGeneSources");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStr, service_id, -1, -1, true);
        /*for(Iterator iter = queryResults.
            
        }*/
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 753 
     * @return 
     */
    protected QueryResult runQuery724() {
        int service_id = 724;
        System.out.println("Service " + service_id + "\tGetAllStudies");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        System.out.println("fetching query Result for " + service_id);
        System.out.println("QueryStr: " + queryStr);
        QueryResult queryResults = getData(queryStr, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 729 
     * @return 
     */
    protected QueryResult runQuery729() {
        int service_id = 729;
        System.out.println("Service " + service_id + "b\tGetAllStudies");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("GENESRCID","3");
        paramMap.put("LIM","3");
        paramMap.put("CHR","1");
        paramMap.put("POSITION","22974603");
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 729 
     * @return 
     */
    protected QueryResult runQuery785() {
        int service_id = 785;
        System.out.println("Service " + service_id + "b\tGet gene range and chromosome");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("GENE_SOURCE_ID","3");
        paramMap.put("GENE_SYMBOL","TNF");
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 721 
     * @return 
     */
    protected QueryResult runQuery721() {
        int service_id = 721;
        System.out.println("Service " + service_id + "\tGet Service Details");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("STUDYID","20");
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 720 
     * @return 
     */
    protected QueryResult runQuery720() {
        int service_id = 720;
        System.out.println("Service " + service_id + "\tGet Set Details");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("STUDY_NAME","DGI");
        paramMap.put("SET_NAME","BROAD_LDL");
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 727 
     * @return 
     */
    protected QueryResult runQuery727() {
        int service_id = 727;
        System.out.println("Service " + service_id + "\tGet Gene By Position");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("GENESRCID","3");
        paramMap.put("START","7500000");
        paramMap.put("STOP", "1075000");
        paramMap.put("CHR","12");
        /*paramMap.put("GENESRCID","3");
        paramMap.put("START","22974001");
        paramMap.put("STOP", "22974603");
        paramMap.put("CHR","1");*/
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 744 
     * @return 
     */
    protected QueryResult runQuery744() {
        int service_id = 744;
        System.out.println("Service " + service_id + "\tGet Data Filtered by Model Gene and Range");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("GENE_NAME","OVOS");
        paramMap.put("RANGE","100000");
        //paramMap.put("MODEL_ID","122");
        //paramMap.put("MODEL_ID","122,123,125");
        paramMap.put("MODEL_ID","122,123,125");
        paramMap.put("SOURCE_ID","3"); 
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 351 
     * @return 
     */
    protected QueryResult runQuery351() {
        int service_id = 351;
        System.out.println("Service " + service_id + "\tGet input parameters for SERV_ID");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("SERV_ID","744");
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 744 
     * @return 
     */
    protected QueryResult runQuery744a() {
        int service_id = 744;
        System.out.println("Service " + service_id + "\tGet Data Filtered by Model Gene and Range");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("GENE_NAME","OVOS");
        paramMap.put("RANGE","0");
        //paramMap.put("MODEL_ID","122");
        //paramMap.put("MODEL_ID","122,123,125");
        paramMap.put("MODEL_ID","122,123,125");
        paramMap.put("SOURCE_ID","3"); 
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("Query str [" + queryStrWithParams + "]");
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 744 
     * @return 
     */
    protected QueryResult runQuery779() {
        int service_id = 779;
        System.out.println("Service " + service_id + "\tGet Search for SNPs around gene");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("GENE_NAME","TNF");
        paramMap.put("RANGE","10000");
        //paramMap.put("MODEL_ID","122");
        //paramMap.put("MODEL_ID","122,123,125");
        paramMap.put("MODEL_ID","100");
        paramMap.put("SOURCE_ID","3"); 
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("Query str [" + queryStrWithParams + "]");
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    protected QueryResult runQuery779Big() {
        int service_id = 779;
        System.out.println("Service " + service_id + "\tGet Search for SNPs around gene");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("GENE_NAME","IL6");
        paramMap.put("RANGE","10000");
        //paramMap.put("MODEL_ID","122");
        //paramMap.put("MODEL_ID","122,123,125");
        paramMap.put("MODEL_ID","9,16,5,12,10,17,8,15,11,18,88,89,6,13,49,50,7,14,51,52");
        paramMap.put("SOURCE_ID","3"); 
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("Query str [" + queryStrWithParams + "]");
        System.out.println("fetching query Result for Big " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    protected QueryResult runQuery779Big1() {
        int service_id = 779;
        System.out.println("Service " + service_id + "\tGet Search for SNPs around gene");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("GENE_NAME","IL6");
        paramMap.put("RANGE","10000");
        //paramMap.put("MODEL_ID","122");
        //paramMap.put("MODEL_ID","122,123,125");
        paramMap.put("MODEL_ID","9,16,5,12,10,17,8,15,11,18");
        paramMap.put("SOURCE_ID","3"); 
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("Query str Big1 [" + queryStrWithParams + "]");
        System.out.println("fetching query Result for Big1 " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    protected QueryResult runQuery779Big2() {
        int service_id = 779;
        System.out.println("Service " + service_id + "\tGet Search for SNPs around gene");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("GENE_NAME","IL6");
        paramMap.put("RANGE","10000");
        //paramMap.put("MODEL_ID","122");
        //paramMap.put("MODEL_ID","122,123,125");
        paramMap.put("MODEL_ID","88,89,6,13,49,50,7,14,51,52");
        paramMap.put("SOURCE_ID","3"); 
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("Query str Big2 [" + queryStrWithParams + "]");
        System.out.println("fetching query Result for Big 2" + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    
    
    /**
     * Query Service 744 
     * @return 
     */
    protected QueryResult runQuery744b() {
        int service_id = 744;
        System.out.println("Service " + service_id + "\tGet Data Filtered by Model Gene and Range");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("GENE_NAME","TNF");
        paramMap.put("RANGE","0");
        //paramMap.put("MODEL_ID","122");
        //paramMap.put("MODEL_ID","122,123,125");
        paramMap.put("MODEL_ID","122,123,125");
        paramMap.put("SOURCE_ID","3"); 
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("Query str [" + queryStrWithParams + "]");
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 744 
     * @return 
     */
    protected QueryResult runQuery744c() {
        int service_id = 744;
        System.out.println("Service " + service_id + "\tGet Data Filtered by Model Gene and Range");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("GENE_NAME","HENSTOCK");
        paramMap.put("RANGE","0");
        //paramMap.put("MODEL_ID","122");
        //paramMap.put("MODEL_ID","122,123,125");
        paramMap.put("MODEL_ID","122,123,125");
        paramMap.put("SOURCE_ID","3"); 
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("Query str [" + queryStrWithParams + "]");
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 743 
     * @return 
     */
    protected QueryResult runQuery743() {
        int service_id = 743;
        System.out.println("Service " + service_id + "\tGet Data for across All Set by Gene and Range");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("GENE_NAME","OVOS");
        paramMap.put("RANGE","0");
        //paramMap.put("SOURCE_ID","");
        //paramMap.put("MODEL_ID","122");
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 760 
     * @return 
     */
    protected QueryResult runQuery760() {
        int service_id = 760;
        System.out.println("Service " + service_id + "\tGet SNPs for annotation");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("SNP_SOURCE","3");
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 745 
     * @return 
     */
    protected QueryResult runQuery745() {
        int service_id = 745;
        System.out.println("Service " + service_id + "\tGet result data for all set by SNPs");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("SNP","3093665,3093661");
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 718 
     * @return 
     */
    protected QueryResult runQuery718() {
        int service_id = 718;
        System.out.println("Service " + service_id + "\tGet Study List");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    /**
     * Query Service 737 
     * @return 
     */
    protected QueryResult runQuery737() {
        int service_id = 737;
        System.out.println("Service " + service_id + "\tGet Study Details");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("STUDY_NAME","Zellers");
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }

    /**
     * Query Service 775 
     * @return 
     */
    protected QueryResult runQuery775() {
        int service_id = 775;
        System.out.println("Service " + service_id + "\tGetAllStudiesWithModels");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        System.out.println("fetching query Result for " + service_id);
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("DATA_TYPE","1"); //1 = GWAS, 2=EQTL, 3=Metabolic
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    
    /**
     * Query Service 769 
     * @return 
     */
    protected QueryResult runQuery769() {
        int service_id = 769;
        System.out.println("Service " + service_id + "\tGet recombination rates");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        System.out.println("fetching query Result for " + service_id);
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("GENE_SOURCE_ID","3"); //1 = GWAS, 2=EQTL, 3=Metabolic
        paramMap.put("RANGE","50000"); //1 = GWAS, 2=EQTL, 3=Metabolic
        paramMap.put("GENE_SYMBOL","TNF"); //1 = GWAS, 2=EQTL, 3=Metabolic
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    
    /**
     * Query Service 729b 
     * @return 
     */
    protected QueryResult runQuery729b() {
        int service_id = 729;
        System.out.println("Service " + service_id + "\tGet recombination rates");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        System.out.println("fetching query Result for " + service_id);
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("GENESRCID","3"); //1 = GWAS, 2=EQTL, 3=Metabolic
        paramMap.put("LIM","5000"); //1 = GWAS, 2=EQTL, 3=Metabolic
        paramMap.put("CHR","6"); 
        paramMap.put("POSITION","31556580"); 
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    
    /**
     * Query Service 729b 
     * @return 
     */
    protected QueryResult runQuery729c() {
        int service_id = 729;
        System.out.println("Service " + service_id + "\tGet recombination rates");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        System.out.println("fetching query Result for " + service_id);
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("GENESRCID","3"); //1 = GWAS, 2=EQTL, 3=Metabolic
        paramMap.put("LIM","50000"); //1 = GWAS, 2=EQTL, 3=Metabolic
        paramMap.put("CHR","6"); 
        paramMap.put("POSITION","31556580"); 
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("QueryWithParams 729c [" + queryStrWithParams + "]");
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    
    /**
     * Query Service 797 search for SNP with a range 
     * @return 
     */
    protected QueryResult runQuery797() {
        int service_id = 797;
        System.out.println("Service " + service_id + "\tSearch by SNP");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        System.out.println("fetching query Result for " + service_id);
        HashMap<String,String> paramMap = new HashMap<String,String>();
        paramMap.put("MODEL_ID","209795313"); //corresponds to study/set/model #
        paramMap.put("RANGE","50000"); //1 = GWAS, 2=EQTL, 3=Metabolic
        paramMap.put("SNP", "rs16837871");
        paramMap.put("HG_VERSION","19"); 
        String queryStrWithParams = addParametersToUrl(queryStr, paramMap);
        System.out.println("QueryWithParams 797 [" + queryStrWithParams + "]");
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    
    
    /**
     * Query Service 16 
     * @return 
     */
    protected QueryResult runQuery16() {
        int service_id = 16;
        System.out.println("Service " + service_id + "\tGet input parameters");
        String queryStr = serverURL + "service=" + service_id + "&SERVICE_RENDERID=7";
        System.out.println("fetching query Result for " + service_id);
        QueryResult queryResults = getData(queryStr, service_id, -1, -1, true);
        printQueryResults(service_id, queryResults);
        return queryResults;
    }
    
    
    
    
    private void printQueryResults(int service_id, QueryResult queryResults) {
        for(String header : queryResults.getColumnNames()) {
            System.out.print(header + "\t");
        }
        System.out.println("");
        for(Integer type : queryResults.getColumnTypes()) {
            System.out.print(type + "\t");
        }
        System.out.println("");
        
        for(List<String> rowData : queryResults.getData()) {
            System.out.print(service_id + "\t");
            for(String entry : rowData) {
                System.out.print(entry + "\t");
            }
            System.out.println("");
        }
        System.out.println("query Result: " + queryResults);
    }

    
    private String addParametersToUrl(String url,Map<String,String> params){
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
        
    
    
      
    
    
    public static void main(String[] args) {
        
        String host = "bioservicesdev.pfizer.com";
        int port = 443;
        String serverUrl = "https://" + host + ":" + port + "/TouVis/user/DataServletFiltered?";

        BioServicesInitParams initParams = new BioServicesInitParams();
        //initParams.setUser("app");
        //initParams.setPasswd("test");
        initParams.setBioServicesServer(serverUrl);
        initParams.setServer(host);
        initParams.setPort(port);
        /*AmiDemo amiDemo = new AmiDemo();
        amiDemo.setInitParams(initParams);
        int service_id = 753;
        System.out.println("Rows\tUnchunked time\tChunked time");


        String qString = serverUrl + "service=" + service_id + "&SERVICE_RENDERID=7";

        System.out.println("Getting the data:");
        QueryResult dataFromBS = amiDemo.getData(qString, service_id, -1, -1, true);
        System.out.println("DataFrameFromBS = " + dataFromBS);
        System.out.println("Writing out the names:");
        System.out.println(dataFromBS.getColumnNames().toString());
        System.out.println("Finished writing out the names:");
        
        service_id = 727;
        String str2 = serverUrl + "service=" + service_id + "&SERVICE_RENDERID=7";
        QueryResult dataFrame2 = amiDemo.getData(str2, service_id, -1, -1, true);
        System.out.println("DataFram2 = " + dataFrame2);*/
    
        AmiDemo amiDemo = new AmiDemo();
    }
}
