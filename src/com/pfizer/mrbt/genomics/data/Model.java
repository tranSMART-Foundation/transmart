/*
 * Contains the information about a single study which is the study, model of
 * model, set, and the modelID
 */
package com.pfizer.mrbt.genomics.data;

import java.util.HashMap;

/**
 *
 * @author henstockpv
 */
public class Model {
    private static int numModels = 0;
    private String study;
    private String model;
    private String set;
    private NumericRange yRange = null;
    private Double maxSnpLog10Pval = 0.0;
    private HashMap<Integer, Double> snpId2pval = new HashMap<Integer, Double>();
    private int id;
    
    
    public Model(String study, String set, String model) {
        this.study = study;
        this.model  = model;
        this.set  = set;
        id = numModels;
        numModels++;
        //System.out.println("Created new model " + set + "\t" + id);
    }
    
    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public int getId() {
        return id;
    }

    public static int getNumModels() {
        return numModels;
    }

    public String getStudy() {
        return study;
    }

    public void setStudy(String study) {
        this.study = study;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
    
    @Override
    public String toString() {
        //return study + "/" + set + "/" + model;
        //return set + " - " + study;
        return study + " - " + set;
    }
    
    /*public String toResultsString() {
        return study + " - " + set;
    }*/
    
    /**
     * This is a kluge since the results table is not returning a unique set of
     * values 1/8/2013 PVH.
     * @return 
     */
    public String toResultsTableString() {
        return study + " - " + model;
    }
    
    public void setYRange(NumericRange yRange) {
        this.yRange = yRange;
    }
    
    public NumericRange getYRange() {
        return yRange;
    }
    
    public Double getPval(int snpId) {
        return snpId2pval.get(snpId);
    }
    
    public Double getPval(SNP snp) {
        return snpId2pval.get(snp.getRsId());
    }
    
    /**
     * Stores the modelSnpId2pval and updates maxSnpLog10Pval;
     * @param modelSnpId2pval 
     */
    public void setSnpMap(HashMap<Integer, Double> modelSnpId2pval) {
        this.snpId2pval = modelSnpId2pval;
        for(Double value : modelSnpId2pval.values()) {
            if(value > maxSnpLog10Pval) {
                maxSnpLog10Pval = value;
            }
        }
    }
    
    public void addSnpPval(SNP currSnp, double logPval) {
        this.snpId2pval.put(currSnp.getRsId(), logPval);
        if(logPval > maxSnpLog10Pval) {
            maxSnpLog10Pval = logPval;
        }
    }
    
    /**
     * Removes all the snp2pval mapping and sets maxSnpLog10Pval to 0.0
     */
    public void clearAllSnp() {
        this.snpId2pval.clear();
        maxSnpLog10Pval = 0.0;
    }
    
    public double getMaxSnpLog10Pval() {
        return maxSnpLog10Pval;
    }
}
