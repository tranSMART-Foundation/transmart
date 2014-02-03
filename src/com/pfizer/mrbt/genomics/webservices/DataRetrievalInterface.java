/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.webservices;

import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.GeneAnnotation;
import com.pfizer.mrbt.genomics.data.SnpRecombRate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author henstockpv
 */
public interface DataRetrievalInterface {

    /**
     * Sets the environment level for the web services
     *
     * @param environment
     */
    public void setEnvironment(Environment environment);

    /**
     * Returns the enum Environment
     *
     * @return
     */
    public Environment getEnvironment();

    /**
     * Returns a list of ModelOption that are the study-set-model for the
     * various phenotype or disease studies included in the database. This
     * populates the names list of the models for the GUI
     *
     * @throws RetrievalException
     * @return
     */
    public List<ModelOption> fetchModelOptions() throws RetrievalException;

    /**
     * Returns a list of DbSnpSourceOptions that represent the choices of the
     * SNP Annotation Source such as HG19 02-2009.
     *
     * @throws RetrievalException
     * @return
     */
    public List<DbSnpSourceOption> getDbSnpSources() throws RetrievalException;

    /**
     * Returns a list of available Gene Annotation Sources as the
     * GeneSourceOption list. These represent the source of gene names and
     * locations on the genome such as the GRCh37 01-2001
     *
     * @throws RetrievalException
     * @return
     */
    public List<GeneSourceOption> getGeneSources() throws RetrievalException;

    /**
     * Returns a DataSet containing the SNPs within basePairRadius of the
     * snpName along with other information including chromosome, geneRange, and
     * a mapping of snp position + model --> pvalue for all the found SNPs in
     * range
     *
     * @param snpName
     * @param modelOptions
     * @param dbSnpOption
     * @param geneSourceOption
     * @param basePairRadius
     * @throws RetrievalException
     * @return
     */
    public DataSet snpSearchforSnpData(String snpName, 
                                       List<ModelOption> modelOptions, 
                                       DbSnpSourceOption dbSnpOption, 
                                       GeneSourceOption geneSourceOption, 
                                       int basePairRadius) throws RetrievalException;

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
     * @throws RetrievalException
     * @return
     */
    public DataSet geneSearchForSnpData(String geneName,
                                        List<ModelOption> modelOptions,
                                        DbSnpSourceOption dbSnpOption,
                                        GeneSourceOption geneSourceOption,
                                        int basePairRadius) throws RetrievalException;

    /**
     * Returns a list of GeneAnnotations that occur on the given chromosome
     * between start and end using the provided geneSourceOption
     *
     * @param geneSourceOption
     * @param chromosome
     * @param start
     * @param end
     * @throws RetrievalException
     * @return
     */
    public ArrayList<GeneAnnotation> fetchGeneAnnotations(GeneSourceOption geneSourceOption, 
                                                          int chromosome, 
                                                          int start, 
                                                          int end) 
            throws RetrievalException;

    /**
     *
     */
     //public void computeGeneBounds(String geneName, GeneSourceOption geneSourceOption);
    /**
     * Retrieves a list of the SnpRecombRate position-rate values for the
     * geneOrSnp within a given basePairRadius of its end, using the
     * GeneSourceOption. The list is assumed to be in increasing order of
     * position
     *
     * @param geneOrSnpName
     * @param basePairRadius
     * @param geneSourceOption
     * @param dbSnpSourceOption
     * @throws RetrievalException
     * @return
     */
    public ArrayList<SnpRecombRate> fetchRecombinationRates(String geneOrSnpName, 
                                                            int basePairRadius, 
                                                            GeneSourceOption geneSourceOption,
                                                            DbSnpSourceOption dbSnpSourceOption) 
            throws RetrievalException;

    /**
     * Returns the name of the data retrieval source for display purposes
     *
     * @return
     */
    public String getSourceName();
}
