/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.data;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.bioservices.DbSnpSourceOption;
import com.pfizer.mrbt.genomics.bioservices.GeneSourceOption;
import com.pfizer.mrbt.genomics.bioservices.QueryParameterFetch;
import com.pfizer.mrbt.genomics.state.State;
import java.util.ArrayList;

/**
 *
 * @author henstockpv
 */
public class DataSet {
    public final static double NO_VALUE = -999.0;
    public final static int UNKNOWN = -1;
      private ArrayList<Integer> snpLoc = new ArrayList<Integer>();
      //private ArrayList<StudySetModel> studySetModels = new ArrayList<StudySetModel>();
      private ArrayList<Model> models = new ArrayList<Model>();
      private ArrayList<SNP> snps = new ArrayList<SNP>();
      private GeneRange geneRange;
      private NumericRange xAxisRange = null;
      private int geneSourceId = UNKNOWN;
      private DbSnpSourceOption dbSnpOption = null;
      private GeneSourceOption geneSourceOption = null;
      //private NumericRange yAxisRange = null;
      private int chromosome;
      private SnpModel2PvalMap snpModel2Pval = new SnpModel2PvalMap();
      private String filename;
      public static int idNumber = -1;
      private int id;
      private ArrayList<SnpRecombRate> snpRecombRates = null;
      private float maxRecombinationRate = 0f;  // max value for plot scaling
      private ArrayList<GeneAnnotation> geneAnnotations = new ArrayList<GeneAnnotation>();
      
      public final static boolean LOAD_RECOMB_FROM_FILE = false;
      /*
       * last name before data sets
       */


      public DataSet() {
          this.id = id;
      }
      
      /**
       * Returns the id for the dataSet;
       */
      public int getId() {
          return id;
      }

      /**
       * Returns GeneRange with info on snp range, gene name, and chromosome
       * @return 
       */
    public GeneRange getGeneRange() {
        return geneRange;
    }

    public void setGeneRange(GeneRange geneRange) {
        this.geneRange = geneRange;
    }
      
      /**
       * Assigns a set of models to the internal storage.
       * @param models 
       */
      public void setModels(ArrayList<Model> models) {
          this.models = models;
      }
      
      public ArrayList<Model> getModels() {
          return models;
      }
      
      /**
       * Returns the model of this dataset associated with Id.  If none
       * found with that id, it returns null
       */
      public Model getModel(int id) {
          for(Model model : models) {
              if(model.getId()==id) {
                  return model;
              }
          }
          return null;
      }
      
      /**
       * Removes the model from the list of models
       * @param model 
       */
      public void removeModel(Model model) {
          models.remove(model);
      }
      
      /**
       * Assigns a set of SNP to the dataset
       * @param models 
       */
      public void setSNPs(ArrayList<SNP> snps) {
          this.snps = snps;
      }
      
      public ArrayList<SNP> getSnps() {
          return snps;
      }
      
      public void setXAxisRange(NumericRange xAxisRange) {
          this.xAxisRange = xAxisRange;
      }
      
      public void setSnpModel2PvalMap(SnpModel2PvalMap snpModel2Pval) {
          this.snpModel2Pval = snpModel2Pval;
      }
      
      /**
       * Returns the value of a snp-model combination as a log-pvalue
       * @param snp
       * @param model
       * @return 
       */
      public Double getPvalFromSnpModel(SNP snp, Model model) {
          return snpModel2Pval.get(snp, model);
      }


      /**
       * Determines the range of x-values from the loaded data in snpLoc;
       * @deprecated
       */
      public NumericRange oldGetXRange() {
            if (xAxisRange == null) {
                  int minLoc = Integer.MAX_VALUE;
                  int maxLoc = Integer.MIN_VALUE;
                  for (int loc : snpLoc) {
                        if (loc < minLoc && loc >= 0) {
                              minLoc = loc;
                        }
                        if (loc > maxLoc && loc >= 0) {
                              maxLoc = loc;
                        }
                  }
                  xAxisRange = new NumericRange(minLoc, maxLoc);
            }
            return xAxisRange;
      }
      
      public NumericRange getXRange() {
          return xAxisRange;
      }

