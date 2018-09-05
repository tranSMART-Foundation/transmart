/*
 * Displays the legend panel that can be captured and copied for display
 * elsewhere.
 */
package com.pfizer.mrbt.genomics.legend;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.data.Model;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import javax.swing.JComponent;

/**
 *
 * @author henstock
 */
public class LegendDisplay extends JComponent {
    private int numEntries;
    public final static int ROW_HEIGHT = 45;
    public final static int TOP_PADDING = 15;
    public final static int LEFT_PADDING = 17;
    public final static int BOX_WIDTH   = 30;
    protected int FONT_SIZE = 16;
    private int lowestDisplay;
    private int rightmostDisplay;

    public LegendDisplay() {
        super();
    }
    
    @Override
    public Dimension getPreferredSize() {
        numEntries = Singleton.getState().getMainView().getModels().size();
        return new Dimension(350, numEntries * ROW_HEIGHT);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        numEntries = Singleton.getState().getMainView().getModels().size();
        g2.setColor(Color.WHITE);
        int estimatedWidth = estimateWidth(g2);
        g2.fillRect(0, 0, estimatedWidth, getHeight());
        lowestDisplay = 0;
        rightmostDisplay = 0;
        for (int modeli = 0; modeli < numEntries; modeli++) {
            Model model = Singleton.getState().getMainView().getModels().get(modeli);
            Color modelColor = Singleton.getState().getPlotColor(modeli);
            g2.setColor(modelColor);
            int boxTop = ROW_HEIGHT * modeli + TOP_PADDING;
            g2.fillRect(LEFT_PADDING, boxTop, BOX_WIDTH, BOX_WIDTH);
            String legendText = Singleton.getState().getLegendFromModel(model);
            g2.setColor(Color.BLACK);
            drawLabel(g2, 
                      legendText,
                      (int) Math.round(LEFT_PADDING + BOX_WIDTH * 1.5),
                      boxTop + BOX_WIDTH/2);
        }
        lowestDisplay = ROW_HEIGHT * (numEntries-1) + 2*TOP_PADDING + BOX_WIDTH;
        rightmostDisplay += LEFT_PADDING;
    }
    
    /**
     * Draws the text to x, y in g2 that is used for the legend text.  The
     * text is drawn centered at the y location
     * @param g2
     * @param text
     * @param x
     * @param y 
     */    
    protected void drawLabel(Graphics2D g2, String text, int x, int y) {
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = new Font("Arial", Font.BOLD, FONT_SIZE);
        TextLayout tl = new TextLayout(text, font, frc);
        int txtWidth = (int) Math.round(tl.getBounds().getWidth());
        rightmostDisplay = Math.max(rightmostDisplay, txtWidth + x);
        int txtHeight = (int) Math.round(tl.getBounds().getHeight());
        y += txtHeight/2;
        tl.draw(g2, x, y);
    }
    
    /**
     * Goes through the legend and estimates the width of the panel using the
     * longest displayModel string as the guide
     * @return 
     */
    private int estimateWidth(Graphics2D g2) {
        FontRenderContext frc = g2.getFontRenderContext();
        numEntries = Singleton.getState().getMainView().getModels().size();
        Font font = new Font("Arial", Font.BOLD, FONT_SIZE);
        int maxWidth = 0;
        for (int modeli = 0; modeli < numEntries; modeli++) {
            Model model = Singleton.getState().getMainView().getModels().get(modeli);
            String legendText = Singleton.getState().getLegendFromModel(model);
            
            TextLayout tl = new TextLayout(legendText, font, frc);
            int txtWidth = (int) Math.round(tl.getBounds().getWidth());
            maxWidth = Math.max(txtWidth, maxWidth);
        }
        return (int) Math.round(maxWidth + LEFT_PADDING*2 + BOX_WIDTH*1.5);
    }

    /**
     * Returns the recommended height after doing the display for cropping
     * @return 
     */
    public int getLowestDisplay() {
        return lowestDisplay;
    }

    
    /**
     * Returns the right most width after doing the display for cropping
     * @return 
     */
    public int getRightmostDisplay() {
        return rightmostDisplay;
    }
    
    
}
