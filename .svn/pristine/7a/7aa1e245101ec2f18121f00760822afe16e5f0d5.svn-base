/*
 * Displays the genes and associated models loaded
 */
package com.pfizer.mrbt.genomics.heatmap;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.data.DataListener;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.GeneAnnotation;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.data.SNP;
import com.pfizer.mrbt.genomics.state.StateListener;
import com.pfizer.mrbt.genomics.state.ViewData;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author henstockpv
 */
public class HeatmapTableModel extends AbstractTableModel {
    private Collection<DataSet> dataSets;
    private ArrayList<String> geneNames = new ArrayList<String>();
    private ArrayList<String> modelNames = new ArrayList<String>();
    private DecimalFormat formatter = new DecimalFormat("0.00");
    private HashMap<String,Double> geneModel2value = new HashMap<String,Double>();
    private JTable heatmapTable = null;  // kluge to get access to table for updaing columns
    
    public final static int MODEL_COL = 0;
    
    public HeatmapTableModel() {
        tempPopulateData();
        
        DataController dataController = new DataController();
        Singleton.getDataModel().addListener(dataController);
        
        StateController stateController = new StateController();
        Singleton.getState().addListener(stateController);
        
        updateData();
    }
    
    protected void tempPopulateData() {
        geneNames.add("a");
        geneNames.add("b");
        geneNames.add("c");
        modelNames.add("1");
        modelNames.add("2");
        modelNames.add("3");
        for(String geneName : geneNames) {
            for(String modelName : modelNames) {
                String key = geneName + "__" + modelName;
                geneModel2value.put(key, Math.random());
            }
        }
        this.fireTableDataChanged();
    }
    
    @Override
    public Object getValueAt(int row, int col) {
        //System.out.println("Value at " + row + "\t" + col);
        if(col==0) {
            return modelNames.get(row);
        } else {
            String key = geneNames.get(col-1) + "__" + modelNames.get(row);
            Object value = geneModel2value.get(key);
            if(value == null) {
                return -1.0;
            } else {
                //return formatter.format(geneModel2value.get(key));
                Double dblvalue = (Double) value;
                dblvalue = Math.round(dblvalue*100.0) / 100.0;
                return dblvalue;
            }
        }
    }
    
    public void updateData() {
        geneNames.clear();
        modelNames.clear();
        geneModel2value.clear();
        HashMap<String,Range> gene2range = new HashMap<String,Range>();
        ArrayList<ViewData> viewDatas = Singleton.getState().getHeatmapViewData();
        for(ViewData viewData : viewDatas) { // list of gene-models provided to state
            DataSet dataSet = viewData.getDataSet();
            String geneName = dataSet.getGeneRange().getName();
            if(! gene2range.containsKey(geneName)) {
                Range range = extractGeneRange(dataSet, geneName);
                if(range == null) {
                    // maybe it's a SNP rather than an annotation
                    int loc = dataSet.getSnpLocation(geneName);
                    gene2range.put(geneName, new Range(loc,loc));
                } else {
                    gene2range.put(geneName, range);
                }
            }
            if(! geneNames.contains(geneName)) {
                // growing list of the unique geneNames (columns)
                //System.out.println("Adding gene " + geneName);
                geneNames.add(geneName);
            }
            for(Model model : viewData.getModels()) {
                String modelName = model.toString();
                if(! modelNames.contains(modelName)) {
                    // growing list of unique modelNames (rows)
                    //System.out.println("Adding model " + modelName);
                    modelNames.add(modelName);
                }
                String key = geneName + "__" + modelName;
                
                double maxpval = Double.MIN_VALUE;
                
                // store a function of the snps here that contain pvals over
                // the appropriate range where we have to sort these and/or
                // apply a function to them
                HeatmapParameters heatmapParams = Singleton.getState().getHeatmapParameters();
                ArrayList<Double> pvals = new ArrayList<Double>();
                Range currGeneRange = gene2range.get(geneName);
                for(SNP snp : dataSet.getSnps()) {
                    int loc = snp.getLoc();
                    if(loc >= currGeneRange.getLeft() - heatmapParams.getRadius() &&
                       loc <= currGeneRange.getRight() + heatmapParams.getRadius()) {
                        Double pval = dataSet.getPvalFromSnpModel(snp, model);
                        if(pval != null) {
                            pvals.add(pval);
                        }
                    }
                }
                double heatmapValue = computeHeatmapValue(pvals, heatmapParams);
                geneModel2value.put(key, heatmapValue);
            }
        }
        this.fireTableStructureChanged();
    }
    
