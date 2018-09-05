/*
 * Queries the Transmart database for recombination rate data
 */
package com.pfizer.mrbt.genomics.TransmartClient;

import com.pfizer.mrbt.genomics.webservices.GeneSourceOption;
import com.pfizer.mrbt.genomics.webservices.DbSnpSourceOption;
import com.pfizer.mrbt.genomics.webservices.ModelOption;
import com.pfizer.mrbt.genomics.data.DataModel;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.GeneRange;
import com.pfizer.mrbt.genomics.data.IntronExon;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.data.NumericRange;
import com.pfizer.mrbt.genomics.data.SNP;
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
public class TransmartSNPDataFetchSnpQuery extends TransmartSNPDataFetch {

    public TransmartSNPDataFetchSnpQuery(final Environment environment) {
        super(environment);
    }

    /**
     * Generates a parameter map for the query
     *
     * @param modelOptions
     * @param dbSnpOption
     * @param geneSourceOption
     * @param snpName
     * @param basePairRadius
     * @return
     */
    @Override
    public HashMap<String, String> generateParameterMap(String snpName,
                                                        List<ModelOption> modelOptions,
                                                        DbSnpSourceOption dbSnpOption,
                                                        GeneSourceOption geneSourceOption,
                                                        int basePairRadius) {
        HashMap<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("range", basePairRadius + "");
        String modelOptionsString = convertModelOptionsToCommaDelimStr(modelOptions);
        paramMap.put("modelId", modelOptionsString);
        paramMap.put("snp", snpName);
        paramMap.put(GENE_SRC_OPTION_KEY, geneSourceOption.getId() + "");
        paramMap.put(DB_SRC_OPTION_KEY, dbSnpOption.getId() + "");
        return paramMap;
    }

    /**
     * Performs the transmart call to get the SNPs and returns the XML file
     * associated with the call.
     * @param paramMap contains all the parameters for the query
     * @return XML string of the results
     * @throws RetrievalException if the retrieval fails
     */
    @Override
    public String fetchSnpData(HashMap<String,String> paramMap) throws RetrievalException {
        String queryStrWithParams = createSnpSearchQueryStr(paramMap);
        System.out.println("SnpSearch Query Str: " + queryStrWithParams);
        try {
            String xmlResult = TransmartUtil.fetchResult(queryStrWithParams);
            //System.out.println("Elapsed time " + (endTime - startTime) / 1000);
            return xmlResult;
        } catch(UniformInterfaceException uiex) {
            throw new RetrievalException(uiex.getMessage(), RetrievalMethod.SNP_SEARCH, paramMap);
        } catch(ClientHandlerException chex) {
            throw new RetrievalException(chex.getMessage(), RetrievalMethod.SNP_SEARCH, paramMap);
        }
    }
    
    /**
     * Creates the query string with the parameters for the snp search
     * @param paramMap
     * @return 
     */
    @Override
    protected String createSnpSearchQueryStr(HashMap<String,String> paramMap) {
        String queryStr = TransmartServicesParameters.getServerURL(environment) + 
                          TransmartServicesParameters.SNP_SEARCH_METHOD;
        ArrayList<String> excludeList = new ArrayList<String>();
        excludeList.add(GENE_SRC_OPTION_KEY);
        String queryStrWithParams = TransmartUtil.addParametersToUrl(queryStr, paramMap, excludeList);
        return queryStrWithParams;
    }

    /**
     * Creates the query string with the parameters for the snp search
     * @param paramMap
     * @return 
     */
    protected String createSnpSearchQueryStrByGene(HashMap<String,String> paramMap) {
        String queryStr = TransmartServicesParameters.getServerURL(environment) + 
                          TransmartServicesParameters.SNP_SEARCH_METHOD;
        ArrayList<String> excludeList = new ArrayList<String>();
        excludeList.add(GENE_SRC_OPTION_KEY);
        excludeList.add(DB_SRC_OPTION_KEY);
        String queryStrWithParams = TransmartUtil.addParametersToUrl(queryStr, paramMap, excludeList);
        return queryStrWithParams;
    }

