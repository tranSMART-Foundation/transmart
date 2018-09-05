/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.resultstable;

import com.pfizer.mrbt.genomics.data.Model;

/**
 *
 * @author henstock
 */
public class GeneModelSnpOffset {
          private String gene;
          private Model model;
          private int geneModelIndex = -1;
          private int offset;
          public GeneModelSnpOffset(String gene, Model model, int geneModelIndex, int offset) {
                    this.gene = gene;
                    this.model = model;
                    this.geneModelIndex = geneModelIndex;
                    this.offset = offset;
          }

          /**
           * Returns the gene corresponding to this
           */
          public String getGene() {
                    return gene;
          }

          /**
           * Returns the model
           * @return 
           */
          public Model getModel() {
                    return model;
          }

          /**
           * Returns the offset in the list of SNPs
           * @return 
           */
          public int getOffset() {
                    return offset;
          }
          
          /**
           * Gene-models are stored as an arrayList so this provides an index
           * into the appropriate one from which the SNP can be obtained
           * @return 
           */
          public int getGeneModelIndex() {
              return geneModelIndex;
          }

}
