/*
 * Display of the axis tick and labels only with special highlighting
 * 
 */
package com.pfizer.mrbt.genomics.axisregion;

import com.pfizer.mrbt.genomics.ManhattanPlot;
import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.state.View;
import com.pfizer.mrbt.genomics.state.ViewListener;
import com.pfizer.mrbt.genomics.data.DataModel;
import com.pfizer.mrbt.genomics.state.AxisChangeEvent;
import com.pfizer.mrbt.axis.AxisScale;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

/**
 *
 * @author Peter v. Henstock
 */
public class AxisRegionX extends AxisRegion {
    private AxisScale xAxis;
    public final static int PREFERRED_HEIGHT = 45;
    

    public AxisRegionX(View view) {
        super(view);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        MouseController mouseController = new MouseController();
        addMouseListener(mouseController);
        addMouseMotionListener(mouseController);
        
        ViewController viewController = new ViewController();
        view.addListener(viewController);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(ManhattanPlot.PREFERRED_WIDTH, PREFERRED_HEIGHT);
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, PREFERRED_HEIGHT);
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, PREFERRED_HEIGHT);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        xAxis = view.getXAxis();
        drawDragZone(g2);
        g2.setColor(Singleton.getUserPreferences().getFrameTextColor());
        drawAxisTicks(g2);
        drawTitleInformation(g2);
        drawBorder(g2);
    }
    
    
    /**
     * Draws the region between dragStart and dragEnd if applicable
     */
    @Override
    protected void drawDragZone(Graphics2D g2) {
        if(dragStart != UNSELECTED  && dragEnd != UNSELECTED) {
            int left = Math.min(dragStart, dragEnd);
            int diff = Math.abs(dragStart - dragEnd);
            g2.setColor(new Color(143, 188, 143));
            g2.fillRect(left, 0, diff, getHeight());
        }
    }

    @Override
    protected void drawAxisTicks(Graphics2D g2) {
        double[] tickLoc = xAxis.getMajorTickLocations();
        int y = 0;
        for (int i = 0; i < tickLoc.length; i++) {
            int xPixLoc = xAxis.getRawPixelFromValue(tickLoc[i], this);
            //System.out.println("XTicks[" + i + "]\t" + tickLoc[i] + "\t" + xPixLoc);
            g2.drawLine(xPixLoc, y, xPixLoc, y + TICK_LENGTH);
            drawXTickLabel(g2, xPixLoc, xAxis.getMajorTickString(i));
        }
    }

    /**
     * Draws the position numbers of the ticks across the horizontal axis
     * @param g2 Graphics2D
     * @param xCenter int
     */
    protected void drawXTickLabel(Graphics2D g2, int xCenter, String str) {
        //int xloc = leftDisplay + (rightDisplay - leftDisplay)*xCenter/getWidth();
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = new Font("Arial", Font.PLAIN, 12);
        TextLayout tl = new TextLayout(str, font, frc);
        int txtWidth = (int) Math.round(tl.getBounds().getWidth());
        int x = xCenter - (int) Math.round(txtWidth / 2);
        
        // pvh removed to implement corner square 7/23/2012
        //x = Math.max(x, 0);
        //x = Math.min(getWidth() - txtWidth - 3, x);

        //int y = yAxis.getRawPixelFromValue(Singleton.getState().getYmin(), this) + TICK_LENGTH + 1;
        int yline = 0;
        int y = yline + TICK_LENGTH + 2 + (int) Math.round(tl.getBounds().getHeight());
        //int y = (int) Math.round(getHeight() - state.getVScale() * state.getBottomPadding() + TICK_LENGTH + 1);
        tl.draw(g2, x, y);
    }

    /**
     * Draws the position numbers of the ticks across the horizontal axis
     * @param g2 Graphics2D
     * @param xCenter int
     */
    @Override
    protected void drawTitleInformation(Graphics2D g2) {
        //int xloc = leftDisplay + (rightDisplay - leftDisplay)*xCenter/getWidth();
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = new Font("Arial", Font.BOLD, 16);
        int chromosomeNumber = Singleton.getState().getMainView().getDataSet().getChromosome();
        String str = "Position on Chromosome " + chromosomeNumber + " (Mb)";
        if (str.length() > 0) {
            TextLayout tl = new TextLayout(str, font, frc);
            int x = (int) Math.round(getWidth()/2 - tl.getBounds().getWidth()/2);
            int y = getHeight() - 4;
            tl.draw(g2, x, y);
        }
    }
    
    /**
     * Draws the border which is the lower part of the plot 
     * @param g2 
     */
    protected void drawBorder(Graphics2D g2) {
        g2.setColor(Singleton.getUserPreferences().getBorderColor());
        g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
    }

    /**
     * Calls the zoomToRange for this axis
     */
    @Override
    protected void zoomToRangeCall() {
        int width = getWidth();
        double hscale = view.getHscale();
        if(dragStart > dragEnd) {
            int temp = dragStart;
            dragStart = dragEnd;
            dragEnd   = temp;
        }
        double left = xAxis.getValueFromRawPixel(dragStart, (int) Math.round(width / hscale));
        double right = xAxis.getValueFromRawPixel(dragEnd, (int) Math.round(width / hscale));
        System.out.println("Hscale is " + hscale);
        System.out.println("Converting [" + dragStart + "\t" + dragEnd + "] -> [" + left + "\t" + right + "]");
        clearDrag();
        view.xZoomToRange(left, right);
    }

    @Override
    protected void zoomOutCall() {
        view.xZoomOut();
    }

    @Override
    protected void zoomInCall() {
        view.xZoomIn();
    }

    @Override
    protected void zoomToOriginalCall() {
        view.xZoomToOriginal();
    }



    public class MouseController implements MouseListener, MouseMotionListener {
        public void mouseEntered(MouseEvent me) { }
        public void mouseExited(MouseEvent me) { }
        public void mouseClicked(MouseEvent me) { 
            clearDrag();
        }
        public void mousePressed(MouseEvent me) { 
            isDragging = true;
            dragStart  = me.getX();
            dragEnd    = me.getX();
            System.out.println("Drag started " + dragStart);
        }
        public void mouseReleased(MouseEvent me) { 
            int x = me.getX();
            x = Math.max(0, x);
            x = Math.min(x, getWidth());
            int y = me.getY();
            y = Math.max(0, y);
            y = Math.min(y, getHeight());
            showPopupMenu(x, y);
        }
        public void mouseDragged(MouseEvent me) { 
            dragEnd = me.getX();
            //System.out.println("Drag region " + dragStart + "\t" + dragEnd);
            repaint();
        }
        public void mouseMoved(MouseEvent me) { }
        
    }
    
    public class ViewController implements ViewListener {
        public void zoomChanged(AxisChangeEvent ce) {
            if(ce.getAxisChanged() == AxisChangeEvent.XAXIS) {
                repaint();
            }
        }
    }
}