    public NumericRange getYRange(Model model) {
        double miny = Double.POSITIVE_INFINITY;
        double maxy = Double.NEGATIVE_INFINITY;
        NumericRange yRange = model.getYRange();
        if (yRange == null) {
            for (SNP snp : snps) {
                Double pval = snpModel2Pval.get(snp, model);
                if (pval != null) {
                    if (pval > maxy) {
                        maxy = pval;
                    }
                    if (pval < miny) {
                        miny = pval;
                    }
                }
            }
            yRange = new NumericRange(miny, maxy);
            model.setYRange(yRange);
        }
        return yRange;
    }
      
      
      public int getChromosome() {
          return chromosome;
      }

      public void setChromosome(int chromosome) {
          this.chromosome = chromosome;
      }

      /*public ArrayList<Integer> getSNPLoc() {
            return snpLoc;
      }*/

      /**
       * Returns the filename associated with this data set
       *
       * @return
       */
      public String getFilename() {
            return filename;
      }
      
    /**
     * Returns the x-y coordinates for recombination rate. It loads them
     * from the file associated with this gene name if necessary.  The
     * snpRecombRates are assumed ordered by the snp.  It also updates the
     * maxRecombinationRate
     * @return 
     */
    public ArrayList<SnpRecombRate> getSnpRecombRates() {
        if (snpRecombRates == null) {
            RecombinationRates recombinationRates = new RecombinationRates();

            if (LOAD_RECOMB_FROM_FILE) {
                String fileSep = System.getProperty("file.separator");
                String path = System.getProperty("user.home") + fileSep + "My Documents" + fileSep + "gwava_data" + fileSep + "recombRate" + fileSep;
                String recombFilename = path + geneRange.getName() + ".txt";
                recombinationRates.loadRecombinationRates(recombFilename);
            } else if (Singleton.getState().getDataMode() == State.BIOSERVICES_MODE &&
                       (geneRange.getName().equalsIgnoreCase("A1BG")
                           || geneRange.getName().equalsIgnoreCase("HYST2477")
                           || geneRange.getName().equalsIgnoreCase("A2M"))) {
                String fileSep = System.getProperty("file.separator");
                String path = System.getProperty("user.home") + fileSep + "My Documents" + fileSep + "gwava_data" + fileSep + "recombRate" + fileSep;
                String recombFilename = path + geneRange.getName() + ".txt";
                recombinationRates.loadRecombinationRates(recombFilename);
                
            } else { /* must be bioservices */
                recombinationRates.fetchRecombinationRates(geneRange.getName(), geneRange.getRadius(), geneSourceOption.getId());
            }
            snpRecombRates = recombinationRates.getSnpRecombRates();
            maxRecombinationRate = recombinationRates.getMaxRecombinationRate();
            
        }
        return snpRecombRates;
    }
    
    /**
     * Sets the recombination rate and maximumRecombinationRate corresponding
     * to the maximum value of the entries in snpRecombRate
     * @param snpRecombRate
     * @param maxRecombinationRate 
     */
    public void setRecombinationRate(ArrayList<SnpRecombRate> snpRecombRate, float maxRecombinationRate) {
        this.maxRecombinationRate = maxRecombinationRate;
        this.snpRecombRates = snpRecombRate;
    }

      /**
       * Returns the max recombination rate.  If undefined since the snp
       * recombination rates haven't been loaded, it loads them.
       * @return 
       */
      public float getMaxRecombinationRate() {
          return maxRecombinationRate;
      }
      
      /**
       * Returns the model with the same study/endpoint/type in models else
       * creates a new one and adds it to models
       * @param study
       * @param endpoint
       * @param type
       * @return 
       */
      public Model checkAddModel(String study, String endpoint, String type) {
          //System.out.println("ModelAdding " + study + "\t" + endpoint + "\t" + type);
          for(Model model : models) {
              if(model.getStudy().equals(study) &&
                 model.getEndpoint().equals(endpoint) &&
                 model.getType().equals(type)) {
                  return model;
              }
          }
          Model model = new Model(study, endpoint, type);
          models.add(model);
          return model;
      }
      
      /**
       * Returns model corresponding to study/endpoint/type else returns null
       * if not found
       * @param study
       * @param endpoint
       * @param type
       * @return 
       */
      public Model getModelElseNull(String study, String endpoint, String type) {
          for(Model model : models) {
              if(model.getStudy().equals(study) &&
                 model.getEndpoint().equals(endpoint) &&
                 model.getType().equals(type)) {
                  return model;
              }
          }
          return null;
      }
      
