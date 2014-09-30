/*
 * Queries the database through bioservices based on the query tab.
 */
package com.pfizer.mrbt.genomics.TransmartClient;

import com.pfizer.mrbt.genomics.webservices.GeneSourceOption;
import com.pfizer.mrbt.genomics.data.GeneAnnotation;
import com.pfizer.mrbt.genomics.webservices.Environment;
import com.pfizer.mrbt.genomics.webservices.RetrievalException;
import com.pfizer.mrbt.genomics.webservices.RetrievalMethod;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author henstockpv
 */
public class TransmartGeneAnnotationFetch {
    private final Environment environment;

    public TransmartGeneAnnotationFetch(final Environment environment) {
        this.environment = environment;
    }

    /**
     * Fetches the gene annotation data from Transmart into an xml string
     * it returns.  
     * @param geneSourceOption
     * @param paramMap
     * @return
     * @throws RetrievalException 
     */
    public String fetchDataIntoQueryResult(GeneSourceOption geneSourceOption, HashMap<String,String> paramMap) throws RetrievalException {
        //String queryStr = TransmartServicesParameters.SERVER_URL + TransmartServicesParameters.GENE_ANNOTATION_METHOD;
        String queryStr = TransmartServicesParameters.getServerURL(environment) + TransmartServicesParameters.GENE_ANNOTATION_METHOD;
        
        String queryStrWithParams = TransmartUtil.addParametersToUrl(queryStr, paramMap);
        System.out.println("Annotation query: " + queryStrWithParams);
        String xmlResults;
        try {
            xmlResults = TransmartUtil.fetchResult(queryStrWithParams);
        } catch(UniformInterfaceException uiex) {
            throw new RetrievalException(uiex.getMessage(), RetrievalMethod.ANNOTATION_SEARCH, paramMap);
        } catch(ClientHandlerException chex) {
            throw new RetrievalException(chex.getMessage(), RetrievalMethod.ANNOTATION_SEARCH, paramMap);
        } catch(Exception ex) {
            throw new RetrievalException(ex.getMessage(), RetrievalMethod.ANNOTATION_SEARCH, paramMap);
        }
        return xmlResults;
    }
    
    /**
     * Parses each row of the queryResults into a list of GeneAnnotation objects
     *
     * @param xmlResults
     * @param paramMap
     * @return
     * @throws RetrievalException
     */
    public ArrayList<GeneAnnotation> parseQueryResultsIntoGeneAnnotations(String xmlResults, HashMap<String, String> paramMap) throws RetrievalException {
        ArrayList<GeneAnnotation> geneAnnotations = new ArrayList<GeneAnnotation>();
        ArrayList<ArrayList<String>> queryResults;
        try {
            queryResults = TransmartUtil.parseXml(xmlResults);
        } catch(Exception ex) {
            throw new RetrievalException("Failed to parseXml in annotation " + RetrievalMethod.ANNOTATION_SEARCH, paramMap);
        }
        for (List<String> row : queryResults) {
            String gene = row.get(TransmartServicesParameters.GENE_ANNOTATION_GENE_SYMBOL_COL);
            try {
                int start = Integer.parseInt(row.get(TransmartServicesParameters.GENE_ANNOTATION_START_COL));
                int stop = Integer.parseInt(row.get(TransmartServicesParameters.GENE_ANNOTATION_STOP_COL));
                int strand = Integer.parseInt(row.get(TransmartServicesParameters.GENE_ANNOTATION_STRAND_COL));
                boolean direction = (strand == 1);
                GeneAnnotation geneAnnotation = new GeneAnnotation(gene, direction, start, stop);
                geneAnnotations.add(geneAnnotation);
            } catch (NumberFormatException nfe) {
                System.out.println("Failed to parse gene annotation line " + row);
                throw new RetrievalException(
                        "Failed to parse gene annotation line " + row + "\n" + nfe.getMessage(),
                        RetrievalMethod.ANNOTATION_SEARCH, paramMap);
            } catch (IndexOutOfBoundsException ioobe) {
                System.out.println("Failed to parse gene annotation line (out of bounds) " + row);
                throw new RetrievalException(
                        "Failed to parse gene annotation line (out of bounds) " + row + "\n" + ioobe.getMessage(),
                        RetrievalMethod.ANNOTATION_SEARCH, paramMap);
            }
        }
        return geneAnnotations;
    }

}
