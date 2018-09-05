/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.state;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author henstockpv
 */
public class HistoryTableModel extends AbstractTableModel {
    private ArrayList<History> historyList = new ArrayList<History>();
    
    public HistoryTableModel() {
        
    }
    
    public Object getValueAt(int row, int col) {
        try {
        switch(col) {
                case History.GENE_COL:
                    return historyList.get(row).getGene();
                case History.MODEL_COUNT_COL:
                    return historyList.get(row).getNumModels();
                case History.MODELS_COL:
                    return historyList.get(row).getModelOptionsString();
                case History.NUM_SNP_COL:
                    return historyList.get(row).getNumSnp();
                case History.RANGE_COL:
                    return historyList.get(row).getRange();
                case History.SECONDS_COL:
                    return historyList.get(row).getElapsedTime();
                case History.STATUS_COL:
                    return historyList.get(row).getSearchStatusStr();
                default:
                    return "Unknown";
        }
        } catch(Exception ex) {
            System.err.println("History table exception " + row + "\t" + col);
            return null;
        }
    }
    
    @Override
    public int getColumnCount() {
        return 7;
    }
    
    @Override
    public int getRowCount() {
        return historyList.size();
    }
    
    public void clear() {
        historyList.clear();
        this.fireTableDataChanged();        
    }
    
    @Override
    public String getColumnName(int column) {
        switch(column) {
                case History.GENE_COL:
                    return "Gene/SNP";
                case History.MODELS_COL:
                    return "Models Requested";
                case History.MODEL_COUNT_COL:
                    return "#Models";
                case History.NUM_SNP_COL:
                    return "#SNPs";
                case History.RANGE_COL:
                    return "Range";
                case History.SECONDS_COL:
                    return "Sec.";
                case History.STATUS_COL:
                    return "Status";
                default:
                    return "Unknown";
        }
    }
    
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
    
    @Override
    public Class getColumnClass(int column) {
        switch(column) {
                case History.GENE_COL:
                    return String.class;
                case History.STATUS_COL:
                    return String.class;
                default:
                    return Integer.class;
        }
    }
    
    public void addHistory(History historyElement) {
        this.historyList.add(historyElement);
        fireTableDataChanged();
    }
    
    public void setHistory(int index, History historyElement) {
        setHistoryList(index, historyElement);
        this.fireTableDataChanged();
    }
    
    public synchronized void setHistoryList(int index, History historyElement) {
        this.historyList.set(index, historyElement);
    }
    
    public History getHistory(int index) {
        return historyList.get(index);
    }
    
    /**
     * Returns the first index into history where the gene matches and the
     * status is WORKING. If none are found it returns -1
     * @param gene is the gene name that you're seeking
     * @param seekSearchStatus is the search status that you're seeking
     * @return 
     */
    public int findFirstInTable(String gene, SearchStatus seekSearchStatus) {
        int index = 0;
        for(History oneHistory : historyList) {
            if( oneHistory.getGene().equalsIgnoreCase(gene) &&
               oneHistory.getSearchStatus() == seekSearchStatus) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Returns the row index that corresponds to history queryId
     * @param queryId
     * @return 
     */
    public int findRowByQueryId(int queryId) {
        int index = 0;
        for(History oneHistory : historyList) {
            if( oneHistory.getQueryId() == queryId) {
                return index;
            }
            index++;
        }
        return -1;
    }
    
}