    /**
     * Parses the queryResult of a SNP search and multiple models into a
     * DataSet structure that it adds (or modifies) in .  In the future, this
     * should be identical to parseSnpQueryResultsIntoDataSet which is a search
     * based on gene.  However, it is not right now 12/2/13
     * @param dataSet
     * @param paramMap hashmap of the parameters and values for the query
     * @param snpSearchName is the snp we're searching for
     * @param radius = base pair radius around the SNP for searching
     * @param geneSourceOption
     * @param dbSnpOption
     * @param xmlResult
     * @throws RetrievalException if query result cannot be parsed
     * @return 
     */
    @Override
    protected DataSet parseQueryResultsIntoDataSet(DataSet dataSet,
                                                      HashMap<String, String> paramMap,
                                                      String snpSearchName,
                                                      int radius,
                                                      GeneSourceOption geneSourceOption,
                                                      DbSnpSourceOption dbSnpOption,
                                                      String xmlResult) throws RetrievalException {
        int rowIndex = 0;
        int minLoc = Integer.MAX_VALUE;
        int maxLoc = Integer.MIN_VALUE;
        int chromosome = -1;
        HashMap<String,Model> studySetModel2model = new HashMap<String,Model>();
        
        ArrayList<ArrayList<String>> queryResults;
        try {
            queryResults = TransmartUtil.parseXml(xmlResult);
        } catch(Exception ex) {
            throw new RetrievalException("Failed to parse xml query in snpSearch " + ex.getMessage(), RetrievalMethod.SNP_SEARCH, paramMap);
        }

        for (List<String> row : queryResults) {
            if (rowIndex == 0) {
                chromosome = DataModel.parseChromosomeStr(row.get(TransmartServicesParameters.SNP_SEARCH_CHROMOSOME_COL));
                dataSet.setChromosome(chromosome);
            }
            ParsedStudySetModel studySetModel = parseOutStudySetModel(row);

            String rsName = row.get(TransmartServicesParameters.SNP_SEARCH_RSID_COL);
            int rsId = Integer.parseInt(rsName.substring(2));
            double logPval = Double.parseDouble(row.get(TransmartServicesParameters.SNP_SEARCH_LOG_PVAL_COL));
            int loc = Integer.parseInt(row.get(TransmartServicesParameters.SNP_SEARCH_POSITION_COL));
            if (rsName.equals(snpSearchName)) {
                minLoc = loc - radius;
                maxLoc = loc + radius;
            } else {
                if(loc < minLoc) { minLoc = loc; }
                if(loc > maxLoc) { maxLoc = loc; }
            }
            SNP currSnp = dataSet.checkAddSnp(rsId);
            if (row.size() == TransmartServicesParameters.SNP_SEARCH_NUM_FIELDS) {
                // Must contain the added 3 features: intron/exon, regulome, recombination rate
                String intronExon = row.get(TransmartServicesParameters.SNP_SEARCH_INTRON_COL);
                if (intronExon.equals("intron")) {
                    currSnp.setIntronExon(IntronExon.INTRON);
                } else if (intronExon.equals("exon")) {
                    currSnp.setIntronExon(IntronExon.EXON);
                }
                /* skipping the recombination rate provided for SNP for now
                 String recombinationRateStr = row.get(TransmartServicesParameters.GENE_SEARCH_RECOMBINATION_RATE_COL);
                 try {
                 int recomboRate = Integer.parseInt(recombinationRateStr);
                 } catch(NumberFormatException nfe) {
                 System.err.println("Failed to parse recombination rate " + recombinationRateStr + " for SNP " + currSnp);
                 }*/
                if(row.get(TransmartServicesParameters.SNP_SEARCH_SNP_GENE_COL).length() > 0) {
                    String geneName = row.get(TransmartServicesParameters.SNP_SEARCH_SNP_GENE_COL);
                    currSnp.setAssociatedGene(geneName);
                }


                String regulomeStr = row.get(TransmartServicesParameters.SNP_SEARCH_REGULOME_COL);
                currSnp.setRegulome(regulomeStr);
            }
            currSnp.setLoc(loc);
            String key = studySetModel.getKey();
            if(! studySetModel2model.containsKey(key)) {
                studySetModel2model.put(key, new Model(studySetModel.getStudy(), studySetModel.getSet(), studySetModel.getModel()));
            } 
            studySetModel2model.get(key).addSnpPval(currSnp, logPval);
            
            //Model currModel = dataSet.checkAddModel(studySetModel.getStudy(), studySetModel.getSet(), studySetModel.getModel());
            //currModel.addSnpPval(currSnp, logPval);
            //dataSet.addSnpModel2Pval(currSnp, currModel, logPval);
            rowIndex++;
        }
        // kluge todo
        //System.out.println("MaxLoc " + maxLoc + "\tminLoc " + minLoc + "\tAvg " + avg + "\tradius " + radius);
        dataSet.setXAxisRange(new NumericRange(minLoc, maxLoc));
        GeneRange geneRange = new GeneRange(snpSearchName, chromosome, minLoc, maxLoc);
        geneRange.setRadius(radius);
        dataSet.setGeneRange(geneRange);
        dataSet.setDbSnpOption(dbSnpOption);
        dataSet.setGeneSourceOption(geneSourceOption);
        for(Model model : studySetModel2model.values()) {
            dataSet.addModel(model);
        }
        return dataSet;
    }

    /**
     * Performs parsing on the GENE_SEARCH_STUDY_SET_MODEL_NAME_COL's entry of
     * row and divides it into the study/set/model. This code has a few
     * different ifs since it seems to keep evolving as different data sets come
     * in This should be identical to the parseOutStuddySetModelFromSnpSearch
     * but right now 12/3/2013, it is not so we have modified the parameters.
     *
     * @param row
     * @return
     */
    protected ParsedStudySetModel parseOutStudySetModel(List<String> row) {
        String studySetModel = row.get(TransmartServicesParameters.SNP_SEARCH_STUDY_SET_MODEL_NAME_COL);
        String[] tokens = studySetModel.split("\\s+\\-\\s+");
        String study = "study";
        String set = "set";
        String model = "model";
        if (tokens.length == 2) {
            set = tokens[0];
            study = tokens[1];
        } else {
            study = row.get(TransmartServicesParameters.SNP_SEARCH_STUDY_SET_MODEL_NAME_COL);
            //set = row.get(TransmartServicesParameters.GENE_SEARCH_SET_COL);
        }
        return new ParsedStudySetModel(study, set, model);
    }
      
}
