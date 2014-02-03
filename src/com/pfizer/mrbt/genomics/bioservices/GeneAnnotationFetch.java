/*
 * Queries the database through bioservices based on the query tab.
 */
package com.pfizer.mrbt.genomics.bioservices;

import com.pfizer.mrbt.genomics.data.GeneAnnotation;
import com.pfizer.tnb.api.server.util.QueryResult;
import com.pfizer.tnb.bsutil.BsServiceClientImpl;
import com.pfizer.tnb.api.server.util.BioServicesInitParams;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author henstockpv
 */
public class GeneAnnotationFetch extends BsServiceClientImpl {

    public GeneAnnotationFetch() {
        super();
        BioServicesInitParams initParams = new BioServicesInitParams();
        initParams.setBioServicesServer(BioservicesParameters.SERVER_URL);
        initParams.setServer(BioservicesParameters.HOST);
        initParams.setPort(BioservicesParameters.PORT);
        setInitParams(initParams);
        
    }

    /**
     * Main routine for loading and populating the GeneAnnotations model. It takes in
     * a single gene from the geneSourceId with the start, stop and chromosome and
     * returns a list of the genes that map into this range based on the bioservices
     * query 
     */
    public ArrayList<GeneAnnotation> fetchData(int geneSourceId, int start, int stop, int chromosome) {
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
    }
    
    /**
     * Fetches the gene annotation data from bioservices into the QueryResult that
     * it returns.  
     * @param geneSourceId
     * @param start
     * @param stop
     * @param chromosome
     * @return 
     */
    public QueryResult fetchDataIntoQueryResult(GeneSourceOption geneSourceOption, int start, int stop, int chromosome) {
        int service_id = BioservicesParameters.GENE_ANNOTATION_SERVICE_ID;
        String queryStr = BioservicesParameters.SERVER_URL + "service=" + service_id + "&SERVICE_RENDERID=7";

        // fill param map with generalities
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("GENESRCID", geneSourceOption.getId() + "");
        paramMap.put("START", start + "");
        paramMap.put("STOP", stop + "");
        paramMap.put("CHR", chromosome + "");


        String queryStrWithParams = BioservicesParameters.addParametersToUrl(queryStr, paramMap);
        //System.out.println("Annotation query: " + queryStrWithParams);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        return queryResults;
    }
    
    /**
     * Parses each row of the queryResults into a list of GeneAnnotation objects 
     * @param queryResults
     * @return 
     */
    public ArrayList<GeneAnnotation> parseQueryResultsIntoGeneAnnotations(QueryResult queryResults) {
        ArrayList<GeneAnnotation> geneAnnotations = new ArrayList<GeneAnnotation>();
        for(List<String> row : queryResults.getData()) {
            String gene = row.get(BioservicesParameters.GENE_ANNOTATION_GENE_SYMBOL_COL);
            try {
                int start   = Integer.parseInt(row.get(BioservicesParameters.GENE_ANNOTATION_START_COL));
                int stop    = Integer.parseInt(row.get(BioservicesParameters.GENE_ANNOTATION_STOP_COL));
                int strand = Integer.parseInt(row.get(BioservicesParameters.GENE_ANNOTATION_STRAND_COL));
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
