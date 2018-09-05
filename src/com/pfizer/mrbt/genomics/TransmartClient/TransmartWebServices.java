/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.pfizer.mrbt.genomics.TransmartClient;

import com.pfizer.mrbt.genomics.data.DataModel;
import com.pfizer.mrbt.genomics.webservices.DbSnpSourceOption;
import com.pfizer.mrbt.genomics.webservices.GeneSourceOption;
import com.pfizer.mrbt.genomics.webservices.ModelOption;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.GeneAnnotation;
import com.pfizer.mrbt.genomics.data.SnpRecombRate;
import com.pfizer.mrbt.genomics.webservices.DataRetrievalInterface;
import com.pfizer.mrbt.genomics.webservices.Environment;
import com.pfizer.mrbt.genomics.webservices.RetrievalException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author henstockpv
 */
public class TransmartWebServices implements DataRetrievalInterface {
    private Environment env;
    
    public TransmartWebServices(Environment env) {
        this.env = env;
    }
    
    /**
     * Returns a list of ModelOption that are the study-set-model for the
     * various phenotype or disease studies included in the database.  This
     * populates the names list of the models for the GUI
     * @throws RetrievalException if service fails to return result

     * @return 
     */
    @Override
     public List<ModelOption> fetchModelOptions() throws RetrievalException {
         TransmartQueryParameterFetch transmartQueryParametersFetch = new TransmartQueryParameterFetch(env);
         List<ModelOption> modelOptions = transmartQueryParametersFetch.fetchModelOptions();
         return modelOptions;
     }
     

     /**
      * Returns a list of DbSnpSourceOptions that represent the choices of the
      * SNP Annotation Source such as HG19 02-2009.
      * @throws RetrievalException if service fails to return result
      * @return 
      */
     @Override
     public List<DbSnpSourceOption> getDbSnpSources() throws RetrievalException {
         TransmartQueryParameterFetch transmartQueryParametersFetch = new TransmartQueryParameterFetch(env);
         List<DbSnpSourceOption> dbSnpSrcOptions = transmartQueryParametersFetch.fetchtDbSnpSources();
         return dbSnpSrcOptions;
     }
     
     
     /**
      * Returns a list of available Gene Annotation Sources as the GeneSourceOption
      * list.  These represent the source of gene names and locations on the
      * genome such as the GRCh37 01-2001
      * @throws RetrievalException if service fails to return result
      * @return 
      */
     @Override
     public List<GeneSourceOption> getGeneSources() throws RetrievalException {
         TransmartQueryParameterFetch transmartQueryParametersFetch = new TransmartQueryParameterFetch(env);
         List<GeneSourceOption> geneSourceOptions = transmartQueryParametersFetch.getGeneSources();
         return geneSourceOptions;
     }
     
          /**
      * Returns a DataSet containing the SNPs within basePairRadius of the snpName
      * along with other information including chromosome, geneRange, and a
      * mapping of snp position + model --> pvalue for all the found SNPs in
      * range
      * @param snpName
      * @param modelOptions
      * @param dbSnpOption
      * @param geneSourceOption
      * @param basePairRadius
      * @throws RetrievalException if service fails to return result
      * @return 
      */
    @Override
    public DataSet snpSearchforSnpData(String snpName,
                                       List<ModelOption> modelOptions,
                                       DbSnpSourceOption dbSnpOption,
                                       GeneSourceOption geneSourceOption,
                                       int basePairRadius)
            throws RetrievalException {
        TransmartSNPDataFetch transmartSnpDataFetch = new TransmartSNPDataFetchSnpQuery(env);
        HashMap<String, String> paramMap = transmartSnpDataFetch.generateParameterMap(snpName, modelOptions, dbSnpOption, geneSourceOption, basePairRadius);
        String xmlQueryResult = transmartSnpDataFetch.fetchSnpData(paramMap);
        DataSet dataSet = new DataSet();
        dataSet = transmartSnpDataFetch.parseQueryResultsIntoDataSet(dataSet,
                                                                        paramMap,
                                                                        snpName,
                                                                        basePairRadius,
                                                                        geneSourceOption,
                                                                        dbSnpOption,
                                                                        xmlQueryResult);
        return dataSet;
    }

