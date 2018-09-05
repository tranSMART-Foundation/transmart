/**
 * This is the integrated data loader for the DataRetrieval.  It handles multiple
 * threads for a single retrieve request of multiple genes and multiple models.
 * The routine works by dividing each gene into a single thread with all the
 * relevant models.  
 * 
 * In the background of the thread, it fetches for a single gene the 
 * SNP, Gene Annotation, and Recombination rates and parses each into a dataSet.
 * In the done part of the thread, the dataset is merged with existing sets and
 * [re]added to the dataModel.  FireDataChanged is called in the process.
 */
package com.pfizer.mrbt.genomics.data;

import java.util.ArrayList;
import java.util.List;


import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.state.SearchStatus;
import com.pfizer.mrbt.genomics.webservices.DataRetrievalInterface;
import com.pfizer.mrbt.genomics.webservices.DbSnpSourceOption;
import com.pfizer.mrbt.genomics.webservices.GeneSourceOption;
import com.pfizer.mrbt.genomics.webservices.ModelOption;
import com.pfizer.mrbt.genomics.webservices.RetrievalException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author henstockpv
 */
public class DataRetrievalWithThreadPool {
    private final DataRetrievalInterface dataRetrievalInterface;
    public static final int DEFAULT_MAX_POOL_SIZE = 10; // thread pool maximum threads
    private int maxThreadPoolSize = DEFAULT_MAX_POOL_SIZE;
    public final static int DEFAULT_MAX_NUM_MODELS = 1;
    private int maxNumModels = DEFAULT_MAX_NUM_MODELS;
    
    private final ExecutorService threadPool = Executors.newFixedThreadPool(maxThreadPoolSize);
    private static int queryNumber = 0;
    

    /**
     * Initializes the DataRetievalInterface and thread pools
     * @param dataRetrievalInterface 
     */
    public DataRetrievalWithThreadPool(DataRetrievalInterface dataRetrievalInterface) {
        this.dataRetrievalInterface = dataRetrievalInterface;
    }

    
    /**
     * Primary method for performing the queries to obtain the snps, recombination
     * rate, and the annotations for a given set of modelOptions, geneList, and
     * basePairRadius under the Dbsnpsource and Genesource options
     * @param modelOptions
     * @param dbSnpOption
     * @param geneSourceOption
     * @param geneRequestList
     * @param basePairRadius 
     */
    public void retrieveData(final List<ModelOption> modelOptions,
                                       final DbSnpSourceOption dbSnpOption,
                                       final GeneSourceOption geneSourceOption,
                                       final List<String> geneRequestList,
                                       final int basePairRadius) {
        

        int numModelOptions = modelOptions.size();
        int startQueryIndex = queryNumber;
        
        // update the history before anything starts
        for(final String geneRequest : geneRequestList) {
            final String geneRequestFormatted = formatGeneSnpRequest(geneRequest);
            for(int modelOptionIndex = 0; modelOptionIndex < numModelOptions; modelOptionIndex += maxNumModels) {
                //System.out.println("Submitting retrieval task for " + geneRequestFormatted + " " + queryNumber);
                List<ModelOption> modelOptionsSubset = subsetModelOptions(modelOptions, 
                                                                          modelOptionIndex, 
                                                                          Math.min(modelOptionIndex + maxNumModels, numModelOptions));
                Singleton.getState().retrievalInitialized(geneRequestFormatted, modelOptionsSubset, basePairRadius, queryNumber++);
            }
        }
        // put all the requests in the pool using startQueryIndex as the starting point
        for(final String geneRequest : geneRequestList) {
            final String geneRequestFormatted = formatGeneSnpRequest(geneRequest);
            for(int modelOptionIndex = 0; modelOptionIndex < numModelOptions; modelOptionIndex += maxNumModels) {
                //System.out.println("Submitting retrieval task for " + geneRequestFormatted + " " + startQueryIndex);
                List<ModelOption> modelOptionsSubset = subsetModelOptions(modelOptions, 
                                                                          modelOptionIndex, 
                                                                          Math.min(modelOptionIndex + maxNumModels, numModelOptions));
                Runnable dataRetrievalWorker = new DataRetrievalWorker(modelOptionsSubset,
                        geneSourceOption,
                        dbSnpOption,
                        geneRequestFormatted,
                        basePairRadius,
                        startQueryIndex++);
                threadPool.execute(dataRetrievalWorker);
            }
        }
    }
    
