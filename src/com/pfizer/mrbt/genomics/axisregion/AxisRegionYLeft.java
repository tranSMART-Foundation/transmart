/*
 * Display of the axis tick and labels only with special highlighting
 * 
 */
package com.pfizer.mrbt.genomics.axisregion;

import com.pfizer.mrbt.genomics.ManhattanPlot;
import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.state.AxisChangeEvent;
import com.pfizer.mrbt.genomics.state.View;
import com.pfizer.mrbt.genomics.state.ViewListener;
import com.pfizerm.mrbt.axis.AxisScale;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;

/**
 *
 * @author Peter v. Henstock
 */
public class AxisRegionYLeft extends AxisRegionY {
    public final static int PREFERRED_WIDTH = 55;
    private String title = "-log10  P-Value";
    private AxisScale yAxis;
    
    public AxisRegionYLeft(View view) {
        super(view);
        
        yAxis = getYAxis();        
        MouseController mouseController = new MouseController();
        addMouseListener(mouseController);
        addMouseMotionListener(mouseController);
        
        ViewController viewController = new ViewController();
        view.addListener(viewController);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PREFERRED_WIDTH, ManhattanPlot.PREFERRED_HEIGHT);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(PREFERRED_WIDTH, Integer.MAX_VALUE);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(PREFERRED_WIDTH, 0);
    }

    @Override
    public void paintComponent(Graphics g) {
        //System.out.println("Painting y-axis ");
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        yAxis = getYAxis();
        drawDragZone(g2);
        g2.setColor(Singleton.getUserPreferences().getFrameTextColor());

        drawAxisTicks(g2);
        drawYAxisTitle(g2);    
        drawBorder(g2);
    }
    
    protected AxisScale getYAxis() {
        //System.out.println("Getting left axis");
        return view.getYAxis();
    }

    @Override
    protected void drawAxisTicks(Graphics2D g2) {
        double[] tickLoc = yAxis.getMajorTickLocations();
        int x = getWidth();
        for (int i = 0; i < tickLoc.length; i++) {
            int yPixLoc = yAxis.getRawPixelFromValue(tickLoc[i], this);
            //System.out.println("YTicks[" + i + "]\t" + tickLoc[i] + "\t" + yPixLoc + "\tvscale = " + view.getVscale());
            g2.drawLine(x, yPixLoc, x - TICK_LENGTH, yPixLoc);
            drawTickLabel(g2, yPixLoc, yAxis.getMajorTickString(i));
        }
        //System.out.println("Drawing axis ticks from " + yAxis.getMajorTickString(0) + " to " + yAxis.getMajorTickString(tickLoc.length-1));
    }
    

    /**
     * Calls the zoomToRange for this axis
     */
    @Override
    protected void zoomToRangeCall() {
        int height = getHeight();
        double vscale = view.getVscale();
        if(dragStart > dragEnd) {
            int temp = dragStart;
            dragStart = dragEnd;
            dragEnd   = temp;
        }
        double top = yAxis.getValueFromRawPixel(dragStart, (int) Math.round(height / vscale));
        double bottom = yAxis.getValueFromRawPixel(dragEnd, (int) Math.round(height / vscale));
        /*double top = yAxis.getValueFromRawPixel(dragStart, (int) Math.round(height));
        double bottom = yAxis.getValueFromRawPixel(dragEnd, (int) Math.round(height));*/
        System.out.println("Vscale is " + vscale);
        System.out.println("Converting [" + dragStart + "\t" + dragEnd + "] -> [" + top + "\t" + bottom + "]");
        clearDrag();
        view.yZoomToRange(bottom, top);
    }

    @Override
    protected void zoomOutCall() {
        view.yZoomOut();
    }

    @Override
    protected void zoomInCall() {
        view.yZoomIn();
    }

    @Override
    protected void zoomToOriginalCall() {
        view.yZoomToOriginal();
    }    
    
    protected void drawYAxisTitle(Graphics2D g2) {
        FontRenderContext frc = g2.getFontRenderContext();
        int FONT_SIZE = 16;
        Font font = new Font("Arial", Font.BOLD, FONT_SIZE);
        TextLayout tl = new TextLayout(title, font, frc);
        int txtwidth = (int) Math.round(tl.getBounds().getWidth());
        g2.transform(AffineTransform.getRotateInstance(-Math.PI/2));
        int indentSpace = 13;
        //g2.setColor(Color.BLACK);
        tl.draw(g2, -getHeight()/2f - txtwidth/2, indentSpace);
        g2.transform(AffineTransform.getRotateInstance(Math.PI/2));
    }
    
    /**
     * Draws the border on the outer edge of the axis which is top and left
     * or top and right if right Y-axis
     * @param g2 
     */
    protected void drawBorder(Graphics2D g2) {
        g2.setColor(Singleton.getUserPreferences().getBorderColor());
        // top side
        g2.drawLine(0, 0, getWidth(), 0);
        // left side
        g2.drawLine(0, 0, 0, getHeight());
    }

    /**
     * Draws the position numbers of the ticks across the horizontal axis
     * @param g2 Graphics2D
     * @param xCenter int
     */
    protected void drawTickLabel(Graphics2D g2, int yCenter, String str) {
        //int xloc = leftDisplay + (rightDisplay - leftDisplay)*xCenter/getWidth();
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = new Font("Arial", Font.PLAIN, 12);
        TextLayout tl = new TextLayout(str, font, frc);
        int leftPadding = Singleton.getState().getLeftPadding();
        int x = (int) getWidth() - 1 - TICK_LENGTH - (int) Math.round(tl.getBounds().getWidth());
        int y = (int) Math.round(yCenter + tl.getBounds().getHeight() / 2);
        tl.draw(g2, x, y);
    }
    

    
    
    public class ViewController implements ViewListener {
        public void zoomChanged(AxisChangeEvent ce) {
            if(ce.getAxisChanged() == AxisChangeEvent.YAXIS ||
               ce.getAxisChanged() == AxisChangeEvent.RIGHT_YAXIS) {
                repaint();
            }
        }
    }    
}
