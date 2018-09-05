/*
 * Rather than rely on the GeneData to figure out the chromosome, this adds it
 * here, but it avoids changing and storing the information everywhere.  This
 * is for selecting a gene annotation that causes the vertical bar to appear
 */
package com.pfizer.mrbt.genomics.state;

import com.pfizer.mrbt.genomics.data.GeneAnnotation;
import com.pfizer.mrbt.genomics.data.NumericRange;

/**
 *
 * @author henstockpv
 */
public class SelectedGeneAnnotation extends GeneAnnotation {
    private int chromosome;

    public SelectedGeneAnnotation(String gene, boolean direction, int start, int end, int chromosome) {
        super(gene, direction, start, end);
        this.chromosome = chromosome;
    }

    public SelectedGeneAnnotation(GeneAnnotation geneAnnotation, int chromosome) {
        super(geneAnnotation.getGene(),
              geneAnnotation.isRightDirection(),
              geneAnnotation.getStart(),
              geneAnnotation.getEnd());
        this.chromosome = chromosome;
    }

    public int getChromosome() {
        return chromosome;
    }

    public void setChromosome(int chromosome) {
        this.chromosome = chromosome;
    }
    
    /**
     * Returns true if the range of this gene overlaps with the range in any way
     * @param range
     * @return 
     */
    public boolean overlaps(NumericRange range) {
        if(this.end < range.getMin() || this.start > range.getMax()) {
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * Returns true if the range of this gene overlaps with the range in any way
     * @param range
     * @return 
     */
    public boolean overlaps(double startx, double endx) {
        if(this.end < startx || this.start > endx) {
            return false;
        } else {
            return true;
        }
    }
    
    
}