    /**
     * Returns a subset of the modelOptions from [startIndex, endIndex>
     */
    private List<ModelOption> subsetModelOptions(List<ModelOption> fullList, int startIndex, int endIndex) {
        return fullList.subList(startIndex, endIndex);
    } 
    
    public final class DataRetrievalWorker implements Runnable {
        private final List<ModelOption> modelOptions;
        private final GeneSourceOption geneSourceOption;
        private final DbSnpSourceOption dbSnpSourceOption;
        private final String geneRequestFormatted;
        private int basePairRadius = 0;
        private final int queryNumber;

        public DataRetrievalWorker(final List<ModelOption> modelOptions,
                                 final GeneSourceOption geneSourceOption,
                                 final DbSnpSourceOption dbSnpSourceOption,
                                 final String geneRequestFormatted,
                                 final int basePairRadius, 
                                 final int queryNumber) {
            this.modelOptions = modelOptions;
            this.geneSourceOption = geneSourceOption;
            this.dbSnpSourceOption = dbSnpSourceOption;
            this.geneRequestFormatted = geneRequestFormatted;
            this.basePairRadius = basePairRadius;
            this.queryNumber = queryNumber;
        }
        @Override
        public void run() {
            DataSet dataSet = null;
            try {
                System.out.println("Retrieval started for gene " + geneRequestFormatted + " queryNumber= " + queryNumber);
                Singleton.getState().retrievalStarted(geneRequestFormatted, modelOptions.size(), basePairRadius, queryNumber);
                dataSet = retrieveSnps(geneRequestFormatted,
                                       modelOptions,
                                       dbSnpSourceOption,
                                       geneSourceOption,
                                       basePairRadius);
                DataSet existingDataSet = Singleton.getDataModel().getDataSet(geneRequestFormatted);
                System.out.println("Model " + modelOptions.get(0).toString());
                if (existingDataSet == null || ! existingDataSet.hasSameRadiusDbSnpGenesource(basePairRadius, dbSnpSourceOption, geneSourceOption)) {
                    assert (dataSet.getChromosome() != 0) : "First data set chromosome 0 " + modelOptions.get(0).toString();
                    ArrayList<GeneAnnotation> geneAnnotations = retrieveAnnotation(dataSet, geneSourceOption);
                    dataSet.setGeneAnnotations(geneAnnotations);

                    ArrayList<SnpRecombRate> recombRate = retrieveRecombinationRates(geneRequestFormatted, basePairRadius, geneSourceOption, dbSnpSourceOption);
                    float maxRecombRate = computeMaxRecombinationRate(recombRate);
                    dataSet.setRecombinationRate(recombRate, maxRecombRate);
                } else if(exceedsPreviousAnnotationBounds(dataSet, existingDataSet)) {
                }
                
                postProcessDataSet(dataSet, queryNumber, geneRequestFormatted);
                
            } catch (RetrievalException rex) {
                System.err.println("Retrieval exception\n" + rex.toString());
                //rex.printStackTrace();
                Singleton.getState().retrievalCompleted(geneRequestFormatted, 0, SearchStatus.FAILED, queryNumber);
            }
        }
    }
    
    /**
     * Returns true if the dataSet xrange is wider than the existingDataSet xrange
     * @param dataSet
     * @param existingDataSet
     * @return 
     */
    private boolean exceedsPreviousAnnotationBounds(DataSet dataSet, DataSet existingDataSet) {
        if(dataSet.getXRange().getMin() < existingDataSet.getXRange().getMin() ||
           dataSet.getXRange().getMax() > existingDataSet.getXRange().getMax()) {
            return true;
        } else {
            return false;
        }
    }
    
