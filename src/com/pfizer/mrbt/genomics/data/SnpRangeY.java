/*
 * Utility class stores the range of a gene in SNP locations as well as the
 * Y-value so that we don't have overlapping genes.
 */
package com.pfizer.mrbt.genomics.data;

/**
 *
 * @author henstockpv
 */
public class SnpRangeY {
    private int start;
    private int end;
    private int y;
    public SnpRangeY(int start, int end, int y) {
        this.start = start;
        this.end = end;
        this.y = y;
    }
    
    /**
     * Returns true if this SnpRangeY has overlapping SNP with [start end]
     * @param start
     * @param end
     * @return 
     */
    public boolean overlaps(int start, int end) {
        if((start >= this.start && start < this.end) ||  //start in range
           (end   > this.start && end   <= this.end) ||  // end in range
           (start < this.start && end   > this.end)) {  // this is subset
            return true;
        } else {
            return false;
        }
    }

    public int getEnd() {
        return end;
    }

    public int getStart() {
        return start;
    }

    public int getY() {
        return y;
    }
    
    public void setY(int y) {
        this.y = y;
    }
}
