/*
 * Utility class to store the study/analysisName/modelName.  We're now curtailing
 * this and using just the analysisName
 */
package com.pfizer.mrbt.genomics.bioservices;

/**
 *
 * @author henstockpv
 */
public class ModelOption implements Comparable {
    public final static long UNKNOWN = -1;
    private String studyName;
    private String analysisName; // used to be set name
    private String modelName;
    private long modelId = UNKNOWN;
    
    public ModelOption(String studyName, String analysisName, String modelName, long modelId) {
        this.studyName = studyName;
        this.analysisName = analysisName;
        this.modelName = modelName;
        this.modelId = modelId;
    }
    
    public ModelOption(String studyName, String analysisName, String modelName) {
        this.studyName = studyName;
        this.analysisName = analysisName;
        this.modelName = modelName;
        this.modelId = UNKNOWN;
    }
    
    public String getStudyName() {
        return studyName;
    }
    
    public String getAnalysisName() {
        return analysisName;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public long getModelId() {
        return modelId;
    }
    
    @Override
    public String toString() {
        //return this.analysisName + " - " + this.studyName;
        return this.studyName + " - " + analysisName;
    }
    
    public String toVerboseString() {
        //return this.analysisName + " - " + this.studyName;
        return this.studyName + " - " + analysisName + "\t" + modelName + "\t" + modelId;
    }
    
    @Override
    public int compareTo(Object other) {
        return this.toString().compareTo(((ModelOption) other).toString());
    }
}
