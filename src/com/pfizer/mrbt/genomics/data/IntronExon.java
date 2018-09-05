/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.data;

/**
 *
 * @author henstockpv
 */
public enum IntronExon {
    INTRON ("intron", "In"), 
    EXON ("exon", "Ex"), 
    OTHER ("", "");
    
    private final String str;
    private final String twoLetterStr;
    IntronExon(String str, String twoLetterStr) {
        this.str = str;
        this.twoLetterStr = twoLetterStr;
    }
    
    @Override
    public String toString() {
        return str;
    }
    
    public String getTwoLetterString() {
        return twoLetterStr;
    }
}
