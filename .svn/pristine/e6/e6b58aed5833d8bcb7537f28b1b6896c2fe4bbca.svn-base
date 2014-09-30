/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.resultstable;

import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.data.SNP;
import java.util.ArrayList;

/**
 *
 * @author henstock
 */
public class GeneModelSnp {

    private String gene;
    private Model model;
    private int numSnpEntries = 0;
    private ArrayList<SNP> snps = new ArrayList<SNP>();

    public GeneModelSnp(String gene, Model model) {
        this.gene = gene;
        this.model = model;
        numSnpEntries = 0;
    }

    /**
     * Adds a SNP to the list if it's not already included
     * @param snp 
     */
    public void addSnp(SNP snp) {
        if (! snps.contains(snp)) {
            snps.add(snp);
            numSnpEntries++;
        }
    }

    /**
     * Adds a members of snpList to the list if not already there
     * @param snp 
     */
    public void addSnpList(ArrayList<SNP> snpList, boolean checkUnique) {
        if (checkUnique) {
            for (SNP snp : snpList) {
                if (snps.contains(snp)) {
                    snps.add(snp);
                    numSnpEntries++;
                }
            }
        } else {
            for (SNP snp : snpList) {
                snps.add(snp);
                numSnpEntries++;
            }
        }
    }
    
    /**
     * Removes the snp from the geneModelSnp
     * @param index 
     */
    public void removeSnp(int index) {
        snps.remove(index);
        numSnpEntries--;
    }

    /**
     * Returns the number of lines held by this
     * @return 
     */
    public int getNumSnpEntries() {
        return numSnpEntries;
    }

    public String getGene() {
        return gene;
    }

    public Model getModel() {
        return model;
    }

    /**
     * Return a list of snps that are in the gene and model
     * @return 
     */
    public ArrayList<SNP> getSnps() {
        return snps;
    }

    public SNP getSnp(int index) {
        return snps.get(index);
    }
}
