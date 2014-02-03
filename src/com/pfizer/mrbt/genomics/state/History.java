/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.state;

/**
 *
 * @author henstockpv
 */
public class History {
    public final static int DONE = 0;
    public final static int WORKING = 1;
    public final static int FAILED  = 2;
    public final static int NONE    = 3;
    
    public final static int GENE_COL = 0;
    public final static int MODELS_COL = 1;
    public final static int RANGE_COL = 2;
    public final static int NUM_SNP_COL = 3;
    public final static int STATUS_COL = 4;
    public final static int SECONDS_COL = 5;
    
    

    private String gene;
    private int numModels = 0;
    private int range;
    private int seconds = 0;
    private int status  = 0;
    private int numSnp  = 0;
    private long startTime = 0;
    
    public History(String gene, int numModels, int range) {
        this.gene   = gene;
        this.numModels = numModels;
        this.range  = range;
        this.status = WORKING;
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * Updates the status of the history.  It also computes the seconds based
     * on the startTime and the current time
     * @param seconds
     * @param status
     * @param numSnp 
     */
    public void update(int status, int numSnp) {
        this.seconds = (int) ((System.currentTimeMillis() - this.startTime) / 1000);
        this.status = status;
        if(status == DONE && numSnp == 0) {
            status = NONE;
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
     * If the status is working then it retunrs the elapsed time since the
     * startTime.  Otherwise, it returns the computed elapsed time stored in
     * seconds
     * @return 
     */
    public int getElapsedTime() {
        if(status == WORKING) {
            return (int) (System.currentTimeMillis() - startTime) / 1000;
        } else {
            return seconds;
        }
    }

    public int getStatus() {
        return status;
    }
    
    public String getStatusStr() {
        switch(status) {
            case DONE: return "Done";
            case WORKING: return "Working";
            case FAILED: return "Failed";
            case NONE: return "Not Found";
            default:
                return "Unknown";
        }
    }
    
}
