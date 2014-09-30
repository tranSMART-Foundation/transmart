package com.pfizer.mrbt.genomics.TransmartClient;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestTransmart {
    private static String SERVICE_PATH = "http://amre1al336.pcld.pfizer.com:8080/transmartPfizer/webservice/";
    private static String GET_SNP_SOURCE = "getSnpSources";
    private static String GET_GENE_SOURCE = "getGeneSources";
    private static String GET_GENE_BY_POSITION = "getGeneByPosition";
    private static String GET_GENE_BOUNDS = "computeGeneBounds";
    //private static String GET_MODEL_INFO_BY_DATA_TYPE = "getModelInfoByDataType";
    private static String GET_MODEL_INFO_BY_DATA_TYPE = "getSecureModelInfoByDataType";
    private static String GET_DATA_FOR_FILERED_BY_MODEL_ID_GENE_AND_RANGE = "resultDataForFilteredByModelIdGeneAndRangeRev";

    public TestTransmart() {
        try {
            long time0 = System.currentTimeMillis();
            testGetSnpSources();
            testGetGeneSources();
            int geneSrcId = 1; int start = 100000; int stop = 300000; int chromosome = 7;
            long time1 = System.currentTimeMillis();
            //testGetGeneByPosition(geneSrcId, start, stop, chromosome);
            long time2 = System.currentTimeMillis();
            testComputeGeneBounds("A1CF");
            long time3 = System.currentTimeMillis();
            int dataType = 1;
            testGetModelInfoByDataType(dataType);
            long time4 = System.currentTimeMillis();
            String gene = "MCR4"; 
            int range = 10000; 
            List<Long> modelIDs = new ArrayList<Long>();
            //modelIDs.add(226266064);
            //modelIDs.add(227238248);
            modelIDs.add(152679278l);
            testGetDataForFilteredByModelIdGeneAndRangeRev(gene, range, modelIDs);
            long time5 = System.currentTimeMillis();
            System.out.println("Time 1 " + (time1-time0)/1000);
            System.out.println("Time 2 " + (time2-time1)/1000);
            System.out.println("Time 3 " + (time3-time2)/1000);
            System.out.println("Time 4 " + (time4-time3)/1000);
            System.out.println("Time 5 " + (time5-time4)/1000);
            
        } catch(UniformInterfaceException uie) {  // resource return code is unexpected.
            int statusCode = uie.getResponse().getClientResponseStatus().getStatusCode();
            System.out.println("status code: " + statusCode);
            uie.printStackTrace();
            
        } catch(ClientHandlerException che) { // error in client handler while processing
            che.printStackTrace();
            
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void testGetSnpSources() throws UniformInterfaceException, ClientHandlerException {
        String path = SERVICE_PATH + GET_SNP_SOURCE;
        String xmlResult = TransmartUtil.fetchResult(path);
        System.out.println("TestGetSnpSources: " + xmlResult);
        TransmartUtil.printXmlParsing(xmlResult);
    }
    
    private void testGetGeneSources() throws UniformInterfaceException, ClientHandlerException {
        String path = SERVICE_PATH + GET_GENE_SOURCE;
        String xmlResult = TransmartUtil.fetchResult(path);
        System.out.println("TestGetGeneSources: " + xmlResult);
        TransmartUtil.printXmlParsing(xmlResult);
    }
    
    /**
     * Returns a list of genes found in a given range of a chromosome
     * @param geneSrcId  Note that this parameter is ignored!
     * @param start
     * @param stop
     * @param chromosome 
     */
    private void testGetGeneByPosition(int geneSrcId, int start, int stop, int chromosome) {
        String path = SERVICE_PATH + GET_GENE_BY_POSITION;
        HashMap<String,String>mapping = new HashMap<String,String>();
        mapping.put("chromosome", "21");
        mapping.put("start", "30991100");
        mapping.put("stop",  "31098000");
        String query = TransmartUtil.addParametersToUrl(path, mapping);
        String xmlResult = TransmartUtil.fetchResult(query);
        System.out.println("TestGeneByPosition: " + xmlResult);
        TransmartUtil.printXmlParsing(xmlResult);
    }
    
    private void testComputeGeneBounds(String geneSymbol) {
        String path = SERVICE_PATH + GET_GENE_BOUNDS;
        HashMap<String,String>mapping = new HashMap<String,String>();
        mapping.put("geneSymbol", "TNF");
        String query = TransmartUtil.addParametersToUrl(path, mapping);
        System.out.println("TestComputQuery [" + query + "]");
        String xmlResult = TransmartUtil.fetchResult(query);
        System.out.println("TestGeneByBounds: " + xmlResult);
        TransmartUtil.printXmlParsing(xmlResult);
    }
    
    /**
     * Returns analysis IDs with model name, analysis name, study name 
     * (dataType 1=GWAS, 2=EQTL, 3=Metabolic).
     */
    private void testGetModelInfoByDataType(int dataType) {
        String path = SERVICE_PATH + GET_MODEL_INFO_BY_DATA_TYPE;
        HashMap<String,String>mapping = new HashMap<String,String>();
        mapping.put("dataType", dataType + "");
        String query = TransmartUtil.addParametersToUrl(path, mapping);
        String xmlResult = TransmartUtil.fetchResult(query);
        System.out.println("TestGetModelInfoByDataType: " + xmlResult);
        TransmartUtil.printXmlParsing(xmlResult);
    }
    
    private void testGetDataForFilteredByModelIdGeneAndRangeRev(String geneName, int range, List<Long> modelIDs) {
        String path = SERVICE_PATH + GET_DATA_FOR_FILERED_BY_MODEL_ID_GENE_AND_RANGE;
        HashMap<String,String>mapping = new HashMap<String,String>();
        mapping.put("geneName", geneName);
        mapping.put("range", range + "");
        String modelIdStr = TransmartUtil.joinLong(",", modelIDs);
        mapping.put("modelId", modelIdStr);
        String query = TransmartUtil.addParametersToUrl(path, mapping);
        System.out.println("Query: " + query);
        String xmlResult = TransmartUtil.fetchResult(query);
        //System.out.println("GetSnpsForModelWithGeneAndRange: " + xmlResult);
        TransmartUtil.printXmlParsing(xmlResult);
    }
    
    public static void main(String[] args) {
        TestTransmart testTransmart = new TestTransmart();
    }
}
