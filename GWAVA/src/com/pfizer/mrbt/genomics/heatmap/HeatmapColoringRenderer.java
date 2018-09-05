/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.heatmap;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author henstockpv
 */
public class HeatmapColoringRenderer implements TableCellRenderer {

    private TableCellRenderer delegate;
    private static Color[] HEATMAP_COLORS;

    public HeatmapColoringRenderer(TableCellRenderer defaultRenderer) {
        this.delegate = defaultRenderer;
        HEATMAP_COLORS = initializeColors();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = delegate.getTableCellRendererComponent(table, value, isSelected,
                                                             hasFocus, row, column);
        if (!isSelected) {	   
            if(column==0) {
                c.setBackground(table.getBackground());
                c.setForeground(table.getForeground());
            } else if((Double) value < 0.0) {
                // negative values indicate nothing present
                c.setBackground(new Color(192,192,192));
                c.setForeground(new Color(192,192,192));
            } else {
                int colorIndex = (int) Math.floor((Double) value*0.9);
                if(colorIndex >= HEATMAP_COLORS.length) {
                    colorIndex = HEATMAP_COLORS.length - 1;
                }
                //System.out.println("Rendering " + value + "\t" + colorIndex + "\t" + HEATMAP_COLORS[colorIndex].getRGB());
                c.setBackground(HEATMAP_COLORS[colorIndex]);
                if(colorIndex > 5) {
                    c.setForeground(Color.WHITE);
                } else {
                    c.setForeground(Color.BLACK);
                }
            }
        } else {
            if(column==0) {
                c.setBackground(table.getBackground());
                c.setForeground(table.getForeground());
            } else if((Double) value < 0.0) {
                // negative values indicate nothing present
                c.setBackground(new Color(192,192,192));
                c.setForeground(new Color(192,192,192));
            } else {
                int colorIndex = (int) Math.floor((Double) value*0.9);
                if(colorIndex >= HEATMAP_COLORS.length) {
                    colorIndex = HEATMAP_COLORS.length - 1;
                }
                //System.out.println("Rendering " + value + "\t" + colorIndex + "\t" + HEATMAP_COLORS[colorIndex].getRGB());
                c.setBackground(HEATMAP_COLORS[colorIndex]);
                if(colorIndex > 5) {
                    c.setForeground(Color.WHITE);
                } else {
                    c.setForeground(Color.BLACK);
                }
            }
	    }           
        return c;
    }
    
    /**
     * Defines a 1-11 color map of yellow to green and also includes a gray
     * for the 0 value
     * @return 
     */
    protected static Color[] initializeColors() {
        Color[] colors = new Color[9];
        colors[0] = new Color(255, 255, 229);
        colors[1] = new Color(247, 252, 185);
        colors[2] = new Color(217, 240, 163);
        colors[3] = new Color(173, 221, 142);
        colors[4] = new Color(120, 198, 121);
        colors[5] = new Color( 65, 171,  93);
        colors[6] = new Color( 35, 132,  67);
        colors[7] = new Color(  0, 104,  55);
        colors[8] = new Color(  0,  69,  41);
        
        /*for(int i = 1; i < 11; i++) {
            colors[i] = new Color(265-10*i, 255, 0);
        }*/
        return colors;
    }
}