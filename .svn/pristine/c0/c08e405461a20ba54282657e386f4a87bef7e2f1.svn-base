/*
 * This is the integrated data loader for the bioservices.  It handles multiple
 * threads for a single retrieve request of multiple genes and multiple models.
 * The routine works by dividing each gene into a single thread with all the
 * relevant models.  
 * 
 * In the background of the thread, it fetches for a single gene the 
 * SNP, Gene Annotation, and Recombination rates and parses each into a dataSet.
 * In the done part of the thread, the dataset is merged with existing sets and
 * [re]added to the dataModel.  FireDataChanged is called in the process.
 */
package com.pfizer.mrbt.genomics.bioservices;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.TransmartClient.TransmartDataLoaderWithThreads;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.GeneAnnotation;
import com.pfizer.mrbt.genomics.data.GeneRange;
import com.pfizer.mrbt.genomics.data.SnpRecombRate;
import com.pfizer.tnb.api.server.util.QueryResult;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingWorker;

/**
 *
 * @author henstockpv
 */
public class DataLoaderWithThreads {
    private List<ModelOption> modelOptions;
    private DbSnpSourceOption dbSnpOption;
    private GeneSourceOption geneSourceOption;
    private List<String> geneRequestList;
    private int basePairRadius = 0;
    
    public DataLoaderWithThreads(List<ModelOption> modelOptions,
                             DbSnpSourceOption dbSnpOption,
                             GeneSourceOption geneSourceOption,
                             List<String> geneRequestList,
                             int basePairRadius) {
        this.modelOptions = modelOptions;
        this.dbSnpOption  = dbSnpOption;
        this.geneSourceOption = geneSourceOption;
        this.geneRequestList  = geneRequestList;
        this.basePairRadius = basePairRadius;
    }
    
    public void fetchGeneData() {
        for(String geneRequest : geneRequestList) {
            final String geneRequestFormatted = TransmartDataLoaderWithThreads.formatGeneSnpRequest(geneRequest);
            Singleton.getState().retrievalStarted(geneRequestFormatted, modelOptions.size(), basePairRadius);
            final String gene = geneRequestFormatted;
            SwingWorker worker = new SwingWorker<DataSet, Void>() {
                @Override
                /**
                 * Performs bioservices fetch for snp, annotation, and recomb rate
                 * Parses the three sets of results into a dataSet that it returns
                 */
                public DataSet doInBackground() {
                    //System.out.println("Fetching gene " + gene);
                    DataSet dataSet = bioservicesFetchParse(gene);
                    //System.out.println("Finished fetching gene " + gene);
                    return dataSet;
                }
                
                @Override
                public void done() {
                    String geneName = "unknown";
                    try {
                        DataSet dataSet = get(); // fetch from parser
                        geneName = dataSet.getGeneRange().getName();
                        DataSet existingDataSet = Singleton.getDataModel().getDataSet(gene);
                        if(existingDataSet == null) {
                            // new gene
                            Singleton.getDataModel().addDataSet(gene, dataSet);
                        } else {
                            // blow away the existing data set
                            // may want to leave a message to user?  todo
                            Singleton.getDataModel().addDataSet(gene, dataSet);
                        }
                        Singleton.getState().retrievalCompleted(gene, dataSet.getSnps().size());
                    } catch(java.lang.InterruptedException ie) {
                        String why = null;
                        Throwable cause = ie.getCause();
                        if(cause != null) {
                            why = cause.getMessage();
                        } else {
                            why = ie.getMessage();
                        }
                    } catch(java.util.concurrent.ExecutionException ee) {
                        String why = null;
                        Throwable cause = ee.getCause();
                        if(cause != null) {
                            why = cause.getMessage();
                        } else {
                            why = ee.getMessage();
                        }
                    } finally {
                        //System.out.println("Finished done " + geneName);
                    }
                }
            };
            //System.out.println("PreWorker " + geneRequest);
            worker.execute();
            //System.out.println("PostWorker " + geneRequest);
        }
    }
    
