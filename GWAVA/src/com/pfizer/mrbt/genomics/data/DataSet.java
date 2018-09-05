/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.data;

import com.pfizer.mrbt.genomics.webservices.DbSnpSourceOption;
import com.pfizer.mrbt.genomics.webservices.GeneSourceOption;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author henstockpv
 */
public class DataSet {
    public final static double NO_VALUE = -999.0;
    public final static int UNKNOWN = -1;
      private CopyOnWriteArrayList<Model> models = new CopyOnWriteArrayList<Model>();
      private CopyOnWriteArrayList<SNP> snps = new CopyOnWriteArrayList<SNP>();
      private GeneRange geneAnnotationRange;
      private NumericRange xAxisRange = null;
      private DbSnpSourceOption dbSnpOption = null;
      private GeneSourceOption geneSourceOption = null;
      private int chromosome = UNKNOWN;
      //private SnpModel2PvalMap snpModel2Pval = new SnpModel2PvalMap();
      private int id;
      private ArrayList<SnpRecombRate> snpRecombRates = null;
      private float maxRecombinationRate = 0f;  // max value for plot scaling
      private ArrayList<GeneAnnotation> geneAnnotations = new ArrayList<GeneAnnotation>();
      
      public final static boolean LOAD_RECOMB_FROM_FILE = false;
      /*
       * last name before data sets
       */


      public DataSet() {
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
        return geneAnnotationRange;
    }

    public void setGeneRange(GeneRange geneRange) {
        this.geneAnnotationRange = geneRange;
    }
    
