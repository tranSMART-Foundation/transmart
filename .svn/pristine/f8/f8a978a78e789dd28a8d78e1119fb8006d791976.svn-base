/*
 * Uses bioservices 729 with the SNP data to figure out the gene range and the
 * corresponding location
 */
package com.pfizer.mrbt.genomics.bioservices;

import com.pfizer.tnb.api.server.util.QueryResult;
import com.pfizer.tnb.bsutil.BsServiceClientImpl;
import com.pfizer.tnb.api.server.util.BioServicesInitParams;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author henstockpv
 */
public class GeneLocationService extends BsServiceClientImpl {

    private static int DEFAULT_CHROMOSOME = -1;
    private static int DEFAULT_START = -1;
    private static int DEFAULT_STOP  = -1;
    private int chromosome = DEFAULT_CHROMOSOME;
    private int geneStart = DEFAULT_START;
    private int geneStop = DEFAULT_STOP;

    public GeneLocationService() {
        super();
        BioServicesInitParams initParams = new BioServicesInitParams();
        initParams.setBioServicesServer(BioservicesParameters.SERVER_URL);
        initParams.setServer(BioservicesParameters.HOST);
        initParams.setPort(BioservicesParameters.PORT);
        setInitParams(initParams);
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
        int service_id = BioservicesParameters.GENE_LOCATION_SERVICE_ID;
        String queryStr = BioservicesParameters.SERVER_URL + "service=" + service_id + "&SERVICE_RENDERID=7";

        // fill param map with generalities
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("GENE_SYMBOL", gene);
        paramMap.put("GENE_SOURCE_ID", geneSourceOption.getId() + "");
        String queryStrWithParams = BioservicesParameters.addParametersToUrl(queryStr, paramMap);
        QueryResult queryResults = getData(queryStrWithParams, service_id, -1, -1, true);
        parseResults(queryResults);
    }

    /**
     * Parses the first gene found into chromosome, geneStart, geneEnd.  If none
     * are found or any of these are not a valid integer, they all resort to their
     * default values.
     */
    protected void parseResults(QueryResult queryResults) {
        if (queryResults.getData().size() > 0) {
            List<String> firstRow = queryResults.getData().get(0);
            try {
                chromosome = Integer.parseInt(firstRow.get(BioservicesParameters.GENE_LOCATION_CHROMOSOME_COL));
                geneStart = Integer.parseInt(firstRow.get(BioservicesParameters.GENE_LOCATION_START_COL));
                geneStop = Integer.parseInt(firstRow.get(BioservicesParameters.GENE_LOCATION_STOP_COL));
            } catch (NumberFormatException nfe) {
                chromosome = DEFAULT_CHROMOSOME;
                geneStart = DEFAULT_START;
                geneStop = DEFAULT_STOP;
                System.out.println("Failed to parse geneLocation "
                                   + firstRow.get(BioservicesParameters.GENE_LOCATION_CHROMOSOME_COL) + " or "
                                   + firstRow.get(BioservicesParameters.GENE_LOCATION_START_COL) + " or "
                                   + firstRow.get(BioservicesParameters.GENE_LOCATION_STOP_COL));
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
        if(chromosome==DEFAULT_CHROMOSOME ||
                  geneStart ==DEFAULT_START ||
                  geneStop   == DEFAULT_STOP) {
            return false;
        } else {
            return true;
        }
               
    }
}