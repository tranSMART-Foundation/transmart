/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.annotation;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.axisregion.AxisRegionYLeft;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 *
 * @author henstockpv
 */
public class LeftAnnotationCorner extends AnnotationCorner {
    public final static int PREFERRED_WIDTH = 55;
    public LeftAnnotationCorner() {
        super();
    }
    
    @Override
    public Dimension getMinimumSize() {
        System.out.println("Min size left " + AxisRegionYLeft.PREFERRED_WIDTH);
        //return new Dimension(AxisRegionYLeft.PREFERRED_WIDTH, AxisRegionYLeft.PREFERRED_WIDTH);
        return new Dimension(PREFERRED_WIDTH, AxisRegionYLeft.PREFERRED_WIDTH);
    }
    
   @Override
    public Dimension getPreferredSize() {
        System.out.println("Min size left " + AxisRegionYLeft.PREFERRED_WIDTH);
        //return new Dimension(AxisRegionYLeft.PREFERRED_WIDTH, AxisRegionYLeft.PREFERRED_WIDTH);
        return new Dimension(PREFERRED_WIDTH, 100);
    }
    
    @Override
    public Dimension getMaximumSize() {
        //return new Dimension(AxisRegionYLeft.PREFERRED_WIDTH, Integer.MAX_VALUE);
        return new Dimension(PREFERRED_WIDTH, Integer.MAX_VALUE);
    }
    
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        System.out.println("Drawing left annotation corner " + getWidth() + "\t" + getHeight() + "\tcompared to " );
        g2.setColor(Color.ORANGE);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, getWidth(), getHeight());
    }
    
    
}
