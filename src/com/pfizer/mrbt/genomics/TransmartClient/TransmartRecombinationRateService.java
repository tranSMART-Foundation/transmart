/*
 * Queries the database through bioservices based on the query tab.
 */
package com.pfizer.mrbt.genomics.TransmartClient;

import com.pfizer.mrbt.genomics.webservices.GeneSourceOption;
import com.pfizer.mrbt.genomics.data.SnpRecombRate;
import com.pfizer.mrbt.genomics.webservices.DbSnpSourceOption;
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
public class TransmartRecombinationRateService {
    private float maxRecombinationRate = 0f;
    private final Environment environment;
    
    public TransmartRecombinationRateService(Environment environment) {
        this.environment = environment;
    }
    

    /**
     * Main routine for loading and populating the data model. It assumes that
     * the radius has been validated and does not validate independently.  Seeks
     * recombination rates for geneName +/- basePairRadius around outside for
     * the gene in geneSourceId (not used)
     * @param geneName
     * @param basePairRadius
     * @param geneSourceId (not used but included for later versions)
     * @throws RetrievalException if service fails
     * @return 
     */
    /*public ArrayList<SnpRecombRate> fetchRecombinationRateData(String geneName,
                                           int basePairRadius,
                                           int geneSourceId) throws RetrievalException {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("geneName", geneName);
        paramMap.put("range", basePairRadius + "");
        String queryStrWithParams = generateRecombinationRateForGeneQuery(paramMap);
        //System.out.println("QueryStr: [" + queryStrWithParams + "]");
        //long startTime = System.currentTimeMillis();
        ArrayList<SnpRecombRate> snpRecombRates = new ArrayList<SnpRecombRate>();
        try {
            String xmlResult = TransmartUtil.fetchResult(queryStrWithParams);
            //QueryResult queryResults = (queryStrWithParams, TransmartServicesParameters.RECOMBINATION_RATE_GENE_METHOD, -1, -1, true);
            //long endTime = System.currentTimeMillis();
            snpRecombRates = parseQueryResults(xmlResult, paramMap, byGeneNotSnp);
        } catch(UniformInterfaceException uiex) {
            throw new RetrievalException(uiex.getMessage(), RetrievalMethod.RECOMBINATION_RATE_SEARCH, paramMap);
        } catch(ClientHandlerException chex) {            
            throw new RetrievalException(chex.getMessage(), RetrievalMethod.RECOMBINATION_RATE_SEARCH, paramMap);
        } catch (Exception ex) {
            throw new RetrievalException(ex.getMessage(), RetrievalMethod.RECOMBINATION_RATE_SEARCH, paramMap);
            //ex.printStackTrace();
        }
        return snpRecombRates;
    }*/
    
    /**
     * Returns the query string for a geneName search +/- basePairRadius
     * @param paramMap
     * @return 
     */
    protected String generateRecombinationRateForGeneQuery(HashMap<String,String> paramMap) {
        String rootQueryStr = TransmartServicesParameters.getServerURL(environment) + 
                              TransmartServicesParameters.RECOMBINATION_RATE_GENE_METHOD + "?";
        String queryStrWithParams = TransmartServicesParameters.addParametersToUrl(rootQueryStr, paramMap);
        return queryStrWithParams;
    }

    /**
     * Returns the query string for a geneName search +/- basePairRadius
     * @param paramMap
     * @return 
     */
    protected String generateRecombinationRateForSnpQuery(HashMap<String,String> paramMap) {
        String rootQueryStr = TransmartServicesParameters.getServerURL(environment) + 
                              TransmartServicesParameters.RECOMBINATION_RATE_SNP_METHOD + "?";
        String queryStrWithParams = TransmartServicesParameters.addParametersToUrl(rootQueryStr, paramMap);
        return queryStrWithParams;
    }

