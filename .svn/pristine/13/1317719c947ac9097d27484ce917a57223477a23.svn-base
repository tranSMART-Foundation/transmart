/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.legend;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.data.Model;
import java.awt.Color;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author henstock
 */
public class LegendTableModel extends AbstractTableModel {
    public final static int COLOR_COL = 0;
    public final static int ORIG_COL  = 1;
    public final static int DISPLAY_COL = 2;
    private String[] headers = {"Clr", "Original Name", "Display Name"};
    
    @Override
    public Object getValueAt(int row, int col) {
        switch(col) {
            case COLOR_COL:
                return Singleton.getState().getPlotColor(row);
            case ORIG_COL:
                return Singleton.getState().getMainView().getModel(row).toString();
            case DISPLAY_COL:
                Model model = Singleton.getState().getMainView().getModel(row);
               return Singleton.getState().getLegendFromModel(model);
            default:
                return "Unknown";
        }
    }
        
    @Override
    public int getRowCount() {
        return Singleton.getState().getMainView().getModels().size();
    }
    
    @Override
    public int getColumnCount() {
        return 3;
    }
    
    @Override
    public Class getColumnClass(int column) {
        switch(column) {
            case COLOR_COL:
                return Color.class;
            case ORIG_COL:
                return String.class;
            case DISPLAY_COL:
               return String.class;
            default:
                return String.class;
        }
    }
    
    @Override
    public String getColumnName(int column) {
        return headers[column];
    }
}
