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
    private ArrayList<History> history = new ArrayList<History>();
    
    public HistoryTableModel() {
        
    }
    
    public Object getValueAt(int row, int col) {
        switch(col) {
                case History.GENE_COL:
                    return history.get(row).getGene();
                case History.MODELS_COL:
                    return history.get(row).getNumModels();
                case History.NUM_SNP_COL:
                    return history.get(row).getNumSnp();
                case History.RANGE_COL:
                    return history.get(row).getRange();
                case History.SECONDS_COL:
                    return history.get(row).getElapsedTime();
                case History.STATUS_COL:
                    return history.get(row).getStatusStr();
                default:
                    return "Unknown";
        }
    }
    
    @Override
    public int getColumnCount() {
        return 6;
    }
    
    @Override
    public int getRowCount() {
        return history.size();
    }
    
    public void clear() {
        history.clear();
        this.fireTableDataChanged();        
    }
    
    @Override
    public String getColumnName(int column) {
        switch(column) {
                case History.GENE_COL:
                    return "Gene Requested";
                case History.MODELS_COL:
                    return "#Models Requested";
                case History.NUM_SNP_COL:
                    return "#SNP Returned";
                case History.RANGE_COL:
                    return "Search Range";
                case History.SECONDS_COL:
                    return "Elapsed Sec.";
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
        this.history.add(historyElement);
        fireTableDataChanged();
    }
    
    public void setHistory(int index, History historyElement) {
        this.history.set(index, historyElement);
        this.fireTableDataChanged();
    }
    
    public History getHistory(int index) {
        return history.get(index);
    }
    
    /**
     * Returns the first index into history where the gene matches and the
     * status is WORKING. If none are found it returns -1
     * @param gene
     * @return 
     */
    public int findFirstWorking(String gene) {
        int index = 0;
        for(History oneHistory : history) {
            if( oneHistory.getGene().equals(gene) &&
               oneHistory.getStatus() == History.WORKING) {
                return index;
            }
            index++;
        }
        return -1;
    }
    
}