    /**
     * Fetches the SNP, gene annotation, and the recombination rate in three
     * bioservices calls.
     * @param gene 
     */
    protected DataSet bioservicesFetchParse(String gene) {
        DataSet dataSet = new DataSet();
        // snps
        SNPDataFetchByGene snpDataFetchbyGene = new SNPDataFetchByGene();
        if(gene.startsWith("rs")) {
            String rsID = gene;
            SNPDataFetchBySNP bioServicesSnpDataFetchBySNP = new SNPDataFetchBySNP();
            QueryResult snpQueryResult = bioServicesSnpDataFetchBySNP.fetchSnpDataSingleSNP(modelOptions, dbSnpOption, geneSourceOption, rsID, basePairRadius);
            dataSet = bioServicesSnpDataFetchBySNP.parseQueryResultsIntoDataSet(dataSet, rsID,
                                                                                modelOptions, dbSnpOption,
                                                                                geneSourceOption, snpQueryResult, basePairRadius);
            int snpLoc = dataSet.getSnpLocation(rsID);
            int chromosome = dataSet.getChromosome();
            GeneRange geneRange = new GeneRange(rsID.toLowerCase(), chromosome, Math.max(0, snpLoc-basePairRadius), snpLoc+basePairRadius);
            dataSet.setGeneRange(geneRange);
            
        } else {
            QueryResult snpQueryResult =
                snpDataFetchbyGene.fetchSnpDataSingleGene(modelOptions, dbSnpOption, geneSourceOption, gene, basePairRadius);
            dataSet = snpDataFetchbyGene.parseQueryResultsIntoDataSet(dataSet, gene, 
                                                    modelOptions, dbSnpOption,
                                                    geneSourceOption, snpQueryResult, basePairRadius);

            GeneLocationService geneLocationService = new GeneLocationService();
            geneLocationService.computeGeneBounds(gene, geneSourceOption);
            GeneRange geneRange = dataSet.getGeneRange();
            int radius = geneRange.getRadius();
            geneRange.setStart((int) geneLocationService.getGeneStart() - radius);
            geneRange.setEnd((int) geneLocationService.getGeneStop() + radius);
            dataSet.setChromosome(geneLocationService.getChromosome());
            dataSet.setGeneRange(geneRange);
        }
        
        // gene annotations
        GeneAnnotationFetch geneAnnotationFetch = new GeneAnnotationFetch();
        int chromosome = dataSet.getChromosome();
        int start      = dataSet.getGeneRange().getStart();
        int end        = dataSet.getGeneRange().getEnd();
        QueryResult geneAnnotationQueryResult = geneAnnotationFetch.fetchDataIntoQueryResult(geneSourceOption, start, end, chromosome);
        
        
        System.out.println("GeneAnnotationQueryResult: " + geneAnnotationQueryResult.getData().size());
        ArrayList<GeneAnnotation> geneAnnotations = geneAnnotationFetch.parseQueryResultsIntoGeneAnnotations(geneAnnotationQueryResult);
        System.out.println("GeneAnnotation size " + geneAnnotations.size());
        dataSet.setGeneAnnotations(geneAnnotations);
        System.out.println("DataSet annotation size " + dataSet.getGeneAnnotations().size());

        
        // recombination rate
        RecombinationRateService recombinationRateService = new RecombinationRateService();
        QueryResult recombRateQueryResult;
        ArrayList<SnpRecombRate> recombRate;
        if(gene.startsWith("rs")) {
            recombRateQueryResult = recombinationRateService.fetchRecombinationRateBySNPQueryResult(gene,
                                               basePairRadius,geneSourceOption);
            recombRate = recombinationRateService.parseQueryResults(recombRateQueryResult, BioservicesParameters.RECOMBINATION_RATE_BY_SNP_SERVICE_ID);
        } else {
            recombinationRateService = new RecombinationRateService();
            recombRateQueryResult = recombinationRateService.fetchRecombinationRateByGeneQueryResult(gene,
                                               basePairRadius,
                                               geneSourceOption);
            // This is a kluge and will not handle SNP searches!!! todo  added 12/19/2012
            recombRate = recombinationRateService.parseQueryResults(recombRateQueryResult,
                                                                                         BioservicesParameters.RECOMBINATION_RATE_BY_GENE_SERVICE_ID);
        }            
        float maxRecombRate = recombinationRateService.getMaxRecombinationRate();
        dataSet.setRecombinationRate(recombRate, maxRecombRate);
        return dataSet;
    }
}
