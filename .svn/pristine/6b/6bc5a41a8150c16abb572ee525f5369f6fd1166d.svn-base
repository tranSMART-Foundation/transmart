/*
 * Queries the database through bioservices based on the query tab.
 */
package com.pfizer.mrbt.genomics.TransmartClient;

import com.pfizer.mrbt.genomics.bioservices.*;
import com.pfizer.mrbt.genomics.data.GeneAnnotation;
import com.pfizer.tnb.api.server.util.QueryResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author henstockpv
 */
public class TransmartGeneAnnotationFetch {

    public TransmartGeneAnnotationFetch() {
        super();
    }

    /**
     * Main routine for loading and populating the GeneAnnotations model. It takes in
     * a single gene from the geneSourceId with the start, stop and chromosome and
     * returns a list of the genes that map into this range based on the bioservices
     * query 
     */
    /*public ArrayList<GeneAnnotation> fetchData(int geneSourceId, int start, int stop, int chromosome) {
        int service_id = BioservicesParameters.GENE_ANNOTATION_SERVICE_ID;
        String queryStr = BioservicesParameters.SERVER_URL + "service=" + service_id + "&SERVICE_RENDERID=7";

        // fill param map with generalities
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("GENESRCID", geneSourceId + "");
        paramMap.put("START", start + "");
        paramMap.put("STOP", stop + "");
        paramMap.put("CHR", chromosome + "");


        String queryStrWithParams = BioservicesParameters.addParametersToUrl(queryStr, paramMap);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        ArrayList<GeneAnnotation> geneAnnotations = parseQueryResultsIntoGeneAnnotations(queryResults);
        return geneAnnotations;
    }*/
    
    /**
     * Fetches the gene annotation data from Transmart into an xml string
     * it returns.  
     * @param geneSourceId
     * @param start
     * @param stop
     * @param chromosome
     * @return 
     */
    public String fetchDataIntoQueryResult(GeneSourceOption geneSourceOption, int start, int stop, int chromosome) {
        //String queryStr = TransmartServicesParameters.SERVER_URL + TransmartServicesParameters.GENE_ANNOTATION_METHOD;
        String queryStr = TransmartServicesParameters.getServerURL() + TransmartServicesParameters.GENE_ANNOTATION_METHOD;
        
        // fill param map with generalities
        HashMap<String, String> paramMap = new HashMap<String, String>();
        //paramMap.put("GENESRCID", geneSourceOption.getId() + "");
        paramMap.put("start", start + "");
        paramMap.put("stop", stop + "");
        paramMap.put("chromosome", chromosome + "");


        String queryStrWithParams = TransmartUtil.addParametersToUrl(queryStr, paramMap);
        System.out.println("Annotation query: " + queryStrWithParams);
        String xmlResults = TransmartUtil.fetchResult(queryStrWithParams);
        return xmlResults;
    }
    
    /**
     * Parses each row of the queryResults into a list of GeneAnnotation objects 
     * @param queryResults
     * @return 
     */
    public ArrayList<GeneAnnotation> parseQueryResultsIntoGeneAnnotations(String xmlResults) {
        ArrayList<GeneAnnotation> geneAnnotations = new ArrayList<GeneAnnotation>();
        ArrayList<ArrayList<String>> queryResults = TransmartUtil.parseXml(xmlResults);
        for(List<String> row : queryResults) {
            String gene = row.get(TransmartServicesParameters.GENE_ANNOTATION_GENE_SYMBOL_COL);
            try {
                int start   = Integer.parseInt(row.get(TransmartServicesParameters.GENE_ANNOTATION_START_COL));
                int stop    = Integer.parseInt(row.get(TransmartServicesParameters.GENE_ANNOTATION_STOP_COL));
                int strand = Integer.parseInt(row.get(TransmartServicesParameters.GENE_ANNOTATION_STRAND_COL));
                boolean direction = (strand==1);
                GeneAnnotation geneAnnotation = new GeneAnnotation(gene, direction, start, stop);
                geneAnnotations.add(geneAnnotation);
            } catch(NumberFormatException nfe) {
                System.out.println("Failed to parse gene annotation line " + row);
            }
        }
        return geneAnnotations;
    }
    
}
