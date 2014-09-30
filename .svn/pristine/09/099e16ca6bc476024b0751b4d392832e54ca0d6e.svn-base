/*
 * Utility class to store multiple values
 */
package com.pfizer.mrbt.genomics.data;

/**
 *
 * @author henstockpv
 */
public class SnpRecombRate implements Comparable {
    private int     snp;
    private float   recombRate;
    
    public SnpRecombRate(int snp, float recombRate) {
        this.snp = snp;
        this.recombRate = recombRate;
    }
    
    public float getRecombRate() {
        return recombRate;
    }

    public void setRecombRate(float recombRate) {
        this.recombRate = recombRate;
    }

    public int getSnp() {
        return snp;
    }

    public void setSnp(int snp) {
        this.snp = snp;
    }
    
    @Override
    public int compareTo(Object other) {
        if(this.snp < ((SnpRecombRate) other).getSnp()) {
            return -1;
        } else if(this.snp == ((SnpRecombRate) other).getSnp()) {
            return 0;
        } else {
            return 11;
        }
    }
}