    /**
     * Main routine for fetching recombination rate around the specified
     * geneName with the given basePairRadius
     * the radius has been validated.  
     * @param geneName
     * @param basePairRadius
     * @param geneSourceOption  (currently ignored)
     * @param dbSnpSourceOption (currently ignored)
     * @throws RetrievalException if fetching result fails
     * @return 
     */
    public ArrayList<SnpRecombRate> fetchRecombinationRateByGeneQueryResult(String geneName,
                                           int basePairRadius,
                                           GeneSourceOption geneSourceOption,
                                           DbSnpSourceOption dbSnpSourceOption) throws RetrievalException {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("geneName", geneName);
        paramMap.put("range", basePairRadius + "");
        String queryStrWithParams = generateRecombinationRateForGeneQuery(paramMap);

        ArrayList<SnpRecombRate> snpRecombRates = new ArrayList<SnpRecombRate>();
        try {
            String xmlResults = TransmartUtil.fetchResult(queryStrWithParams);
            System.out.println("ReombQuery with params: " + queryStrWithParams);
            snpRecombRates = parseQueryResults(xmlResults, paramMap, true);
            //System.out.println("XML results " + xmlResults);
        } catch(UniformInterfaceException uiex) {
            throw new RetrievalException(uiex.getMessage(), RetrievalMethod.RECOMBINATION_RATE_SEARCH, paramMap);
        } catch(ClientHandlerException chex) {            
            throw new RetrievalException(chex.getMessage(), RetrievalMethod.RECOMBINATION_RATE_SEARCH, paramMap);
        } catch (Exception ex) {
            throw new RetrievalException(ex.getMessage(), RetrievalMethod.RECOMBINATION_RATE_SEARCH, paramMap);
            //ex.printStackTrace();
        }
        return snpRecombRates;
    }

    /**
     * Main routine for fetching recombination rate around the specified
     * geneName with the given basePairRadius
     * the radius has been validated.  
     * @param snpName
     * @param basePairRadius
     * @param geneSourceOption (currently ignored)
     * @param dbSnpSourceOption
     * @throws RetrievalException if fetching service fails
     * @return 
     */
    public ArrayList<SnpRecombRate> fetchRecombinationRateBySnpQueryResult(String snpName,
                                           int basePairRadius,
                                           GeneSourceOption geneSourceOption,
                                           DbSnpSourceOption dbSnpSourceOption) throws RetrievalException {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("snp", snpName);
        paramMap.put("range", basePairRadius + "");
        paramMap.put("snpSource", dbSnpSourceOption.getId() + "");
        String queryStrWithParams = generateRecombinationRateForSnpQuery(paramMap);

        ArrayList<SnpRecombRate> snpRecombRates = new ArrayList<SnpRecombRate>();
        try {
            String xmlResults = TransmartUtil.fetchResult(queryStrWithParams);
            System.out.println("ReombQuery with params: " + queryStrWithParams);
            snpRecombRates = parseQueryResults(xmlResults, paramMap, false);
            //System.out.println("XML results " + xmlResults);
        } catch(UniformInterfaceException uiex) {
            throw new RetrievalException(uiex.getMessage(), RetrievalMethod.RECOMBINATION_RATE_SEARCH, paramMap);
        } catch(ClientHandlerException chex) {            
            throw new RetrievalException(chex.getMessage(), RetrievalMethod.RECOMBINATION_RATE_SEARCH, paramMap);
        } catch (Exception ex) {
            throw new RetrievalException(ex.getMessage(), RetrievalMethod.RECOMBINATION_RATE_SEARCH, paramMap);
            //ex.printStackTrace();
        }
        return snpRecombRates;
    }

