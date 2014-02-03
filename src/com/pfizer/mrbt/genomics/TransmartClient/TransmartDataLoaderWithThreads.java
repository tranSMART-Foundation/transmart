/*
 * This is the integrated data loader for the Transmart webservices.  It handles multiple
 * threads for a single retrieve request of multiple genes and multiple models.
 * The routine works by dividing each gene into a single thread with all the
 * relevant models.  
 * 
 * In the background of the thread, it fetches for a single gene the 
 * SNP, Gene Annotation, and Recombination rates and parses each into a dataSet.
 * In the done part of the thread, the dataset is merged with existing sets and
 * [re]added to the dataModel.  FireDataChanged is called in the process.
 */
package com.pfizer.mrbt.genomics.TransmartClient;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.bioservices.*;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.GeneAnnotation;
import com.pfizer.mrbt.genomics.data.GeneRange;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.data.SnpRecombRate;
import com.pfizer.tnb.api.server.util.QueryResult;

/**
 *
 * @author henstockpv
 */
public class TransmartDataLoaderWithThreads {
    private List<ModelOption> modelOptions;
    private DbSnpSourceOption dbSnpOption;
    private GeneSourceOption geneSourceOption;
    private List<String> geneRequestList;
    private int basePairRadius = 0;
    private GeneSourceOption topBioservicesGeneSourceOption; //kluge to get recombination rate
    
