/*
 * This class handles the exporting of the full data set and possibly various
 * incarnations of the export of data such that it can be re-loaded.
 */
package com.pfizer.mrbt.genomics.data;

import com.pfizer.mrbt.genomics.Singleton;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author henstockpv
 */
public class DataExporter {
    private HashMap<Model,Integer> model2col = new HashMap<Model,Integer>();
    private List<Model> allModels;
    private int numModels;
    
    /**
     * Exports all the loaded genes and models to a file in the same format
     * that the GWAS provides (although some columns will have no information)
     * @param filename 
     */
    public void exportAllData(String filename) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            fw = new FileWriter(filename);
            bw = new BufferedWriter(fw);
            computeAllModels();
            exportHeader(bw);
            exportDataSets(bw);
        } catch (IOException fnfe) {
            System.out.println("IO Exception exporting data for file " + filename);
            
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException ioe) {
                System.out.println("IO Exception failed to close file " + filename);
            }
        }
    }
    
    /**
     * Fills model2col, allModels, and numModels of the class variable with the
     * set of models that occur throughout all the data sets.
     */
    protected void computeAllModels() {
        model2col.clear();
        allModels.clear();
        numModels = 0;
        for(DataSet dataSet : Singleton.getDataModel().getDataSets()) {
            for(Model model : dataSet.getModels()) {
                if(! model2col.containsKey(model)) {
                    model2col.put(model, numModels++);
                    allModels.add(model);
                }
            }
        }
    }
    
    /**
     * Exports a header line with the standard headers plus all the model headers
     * @param bw
     * @throws IOException 
     */
    protected void exportHeader(BufferedWriter bw)  throws IOException {
        bw.write("Entrez Gene ID\t");
        bw.write("GeneId\t");
        bw.write("Chr\t");
        bw.write("Start\t");
        bw.write("Stop\t");
        bw.write("Strand\t");
        bw.write("rs Id\t");
        bw.write("SNP start\t");
        bw.write("SNP stop\t");
        bw.write("SNP class\t");
        bw.write("SNP func\t");
        for(Model model : allModels) {
            bw.write(model.toString() + "\t");
        }
        bw.write("\n");
    }
    
    protected void exportDataSets(BufferedWriter bw) throws IOException {
        for(DataSet dataSet : Singleton.getDataModel().getDataSets()) {
            for(SNP snp : dataSet.getSnps()) {
                bw.write("\t"); // skip entrez-gene
                bw.write("\t"); // skip geneId
                bw.write(dataSet.getGeneRange().getName() + "\t");
                bw.write(dataSet.getChromosome() + "\t");
                bw.write(dataSet.getGeneRange().getStart() + "\t"); // may need to change this
                bw.write(dataSet.getGeneRange().getEnd() + "\t"); // may need to change this
                bw.write("\t"); // strand
                bw.write(snp.getRsId() + "\t");
                bw.write(snp.getLoc() + "\t");
                bw.write(( snp.getLoc()+1) + "\t");
                bw.write("\t"); // strand
                bw.write("\t"); // snp class
                bw.write("\t"); // SNP func
                ArrayList<Double> pvalues = new ArrayList<Double>();
                for(Model model : dataSet.getModels()) {
                    double pval = dataSet.getPvalFromSnpModel(snp, model);
                    pvalues.set(model2col.get(model), pval);
                }
                for(Double pvalue : pvalues) {
                    bw.write(pvalue + "\t");
                }
                bw.write("\n");
            }
        }
    }
    
}
