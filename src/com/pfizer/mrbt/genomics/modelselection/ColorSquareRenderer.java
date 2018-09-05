/**
 *
 */
package com.pfizer.mrbt.genomics.modelselection;

import java.awt.Color;
import java.awt.Component;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author Shreyas Dube; modified by Peter V. Henstock
 * @date Feb 19, 2009
 * @time 11:30:07 AM
 */
public class ColorSquareRenderer extends JComponent implements TableCellRenderer {
    private Color color = null;
    public static final int PREFERRED_WIDTH = 14;
    public static final int PREFERRED_HEIGHT = 14;    
    
  public ColorSquareRenderer(Color color) {
        super();
      this.color = color;
  }

  public ColorSquareRenderer() {
        super();
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus, int row,
                                                 int column) {
      if(value != null) {
        color = (Color) value;
      } else {
          color = null;
      }
    return this;
  }
  
  @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
        if(color != null) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(color);
            setOpaque(true);
            int height = getHeight();
            int width  = getWidth();
            g2.fillRect(0, 0, width, height);
        }
    }   

      
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT);
    }
}
