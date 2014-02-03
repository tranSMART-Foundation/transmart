/*
 * Contains the information about a single study which is the study, model of
 * model, set, and the modelID
 */
package com.pfizer.mrbt.genomics.data;

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
    
}
