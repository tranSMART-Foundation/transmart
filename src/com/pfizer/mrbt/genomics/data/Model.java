/*
 * Contains the information about a single study which is the study, type of
 * model, endpoint, and the modelID
 */
package com.pfizer.mrbt.genomics.data;

/**
 *
 * @author henstockpv
 */
public class Model {
    private static int numModels = 0;
    private String study;
    private String type;
    private String endpoint;
    private NumericRange yRange = null;
    private int id;
    
    
    public Model(String study, String endpoint, String type) {
        this.study = study;
        this.type  = type;
        this.endpoint  = endpoint;
        id = numModels;
        numModels++;
        //System.out.println("Created new model " + endpoint + "\t" + id);
    }
    
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String data) {
        this.endpoint = data;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        //return study + "/" + endpoint + "/" + type;
        //return endpoint + " - " + study;
        return study + " - " + endpoint;
    }
    
    /*public String toResultsString() {
        return study + " - " + endpoint;
    }*/
    
    public void setYRange(NumericRange yRange) {
        this.yRange = yRange;
    }
    
    public NumericRange getYRange() {
        return yRange;
    }
    
}
