/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.annotation;

import com.pfizer.mrbt.genomics.axisregion.AxisRegionYLeft;
import com.pfizer.mrbt.genomics.axisregion.AxisRegionYRight;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JScrollPane;

/**
 *
 * @author henstockpv
 */
public class RightAnnotationCorner extends AnnotationCorner {
    public final static int PREFERRED_WIDTH = 55;
    private JScrollPane scrollPane;
    public RightAnnotationCorner(JScrollPane lowerPaneScrollPane) {
        super();
        this.scrollPane = lowerPaneScrollPane;
    }
    
    @Override
    public Dimension getMinimumSize() {
        //return new Dimension(AxisRegionYRight.PREFERRED_WIDTH, AxisRegionYRight.PREFERRED_WIDTH);
        return new Dimension(PREFERRED_WIDTH, AxisRegionYRight.PREFERRED_WIDTH);
    }

    @Override
    public Dimension getMaximumSize() {
        //return new Dimension(AxisRegionYRight.PREFERRED_WIDTH, Integer.MAX_VALUE);
        return new Dimension(PREFERRED_WIDTH, Integer.MAX_VALUE);
    }
    
    @Override
    public Dimension getPreferredSize() {
        System.out.println("Min size right " + AxisRegionYLeft.PREFERRED_WIDTH);
        //return new Dimension(AxisRegionYLeft.PREFERRED_WIDTH, AxisRegionYLeft.PREFERRED_WIDTH);
        return new Dimension(PREFERRED_WIDTH, 100);
    }
    

    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.PINK);
        System.out.println("Drawing right annotation corner " + getWidth() + "\t" + getHeight());
        if(scrollPane == null || scrollPane.getVerticalScrollBar()==null) {
            System.out.println("Scrollpane null");
        } else {
            System.out.println("ScrollPane " + scrollPane.getVerticalScrollBar().getWidth() + "\t" + 
                               scrollPane.getVerticalScrollBar().isVisible());
        }
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, getWidth(), getHeight());
    }
    
    
    
}
