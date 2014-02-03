/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.axisregion;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.state.View;
import com.pfizerm.mrbt.axis.AxisScale;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import javax.swing.JComponent;

/**
 *
 * @author henstockpv
 */
public class LeftCornerSquare extends CornerSquare {
    public LeftCornerSquare(View view, AxisRegion xAxisRegion, AxisRegion yAxisRegion, AxisRegion yRightAxisRegion) {
        super(view, xAxisRegion, yAxisRegion, yRightAxisRegion);
    }
    
    @Override
    protected void drawYAxisTicks(Graphics2D g2) {
        AxisScale yAxis = view.getYAxis();
        double[] tickLoc = yAxis.getMajorTickLocations();
        int x = yAxisRegion.getWidth();
        for (int i = 0; i < tickLoc.length; i++) {
            int yPixLoc = yAxis.getRawPixelFromValue(tickLoc[i], yAxisRegion) - yAxisRegion.getHeight(); 
            //System.out.println("YTicks[" + i + "]\t" + tickLoc[i] + "\t" + yPixLoc + "\t" + x + "\tyaxisheight = " + yAxisRegion.getHeight() + "\t" + yAxisRegion.getWidth() + "-->\t" + (yPixLoc - yAxisRegion.getHeight()) + "");
            g2.drawLine(x, yPixLoc, x - AxisRegion.TICK_LENGTH, yPixLoc);
            drawYTickLabel(g2, yPixLoc, yAxis.getMajorTickString(i));
        }
    }
    
    @Override
    protected void drawRightYAxisTicks(Graphics2D g2) { }
    
    /**
     * Draws the position numbers of the ticks across the horizontal axis
     * @param g2 Graphics2D
     * @param xCenter int
     */
    protected void drawYTickLabel(Graphics2D g2, int yCenter, String str) {
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = new Font("Arial", Font.PLAIN, 12);
        TextLayout tl = new TextLayout(str, font, frc);
        int leftPadding = Singleton.getState().getLeftPadding();
        int x = (int) getWidth() - 1 - AxisRegion.TICK_LENGTH - (int) Math.round(tl.getBounds().getWidth());
        int y = (int) Math.round(yCenter + tl.getBounds().getHeight() / 2);
        tl.draw(g2, x, y);
    }
    
    protected void drawXAxisTicks(Graphics2D g2) {
        AxisScale xAxis = view.getXAxis();
        double[] tickLoc = xAxis.getMajorTickLocations();
        int y = 0;
        for (int i = 0; i < tickLoc.length; i++) {
            int xPixLoc = xAxis.getRawPixelFromValue(tickLoc[i], xAxisRegion) + this.getWidth();
            g2.drawLine(xPixLoc, y, xPixLoc, y + AxisRegion.TICK_LENGTH);
            drawXTickLabel(g2, xPixLoc, xAxis.getMajorTickString(i));
        }
    }

    /**
     * Draws the position numbers of the ticks across the horizontal axis
     * @param g2 Graphics2D
     * @param xCenter int
     */
    protected void drawXTickLabel(Graphics2D g2, int xCenter, String str) {
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = new Font("Arial", Font.PLAIN, 12);
        TextLayout tl = new TextLayout(str, font, frc);
        int txtWidth = (int) Math.round(tl.getBounds().getWidth());
        int x = xCenter - (int) Math.round(txtWidth / 2);
        
        // pvh removed to implement corner square 7/23/2012
        //x = Math.max(x, 0);
        //x = Math.min(getWidth() - txtWidth - 3, x);

        int yline = 0;
        int y = yline + AxisRegion.TICK_LENGTH + 2 + (int) Math.round(tl.getBounds().getHeight());
        tl.draw(g2, x, y);
    }

    /**
     * Draws the border which is the left and bottom side
     * @param g2 
     */
    @Override
    protected void drawBorder(Graphics2D g2) {
        g2.setColor(Singleton.getUserPreferences().getBorderColor());
        g2.drawLine(0, 0, 0, getHeight()-1);
        g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
    }
    
    
}