      /**
       * Returns true if the model already exists
       * @param study
       * @param endpoint
       * @param type
       * @return 
       */
      public boolean modelExists(String study, String endpoint, String type) {
          for(Model model : models) {
              if(model.getStudy().equals(study) &&
                 model.getEndpoint().equals(endpoint) &&
                 model.getType().equals(type)) {
                  return true;
              }
          }
          return false;
      }

      /**
       * Returns the snp with the same rsid from snplist.  If not found, it
       * creates a new snp and adds it to the snplist before returning it
       * @param rsid
       * @return 
       */
      public SNP checkAddSnp(int rsid) {
          for(SNP snp : snps) {
              if(snp.getRsId() == rsid) {
                  return snp;
              }
          }
          SNP snp = new SNP();
          snp.setRsId(rsid);
          snps.add(snp);
          return snp;
      }
      
      /**
       * Adds the logpval to the current snp and model specified 
       * @param currSnp
       * @param currModel
       * @param logPval 
       */
      public void addSnpModel2Pval(SNP currSnp, Model currModel, double logPval) {
          snpModel2Pval.put(currSnp, currModel, logPval);
      }

    /**
     * This is a solution for initialization issues where we are trying to display
     * an empty dataset.  This populates the data structures with empty values and
     * semi-real ranges
     * @return 
     */
    public static DataSet createDummyDataset() {
        DataSet dummyDataSet = new DataSet();
        int chromosome = 1;
        dummyDataSet.setChromosome(chromosome);
        dummyDataSet.setGeneRange(new GeneRange("", chromosome, 100, 200));
        QueryParameterFetch queryParameterFetch = new QueryParameterFetch();
        GeneSourceOption defaultGeneSourceOption = queryParameterFetch.getGeneSources().get(0);
        DbSnpSourceOption defaultDbSnpSource = queryParameterFetch.getDbSnpSources().get(0);
        dummyDataSet.setXAxisRange(new NumericRange(0.0, 1000.0));
        dummyDataSet.setGeneSourceOption(defaultGeneSourceOption);
        dummyDataSet.setDbSnpOption(defaultDbSnpSource);
        return dummyDataSet;
    }

    public DbSnpSourceOption getDbSnpOption() {
        return dbSnpOption;
    }

    public void setDbSnpOption(DbSnpSourceOption dbSnpOption) {
        this.dbSnpOption = dbSnpOption;
    }

    public GeneSourceOption getGeneSourceOption() {
        return geneSourceOption;
    }

    public void setGeneSourceOption(GeneSourceOption geneSourceOption) {
        this.geneSourceOption = geneSourceOption;
    }

    public ArrayList<GeneAnnotation> getGeneAnnotations() {
        return geneAnnotations;
    }

    public void setGeneAnnotations(ArrayList<GeneAnnotation> geneAnnotations) {
        this.geneAnnotations = geneAnnotations;
    }
    
    /*
     * Returns the snp location with the given rsNumber
     */
    public int getSnpLocation(String rsStr) {
        int rsNumber = Integer.parseInt(rsStr.substring(2));
        for(SNP snp : snps) {
            if(snp.getRsId() == rsNumber) {
                return snp.getLoc();
            }
        }
        return -1;
    }
    
    /**
     * This is a purge step where we are about to add new data for the existing
     * model and want to get rid of all the data with the current model from the
     * snpModel2Pval stuff
     * @param model 
     */
    public void removeAllSnpWithModel(Model model) {
        snpModel2Pval.removeAllSnpWithModel(model);
    }
    
    /**
     * Adds all the snpModel2Pval from dataSet to this's snpModel2Pval
     * @param dataSet 
     * 
     */
    public void addAllSnpWithModels(DataSet dataSet) {
        for(Model currModel : dataSet.getModels()) {
            Model newModel = this.checkAddModel(currModel.getStudy(), currModel.getEndpoint(), currModel.getType());
            for(SNP snp : dataSet.getSnps()) {
                Double pval = dataSet.getPvalFromSnpModel(snp, currModel);
                if(pval != null) {
                    this.addSnpModel2Pval(snp, newModel, pval);
                }
            }
        }
    }
}
