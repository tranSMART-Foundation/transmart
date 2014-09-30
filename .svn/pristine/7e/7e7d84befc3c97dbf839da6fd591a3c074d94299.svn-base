/*
 * Records and provides access to the gene annotations for each chromosome.
 * The data are loaded on a per-chromosome basis.
 */
package com.pfizer.mrbt.genomics.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author henstockpv
 */
public class GeneAnnotations {
    private HashMap<Integer, ArrayList<GeneAnnotation>> annotations = new HashMap<Integer, ArrayList<GeneAnnotation>>();

    /**
     * Returns an arraylist of GeneAnnotation for the given chromosome betweent he startLoc
     * and endLoc.  If the appropriate chromosome file has not been loaded, then it has
     * to load it here and it will store it.
     * @param chromosome
     * @param startLoc
     * @param endLoc
     * @return 
     */
    public List<GeneAnnotation> getAnnotations(int chromosome, int startLoc, int endLoc) {
        if(! annotations.containsKey(chromosome)) {
            loadGeneAnnotationData(chromosome);
        }
        List<GeneAnnotation> annotationsInRange = computeRelevantAnnotations(chromosome, startLoc, endLoc);
        return annotationsInRange;
    }
    
    /**
     * Loads in teh appropriate annotations file for the specified chromosome
     * from teh data\\annotatedGenes folder.  Note that it puts the start as
     * the left component even though that may not always be the case
     * @param chromosome 
     */
    protected void loadGeneAnnotationData(int chromosome) {
        ArrayList<GeneAnnotation> geneAnnotations = new ArrayList<GeneAnnotation>();
        FileReader fr = null;
        BufferedReader br = null;
        try {
            //fr = new FileReader("c:\\Data\\Manhattadata\\annotatedGenes\\" + chromosome + ".txt");

            String fileSep = System.getProperty("file.separator");
            String path = System.getProperty("user.home") + fileSep + "My Documents" + fileSep + "gwava_data" + fileSep + "annotatedGenes" + fileSep;
            
            
            fr = new FileReader(path + "chromosome" + chromosome + ".txt");
            br = new BufferedReader(fr);
            String line;
            while((line = br.readLine()) != null) {
                String[] tokens = line.split("\\t");
                if(tokens.length != 5) {
                    System.out.println("Invalid num args: [" + line + "]");
                } else {
                    try {
                        int startLoc = Integer.parseInt(tokens[2]);
                        int endLoc   = Integer.parseInt(tokens[3]);
                        if(endLoc < startLoc) {
                            int temp = endLoc;
                            endLoc = startLoc;
                            startLoc = temp;
                        }
                        int direction = Integer.parseInt(tokens[4]);
                        GeneAnnotation annotation = new GeneAnnotation(tokens[0], (direction==1), startLoc, endLoc);
                        geneAnnotations.add(annotation);
                    } catch(NumberFormatException nfe) {
                        System.out.println("Failed to convert geneLoc for " + tokens[0]);
                    }
                }
            }
            annotations.put(chromosome, geneAnnotations);
        } catch(FileNotFoundException fnfe) {
            System.out.println("Cannot find gene data file for chromosome " + chromosome);
        } catch(IOException ioe) {
            System.out.println("IOException in loading gene data file for chromosome " + chromosome + "\n" + ioe.getMessage());
        } finally {
            try {
                if(br != null) {
                    br.close();
                }
                if(fr != null) {
                    fr.close();
                }
            } catch(IOException ioe) {
                System.out.println("Failed to close file for gene data for chromosome " + chromosome);
            }
        }
    }
    
    /**
     * Returns the list of GeneAnnotation in order for the specified chromosome in
     * the range [startLoc, endLoc].  It uses the fact that the geneAnnotations are
     * in order
     * @param chromosome
     * @param startLoc
     * @param endLoc
     * @return 
     */
    protected List<GeneAnnotation> computeRelevantAnnotations(int chromosome, int startLoc, int endLoc) {
        ArrayList<GeneAnnotation> fullSet = annotations.get(chromosome);
        int min = 0;
        int max = fullSet.size();
        min = binarySearch(min, max, startLoc, fullSet);
        int validMax = min;
        for(int i = min; i < max; i++) {
            if(fullSet.get(i).getStart() > endLoc) {
                break;
            } else {
                validMax = i;
            }
        }
        //max = binarySearch(min, max, endLoc, fullSet);
        return fullSet.subList(min, validMax+1);
    }
    
    /**
     * Finds the index of the gene in fullSet where its startLoc is the left
     * most value greater than searchVal for the indicies [min max].
     * @param min
     * @param max
     * @param startLoc
     * @param fullSet
     * @return 
     */
    protected int binarySearch(int min, int max, int searchVal, ArrayList<GeneAnnotation> fullSet) {
        int middle = (max + min)/2;
        int middleVal = fullSet.get(middle).getStart();
        while(middle > min) {
            if(middleVal <= searchVal) {
                min = middle;
                middle = (max + min)/2;
                middleVal = fullSet.get(middle).getStart();
            } else {
                max = middle;
                middle = (max + min)/2;
                middleVal = fullSet.get(middle).getStart();
            }
        }
        return middle;
    }
}
