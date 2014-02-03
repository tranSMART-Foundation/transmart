/*
 * Storage for a SNP.  It is broken out separately since one SNP can have multiple
 * values with each model
 */
package com.pfizer.mrbt.genomics.data;

/**
 *
 * @author henstockpv
 */
public class SNP implements Comparable {
    private int rsId;
    private int loc;
    
    public SNP() {
        
    }
    
    public SNP(int rsId, int loc) {
        this.rsId = rsId;
        this.loc = loc;
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public int getRsId() {
        return rsId;
    }

    public void setRsId(int rsId) {
        this.rsId = rsId;
    }
    
    @Override
    public int compareTo(Object other) {
        if(loc < ((SNP) other).getLoc()) {
            return -1;
        } else if(loc > ((SNP) other).getLoc()) {
            return 1;
        } else return 0;
    }
}
