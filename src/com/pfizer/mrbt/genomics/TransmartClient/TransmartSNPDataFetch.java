/*
 * Queries the Transmart database for recombination rate data
 */
package com.pfizer.mrbt.genomics.TransmartClient;

import com.pfizer.mrbt.genomics.webservices.GeneSourceOption;
import com.pfizer.mrbt.genomics.webservices.DbSnpSourceOption;
import com.pfizer.mrbt.genomics.webservices.ModelOption;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.webservices.Environment;
import com.pfizer.mrbt.genomics.webservices.RetrievalException;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author henstockpv
 */
public abstract class TransmartSNPDataFetch {
    protected final Environment environment;
    public final static String GENE_SRC_OPTION_KEY = "geneSource";
    public final static String DB_SRC_OPTION_KEY   = "snpSource";

    public TransmartSNPDataFetch(final Environment environment) {
        this.environment = environment;
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
    public abstract HashMap<String, String> generateParameterMap(String snpName,
                                                        List<ModelOption> modelOptions,
                                                        DbSnpSourceOption dbSnpOption,
                                                        GeneSourceOption geneSourceOption,
                                                        int basePairRadius);
   

    /**
     * Returns a comma-delimited string from the modelOptions's modelID field
     * @param modelOptions
     * @return 
     */
    protected String convertModelOptionsToCommaDelimStr(List<ModelOption> modelOptions) {
        StringBuilder sb = new StringBuilder();
        int cnt = 0;
        for (ModelOption modelOption : modelOptions) {
            if (cnt > 0) {
                sb.append(",");
            }
            sb.append(modelOption.getModelId() + "");
            cnt++;
        }
        return sb.toString();
    }
    

    /**
     * Performs the transmart call to get the SNPs and returns the XML file
     * associated with the call.
     * @param paramMap contains mapping of all the input parameters for the query
     * @throws RetrievalException if the retrieval fails
     * @return XML string containing results
     */
    public abstract String fetchSnpData(HashMap<String,String> paramMap) throws RetrievalException;
    
    
    /**
     * Creates the query string with the parameters for the snp search
     * @param paramMap
     * @return 
     */
    protected abstract String createSnpSearchQueryStr(HashMap<String,String> paramMap);

    
    /**
     * Parses the queryResult of a single gene and multiple models into a
     * DataSet structure that it adds (or modifies) in 
     * @param dataSet
     * @param params list of the parameters included in the model
     * @param searchGene =pa gene searching for with this query
     * @param radius base pair distance from each end of the search gene
     * @param dbSnpOption included to insert into dataSet
     * @param geneSourceOption included to insert into dataSEt
     * @param xmlResult
     * @return 
     */
    protected abstract DataSet parseQueryResultsIntoDataSet(DataSet dataSet, 
                                                   HashMap<String,String> params,
                                                   String searchGene,
                                                   int radius,
                                                   GeneSourceOption geneSourceOption,
                                                   DbSnpSourceOption dbSnpOption,
                                                   String xmlResult) throws RetrievalException;
    
    

    protected String join(String delimiter, List<String> list) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (String listEntry : list) {
            if (index > 0) {
                sb.append(delimiter);
            }
            sb.append(listEntry);
            index++;
        }
        return sb.toString();
    }

    /**
     * Performs parsing on the GENE_SEARCH_STUDY_SET_MODEL_NAME_COL's entry of
     * row and divides it into the study/set/model. This code has a few
     * different ifs since it seems to keep evolving as different data sets come
     * in
     *
     * @param row
     * @return
     */
    protected abstract ParsedStudySetModel parseOutStudySetModel(List<String> row);

    
    /**
     * Storgae class to store Study, set, and model for a single return 
     */
    protected class ParsedStudySetModel {
        private final String study;
        private final String model;
        private final String set;
        public ParsedStudySetModel(String study, String set, String model) {
            this.study = study;
            this.model = model;
            this.set = set;
        }

        public String getStudy() {
            return study;
        }

        public String getModel() {
            return model;
        }

        public String getSet() {
            return set;
        }
        
        public String getKey() {
            return study + "_" + set + "_" + model;
        }
    }
}
