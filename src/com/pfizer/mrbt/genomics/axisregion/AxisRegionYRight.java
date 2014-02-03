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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;


/**
 *
 * @author Peter v. Henstock
 */
public class AxisRegionYRight extends AxisRegionY {
    public final static int PREFERRED_WIDTH = 55;
    private AxisScale yAxis;
    private String title = "Recombination Rate (cm/Mb)";

    public AxisRegionYRight(View view) {
        super(view);
        
        yAxis = getYAxis();
        ViewController viewController = new ViewController();
        view.addListener(viewController);

        MouseController mouseController = new MouseController();
        addMouseListener(mouseController);
        addMouseMotionListener(mouseController);
        
    }
    

    @Override
    protected void drawAxisTicks(Graphics2D g2) {
        if (Singleton.getState().getShowRecombinationRate()) {
            yAxis = getYAxis();
            //System.out.println("drawAxisTicks YAxis " + yAxis.getMaxDisplayValue() + "\t" + yAxis.getMinDisplayValue());
            double[] tickLoc = yAxis.getMajorTickLocations();
            int x = getWidth();
            for (int i = 0; i < tickLoc.length; i++) {
                int yPixLoc = yAxis.getRawPixelFromValue(tickLoc[i], this);
                //System.out.println("YTicks[" + i + "]\t" + tickLoc[i] + "\t" + yPixLoc);
                g2.drawLine(0, yPixLoc, TICK_LENGTH, yPixLoc);
                //System.out.println("Yaxis major tick string[" + i + "]\t" + yAxis.getMajorTickString(i));
                drawTickLabel(g2, yPixLoc, yAxis.getMajorTickString(i));
            }
        }
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
    protected AxisScale getYAxis() {
        return view.getRightYAxis();
    }

    /**
     * Draws the position numbers of the ticks across the horizontal axis
     * @param g2 Graphics2D
     * @param xCenter int
     */
    @Override
    protected void drawTickLabel(Graphics2D g2, int yCenter, String str) {
        if (Singleton.getState().getShowRecombinationRate()) {
            //int xloc = leftDisplay + (rightDisplay - leftDisplay)*xCenter/getWidth();
            FontRenderContext frc = g2.getFontRenderContext();
            Font font = new Font("Arial", Font.PLAIN, 12);
            TextLayout tl = new TextLayout(str, font, frc);
            int leftPadding = Singleton.getState().getLeftPadding();
            int x = 1 + TICK_LENGTH;
            int y = (int) Math.round(yCenter + tl.getBounds().getHeight() / 2);
            tl.draw(g2, x, y);
        }
    }

    /**
     * Calls the zoomToRange for this axis
     */
    @Override
    protected void zoomToRangeCall() {
        int height = getHeight();
        double vscale = view.getVscale();
        if (dragStart > dragEnd) {
            int temp = dragStart;
            dragStart = dragEnd;
            dragEnd = temp;
        }
        yAxis = getYAxis();
        double top = yAxis.getValueFromRawPixel(dragStart, (int) Math.round(height / vscale));
        double bottom = yAxis.getValueFromRawPixel(dragEnd, (int) Math.round(height / vscale));
        /*System.out.println("Axis YR - Vscale is " + vscale);
        System.out.println("Converting [" + dragStart + "\t" + dragEnd + "] -> [" + top + "\t" + bottom + "]");*/
        clearDrag();
        view.rightYZoomToRange(bottom, top);
    }

    @Override
    protected void zoomOutCall() {
        view.rightYZoomOut();
    }

    @Override
    protected void zoomInCall() {
        view.rightYZoomIn();
    }

    @Override
    protected void zoomToOriginalCall() {
        view.rightYZoomToOriginal();
    }

    @Override
    protected void drawYAxisTitle(Graphics2D g2) {
        if (Singleton.getState().getShowRecombinationRate()) {
            FontRenderContext frc = g2.getFontRenderContext();
            int FONT_SIZE = 16;
            Font font = new Font("Arial", Font.BOLD, FONT_SIZE);
            TextLayout tl = new TextLayout(title, font, frc);
            int txtwidth = (int) Math.round(tl.getBounds().getWidth());
            g2.transform(AffineTransform.getRotateInstance(-Math.PI / 2));
            int indentSpace = 46;
            //g2.setColor(Color.BLACK);
            tl.draw(g2, -getHeight() / 2f - txtwidth / 2, indentSpace);
            g2.transform(AffineTransform.getRotateInstance(Math.PI / 2));
        }
    }
    
    /**
     * Draws the border on the outer edge of the axis which is top and left
     * or top and right if right Y-axis
     * @param g2 
     */
    @Override
    protected void drawBorder(Graphics2D g2) {
        g2.setColor(Singleton.getUserPreferences().getBorderColor());
        // top side
        g2.drawLine(0, 0, getWidth(), 0);
        // right side
        g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
    }
    
    
    public class ViewController implements ViewListener {
        @Override
        public void zoomChanged(AxisChangeEvent ce) {
            //System.out.println("Trying to repaint");
            if(ce.getAxisChanged() == AxisChangeEvent.RIGHT_YAXIS) {
                //System.out.println("Repainting right y-axis");
                repaint();
            }
        }
    }    
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        //System.out.println("Repainting the axisRegionY ");
        //super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        yAxis = getYAxis();
        drawDragZone(g2);
        g2.setColor(Singleton.getUserPreferences().getFrameTextColor());

        drawAxisTicks(g2);
        drawYAxisTitle(g2);    
        drawBorder(g2);
    }
}