    public TransmartDataLoaderWithThreads(List<ModelOption> modelOptions,
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
    
    /**
     * Main routine to put in a threaded request for SNP for each gene
     */
    public void fetchGeneData() {
        for(String geneRequest : geneRequestList) {
            final String geneRequestFormatted = formatGeneSnpRequest(geneRequest);
            Singleton.getState().retrievalStarted(geneRequestFormatted, modelOptions.size(), basePairRadius);
            SwingWorker worker = new SwingWorker<DataSet, Void>() {
                @Override
                /**
                 * Performs bioservices fetch for snp, annotation, and recomb rate
                 * Parses the three sets of results into a dataSet that it returns
                 */
                public DataSet doInBackground() {
                    DataSet dataSet = transmartFetchParse(geneRequestFormatted);
                    return dataSet;
                }
                
                @Override
                public void done() {
                    String geneName = "unknown";
                    try {
                        DataSet dataSet = get(); // fetch result from swingworker process
                        geneName = dataSet.getGeneRange().getName();
                        DataSet existingDataSet = Singleton.getDataModel().getDataSet(geneName);
                        if(existingDataSet == null) {  // new gene
                            Singleton.getDataModel().addDataSet(geneName, dataSet);
                            
                        }  else if(sameRadiusDbSnpGeneSource(dataSet, existingDataSet)) {
                            /* allows new models for same gene to be added if same radius/dbSnp/GeneSource */
                            removeDataSetModelSnpFromExistingDataSet(dataSet, existingDataSet);
                            for (Model addingModel : dataSet.getModels()) {
                                Model existingModel = existingDataSet.getModelElseNull(addingModel.getStudy(),
                                                                                       addingModel.getEndpoint(),
                                                                                       addingModel.getType());
                                if(existingModel != null) {
                                    existingDataSet.removeAllSnpWithModel(existingModel);
                                } 
                            }
                            existingDataSet.addAllSnpWithModels(dataSet);
                            Singleton.getDataModel().addDataSet(geneName, existingDataSet);
                            
                        } else {
                            // blow away the existing data set w/o informing user as replacing it
                            Singleton.getDataModel().addDataSet(geneName, dataSet);
                        }
                        Singleton.getState().retrievalCompleted(geneName, dataSet.getSnps().size());
                    } catch(java.lang.InterruptedException ie) {
                        String why = grabException(ie);
                    } catch(java.util.concurrent.ExecutionException ee) {
                        String why = grabException(ee);
                    }
                }
            };
            worker.execute();
        }
    }

    /**
     * Returns true if the new data set is an addition of models to existingDataSet
     * identified by same radius, DbSnp and GeneSource
     * @param dataSet
     * @param existingDataSet
     * @return 
     */
    private boolean sameRadiusDbSnpGeneSource(DataSet dataSet, DataSet existingDataSet) {
        if( existingDataSet.getGeneRange().getRadius()==dataSet.getGeneRange().getRadius() &&
            existingDataSet.getDbSnpOption().getId() == dataSet.getDbSnpOption().getId() &&
            existingDataSet.getGeneSourceOption().getId() == dataSet.getGeneSourceOption().getId()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Searches for all models of dataSet that are in existingDataSet Removes
     * all the SNP in existingDataSet for these models
     *
     * @param dataSet
     * @param existingDataSet
     */
    private void removeDataSetModelSnpFromExistingDataSet(DataSet dataSet, DataSet existingDataSet) {
        for (Model addingModel : dataSet.getModels()) {
            if (existingDataSet.modelExists(addingModel.getStudy(), 
                                            addingModel.getEndpoint(), 
                                            addingModel.getType())) {
                Model existingModel = existingDataSet.checkAddModel(addingModel.getStudy(), 
                                                                    addingModel.getEndpoint(), 
                                                                    addingModel.getType());
                existingDataSet.removeAllSnpWithModel(existingModel);
            }
        }
    }
    
    /**
     * Returns the cause or message from the exception as a string
     *
     * @param ex
     * @return
     */
    private String grabException(Exception ex) {
        String why;
        Throwable cause = ex.getCause();
        if (cause != null) {
            why = cause.getMessage();
        } else {
            why = ex.getMessage();
        }
        return why;

    }
    
    /**
     * Reformats the inputGeneSnp as rs#### if snp and upper case if it's a gene
     * @param inputGeneSnp
     * @return 
     */
    public static String formatGeneSnpRequest(String inputGeneSnp) {
        if (inputGeneSnp.matches("^[0-9]+$") || 
            inputGeneSnp.matches("^[rR][sS][0-9]+$")) { // search for SNP
            String rsID = inputGeneSnp.toLowerCase();
            if (!rsID.startsWith("rs")) {
                rsID = "rs" + rsID;
            }
            return rsID;
        } else {
            String gene = inputGeneSnp.trim();
            return gene;
        }
    }
    
    /**
     * Fetches the SNP, gene annotation, and the recombination rate in three
     * bioservices calls.
     * @param gene 
     */
    protected DataSet transmartFetchParse(String gene) {
        // snps
        DataSet dataSet;
        if(gene.startsWith("rs")) {
            dataSet = bioservicesSNPSearch(gene);
        } else { // gene search
            dataSet = transmartGeneSearch(gene);
        }
        
        // gene annotations
        TransmartGeneAnnotationFetch geneAnnotationFetch = new TransmartGeneAnnotationFetch();
        int chromosome = dataSet.getChromosome();
        int start      = dataSet.getGeneRange().getStart();
        int end        = dataSet.getGeneRange().getEnd();
        String geneAnnotationXmlResult = geneAnnotationFetch.fetchDataIntoQueryResult(geneSourceOption, start, end, chromosome);
        
        
        //System.out.println("GeneAnnotationQueryResult: " + geneAnnotationXmlResult.size());
        ArrayList<GeneAnnotation> geneAnnotations = geneAnnotationFetch.parseQueryResultsIntoGeneAnnotations(geneAnnotationXmlResult);
        dataSet.setGeneAnnotations(geneAnnotations);
        
        // recombination rate
        RecombinationRateService recombinationRateService = new RecombinationRateService();
        QueryResult recombRateQueryResult;
        ArrayList<SnpRecombRate> recombRate;
        if(gene.startsWith("rs")) {
            recombRateQueryResult = recombinationRateService.fetchRecombinationRateBySNPQueryResult(gene,
                                               basePairRadius,
                                               getTopBioservicesGeneSourceOption());
            recombRate = recombinationRateService.parseQueryResults(recombRateQueryResult, BioservicesParameters.RECOMBINATION_RATE_BY_SNP_SERVICE_ID);
        } else {
            recombRateQueryResult = recombinationRateService.fetchRecombinationRateByGeneQueryResult(gene,
                                               basePairRadius,
                                               getTopBioservicesGeneSourceOption());
            recombRate = recombinationRateService.parseQueryResults(recombRateQueryResult, BioservicesParameters.RECOMBINATION_RATE_BY_GENE_SERVICE_ID);
        }
        float maxRecombRate = recombinationRateService.getMaxRecombinationRate();
        dataSet.setRecombinationRate(recombRate, maxRecombRate);
        
        return dataSet;
    }

    /**
     * Calls BioServices request to perform a SNP search
     * @param rsID
     * @return dataSet containing the rsID results
     */
    private DataSet bioservicesSNPSearch(String rsID) {
        DataSet dataSet = new DataSet();
        SNPDataFetchBySNP bioServicesSnpDataFetchBySNP = new SNPDataFetchBySNP();
        QueryResult snpQueryResult = bioServicesSnpDataFetchBySNP.fetchSnpDataSingleSNP(modelOptions, dbSnpOption, geneSourceOption, rsID, basePairRadius);

        dataSet = bioServicesSnpDataFetchBySNP.parseQueryResultsIntoDataSet(dataSet, rsID,
                                                                            modelOptions, dbSnpOption,
                                                                            geneSourceOption, snpQueryResult, basePairRadius);
        int snpLoc = dataSet.getSnpLocation(rsID);
        int chromosome = dataSet.getChromosome();
        GeneRange geneRange = new GeneRange(rsID.toLowerCase(), chromosome, Math.max(0, snpLoc - basePairRadius), snpLoc + basePairRadius);
        dataSet.setGeneRange(geneRange);
        return dataSet;
    }

    /**
     * Search for the gene in TransMart
     * @param gene
     * @return 
     */
    private DataSet transmartGeneSearch(String gene) {
        TransmartSNPDataFetch transmartSnpDataFetch = new TransmartSNPDataFetch();
        String xmlQueryResult =
               transmartSnpDataFetch.fetchSnpDataSingleGene(modelOptions, dbSnpOption, geneSourceOption, gene, basePairRadius);
        DataSet dataSet = new DataSet();
        dataSet = transmartSnpDataFetch.parseQueryResultsIntoDataSet(dataSet, gene,
                                                                     modelOptions, dbSnpOption,
                                                                     geneSourceOption, xmlQueryResult, basePairRadius);

        TransmartGeneLocationService geneLocationService = new TransmartGeneLocationService();
        geneLocationService.computeGeneBounds(gene, geneSourceOption);
        dataSet.setChromosome(geneLocationService.getChromosome());

        
        GeneRange geneRange = computeGeneRange(geneLocationService.getGeneStart(),
                                               geneLocationService.getGeneStop(),
                                                dataSet);
        dataSet.setGeneRange(geneRange);
        return dataSet;
    }

    /**
     * Computes geneRange object from the start/stop and the radius obtained
     * from the dataSet (search)
     * @param start
     * @param stop
     * @param dataSet
     * @return 
     */
    private GeneRange computeGeneRange(int start, int stop, DataSet dataSet) {
        GeneRange geneRange = dataSet.getGeneRange();
        int radius = geneRange.getRadius();
        geneRange.setStart(start - radius);
        geneRange.setEnd(stop + radius);
        return geneRange;
    }
    
    /**
     * This is the Transmart service and it needs to have the Bioservices 
     * gene source option in order to get the Recombination rates.  So we get it
     * here which is a fetch of the service and returns the first one.
     * @return 
     */
    protected GeneSourceOption getTopBioservicesGeneSourceOption() {
        if(topBioservicesGeneSourceOption == null) {
            QueryParameterFetch qpf = new QueryParameterFetch();
            List<GeneSourceOption> geneSourceOptions = qpf.getGeneSources();
            topBioservicesGeneSourceOption = geneSourceOptions.get(0);
        }
        return topBioservicesGeneSourceOption;
    }
}
