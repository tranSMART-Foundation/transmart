/*
 * Uses bioservices 729 with the SNP data to figure out the gene range and the
 * corresponding location
 */
package com.pfizer.mrbt.genomics.TransmartClient;

import com.pfizer.mrbt.genomics.bioservices.*;
import com.pfizer.tnb.bsutil.BsServiceClientImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author henstockpv
 */
public class TransmartGeneLocationService extends BsServiceClientImpl {

    private static int DEFAULT_CHROMOSOME = -1;
    private static int DEFAULT_START = -1;
    private static int DEFAULT_STOP  = -1;
    private int chromosome = DEFAULT_CHROMOSOME;
    private int geneStart = DEFAULT_START;
    private int geneStop = DEFAULT_STOP;

    public TransmartGeneLocationService() {
        super();
    }

    /**
     * Performs a bioServices call to fetch the chromosome, geneStart, and
     * geneStop given the geneSourceOption.  It could fail since the gene isn't
     * found or if one of the numbers returned is not a number.  In which case
     * it defaults to the DEFAULT values for each.
     * @param gene
     * @param geneSourceOption 
     */
    public void computeGeneBounds(String gene, GeneSourceOption geneSourceOption) {
        String queryStr = TransmartServicesParameters.getServerURL() + TransmartServicesParameters.GENE_LOCATION_METHOD;

        // fill param map with generalities
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("geneSymbol", gene);
        //paramMap.put("GENE_SOURCE_ID", geneSourceOption.getId() + "");
        String queryStrWithParams = TransmartUtil.addParametersToUrl(queryStr, paramMap);
        String xmlResults = TransmartUtil.fetchResult(queryStrWithParams);
        parseResults(xmlResults);
    }

    /**
     * Parses the first gene found into chromosome, geneStart, geneEnd.  If none
     * are found or any of these are not a valid integer, they all resort to their
     * default values.
     * They have screwed up the xmlResults so that it returns 3 different rows with
     * one data point that correspond to the values here.
     */
    protected void parseResults(String xmlResults) {
        ArrayList<ArrayList<String>> queryResults = TransmartUtil.parseXml(xmlResults);
        if (queryResults.size() >= 3) {
            try {
                chromosome = Integer.parseInt(queryResults.get(TransmartServicesParameters.GENE_LOCATION_CHROMOSOME_COL).get(0));
                geneStart = Integer.parseInt(queryResults.get(TransmartServicesParameters.GENE_LOCATION_START_COL).get(0));
                geneStop = Integer.parseInt(queryResults.get(TransmartServicesParameters.GENE_LOCATION_STOP_COL).get(0));
            } catch (NumberFormatException nfe) {
                chromosome = DEFAULT_CHROMOSOME;
                geneStart = DEFAULT_START;
                geneStop = DEFAULT_STOP;
                System.out.println("Failed to parse geneLocation "
                                   + queryResults.get(TransmartServicesParameters.GENE_LOCATION_CHROMOSOME_COL).get(0) + " or "
                                   + queryResults.get(TransmartServicesParameters.GENE_LOCATION_START_COL).get(0) + " or "
                                   + queryResults.get(TransmartServicesParameters.GENE_LOCATION_STOP_COL).get(0));
            }
        }
    }

    public int getChromosome() {
        return chromosome;
    }

    public int getGeneStop() {
        return geneStop;
    }

    public int getGeneStart() {
        return geneStart;
    }
    
    /**
     * Returns true if the values have been set (e.g. not default because the
     * previous operation was successful.
     * @return 
     */
    public boolean isSuccess() {
        if (chromosome == DEFAULT_CHROMOSOME
            || geneStart == DEFAULT_START
            || geneStop == DEFAULT_STOP) {
            return false;
        } else {
            return true;
        }

    }
}