        /**
     * Moves the retrieved data set into the required data structures of the
     * data model and fires State.retrievalCompleted events.  The dataSet is
     * modified by updating by adding/removing information
     * @param dataSet 
     * @param queryId
     * @param geneName
     */
    protected void postProcessDataSet(DataSet dataSet, int queryId, String geneName) throws RetrievalException {
        if(dataSet==null) {
            Singleton.getState().retrievalCompleted(geneName, 0, SearchStatus.FAILED, queryId);
            return;
        }
        
        //Singleton.getDataModel().updateDataSets(dataSet, geneName);
        //String geneName = dataSet.getGeneRange().getName();
        System.out.println("\nPostprocessing new data set " + geneName + "\t" + dataSet.getSnps().size());
        DataSet existingDataSet = Singleton.getDataModel().getDataSet(geneName);
        /*if(existingDataSet != null) {
            System.out.println("Postprocessing existing data set " + geneName + "\t" + existingDataSet.getSnps().size());
            //existingDataSet.updateXAxisRange(dataSet.getXRange());
        }*/
        if (existingDataSet == null) {  // new gene
            Singleton.getDataModel().addDataSet(geneName, dataSet);
            System.out.println("Postprocessing adding " + geneName + "\t" + Singleton.getDataModel().getDataSet(geneName).getSnps().size());

        } else if (existingDataSet.hasSameRadiusDbSnpGeneSource(dataSet)) {
            existingDataSet.addDataSetModels(dataSet.getModels());
            existingDataSet.updateSnps(dataSet.getSnps());
            Singleton.getDataModel().updateDataSet(geneName, existingDataSet, dataSet);
            System.out.println("Existing data range [" + existingDataSet.getXRange().getMin() + "\t" + existingDataSet.getXRange().getMax() + "]");
            System.out.println("Incoming data range [" + dataSet.getXRange().getMin() + "\t" + dataSet.getXRange().getMax() + "]");
            boolean updatedXRange = existingDataSet.updateXAxisRange(dataSet.getXRange());
            System.out.println("Existing data range [" + existingDataSet.getXRange().getMin() + "\t" + existingDataSet.getXRange().getMax() + "]");
            //System.err.println("Updated XRange for " + dataSet.getModels().get(0).toString() + "\t" + updatedXRange);
            if(existingDataSet.xRangeExceedsGeneAnnotationRange()) {
                assert (existingDataSet.getChromosome()!= 0) : " xRange changed chromosome 0";
                ArrayList<GeneAnnotation> geneAnnotations = retrieveAnnotation(existingDataSet, existingDataSet.getGeneSourceOption());
                System.out.println("Fetched gene annotations");
                existingDataSet.setGeneAnnotations(geneAnnotations);
                System.out.println("Assigned gene annotations to existing");
                //existingDataSet.setGeneRange(new GeneRange());
            }
            System.out.println("Postprocessing merging " + geneName + "\t" + existingDataSet.getSnps().size());

        } else {// blow away existing data set w/o informing user as replacing it
            Singleton.getDataModel().addDataSet(geneName, dataSet);
            System.out.println("Postprocessing blowAway  " + geneName + "\t" + existingDataSet.getSnps().size());
        }
        Singleton.getState().retrievalCompleted(geneName, dataSet.getSnps().size(), SearchStatus.SUCCESS, queryId);
    }
    
    
    /**
     * Retrieves a dataSet containing the SNP for requestedGeneOrSnp
     * @param requestedGeneOrSnp
     * @param modelOptions
     * @param dbSnpOption
     * @param geneSourceOption
     * @param basePairRadius
     * @throws RetrievalException is dataRetrieval method throws exception
     * @return 
     */
    protected DataSet retrieveSnps(String requestedGeneOrSnp,
                                   List<ModelOption> modelOptions,
                                   DbSnpSourceOption dbSnpOption,
                                   GeneSourceOption geneSourceOption,
                                   int basePairRadius) throws RetrievalException {
        DataSet dataSet;
        if(requestedGeneOrSnp.startsWith("rs")) { // SNP search
            dataSet = dataRetrievalInterface.snpSearchforSnpData(requestedGeneOrSnp, 
                                                                 modelOptions, 
                                                                 dbSnpOption, 
                                                                 geneSourceOption, 
                                                                 basePairRadius);
        } else { // gene search
            dataSet = dataRetrievalInterface.geneSearchForSnpData(requestedGeneOrSnp, 
                                                                  modelOptions, 
                                                                  dbSnpOption, 
                                                                  geneSourceOption, 
                                                                  basePairRadius);
        }
        return dataSet;
    }
    

