/*
 * Displays the genes and associated models loaded
 */
package com.pfizer.mrbt.genomics.modelselection;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.data.DataListener;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.data.SNP;
import com.pfizer.mrbt.genomics.state.State;
import com.pfizer.mrbt.genomics.state.StateListener;
import com.pfizer.mrbt.genomics.state.View;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author henstockpv
 */
public class ModelSelectionTableModel extends AbstractTableModel {
    private ArrayList<Integer> indicesToDisplay = new ArrayList<Integer>();
    private ArrayList<String> geneNames = new ArrayList<String>();
    private ArrayList<String> modelNames = new ArrayList<String>();
    private ArrayList<Float> scores = new ArrayList<Float>();
    private HashMap<String,Float> geneModel2value = new HashMap<String,Float>();
    private ArrayList<ResultsTableListener> listeners = new ArrayList<ResultsTableListener>();
    public final static int GENE_COL  = 0;
    public final static int COLOR_COL = 1;
    public final static int SCORE_COL = 2;
    public final static int MODEL_COL = 3;
    //private float negLogPvalThreshold = -1f;
    //private String geneModelFilterStr = "";
    public ModelSelectionTableModel() {
        DataController dataController = new DataController();
        Singleton.getDataModel().addListener(dataController);
        
        StateController stateController = new StateController();
        Singleton.getState().addListener(stateController);
        
        updateData();
    }
    
    protected void updateData() {
        geneNames.clear();
        modelNames.clear();
        indicesToDisplay.clear();        
        scores.clear();
        
        int lineIndex = 0;
        for(DataSet dataSet : Singleton.getDataModel().getDataSets()) {
            int modelIndex = 0;
            for(Model model : dataSet.getModels()) {
                geneNames.add(dataSet.getGeneRange().getName());
                //modelNames.add(model.toResultsString());
                modelNames.add(model.toString());
                
                ArrayList<SNP> snps = dataSet.getSnps();
                float maxSnp = Float.NEGATIVE_INFINITY;
                for(SNP snp : snps) {
                    //double dataSet.getPvalFromSnpModel(snp, model);
                    if(dataSet.getPvalFromSnpModel(snp, model) != null) {
                        maxSnp = (float) Math.max(maxSnp, dataSet.getPvalFromSnpModel(snp, model));
                    }
                }
                scores.add(maxSnp);
                //if(maxSnp > negLogPvalThreshold) {
                    indicesToDisplay.add(lineIndex++);
                //}
                modelIndex++;
            }
        }
        fireTableDataChanged();        
        fireResultsUpdated();
    }
    
    /**
     * Modifies indicesToDisplay based on the score
     */
    protected void filterResults(float negLogPValThreshold, String filterString) {
        indicesToDisplay.clear();
        int numRows = geneNames.size();
        String filterStr = ".*(?i)" + filterString + ".*";
        for(int i = 0; i < numRows; i++) {
            if(scores.get(i) > negLogPValThreshold && 
               (geneNames.get(i).matches(filterStr) ||
                modelNames.get(i).matches(filterStr))) {
                indicesToDisplay.add(i);
            }
        }
        fireTableDataChanged();
    }
    
    /**
     * Fills indicesToDisplay with all the rows
     */
    protected void showAllResults() {
        int numRows = geneNames.size();
        indicesToDisplay.clear();
        for(int i = 0; i < numRows; i++) {
            indicesToDisplay.add(i);
        }
        fireTableDataChanged();        
    }

     public String getFilterShowingStatusString() {
        return "Displaying " + indicesToDisplay.size() + " of " + modelNames.size();
    }

    
    /**
     * Returns a vector containing 0..numRows-1 of the row indices.  This is
     * intended to replace the selection with a select-all type of idea
     * @return 
     */
    public int[] getAllRows() {
        int numRows = getRowCount();
        int[] allRows = new int[numRows];
        for(int i = 0; i < numRows; i++) {
            allRows[i] = i;
        }
        return allRows;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int col) {
        int row = indicesToDisplay.get(rowIndex);
        if(col==GENE_COL) {
            return geneNames.get(row);
        } else if(col==MODEL_COL) {
            String shorterName = modelNames.get(row);
            shorterName = shorterName.replaceAll("\\(-log10 P-Value\\)", "");
            return shorterName;
        } else if(col==SCORE_COL) {
            return Math.round(scores.get(row)*10f)/10f;
        } else if(col==COLOR_COL) {
            State state = Singleton.getState();
            View mainView = Singleton.getState().getMainView();
            String geneName = mainView.getDataSet().getGeneRange().getName();
            if(geneNames.get(row).equals(geneName)) {
                int modelIndex = 0;
                if(mainView.getModels().size() > 1) {
                    for(Model model : mainView.getModels()) {
                        if(model.toString().equals(modelNames.get(row))) {
                            return state.getPlotColor(modelIndex);
                        }
                        modelIndex++;
                    } 
                } else {
                    for(Model model : mainView.getModels()) {
                        if(model.toString().equals(modelNames.get(row))) {
                            return Singleton.getUserPreferences().getPointColor();
                        } else {
                            return null;
                        }
                    } 
                }
            }
            return null;
        } else {
            return null;
        }
    }
    
    @Override
    public int getColumnCount() {
        return 4;
    }
    
    @Override
    public int getRowCount() {
        //System.out.println("Row count is " + modelNames.size() + "\t" + geneNames.size());
        //return modelNames.size();
        return indicesToDisplay.size();
    }
    

    @Override
    public String getColumnName(int column) {
        if(column==GENE_COL) {
            return "Gene";
        } else if(column==MODEL_COL) {
            return "Model";
        } else if(column==SCORE_COL) {
            return "Score";
        } else if(column==COLOR_COL) {
            return "Clr";
        } else {
            return "Unknown";
        }
    }
    
    @Override   
    public Class getColumnClass(int column) {
        switch(column) {
            case GENE_COL:  return String.class;
            case SCORE_COL:  return Float.class;
            case MODEL_COL: return String.class;
            case COLOR_COL: return Color.class;
            default:
                        return String.class;
        }
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
    
    
    public class DataController implements DataListener {
        @Override
        public void dataChanged(ChangeEvent ce) {
            updateData();
            System.out.println("Update data called");
            //fireTableDataChanged();
        }
    }
    
    public class StateController implements StateListener {
        @Override
        public void mainPlotChanged(ChangeEvent ce) {
            updateData();
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
        public void heatmapChanged(ChangeEvent ce) { }
        

    }
    
    public void addListener(ResultsTableListener rsl) {
        if(! listeners.contains(rsl)) {
            listeners.add(rsl);
        }
    }
    
    public void removeListener(ResultsTableListener rsl) {
        if(listeners.contains(rsl)) {
            listeners.remove(rsl);
        }
    }
    
    public void fireResultsUpdated() {
        ChangeEvent ce = new ChangeEvent(this);
        for(ResultsTableListener rsl : listeners) {
            rsl.resultsUpdated(ce);
        }
    }
}
