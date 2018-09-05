/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.state;

import com.pfizer.mrbt.genomics.webservices.ModelOption;
import java.util.List;

/**
 *
 * @author henstockpv
 */
public class History {
    /*public final static int DONE = 0;
    public final static int WORKING = 1;
    public final static int FAILED  = 2;
    public final static int NONE    = 3;*/
    
    public final static int STATUS_COL  = 0;
    public final static int NUM_SNP_COL = 1;
    public final static int GENE_COL    = 2;
    public final static int RANGE_COL   = 3;
    public final static int SECONDS_COL = 4;
    public final static int MODEL_COUNT_COL  = 5;
    public final static int MODELS_COL  = 6;
    

    private String gene;
    private int numModels = 0;
    private List<ModelOption> modelOptions;
    private int range;
    private int seconds = 0;
    private SearchStatus searchStatus = null;
    private int numSnp  = 0;
    private long startTime = 0;
    private int queryId;
    
    public History(String gene, int numModels, int range) {
        this.gene   = gene;
        this.numModels = numModels;
        this.range  = range;
        this.searchStatus = SearchStatus.WAITING;
        this.startTime = System.currentTimeMillis();
    }
    
    public History(String gene, int numModels, int range, SearchStatus searchStatus, int queryId) {
        this.gene   = gene;
        this.numModels = numModels;
        this.range  = range;
        this.searchStatus = searchStatus;
        this.startTime = System.currentTimeMillis();
        this.queryId = queryId;
    }
    
    public History(String gene, List<ModelOption> modelOptions, int range, SearchStatus searchStatus, int queryId) {
        this.gene   = gene;
        this.numModels = numModels;
        this.modelOptions = modelOptions;
        this.range  = range;
        this.searchStatus = searchStatus;
        this.startTime = System.currentTimeMillis();
        this.queryId = queryId;
    }
    
    /**
     * Updates the status of the history.  It also computes the seconds based
     * on the startTime and the current time
     * @param status is SearchStatus enum on where the progress is
     * @param numSnp 
     */
    public void update(SearchStatus status, int numSnp) {
        this.seconds = (int) ((System.currentTimeMillis() - this.startTime) / 1000);
        this.searchStatus = status;
        if(status == SearchStatus.SUCCESS && numSnp <= 0) {
            this.searchStatus = SearchStatus.NOT_FOUND;
        }
        this.numSnp = numSnp;
    }

    public String getGene() {
        return gene;
    }

    public int getNumModels() {
        return numModels;
    }

    public int getNumSnp() {
        return numSnp;
    }

    public int getRange() {
        return range;
    }

    public int getSeconds() {
        return seconds;
    }
    
    /**
     * Returns semicolon+space delimited list of the model options
     * @return 
     */
    public String getModelOptionsString() {
        StringBuilder sb = new StringBuilder();
        int cnt = 0;
        for(ModelOption modelOption : modelOptions) {
            if(cnt > 0) {
                sb.append(";  ");
            }
            sb.append(modelOption.toString());
            cnt++;
        }
        return sb.toString();
    }
    
    /**
     * If the status is working then it retunrs the elapsed time since the
     * startTime.  Otherwise, it returns the computed elapsed time stored in
     * seconds
     * @return 
     */
    public int getElapsedTime() {
        if(this.searchStatus == SearchStatus.WORKING) {
            return (int) (System.currentTimeMillis() - startTime) / 1000;
        } else {
            return seconds;
        }
    }

    public SearchStatus getSearchStatus() {
        return searchStatus;
    }
    
    public String getSearchStatusStr() {
        return searchStatus.getMessage();
    }
    
    /**
     * Returns the tag id for tracking the history
     * @return 
     */
    public int getQueryId() {
        return queryId;
    }
}
