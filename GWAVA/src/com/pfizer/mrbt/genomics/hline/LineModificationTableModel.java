/*
 * Table model to support the markers for the given plot.  It is used in the
 * MarkerModificationPane as the primary display.
 */

package com.pfizer.mrbt.genomics.hline;

import com.pfizer.mrbt.genomics.Singleton;
import java.awt.Color;
import java.text.DecimalFormat;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Peter V. Henstock
 */
public class LineModificationTableModel extends AbstractTableModel {
    public final static int NAME_COL  = 0;
    public final static int YVAL_COL  = 1;
    public final static int SCOPE_COL = 2;
    public final static int STYLE_COL = 3;
    public final static int COLOR_COL = 4;
    
    private DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private String[] headers = {"Name","Log10 PValue","Scope", "Style","Color"};

    @Override
    public Object getValueAt(int row, int col) {

        HLine line = Singleton.getState().getLines().get(row);
        switch(col) {
            case NAME_COL:
                return line.getLineName();
            case YVAL_COL:
                return decimalFormat.format(line.getyValue());
            case SCOPE_COL:
                return HLine.scopeOptions[line.getLineScope()];
            case STYLE_COL:
                return HLine.lineStyleOptions[line.getLineStyle()];
            case COLOR_COL:
                return line.getLineColor();
            default:
                return "Unknown";
        }
    }

    @Override
    public int getColumnCount() {
        return headers.length;
    }

    @Override
    public int getRowCount() {
        int numRows = Singleton.getState().getLines().size();
        return numRows;
    }

    @Override
    public Class getColumnClass(int column) {
        if(column < 4) {
            return String.class;
        } else {
            return Color.class;
        }
    }

    @Override
    public String getColumnName(int column) {
        return headers[column];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}
