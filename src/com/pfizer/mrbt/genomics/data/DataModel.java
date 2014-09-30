/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.data;

import com.pfizer.mrbt.genomics.webservices.DbSnpSourceOption;
import com.pfizer.mrbt.genomics.webservices.GeneSourceOption;
import com.pfizer.mrbt.genomics.webservices.ModelOption;
import com.pfizer.mrbt.genomics.webservices.DataRetrievalInterface;
import com.pfizer.mrbt.genomics.webservices.RetrievalException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.ChangeEvent;


/**
 *
 * @author henstockpv
 */
public class DataModel {
    public final static Integer X = 24;
    public final static Integer Y = 25;
    private DataRetrievalInterface webServices;
    private HashMap<String, DataSet> name2dataset = new HashMap<String,DataSet>();
    public static String FILENAME = "1336590392598gene_snp_gwa_results.txt";
    private ArrayList<DataListener> listeners = new ArrayList<DataListener>();
    private DataRetrievalWithThreadPool dataRetrievalWithThreadPool;
    private ArrayList<GeneModelScore> geneModelScoreList = new ArrayList<GeneModelScore>();
    
    public DataModel() {
    }
    
    /**
     * Initializes the data with the command-line arguments passed in from the
     * java webstart or command-line.  The argv contain the following:
     * Argv[0]: A comma-separated list of selected model IDs
     * Argv[1]: GENE,RADIUS pairs. If more than one gene is selected, these will be separated by semicolons
     * Argv[2]: The gene annotation source – we currently only support one, so this will always be GRCh37.
     * Argv[3]: The SNP annotation source – arrives as 19 (=HG19) or 18 (=HG18), matching the IDs given by the GetSnpSources webservice.
     * Argv[4]: The selected p-value cutoff – you can ignore this if the application doesn’t support it.
     * @param argv = startup parameters
     * @throws RetrievalException if any of the dbSnpSource, getGeneSource or full query fail
     */
    public void initializeData(String[] argv) throws RetrievalException {
        ArrayList<StartupInfo> startupInfos = StartupInfo.parse(argv);
        //int stateDataMode = Singleton.getState().getDataMode();
        /*if(startupInfos.size() > 0 && (stateDataMode == State.TRANSMART_DEV_SERVICES_MODE || 
                                       stateDataMode == State.TRANSMART_SERVICES_MODE)) {   */
        if(startupInfos.size() > 0) {
            //List<String> geneNames = new ArrayList<String>();
            List<ModelOption> modelOptions = new ArrayList<ModelOption>();
            int geneSourceId    = startupInfos.get(0).getGeneSourceId();
            int dbSnpSourceId   = startupInfos.get(0).getSnpSourceId();
            //int radius          = 0;
            int index           = 0;
            
            
            // create a fake modelOptions with the modelIndex common to all the startupInfos
            for(Long modelIndex : startupInfos.get(0).getStudySetModelIndexList()) {
               modelOptions.add(new ModelOption("study" + index,"set" + index,"model" + index, modelIndex));
               index++;
            }
            
            // get dbSnpSource options and choose the option corresponding to dbSnpSourceId
            List<DbSnpSourceOption> dbSnpSrcOptions = webServices.getDbSnpSources();
            DbSnpSourceOption dbSnpSourceOption = null;
            for(DbSnpSourceOption option : dbSnpSrcOptions) {
                if(option.getId() == dbSnpSourceId) {
                    dbSnpSourceOption = option;
                }
            }
            // get geneSource options and choose the option corresponding to genesourceId
            List<GeneSourceOption>  geneSrcOptions  = webServices.getGeneSources();
            //List<GeneSourceOption>  geneSrcOptions  = transmartQueryParameterFetch.getGeneSources();
            GeneSourceOption geneSourceOption = null;
            for(GeneSourceOption option : geneSrcOptions) {
                if(option.getId() == geneSourceId) {
                    geneSourceOption = option;
                }
            }
            
            for(StartupInfo startupInfo : startupInfos) {
                List<String> genes = new ArrayList<String>();
                genes.add(startupInfo.getGene());
                
                fetchModelSnpData(modelOptions,
                                  dbSnpSourceOption,
                                  geneSourceOption,
                                  genes,
                                  startupInfo.getRange());
                /*TransmartDataLoaderWithThreads tdlwt = new TransmartDataLoaderWithThreads(
                    modelOptions, dbSnpSourceOption, geneSourceOption, genes, startupInfo.getRange());
                tdlwt.fetchGeneData();*/
                
            }
        }             
    }
    