    /**
     * Returns a DataSet containing the SNPs within basePairRadius of the
     * snpName along with other information including chromosome, geneRange, and
     * a mapping of snp position + model --> pvalue for all the found SNPs in
     * range
     *
     * @param geneName
     * @param modelOptions
     * @param dbSnpOption
     * @param geneSourceOption
     * @param basePairRadius
      * @throws RetrievalException if service fails to return result
     * @return
     */
    @Override
    public DataSet geneSearchForSnpData(String geneName, List<ModelOption> modelOptions,
                                        DbSnpSourceOption dbSnpOption,
                                        GeneSourceOption geneSourceOption,
                                        int basePairRadius) throws RetrievalException {
        TransmartSNPDataFetch transmartSnpDataFetch = new TransmartSNPDataFetchGeneQuery(env);
        HashMap<String,String> paramMap = transmartSnpDataFetch.generateParameterMap(geneName, modelOptions, dbSnpOption, geneSourceOption, basePairRadius);
        String xmlQueryResult = transmartSnpDataFetch.fetchSnpData(paramMap);
        DataSet dataSet = new DataSet();
        dataSet = transmartSnpDataFetch.parseQueryResultsIntoDataSet(dataSet,
                                                                     paramMap,
                                                                     geneName,
                                                                     basePairRadius,
                                                                     geneSourceOption,
                                                                     dbSnpOption,
                                                                     xmlQueryResult);
        return dataSet;
    }

     /**
      * Returns a list of GeneAnnotations that occur on the given chromosome between start and end using the
      * provided geneSourceOption
      * @param geneSourceOption
      * @param chromosome
      * @param start
      * @param end
      * @throws RetrievalException if service fails to return result
      * @return 
      */
    @Override
    public ArrayList<GeneAnnotation> fetchGeneAnnotations(GeneSourceOption geneSourceOption, int chromosome, int start, int end) throws RetrievalException {
            TransmartGeneAnnotationFetch geneAnnotationFetch = new TransmartGeneAnnotationFetch(env);
        HashMap<String, String> paramMap = new HashMap<String, String>();
        //paramMap.put("GENESRCID", geneSourceOption.getId() + "");
        paramMap.put("start", start + "");
        paramMap.put("stop", end + "");
        if (chromosome == DataModel.X) {
            paramMap.put("chromosome", "X");
        } else if (chromosome == DataModel.Y) {
            paramMap.put("chromosome", "Y");
        } else {
            paramMap.put("chromosome", chromosome + "");
        }
        String geneAnnotationXmlResult = geneAnnotationFetch.fetchDataIntoQueryResult(geneSourceOption, paramMap);
        ArrayList<GeneAnnotation> geneAnnotations = geneAnnotationFetch.parseQueryResultsIntoGeneAnnotations(geneAnnotationXmlResult, paramMap);
        return geneAnnotations;
    }

    
     /**
      * Retrieves a list of the SnpRecombRate position-rate values for the geneOrSnp within
      * a given basePairRadius of its end, using the GeneSourceOption.  The list is assumed
      * to be in increasing order of position
      * @param geneOrSnpName
      * @param basePairRadius
      * @param geneSourceOption
      * @param dbSnpSourceOption
      * @throws RetrievalException if service fails to return result
      * @return 
      */

    @Override
    public ArrayList<SnpRecombRate> fetchRecombinationRates(String geneOrSnpName, int basePairRadius, GeneSourceOption geneSourceOption, DbSnpSourceOption dbSnpSourceOption) throws RetrievalException {
            TransmartRecombinationRateService transmartRecombinationRateService = new TransmartRecombinationRateService(env);
            ArrayList<SnpRecombRate> recombRateData;
            if(geneOrSnpName.startsWith("rs")) {
                // todo need to change to SNP service
                recombRateData = transmartRecombinationRateService.fetchRecombinationRateBySnpQueryResult(geneOrSnpName,
                                               basePairRadius, geneSourceOption, dbSnpSourceOption);
            } else {
                recombRateData = transmartRecombinationRateService.fetchRecombinationRateByGeneQueryResult(geneOrSnpName,
                                               basePairRadius, geneSourceOption, dbSnpSourceOption);
            }
            //maxRecombRate = transmartRecombinationRateservice.getMaxRecombinationRate();
            return recombRateData;
    }
            
    
    @Override
    public void setEnvironment(Environment env) {
        this.env = env;
    }
    
    @Override
    public Environment getEnvironment() {
        return env;
    }
     
    /**
     * Returns the source of the data + the environment
     * @return 
     */
    @Override
    public String getSourceName() {
        return "TranSMART " + env.name();
    }
}
