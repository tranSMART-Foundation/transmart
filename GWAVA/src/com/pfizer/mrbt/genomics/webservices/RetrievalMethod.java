/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.pfizer.mrbt.genomics.webservices;


/**
 *
 * @author henstockpv
 */
public enum RetrievalMethod {
    MODELS_SEARCH("Available models"),
    DB_SNP_SOURCES_SEARCH("DbSnp sources"),
    GENE_SOURCES_SEARCH("Gene sources"),
    SNP_SEARCH("Snp"),
    GENE_SEARCH("Gene"),
    ANNOTATION_SEARCH("Annotationh"),
    RECOMBINATION_RATE_SEARCH("Recombination rate"),
    NOT_SPECIFIED("Not specified");
    
    private final String displayStr;
    RetrievalMethod(String displayStr) {
        this.displayStr = displayStr;
    }
    
    @Override
    public String toString() {
        return displayStr;
    }
}