    /**
     * Returns the dataset associated with name
     * @param name
     * @return 
     */
    public DataSet getDataSet(String name) {
        return name2dataset.get(name);
    }

    /**
     * Returns a collection of all the data sets regardless of the name
     * @return 
     */
    public Collection<DataSet> getDataSets() {
        return name2dataset.values();
    }
    
    /**
     * Removes the data set specified by name from the name2dataset
     * @param name 
     */
    public void removeDataSet(String name) {
        name2dataset.remove(name);
    }
    
    /**
     * Loads in a file that is a [multi-]gene/[multi-]model query result in a
     * file that comes back as the AQG format
     * @param filename 
     */
    /*public void loadDataSets(String filename) {
        DataLoader loader = new DataLoader();
        loader.loadDataSets(filename);
        HashMap<String, DataSet> loadResults = loader.getLoadResults();

        for(Map.Entry<String, DataSet> entry : loadResults.entrySet()) {
            name2dataset.put(entry.getKey(), entry.getValue());
        }
        fireDataChanged();
    }*/
    
    public void exportDataSets(String filename) {
        DataExporter exporter = new DataExporter();
        exporter.exportAllData(filename);
    }
    
    /**
     * Parses the chromosomeStr into an integer.  This includes X and Y as 
     * case-insensitive entities.  Other values are returned as -1.
     * @param chromosomeStr
     * @return 
     */
    public static int parseChromosomeStr(String chromosomeStr) {
        if(chromosomeStr.equalsIgnoreCase("X")) {
            return X;
        } else if(chromosomeStr.equalsIgnoreCase("Y")) {
            return Y;
        } else {
            try {
                int chromosomeNumber = Integer.parseInt(chromosomeStr);
                return chromosomeNumber;
            } catch(NumberFormatException nfe) {
                System.out.println("Failed to parse chromosome " + chromosomeStr);
                return -1;
            }
        }
    }
    
    public static String getChromosomeString(int chromosomeNumber) {
        if(chromosomeNumber == X) {
            return "X";
        } else if(chromosomeNumber == Y) {
            return "Y";
        } else {
            return chromosomeNumber + "";
        }
    }
    
    /**
     * Over-writes the dataSet if same name but adds the name->dataSet to the
     * map of datasets
     * @param name
     * @param dataSet 
     */
    public void addDataSet(String name, DataSet dataSet) {
        synchronized(this) {
            name2dataset.put(name, dataSet);
            boolean modifiedExisting = false;
            boolean addedData = false;
            // update geneModelScoreList
            System.out.println("Adding data set with " + dataSet.getModels().size());
            for(Model model : dataSet.getModels()) {
                System.out.println("AddingDataSet " + model.toString());
                GeneModelScore candidateGeneModelScore = new GeneModelScore(name, model.toString(), model.getMaxSnpLog10Pval());
                int indexOfExisting = geneModelScoreList.indexOf(candidateGeneModelScore);
                System.out.println("Add: Index of existing " + indexOfExisting + "\t" + candidateGeneModelScore.getModelName());
                if(indexOfExisting >= 0) {
                    geneModelScoreList.get(indexOfExisting).setScore(model.getMaxSnpLog10Pval());
                    modifiedExisting = true;
                } else {
                    geneModelScoreList.add(candidateGeneModelScore);
                    addedData = true;
                }
            }
            System.out.println("Adding data set " + modifiedExisting + "\t" + addedData);
            if(modifiedExisting) {
                fireDataChanged();
            } else if(addedData) {
                fireDataAdded();
            }
        }
    }
    
