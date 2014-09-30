/*
 * This is an alternative to GeneAnnotations in that it stores the gene
 * annotations based on the gene with its surrounding radius.  All the genes
 * are then returned for a given gene.  It uses the bioservices look-up.
 */
package com.pfizer.mrbt.genomics.data;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.bioservices.GeneAnnotationFetch;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author henstockpv
 */
public class GeneAnnotationsByGene {
    private HashMap<String, ArrayList<GeneAnnotation>> annotations = new HashMap<String, ArrayList<GeneAnnotation>>();

    /**
     * Returns an arraylist of GeneAnnotation for the given chromosome betweent he startLoc
     * and endLoc.  If the appropriate chromosome file has not been loaded, then it has
     * to load it here and it will store it.
     * @param chromosome
     * @param startLoc
     * @param endLoc
     * @return 
     */
    public List<GeneAnnotation> getAnnotations(String gene) {
        if(! annotations.containsKey(gene)) {
            fetchGeneAnnotationData(gene);
        }
        return annotations.get(gene);
    }
    
    /**
     * Loads in teh appropriate annotations file for the specified chromosome
     * from teh data\\annotatedGenes folder.  Note that it puts the start as
     * the left component even though that may not always be the case
     * @param chromosome 
     */
    protected void fetchGeneAnnotationData(String gene) {
        DataSet dataSet = Singleton.getDataModel().getDataSet(gene);
        int startLoc    = dataSet.getGeneRange().getStart();
        int endLoc      = dataSet.getGeneRange().getEnd();
        int geneSrcId   = dataSet.getGeneSourceOption().getId();
        int chromosome  = dataSet.getChromosome();
        GeneAnnotationFetch geneAnnotationFetch = new GeneAnnotationFetch();
        ArrayList<GeneAnnotation> geneAnnotations = geneAnnotationFetch.fetchData(geneSrcId, startLoc, endLoc, chromosome);
        annotations.put(gene, geneAnnotations);
    }
}
