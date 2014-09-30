/*
 * Displays the genes and associated models loaded
 */
package com.pfizer.mrbt.genomics.modelselection;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.data.DataListener;
import com.pfizer.mrbt.genomics.data.GeneModelScore;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.state.State;
import com.pfizer.mrbt.genomics.state.StateListener;
import com.pfizer.mrbt.genomics.state.View;
import java.awt.Color;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author henstockpv
 */
public class ModelSelectionTableModel extends AbstractTableModel {
    private final CopyOnWriteArrayList<Integer> indicesToDisplay = new CopyOnWriteArrayList<Integer>();
    //private ArrayList<String> geneNames = new ArrayList<String>();
    //private ArrayList<String> modelNames = new ArrayList<String>();
    //private ArrayList<Float> scores = new ArrayList<Float>();
    private final ArrayList<ResultsTableListener> listeners = new ArrayList<ResultsTableListener>();
    private int numRows = 0;
    public final static int GENE_COL  = 0;
    public final static int COLOR_COL = 1;
    public final static int SCORE_COL = 2;
    public final static int MODEL_COL = 3;
    public final static int INDEX_COL = 4;
    
    private int lastOrigMaxIndexIncluded = 0;

    //private float negLogPvalThreshold = -1f;
    //private String geneModelFilterStr = "";
    public ModelSelectionTableModel() {
        DataController dataController = new DataController();
        Singleton.getDataModel().addListener(dataController);
        
        StateController stateController = new StateController();
        Singleton.getState().addListener(stateController);
        
        processChangedData();
    }
    
    /**
     * Retrieves a brand new set of data from the DataModel and updates the
     * indicesToDisplay to show all of that data.  Modifies numRows and
     * lastOrigMaxIndexincluded to reflect the full number of rows
     */
    protected synchronized void processChangedData() {
        try {
            numRows = Singleton.getDataModel().getGeneModelScoreListSize();
            indicesToDisplay.clear();
            for(int i = 0; i < numRows; i++) {
                indicesToDisplay.add(i);
            }
            numRows = indicesToDisplay.size();
            lastOrigMaxIndexIncluded = numRows;
            fireTableDataChanged();
            fireResultsUpdated();
        } catch (Exception ex) {
            System.err.println("processChangedData out of bounds");
            ex.printStackTrace();
        }
        System.err.println("Changed led to #rows" + numRows + "\tlastOrigMax " + lastOrigMaxIndexIncluded);
    }
    
    /**
     * Updates indicesToDisplay by adding the rows from lastOrigMaxIndexIncluded
     * to the new number of rows available from the GeneModelScoreListSize.  
     * Updates the lastOrigMaxIndexIncluded to the new number of rows availalbe
     * and updates numRows to the size displayed.  It fires a tableRowsInserted.
     */
    protected synchronized void processAddedData() {
        try {
            int newLen = Singleton.getDataModel().getGeneModelScoreListSize();
            for(int i = lastOrigMaxIndexIncluded; i < newLen; i++) {
                indicesToDisplay.add(i);
            }
            if(newLen > lastOrigMaxIndexIncluded) { //nothing udpated
                int startRowInserted = numRows - (newLen - lastOrigMaxIndexIncluded);
                numRows = indicesToDisplay.size();
                lastOrigMaxIndexIncluded = newLen;
                System.out.println("Inserting rows " + (startRowInserted) + " to " + Math.max(startRowInserted, (numRows-1)) + "\tgiven " + newLen + "\t" +  lastOrigMaxIndexIncluded);
                System.out.println("Updating model selection table from " + lastOrigMaxIndexIncluded + " to " + newLen + "\tnumRows= " + numRows);
            
                fireTableRowsInserted(startRowInserted, Math.max(startRowInserted, (numRows-1))); // kluge
                fireResultsUpdated();
            }
        } catch (Exception ex) {
            System.err.println("processAddedData out of bounds");
            ex.printStackTrace();
        }
    }
    
    /**
     * Modifies indicesToDisplay based on the score
     */
    /*protected void filterResultsOld(float negLogPValThreshold, String filterString) {
        indicesToDisplay.clear();
        int totalRows = geneModelScoreList.size();
        String filterStr = ".*(?i)" + filterString + ".*";
        for(int i = 0; i < totalRows; i++) {
            GeneModelScore oneGeneModelScore = geneModelScoreList.get(i);
            if(oneGeneModelScore.getScore() > negLogPValThreshold && 
               (oneGeneModelScore.getGeneName().matches(filterStr) ||
                oneGeneModelScore.getModelName().matches(filterStr))) {
                indicesToDisplay.add(i);
            }
        }
        numRows = indicesToDisplay.size();
        fireTableDataChanged();
    }*/
    
    public synchronized void removeAll() {
        numRows = 0;
        indicesToDisplay.clear();
        processChangedData();
    }
    