    /**
     * Over-writes the dataSet if same name but adds the name->dataSet to the
     * map of datasets
     * @param name
     * @param existingDataSet has had the dataSet models added to it
     * @param addedDataSet is the retrieved data that either updates existing GeneModelScore or adds new ones
     */
    public void updateDataSet(String name, DataSet existingDataSet, DataSet addedDataSet) {
        synchronized(this) {
            name2dataset.put(name, existingDataSet);
            boolean modifiedExisting = false;
            boolean addedData = false;
            // update geneModelScoreList
            System.out.println("Updating data set with " + addedDataSet.getModels().size());
            for(Model model : addedDataSet.getModels()) {
                GeneModelScore candidateGeneModelScore = new GeneModelScore(name, model.toString(), model.getMaxSnpLog10Pval());
                int indexOfExisting = geneModelScoreList.indexOf(candidateGeneModelScore);
                System.out.println("Update: Index of existing " + indexOfExisting + "\t" + candidateGeneModelScore.getModelName());
                if(indexOfExisting >= 0) {
                    geneModelScoreList.get(indexOfExisting).setScore(model.getMaxSnpLog10Pval());
                    modifiedExisting = true;
                } else {
                    geneModelScoreList.add(candidateGeneModelScore);
                    addedData = true;
                }
            }
            System.out.println("Updating data set " + modifiedExisting + "\t" + addedData);
            if(modifiedExisting) {
                fireDataChanged();
            } else if(addedData) {
                fireDataAdded();
            }
        }
    }
    
    public void addListener(DataListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(DataListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    public void fireDataChanged() {
        ChangeEvent ce = new ChangeEvent(this);
        for (DataListener listener : listeners) {
            listener.dataChanged(ce);
        }
    }

    public void fireDataAdded() {
        ChangeEvent ce = new ChangeEvent(this);
        for (DataListener listener : listeners) {
            listener.dataAdded(ce);
        }
    }

    /**
     * Retrieves interface for all the calls to the database
     *
     * @return
     */
    public DataRetrievalInterface getWebServices() {
        return webServices;
    }

    /**
     * Sets the interface for all the calls to the database
     *
     * @param webServices
     */
    public void setWebServices(DataRetrievalInterface webServices) {
        this.webServices = webServices;
    }

    /**
     * Main call for the primary data retrieval of the data for generating the
     * Results view including the SNPs (by gene or SNP search), annotations, and
     * recombination rate
     *
     * @param selectedModels
     * @param selectedDbSnpOption
     * @param geneSourceOption
     * @param geneRequestList
     * @param basePairRadius
     */
    public void fetchModelSnpData(List<ModelOption> selectedModels,
                                  DbSnpSourceOption selectedDbSnpOption,
                                  GeneSourceOption geneSourceOption,
                                  List<String> geneRequestList,
                                  int basePairRadius) {
        getDataRetrievalWithThreadPool().retrieveData(
                                                 selectedModels,
                                                 selectedDbSnpOption,
                                                 geneSourceOption,
                                                 geneRequestList,
                                                 basePairRadius);
    }
    
    /**
     * Kluge class that initializes once we have the webServices else it will
     * die unexpectedly
     * @return 
     */
    protected DataRetrievalWithThreadPool getDataRetrievalWithThreadPool() {
        if(dataRetrievalWithThreadPool == null) {
            dataRetrievalWithThreadPool = new DataRetrievalWithThreadPool(webServices);
        }
        return dataRetrievalWithThreadPool;
    }
    

    /**
     * Returns the number of entries in the list
     * @return 
     */
    public int getGeneModelScoreListSize() {
        return geneModelScoreList.size();
    }
    
    /**
     * Returns the GeneModelScore of the current index
     * @param index
     * @return 
     */
    public GeneModelScore getGeneModelScore(int index) {
        return geneModelScoreList.get(index);
    }
    
    public void removeGeneModelScore(GeneModelScore candidateGeneModelScore) {
        int index = geneModelScoreList.indexOf(candidateGeneModelScore);
        System.out.println("Removing index genemodelScore " + index);
        if(index >= 0) {
            geneModelScoreList.remove(index);
        }
    }
    
    public void clearGeneModelScoreList() {
        geneModelScoreList.clear();
    }
}