    /**
     * Parses each row and puts the loc and rate into snpRecombRates.  It also
     * stores maxRecombinatinoRate with the maximum rate value.  If either value
     * cannot be parsed, it puts a sysmtem.out.println call and doesn't include
     * it in the list.
     * @param xmlResults
     * @param paramMap
     * @throws RetrievalException
     * @return 
     */
    public ArrayList<SnpRecombRate> parseQueryResults(String xmlResults, HashMap<String,String> paramMap, boolean byGeneNotSnp)  throws RetrievalException {
        ArrayList<SnpRecombRate> snpRecombRates = new ArrayList<SnpRecombRate>();
        maxRecombinationRate = 0f;
        ArrayList<ArrayList<String>> queryResults;        
        try {
            queryResults = TransmartUtil.parseXml(xmlResults);
        } catch(Exception ex) {
            throw new RetrievalException("Failed parse in recombination rates " + ex.getMessage(), RetrievalMethod.RECOMBINATION_RATE_SEARCH, paramMap);
        }
        int positionCol = TransmartServicesParameters.RECOMBINATION_GENE_POSITION_COL;
        int rateCol     = TransmartServicesParameters.RECOMBINATION_GENE_RATE_COL;
        if(! byGeneNotSnp) {
            positionCol = TransmartServicesParameters.RECOMBINATION_SNP_POSITION_COL;
            rateCol     = TransmartServicesParameters.RECOMBINATION_SNP_RATE_COL;
        }
        for(List<String> row : queryResults) {
            try {
                int loc     = Integer.parseInt(row.get(positionCol));
                float rate  = Float.parseFloat(row.get(rateCol));
                snpRecombRates.add(new SnpRecombRate(loc, rate));
                if(rate > maxRecombinationRate) {
                    maxRecombinationRate = rate;
                }
            } catch(NumberFormatException nfe) {
                System.out.println("Failed to parse [" + 
                                   row.get(positionCol) + "]\t[" +               
                    row.get(rateCol) + "]");
                throw new RetrievalException(nfe.getMessage(), RetrievalMethod.RECOMBINATION_RATE_SEARCH, paramMap);
            } catch(IndexOutOfBoundsException ioobex) {
                throw new RetrievalException(ioobex.getMessage(), RetrievalMethod.RECOMBINATION_RATE_SEARCH, paramMap);
            }
        }
        return snpRecombRates;
    }

    /**
     * Parses each row and puts the loc and rate into snpRecombRates.  It also
     * stores maxRecombinatinoRate with the maximum rate value.  If either value
     * cannot be parsed, it puts a sysmtem.out.println call and doesn't include
     * it in the list.
     * @param xmlResults
     * @param paramMap
     * @throws RetrievalException
     * @return 
     */
    public ArrayList<SnpRecombRate> parseQueryBySnpResults(String xmlResults, HashMap<String,String> paramMap)  throws RetrievalException {
        ArrayList<SnpRecombRate> snpRecombRates = new ArrayList<SnpRecombRate>();
        maxRecombinationRate = 0f;
        ArrayList<ArrayList<String>> queryResults;        
        try {
            queryResults = TransmartUtil.parseXml(xmlResults);
        } catch(Exception ex) {
            throw new RetrievalException("Failed parse in recombination rates " + ex.getMessage(), RetrievalMethod.RECOMBINATION_RATE_SEARCH, paramMap);
        }
        int positionCol = TransmartServicesParameters.RECOMBINATION_SNP_POSITION_COL;
        int rateCol     = TransmartServicesParameters.RECOMBINATION_SNP_RATE_COL;
        for(List<String> row : queryResults) {
            try {
                int loc     = Integer.parseInt(row.get(positionCol));
                float rate  = Float.parseFloat(row.get(rateCol));
                snpRecombRates.add(new SnpRecombRate(loc, rate));
                if(rate > maxRecombinationRate) {
                    maxRecombinationRate = rate;
                }
            } catch(NumberFormatException nfe) {
                System.out.println("Failed to parse [" + 
                                   row.get(positionCol) + "]\t[" +               
                    row.get(rateCol) + "]");
                throw new RetrievalException(nfe.getMessage(), RetrievalMethod.RECOMBINATION_RATE_SEARCH, paramMap);
            } catch(IndexOutOfBoundsException ioobex) {
                throw new RetrievalException(ioobex.getMessage(), RetrievalMethod.RECOMBINATION_RATE_SEARCH, paramMap);
            }
        }
        return snpRecombRates;
    }

    public float getMaxRecombinationRate() {
        return maxRecombinationRate;
    }
    
    
}
