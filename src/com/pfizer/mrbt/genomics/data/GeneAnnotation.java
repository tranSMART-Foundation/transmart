/*
 * Storage for an annotation format.  This may have to be expanded later but
 * represents a single gene along the chromosome.  We have ignored storing
 * the chromosome here and assume it will be included at a higher level
 */
package com.pfizer.mrbt.genomics.data;

/**
 *
 * @author henstockpv
 */
public class GeneAnnotation {
    protected String gene;
    private boolean rightDirection;
    protected int start;
    protected int end;
    
    public GeneAnnotation(String gene, boolean direction, int start, int end) {
        this.gene = gene;
        this.rightDirection = direction;
        this.start = start;
        this.end = end;
    }

    /**
     * End location of the gene on the chromosome
     * @return 
     */
    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * Gene name
     * @return 
     */
    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    /**
     * Gene goes to the right on the chromosome
     * @return 
     */
    public boolean isRightDirection() {
        return rightDirection;
    }

    public void setRightDirection(boolean rightDirection) {
        this.rightDirection = rightDirection;
    }

    /**
     * Start location of the gene on the chromosome
     * @return 
     */
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }
}
    
    
