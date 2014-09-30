/*
 * Storage for a particular locus of points chromosome + range of positions
 */
package com.pfizer.mrbt.genomics.data;

/**
 *
 * @author henstockpv
 */
public class ChromosomeRange {
    private int chromosome;
    private int start;
    private int end;
    private int id;
    private static int numChromosomeRanges = 0;
    private String name; // may be used by search to characterize source
    public ChromosomeRange(int chromosome, int start, int end) {
        this.chromosome = chromosome;
        this.start      = start;
        this.end        = end;
        this.id         = numChromosomeRanges++;
    }
    
    public ChromosomeRange() {
        this.id = numChromosomeRanges++;
    }

    public int getChromosome() {
        return chromosome;
    }

    public void setChromosome(int chromosome) {
        this.chromosome = chromosome;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }
    
    public String getName() {
        return "Query" + id;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
