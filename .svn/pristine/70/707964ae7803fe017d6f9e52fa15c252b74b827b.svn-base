/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.thumbnail;

import com.pfizer.mrbt.genomics.Singleton;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author henstockpv
 */
public class ThumbnailTableModel extends AbstractTableModel {
    private int numCols = 4;
    
    /**
     * Returns the ViewData for the given plot that will have one 
     * model associated with it.  The renderer uses the viewData
     * to create a new plot.  The numCols are fixed and it's a
     * top-to-bottom, left-to-right ordering until done.  If out of
     * range it returns null
     * @param row
     * @param col
     * @return 
     */
    @Override
    public Object getValueAt(int row, int col) {
        int index = row*numCols + col;
        return Singleton.getState().getThumbnailViewData(index);
    }
    
    @Override
    public int getColumnCount() {
        return numCols;
    }
    
    @Override
    public int getRowCount() {
        int numThumbnails = Singleton.getState().getNumThumbnails();
        if(numThumbnails == 0) {
            return 0;
        } else {
            return (int) Math.ceil(numThumbnails * 1.0 / numCols);
        }
    }
    
    public void setColumnCount(int numColumns) {
        if(this.numCols != numColumns) {
            this.numCols = numColumns;
            fireTableStructureChanged();
        }
    }
    
}