    /**
     * Modifies indicesToDisplay based on the score
     */
    protected synchronized void filterResults(float negLogPValThreshold, String filterString) {
        indicesToDisplay.clear();
        int dataRowsAvailable = Singleton.getDataModel().getGeneModelScoreListSize();
        String filterStr = ".*(?i)" + filterString + ".*";
        for(int i = 0; i < dataRowsAvailable; i++) {
            GeneModelScore oneGeneModelScore = Singleton.getDataModel().getGeneModelScore(i);
            if(oneGeneModelScore.getScore() > negLogPValThreshold && 
               (oneGeneModelScore.getGeneName().matches(filterStr) ||
                oneGeneModelScore.getModelName().matches(filterStr))) {
                indicesToDisplay.add(i);
            }
        }
        numRows = indicesToDisplay.size();
        lastOrigMaxIndexIncluded = dataRowsAvailable;

        fireTableDataChanged();
    }
    
    /**
     * Fills indicesToDisplay with all the rows
     */
    protected void showAllResults() {
        int totalNumRows = Singleton.getDataModel().getGeneModelScoreListSize();
        indicesToDisplay.clear();
        for(int i = 0; i < totalNumRows; i++) {
            indicesToDisplay.add(i);
        }
        numRows = indicesToDisplay.size();
        lastOrigMaxIndexIncluded = numRows;
        fireTableDataChanged();        
    }

     public String getFilterShowingStatusString() {
        return "Displaying " + indicesToDisplay.size() + " of " + Singleton.getDataModel().getGeneModelScoreListSize();
    }

    
    /**
     * Returns a vector containing 0..numRows-1 of the row indices. This is
     * intended to replace the selection with a select-all type of idea
     * @return
     */
    public int[] getAllRows() {
        int totNumRows = getRowCount();
        int[] allRows = new int[totNumRows];
        try {
            for (int i = 0; i < totNumRows; i++) {
                allRows[i] = i;
            }
        } catch (Exception ex) {
            System.err.println("GetAllRows out of bounds");
        } finally {
            return allRows;
        }
    }
    
    @Override
    public Object getValueAt(int rowIndex, int col) {
        int row = 0;
        try {
            if(numRows <= rowIndex) {
                System.err.println("Err:  ind2DisplayLen " + numRows + "\t" + rowIndex);
            }
            row = indicesToDisplay.get(rowIndex);
            if (col == GENE_COL) {
                //return geneNames.get(row);
                //return geneModelScoreList.get(row).getGeneName();
                return Singleton.getDataModel().getGeneModelScore(row).getGeneName();
            } else if (col == MODEL_COL) {
                //return modelNames.get(row);
                //return geneModelScoreList.get(row).getModelName();
                return Singleton.getDataModel().getGeneModelScore(row).getModelName();
                //System.out.println("shorter name : " + shorterName);
                //shorterName = shorterName.replaceAll("\\(-log10 P-Value\\)", "");
                //return shorterName;
            } else if (col == SCORE_COL) {
                //return Math.round(scores.get(row) * 10f) / 10f;
                return Singleton.getDataModel().getGeneModelScore(row).getScore();
                //return geneModelScoreList.get(row).getScore();
            } else if (col == COLOR_COL) {
                String rowGeneName = Singleton.getDataModel().getGeneModelScore(row).getGeneName();
                String rowModelName   = Singleton.getDataModel().getGeneModelScore(row).getModelName();
                State state = Singleton.getState();
                View mainView = Singleton.getState().getMainView();
                String geneName = mainView.getDataSet().getGeneRange().getName();
                if (rowGeneName.equals(geneName)) {
                    int modelIndex = 0;
                    if (mainView.getModels().size() > 1) {
                        for (Model model : mainView.getModels()) {
                            if (model.toString().equals(rowModelName)) {
                                return state.getPlotColor(modelIndex);
                            }
                            modelIndex++;
                        }
                    } else {
                        for (Model model : mainView.getModels()) {
                            if (model.toString().equals(rowModelName)) {
                                return Singleton.getUserPreferences().getPointColor();
                            } else {
                                return null;
                            }
                        }
                    }
                }
                return null;
            } else if (col == INDEX_COL) {
                return (row + 1);
            } else {
                return null;
            }
        } catch (IndexOutOfBoundsException ioobe) {
            System.err.println("Caught Index out of bounds exception (" + rowIndex + "\t" + col + "\t" + numRows + "\t" + Singleton.getDataModel().getGeneModelScoreListSize()+ "\t" + row  + ")");
            return null;
        } catch (Exception ex) {
            System.err.println("ModelSelectionTableModel exception (" + rowIndex + "\t" + col + ")");
            return null;
        }
    }
    
    @Override
    public int getColumnCount() {
        return 5;
    }
    
    @Override
    public int getRowCount() {
        //return indicesToDisplay.size();
        return numRows;
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
        } else if(column==INDEX_COL) {
            return "Idx";
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
            case INDEX_COL: return Integer.class;
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
            System.out.println("Data changed");
            processChangedData();
        }
        
        @Override
        public void dataAdded(ChangeEvent ce) {
            System.out.println("Data added");
            processAddedData();
        }
    }
    
    public class StateController implements StateListener {
        @Override
        public void mainPlotChanged(ChangeEvent ce) {
            fireTableDataChanged(); // changes colors selected
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
    
    
    public class TableData {
        private String geneName;
        private String modelName;
        private double score;
        public TableData(String geneName, String modelName, double score) {
            this.geneName = geneName;
            this.modelName = modelName;
            this.score = score;
        }
    }
}