    /**
     * Returns a list of GeneAnnotation for the range of data retrieved in the dataSet
     * It also updates the dataSet.geneRange if the retrieval was successful.  The
     * search is the xRange for the dataSet plus a 10% window to avoid very similar
     * genes requiring separate searches
     * @param dataSet
     * @param geneSourceOption
     * @throws RetrievalException if gene annotation fetch fails
     * @return 
     */
    protected ArrayList<GeneAnnotation> retrieveAnnotation(DataSet dataSet, GeneSourceOption geneSourceOption) throws RetrievalException {
        int chromosome = dataSet.getChromosome();
        assert (chromosome != 0) : "Chromosome = 0 from retrieveAnnotation";
        /*int start      = dataSet.getGeneRange().getStart();
        int end        = dataSet.getGeneRange().getEnd();*/
        double start = dataSet.getXRange().getMin();
        double end   = dataSet.getXRange().getMax();
        double range = Math.min(1000, end-start);
        int minX = (int) Math.floor(start - range/10);
        int maxX = (int) Math.ceil(end + range/10);
        ArrayList<GeneAnnotation> geneAnnotations = dataRetrievalInterface.fetchGeneAnnotations(geneSourceOption, chromosome, minX, maxX);
        dataSet.getGeneRange().setStart(minX);
        dataSet.getGeneRange().setEnd(maxX);
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
      * @throws RetrievalException if data retrieval operation fails
      * @return 
      */
    protected ArrayList<SnpRecombRate> retrieveRecombinationRates(String geneOrSnpName, int basePairRadius, GeneSourceOption geneSourceOption, DbSnpSourceOption dbSnpSourceOption) throws RetrievalException {
        ArrayList<SnpRecombRate> recombRate = dataRetrievalInterface.fetchRecombinationRates(geneOrSnpName, basePairRadius, geneSourceOption, dbSnpSourceOption);
        return recombRate;
    }
    
    /**
     * Returns the maximum recombination rate in recombRates 
     * @param recombRates
     * @return 
     */
    private float computeMaxRecombinationRate(ArrayList<SnpRecombRate> recombRates) {
        float maxRate = Float.NEGATIVE_INFINITY;
        for(SnpRecombRate recombRate : recombRates) {
            if(recombRate.getRecombRate() > maxRate) {
                maxRate = recombRate.getRecombRate();
            }
        }
        return maxRate;
    }


    /**
     * Returns the cause or message from the exception as a string
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
    protected String formatGeneSnpRequest(String inputGeneSnp) {
        inputGeneSnp = inputGeneSnp.trim();
        if (inputGeneSnp.matches("^[0-9]+$")) {
            return "rs" + inputGeneSnp.trim();
            
        } else if(inputGeneSnp.matches("^[rR][sS][0-9]+$")) { // search for SNP
            String rsID = inputGeneSnp.toLowerCase().trim();
            return rsID;
            
        /*} else if(inputGeneSnp.matches("\".*\"")) {
            String gene = inputGeneSnp.trim();
            return gene;*/
        } else {
            String gene = inputGeneSnp.trim();
            return gene;
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
        return (existingDataSet.getGeneRange().getRadius()    == dataSet.getGeneRange().getRadius() &&
                existingDataSet.getDbSnpOption().getId()      == dataSet.getDbSnpOption().getId() &&
                existingDataSet.getGeneSourceOption().getId() == dataSet.getGeneSourceOption().getId());
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
                                            addingModel.getSet(), 
                                            addingModel.getModel())) {
                Model existingModel = existingDataSet.checkAddModel(addingModel.getStudy(), 
                                                                    addingModel.getSet(), 
                                                                    addingModel.getModel());
                existingDataSet.removeAllSnpWithModel(existingModel);
            }
        }
    }

    /**
     * Halts the thread pool killing all exiting running operations and removing
     * all future ones
     */
    public void shutdownThreadPool() {
        this.threadPool.shutdownNow();
    }
    
    /**
     * Changes the threading conditions for the maximum poolSize
     * @param threadPoolSize 
     */
    public void setMaxThreadPoolSize(int threadPoolSize) {
        this.maxThreadPoolSize = threadPoolSize;
    }
    
    /**
     * Returns the maximum pool size for the thread pool
     * @return 
     */
    public int getMaxThreadPoolSize() {
        return this.maxThreadPoolSize;
    }
    
    /**
     * Returns number of models for a given gene that can be run in a single
     * thread
     * @param maxNumModels 
     */
    public void setMaxNumModels(int maxNumModels) {
        this.maxNumModels = maxNumModels;
    }

}
