/*
 * Storage for a locus of points including the gene name.  This is used for
 * representing a query by gene name
 */
package com.pfizer.mrbt.genomics.data;

/**
 *
 * @author henstockpv
 */
public class GeneRange extends ChromosomeRange {
    private String genename;
    private int radius = 0;     // radius of the query
    
    public GeneRange(String genename, int chromosome, int start, int end) {
        super(chromosome, start, end);
        this.genename = genename;
    }
    
    public GeneRange(String genename) {
        this.genename = genename;
    }

    @Override
    public String getName() {
        return genename;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
