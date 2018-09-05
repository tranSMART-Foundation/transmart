/*
 * Utility storage class to represent the access to a single point.
 * It includes the filename that it came from, the column (studySetModel)
 * and the integer index into the snpIndex
 */
package com.pfizer.mrbt.genomics.data;


/**
 *
 * @author henstock
 */
public class DataPointEntry {
      private DataSet dataSet;
      private Model model;
      private SNP snp;
      
      public DataPointEntry(DataSet dataSet, Model model, SNP snp) {
            this.dataSet   = dataSet;
            this.model     = model;
            this.snp       = snp;
      }

      public DataSet getDataSet() {
            return dataSet;
      }

      public Model getModel() {
            return model;
      }

      public SNP getSnp() {
            return snp;
      }
      
      /**
       * Returns true if it matches all the entries
       * @param filename
       * @param studySetModelIndex
       * @param snpIndex
       * @return 
       */
      public boolean equals(DataPointEntry dataPointEntry) {
          if(this.model.getId() == dataPointEntry.getModel().getId() &&
             this.snp.getRsId() == dataPointEntry.getSnp().getRsId() &&
             this.dataSet.getId() == dataPointEntry.getDataSet().getId()) {
              return true;
          } else {
              return false;
          }
      }
      
      @Override
      public String toString() {
          String str = "Set " + this.dataSet.getId() + " Snp: " + snp.getRsId() + " model " + this.getModel().getId();
          return str;
      }
      
}