    /**
     * Looks up the range spanned by gene in dataSet.getGeneAnnotations().  If
     * it is not found, it returns null;
     */
    protected Range extractGeneRange(DataSet dataSet, String gene) {
        for(GeneAnnotation geneAnnotation : dataSet.getGeneAnnotations()) {
            if(geneAnnotation.getGene().equals(gene)) {
                return new Range(geneAnnotation.getStart(), geneAnnotation.getEnd());
            }
        }
        System.out.println("!Failed to get the gene range from the data set for gene " + gene);
        return null;
    }
    
    /**
     * Applies the function and topN to the pvals that are already with the required range
     */
    protected double computeHeatmapValue(ArrayList<Double> pvals, HeatmapParameters heatmapParams) {
        Collections.sort(pvals, Collections.reverseOrder());
        if(pvals.isEmpty()) {
            return 0.0;
        }
        /*System.out.println("Compute heatmap: " + heatmapParams.getFunction() + "\t" + heatmapParams.getTopn() + "\t" + heatmapParams.getRadius());;
        System.out.println("TopN = " + heatmapParams.getTopn() + " with " + pvals.size());
        for(int i = 0; i < pvals.size() && i < 10; i++)
            System.out.print("i->" + pvals.get(i) + " ");
        System.out.println("");*/
        switch(heatmapParams.getFunction()) {
            case HeatmapParameters.FUNCTION_MAXIMUM:
                //System.out.println("Max of " + pvals.size() + "\t" + pvals.get(0));
                return pvals.get(0);
            case HeatmapParameters.FUNCTION_MINIMUM:
                int minLen = Math.min(pvals.size(), heatmapParams.getTopn());
                //System.out.println("Min of " + minLen + "\t" + pvals.get(minLen-1));
                return pvals.get(minLen-1);
            case HeatmapParameters.FUNCTION_AVERAGE:
                double mean = 0.0;
                int cnt = 0;
                minLen = Math.min(pvals.size(), heatmapParams.getTopn());
                for(int i = 0; i < minLen; i++) {
                    mean += pvals.get(i);
                    cnt++;
                }
                //System.out.println("Mean of " + minLen + "\t" + (mean/cnt) + " given cnt=" + cnt);
                return mean/cnt;
            case HeatmapParameters.FUNCTION_MEDIAN:
                minLen = Math.min(pvals.size(), heatmapParams.getTopn());
                double median = 0.0;
                if(minLen%2 == 0) {
                    median = (pvals.get(minLen/2) + pvals.get(minLen/2-1)) / 2.0;
                } else {
                    median =  pvals.get((minLen-1)/2);
                }
                //System.out.println("Median of " + minLen + "\t" + median);
                return median;
            default:
                System.out.println("Unknown heatmap function value");
                return -1.0;
        }
    }
    
    @Override
    public int getColumnCount() {
        return geneNames.size()+1;
    }
    
    @Override
    public int getRowCount() {
        return modelNames.size();
    }
    

    @Override
    public String getColumnName(int column) {
        if(column==0) {
            return "Model";
        }
        return geneNames.get(column-1);
    }
    
    @Override   
    public Class getColumnClass(int column) {
        return String.class;
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
    
    
    public class DataController implements DataListener {
        @Override
        public void dataChanged(ChangeEvent ce) {
            updateData();
            fireTableDataChanged();
        }
    }
    
    public void setTable(JTable tbl) {
        this.heatmapTable = tbl;
    }
    
    public class StateController implements StateListener {
        @Override
        public void mainPlotChanged(ChangeEvent ce) {
            //fireTableDataChanged();
        }
        @Override
        public void currentChanged(ChangeEvent ce) {   }

        @Override
        public void thumbnailsChanged(ChangeEvent ce) {   }

        @Override
        public void currentAnnotationChanged(ChangeEvent ce) { }

        @Override
        public void selectedAnnotationChanged(ChangeEvent ce) { }

        @Override
        public void averagingWindowChanged(ChangeEvent ce) {    }
        
        @Override
        public void legendSelectedRowChanged(ChangeEvent ce) { }
        
        @Override
        public void heatmapChanged(ChangeEvent ce) { 
            //updateData();
        }
        

    }
    
    public class Range {
        private int left;
        private int right;
        public Range(int left, int right) {
            this.left = left;
            this.right = right;
        }

        public int getLeft() {
            return left;
        }

        public int getRight() {
            return right;
        }
        
    }
}