    /**
     * The xRange is the displayed range of values.  The geneAnnotationRange is
     * that retrieved for the range of annotations fetched. If the xRange exceeds
     * that of the geneAnnotationRange it returns true else returns false
     * @return 
     */
    public boolean xRangeExceedsGeneAnnotationRange() {
        if(xAxisRange.getMin() < geneAnnotationRange.getStart() ||
           xAxisRange.getMax() > geneAnnotationRange.getEnd()) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Updates the geneAnnotationRange with the max entent of the geneAnnotationRange and newGeneRange
     * @param newGeneRange
     * @return true if the gene range has been changed
     */
    public boolean updateGeneRange(GeneRange newGeneRange) {
        boolean updated = false;
        if(newGeneRange.getStart() < this.geneAnnotationRange.getStart() ||
           newGeneRange.getEnd() > this.geneAnnotationRange.getEnd()) {
            geneAnnotationRange.setStart( Math.min(newGeneRange.getStart(), this.geneAnnotationRange.getStart()));
            geneAnnotationRange.setEnd( Math.max(newGeneRange.getEnd(), this.geneAnnotationRange.getEnd()));
            updated = true;
        }
        return updated;
    }
      
      /**
       * Assigns a set of models to the internal storage.
       * @param models 
       */
      public void setModels(CopyOnWriteArrayList<Model> models) {
          this.models = models;
      }
      
      public CopyOnWriteArrayList<Model> getModels() {
          return models;
      }
      
      /**
       * Returns the model of this dataset associated with Id.  If none
       * found with that id, it returns null
       * @param id
       * @return Model corresponding to id
       */
      public Model getModel(final int id) {
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
       * @param snps 
       */
      public void setSNPs(CopyOnWriteArrayList<SNP> snps) {
          this.snps = snps;
      }
      
      public CopyOnWriteArrayList<SNP> getSnps() {
          return snps;
      }
      
      public synchronized void updateSnps(CopyOnWriteArrayList<SNP> updatingSnps) {
          for(SNP updatingSnp : updatingSnps) {
              if(! snps.contains(updatingSnp)) {
                  snps.add(updatingSnp);
              }
          }
      }
      
      public void setXAxisRange(NumericRange xAxisRange) {
          this.xAxisRange = xAxisRange;
      }
      
      /**
       * Expands xAxisRange if necessary to include the new XAxisRange.
       * @return Returns true if updated
       * @param newXAxisRange 
       */
      public boolean updateXAxisRange(NumericRange newXAxisRange) {
          boolean updated = false;
          if(newXAxisRange.getMin() < xAxisRange.getMin() ||
             newXAxisRange.getMax() > xAxisRange.getMax()) {
              double xMin = Math.min(xAxisRange.getMin(), newXAxisRange.getMin());
              double xMax = Math.max(xAxisRange.getMax(), newXAxisRange.getMax());
              xAxisRange = new NumericRange(xMin, xMax);
              updated = true;
          }
          return updated;
      }
      
      /*public void setSnpModel2PvalMap(SnpModel2PvalMap snpModel2Pval) {
          this.snpModel2Pval = snpModel2Pval;
      }*/
      
      /**
       * Returns the value of a snp-model combination as a log-pvalue
       * @param snp
       * @param model
       * @return 
       */
      public Double getPvalFromSnpModel(SNP snp, Model model) {
          //return snpModel2Pval.get(snp, model);
          if(model == null) {
              System.err.println("Null model");
          }
          if(snp == null) {
              System.err.println("Null snp");
          }
          return model.getPval(snp);
      }

      /**
       * @return the xAxisRange
       */
      public NumericRange getXRange() {
          return xAxisRange;
      }

    public NumericRange getYRange(Model model) {
        double miny = Double.POSITIVE_INFINITY;
        double maxy = Double.NEGATIVE_INFINITY;
        NumericRange yRange = model.getYRange();
        if (yRange == null) {
            for (SNP snp : snps) {
                //Double pval = snpModel2Pval.get(snp, model);
                Double pval = model.getPval(snp);
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
     * Returns the x-y coordinates for recombination rate. It loads them
     * from the file associated with this gene name if necessary.  The
     * snpRecombRates are assumed ordered by the snp.  It also updates the
     * maxRecombinationRate
     * @return 
     */
    public ArrayList<SnpRecombRate> getSnpRecombRates() {
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
       * @param set
       * @param modelStr
       * @return 
       */
      public Model checkAddModel(String study, String set, String modelStr) {
          //System.out.println("ModelAdding " + study + "\t" + endpoint + "\t" + type);
          for(Model model : models) {
              if(model.getStudy().equals(study) &&
                 model.getSet().equals(set) &&
                 model.getModel().equals(modelStr)) {
                  return model;
              }
          }
          Model model = new Model(study, set, modelStr);
          models.add(model);
          return model;
      }
      
      public void addModel(Model model) {
          models.add(model);
      }
      
      /**
       * Returns model corresponding to study/endpoint/type else returns null
       * if not found
       * @param study
       * set endpoint
       * @param modelStr
       * @return 
       */
      public Model getModelElseNull(String study, String set, String modelStr) {
          for(Model model : models) {
              if(model.getStudy().equals(study) &&
                 model.getSet().equals(set) &&
                 model.getModel().equals(modelStr)) {
                  return model;
              }
          }
          return null;
      }
      
      /**
       * Returns true if the modelStr already exists
       * @param study
       * @param set
       * @param modelStr
       * @return 
       */
      public boolean modelExists(String study, String set, String modelStr) {
          for(Model model : models) {
              if(model.getStudy().equals(study) &&
                 model.getSet().equals(set) &&
                 model.getModel().equals(modelStr)) {
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
      /*public void addSnpModel2Pval(SNP currSnp, Model currModel, double logPval) {
          snpModel2Pval.put(currSnp, currModel, logPval);
      }*/

    /**
     * This is a solution for initialization issues where we are trying to display
     * an empty dataset.  This populates the data structures with empty values and
     * semi-real ranges
     * 12/6/2013 pvh modified to remove the source options and bioservice dependence
     * @return 
     */
    public static DataSet createDummyDataset() {
        DataSet dummyDataSet = new DataSet();
        int chromosome = 1;
        dummyDataSet.setChromosome(chromosome);
        dummyDataSet.setGeneRange(new GeneRange("", chromosome, 100, 200));
        ArrayList<GeneAnnotation> geneAnnotations = new ArrayList<GeneAnnotation>();
        dummyDataSet.setGeneAnnotations(geneAnnotations);
        dummyDataSet.setModels(new CopyOnWriteArrayList<Model>());
        dummyDataSet.setSNPs(new CopyOnWriteArrayList<SNP>());
        dummyDataSet.setRecombinationRate(new ArrayList<SnpRecombRate>(), 8f);
        //QueryParameterFetch queryParameterFetch = new QueryParameterFetch();
        //GeneSourceOption defaultGeneSourceOption = queryParameterFetch.getGeneSources().get(0);
        //DbSnpSourceOption defaultDbSnpSource = queryParameterFetch.getDbSnpSources().get(0);
        dummyDataSet.setXAxisRange(new NumericRange(0.0, 1000.0));
        //dummyDataSet.setGeneSourceOption(defaultGeneSourceOption);
        //dummyDataSet.setDbSnpOption(defaultDbSnpSource);
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
        //snpModel2Pval.removeAllSnpWithModel(model);
        model.clearAllSnp();
    }
    
    /**
     * Adds all the snpModel2Pval from dataSet to this's snpModel2Pval
     * @param dataSet 
     * 
     */
    /*public void addAllSnpWithModels(DataSet dataSet) {
        for(Model currModel : dataSet.getModels()) {
            Model newModel = this.checkAddModel(currModel.getStudy(), currModel.getSet(), currModel.getModel());
            for(SNP snp : dataSet.getSnps()) {
                Double pval = dataSet.getPvalFromSnpModel(snp, currModel);
                if(pval != null) {
                    this.addSnpModel2Pval(snp, newModel, pval);
                }
            }
        }
    }*/
    
    /**
     * Adds the set of models with associated SNP to the dataSet
     * @param models 
     */
    public void addDataSetModels(CopyOnWriteArrayList<Model> models) {
        for(Model model : models) {
            if(! alreadyContainsModel(model)) {
                this.addModel(model);
            }
        }
    }
    
    protected boolean alreadyContainsModel(Model modelToAdd) {
        for(Model model : this.getModels()) {
            if(model.getModel().equals(modelToAdd.getModel()) &&
               model.getSet().equals(modelToAdd.getSet())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns true if the otherDataSet has the same same radius, DbSnp and GeneSource
     * @param otherDataSet
     * @return 
     */
    public boolean hasSameRadiusDbSnpGeneSource(DataSet otherDataSet) {
        return (this.geneAnnotationRange.getRadius()    == otherDataSet.getGeneRange().getRadius() &&
                this.dbSnpOption.getId()      == otherDataSet.getDbSnpOption().getId() &&
                this.geneSourceOption.getId() == otherDataSet.getGeneSourceOption().getId());
    }
    
    public boolean hasSameRadiusDbSnpGenesource(int basePairRadius, DbSnpSourceOption dbSnpSourceOption, GeneSourceOption geneSourceOption) {
        return (this.geneAnnotationRange.getRadius()    == basePairRadius &&
                this.dbSnpOption.getId()      == dbSnpSourceOption.getId() &&
                this.geneSourceOption.getId() == geneSourceOption.getId());
    }
}
