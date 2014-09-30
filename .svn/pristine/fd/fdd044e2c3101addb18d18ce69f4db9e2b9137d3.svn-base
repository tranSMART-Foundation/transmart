/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.data;

import com.pfizer.mrbt.genomics.bioservices.RecombinationRateService;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author henstockpv
 */
public class RecombinationRates {

    /* Note it assumes that the snpRecombRates are in order of snp.  Could sort
     * but that would take a while */
    private ArrayList<SnpRecombRate> snpRecombRates = new ArrayList<SnpRecombRate>();
    private float maxRecombinationRate = 0f;

    public RecombinationRates() {
        
    }

    /**
     * Currently a file-based system that reads from a filename to get the
     * snp associated with the region around a particular gene.  Should be
     * replaced by a search.  Fills the snpRecombRates data.
     * @param filename 
     */
    public void loadRecombinationRates(String filename) {
        FileReader fr = null;
        BufferedReader br = null;
        snpRecombRates.clear();
        try {
            fr = new FileReader(filename);
            br = new BufferedReader(fr);
            String line;
            int linenum = 0;
            while((line = br.readLine()) != null) {
                if(linenum > 0) {  // skip header row
                    String[] tokens = line.split("\\t");
                    try {
                        int   snp = Integer.parseInt(tokens[0]);
                        float val = Float.parseFloat(tokens[1]);
                        snpRecombRates.add(new SnpRecombRate(snp, val));
                        if(val > maxRecombinationRate ) maxRecombinationRate = val;
                    } catch(IndexOutOfBoundsException ioobe) {
                        System.out.println("Couldn't parse line [out of bounds] <ignored> " + line);
                    } catch(NumberFormatException nfe) {
                        System.out.println("Failed to parse line <ignored> " + line);
                    }
                }
                linenum++;
            }
        } catch(FileNotFoundException fnfe) {
            System.out.println("Could not load recombination rates file: " + filename);
        } catch(IOException ioe) {
            System.out.println("I/O exception in recombination rates file: " + filename);
        } finally {
            try {
                if(br != null) {
                    br.close();
                }
                if(fr != null) {
                    fr.close();
                }
            } catch(IOException ioe) {
                System.out.println("Failed to close file " + filename);
            }
        }
    }
    
    public void fetchRecombinationRates(String gene, int radius, int geneSourceId) {
        RecombinationRateService recombinationRateService = new RecombinationRateService();
        snpRecombRates = recombinationRateService.fetchRecombinationRateData(gene, radius, geneSourceId);
        maxRecombinationRate = recombinationRateService.getMaxRecombinationRate();
    }
    
    /**
     * Returns the sorted list of snp-recombRates
     * @return 
     */
    public ArrayList<SnpRecombRate> getSnpRecombRates() {
        return snpRecombRates;
    }

    /**
     * Returns the maximum recombination rate
     */
    public float getMaxRecombinationRate() {
        return maxRecombinationRate;
    }
    
}
