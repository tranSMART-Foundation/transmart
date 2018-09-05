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
    private String associatedGene;
    private IntronExon intronExon = IntronExon.OTHER;
    private String regulome = "";
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

    public IntronExon getIntronExon() {
        return intronExon;
    }

    public void setIntronExon(IntronExon intronExon) {
        this.intronExon = intronExon;
    }

    public String getRegulome() {
        return regulome;
    }

    public void setRegulome(String regulome) {
        this.regulome = regulome;
    }
    
    public String getAssociatedGene() {
        if(associatedGene == null) {
            return "";
        } else {
            return associatedGene;
        }
    }
    
    public void setAssociatedGene(String associatedGene) {
        this.associatedGene = associatedGene;
    }
    
    @Override
    public int compareTo(Object other) {
        if(loc < ((SNP) other).getLoc()) {
            return -1;
        } else if(loc > ((SNP) other).getLoc()) {
            return 1;
        } else return 0;
    }
    
    @Override
    public boolean equals(Object other) {
        return (loc == ((SNP) other).getLoc());
    }
    
    @Override
    public int hashCode() {
        int hashVal = new Integer(loc).hashCode();
        return hashVal;
    }
}
