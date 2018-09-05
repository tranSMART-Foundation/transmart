/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics;

import com.pfizer.mrbt.genomics.state.State;
import com.pfizer.mrbt.genomics.state.View;
import com.pfizer.mrbt.genomics.state.ViewListener;
import com.pfizer.mrbt.genomics.state.StateListener;
import com.pfizer.mrbt.genomics.data.DataPointEntry;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.data.SNP;
import com.pfizer.mrbt.genomics.data.SnpRecombRate;
import com.pfizer.mrbt.genomics.data.XYPoint;
import com.pfizer.mrbt.genomics.hline.HLine;
import com.pfizer.mrbt.genomics.state.AxisChangeEvent;
import com.pfizer.mrbt.genomics.state.SelectedGeneAnnotation;
import com.pfizer.mrbt.genomics.state.ViewData;
import com.pfizer.mrbt.genomics.userpref.UserPrefListener;
import com.pfizer.mrbt.axis.AxisScale;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author henstockpv
 */
public class ManhattanPlot extends JComponent {

    public final static int PREFERRED_HEIGHT = 300;
    public final static int PREFERRED_WIDTH = 860;
    public final static int TICK_LENGTH = 10;
    public final static int CURR_LENGTH = 10; /*
     * Radius of cross showing current
     */

    public final static int DOT_WIDTH = 3;
    public final static int CIRCLE_WIDTH = 7; //8;
    public final static double DIST_SQUARED_THRESHOLD = 500.0;
    public final static Color PALE_GRAY = new Color(220, 220, 220);
    private View view = null;
    private DataSet dataSet;
    private AxisScale xAxis, yAxis, rightYAxis;
    private double hscale, vscale;
    private Graphics2D offscreenG2;
    private BufferedImage bufferedImage;
    private int preferredHeight = PREFERRED_HEIGHT;
    private int preferredWidth = PREFERRED_WIDTH;
    private final Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.CYAN, Color.MAGENTA, Color.YELLOW};
    private final int NUM_AVAILABLE_COLORS = colors.length;

    public ManhattanPlot(View view) {
        super();
        if(view != null) {
            this.dataSet = view.getDataSet();
        } else {
            /* This populates the view, viewdata and dataset with some dummy empty data */
            /*this.dataSet = DataSet.createDummyDataset();
            ViewData viewData = new ViewData(dataSet);
            view = new View(viewData);  pvh 12/6/2013 */
        }
        this.view = view;

        reStartGraphics();
        render();
        repaint();

        if(view != null) {
            ViewController viewController = new ViewController();
            view.addListener(viewController);
        }

        StateController stateController = new StateController();
        Singleton.getState().addListener(stateController);

        UserPrefController userPrefController = new UserPrefController();
        Singleton.getUserPreferences().addListener(userPrefController);
        
        MouseController mouseController = new MouseController();
        addMouseListener(mouseController);
        addMouseMotionListener(mouseController);
    }

    protected void reStartGraphics() {
        if(view != null) {
            bufferedImage = getBufferedImage();
            setupAxes();
        }
    }
    
    protected void render() {
        if(view != null) {
        updateScales();
        offscreenG2.setColor(Singleton.getUserPreferences().getBackgroundColor());
        offscreenG2.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        // top border with title
        offscreenG2.setColor(Singleton.getUserPreferences().getFrameColor());
        State state = Singleton.getState();
        offscreenG2.fillRect(0, 0, bufferedImage.getWidth() - 1, state.getTopPadding() - 1);
        
        // line along the top of the Manhattan plot excluding left/right axes
        //offscreenG2.setColor(Singleton.getUserPreferences().getBorderColor());
        //offscreenG2.drawLine(0, 0, getWidth(), 0);

        // refetch the axes to ensure current
        xAxis = view.getXAxis();
        yAxis = view.getYAxis();
        rightYAxis = view.getRightYAxis();


        // selected gene annotation drawn as vertical bar
        SelectedGeneAnnotation selectedGeneAnnotation = Singleton.getState().getSelectedGeneAnnotation();
        if (selectedGeneAnnotation != null
                && selectedGeneAnnotation.getChromosome() == view.getDataSet().getChromosome()) {
            int start = selectedGeneAnnotation.getStart();
            int end = selectedGeneAnnotation.getEnd();
            int startx = xAxis.getImagePixelFromValue(start, bufferedImage);
            int endx = Math.max(startx + 1, xAxis.getImagePixelFromValue(end, bufferedImage));
            if (selectedGeneAnnotation.overlaps(xAxis.getMinDisplayValue(), xAxis.getMaxDisplayValue())) {
                offscreenG2.setColor(Singleton.getUserPreferences().getSelectionBandColor());
                offscreenG2.fillRect(startx,
                        state.getTopPadding(),
                        endx - startx,
                        bufferedImage.getHeight() - state.getTopPadding() - state.getBottomPadding());
            }
        }

        // top border with title
        offscreenG2.setColor(Singleton.getUserPreferences().getFrameColor());
        offscreenG2.fillRect(0, 0, bufferedImage.getWidth(), state.getTopPadding());

        //ArrayList<Integer> xPoints = dataSet.getSNPLoc();
        //int numPoints = xPoints.size();
        int topPadding = Singleton.getState().getTopPadding();
        int studySetModelIndex = 0;

        drawHorizontalLines(offscreenG2);

        if (Singleton.getState().getShowRecombinationRate()) {
            drawRecombinationRate(offscreenG2);
        }

        CopyOnWriteArrayList<SNP> snps = dataSet.getSnps();
        int modelIndex = 0;
        
        DataPointEntry currDataPointEntry = Singleton.getState().getCurrenDataEntry();
        for (Model model : view.getModels()) {
            Color modelColor;
            if(view.getModels().size()==1) {
                modelColor = Singleton.getUserPreferences().getPointColor();
            } else {
                modelColor = Singleton.getState().getPlotColor(modelIndex);
            }
            offscreenG2.setColor(modelColor);

            if (Singleton.getState().getShowAveragingLines()) {
                AverageVec averageVec = new AverageVec(dataSet, model, snps, xAxis, yAxis, bufferedImage);
                averageVec.computeAverageVec(Singleton.getState().getAveragingWindowWidth());
                int[] resultVec = averageVec.getYPixVector();
                int minXAvgVec = averageVec.getMinXVal();
                for (int i = 1; i < resultVec.length; i++) {
                    offscreenG2.drawLine(i - 1, resultVec[i - 1], i, resultVec[i]);
                }
            }
            
            switch(Singleton.getState().getSnpLineChoice()) {
                case NONE: break;
                case AVERAGE:
                    AverageVec averageVec = new AverageVec(dataSet, model, snps, xAxis, yAxis, bufferedImage);
                    averageVec.computeAverageVec(Singleton.getState().getAveragingWindowWidth());
                    int[] resultVec = averageVec.getYPixVector();
                    int minXAvgVec = averageVec.getMinXVal();
                    for (int i = 1; i < resultVec.length; i++) {
                        offscreenG2.drawLine(i - 1, resultVec[i - 1], i, resultVec[i]);
                    }
                    break;
                case CONNECTING:
                    boolean turnOffWideLine = false;
                    Stroke prevStroke = null;
                    // if connecting lines, it will highlight the line with the 'current' SNP
                    if(currDataPointEntry != null && currDataPointEntry.getModel().equals(model)) {
                        turnOffWideLine = true;
                         prevStroke = offscreenG2.getStroke();
                        offscreenG2.setStroke(new BasicStroke(3));
                    }
                    ConnectingLines connectingLines = new ConnectingLines(dataSet, model, snps, xAxis, yAxis, bufferedImage);
                    ArrayList<XYPoint> xyPoints = connectingLines.computeConnectingLines();
                    XYPoint prevPoint = null;
                    for(XYPoint point : xyPoints) {
                        if(prevPoint != null) {
                            offscreenG2.drawLine(prevPoint.getX(), prevPoint.getY(), point.getX(), point.getY());
                        }
                        prevPoint = point;
                    }
                    if(turnOffWideLine) {
                        // changing strokes seems to take CPU so only do if necessary
                        offscreenG2.setStroke(prevStroke);
                    }
                    break;
            }
                
            

            if (Singleton.getState().getShowPoints()) {
                boolean printReady = Singleton.getState().isDisplayPrintReady();
                int circleRadius = CIRCLE_WIDTH/2;
                for (SNP snp : snps) {
                    Double logpval = dataSet.getPvalFromSnpModel(snp, model);
                    if (logpval != null) {
                        double imagex = xAxis.getImagePixelFromValue(snp.getLoc(), bufferedImage);
                        double imagey = yAxis.getImagePixelFromValue(logpval, bufferedImage);
                        if (imagey < topPadding) { // top points stay on image
                            imagey = topPadding;
                        }
                        if(printReady) {
                            int topXCorner = (int) Math.round(imagex - circleRadius);
                            int topYCorner = (int) Math.round(imagey - circleRadius);
                            offscreenG2.fillOval(topXCorner, topYCorner, CIRCLE_WIDTH, CIRCLE_WIDTH);
                            offscreenG2.fillOval(topXCorner, topYCorner, CIRCLE_WIDTH, CIRCLE_WIDTH);
                        } else {
                            offscreenG2.fillRect((int) Math.round(imagex),
                                    (int) Math.round(imagey), DOT_WIDTH, DOT_WIDTH);
                        }
                    }
                }
                // draw the edge so that not have to change color
                if(printReady) {
                    offscreenG2.setColor(Color.BLACK);
                    for(SNP snp : snps) {
                        Double logpval = dataSet.getPvalFromSnpModel(snp, model);
                        if (logpval != null) {
                            double imagex = xAxis.getImagePixelFromValue(snp.getLoc(), bufferedImage);
                            double imagey = yAxis.getImagePixelFromValue(logpval, bufferedImage);
                            if (imagey < topPadding) { // top points stay on image
                                imagey = topPadding;
                            }
                            if(printReady) {
                                int topXCorner = (int) Math.round(imagex - circleRadius);
                                int topYCorner = (int) Math.round(imagey - circleRadius);
                                offscreenG2.drawOval(topXCorner, topYCorner, CIRCLE_WIDTH, CIRCLE_WIDTH);
                                offscreenG2.drawOval(topXCorner, topYCorner, CIRCLE_WIDTH, CIRCLE_WIDTH);
                            }
                        }
                    }
                }
            }
            modelIndex++;
        }

        // draw out-of-data ranges
        //int maxGeneRange = dataSet.getGeneRange().getEnd();
        double maxGeneRange = xAxis.getOrigMaxValue();
        if (maxGeneRange < xAxis.getMaxDisplayValue()) {
            offscreenG2.setColor(Color.GRAY);
            int imageX = (int) Math.round(xAxis.getImagePixelFromValue(maxGeneRange, bufferedImage));
            offscreenG2.fillRect(imageX,
                    state.getTopPadding(),
                    bufferedImage.getWidth() - imageX,
                    bufferedImage.getHeight() - state.getTopPadding());
        }
        //int minGeneRange = dataSet.getGeneRange().getStart();
        double minGeneRange = xAxis.getOrigMinValue();
        if (minGeneRange > xAxis.getMinDisplayValue()) {
            offscreenG2.setColor(Color.GRAY);
            int imageX = (int) Math.round(xAxis.getImagePixelFromValue(minGeneRange, bufferedImage));
            offscreenG2.fillRect(0,
                    state.getTopPadding(),
                    imageX,
                    bufferedImage.getHeight() - state.getTopPadding());
        }

        // rectangular frame on window
        offscreenG2.setColor(Color.DARK_GRAY);
        offscreenG2.drawRect(state.getLeftPadding(), state.getTopPadding(),
                bufferedImage.getWidth() - state.getLeftPadding() - state.getRightPadding() - 1,
                bufferedImage.getHeight() - state.getTopPadding() - state.getBottomPadding() - 1);
        }
    }

    /**
     * Creates a buffered image of the PREFERRED_WIDTH and PREFERRED_HEIGHT and
     * also an offscreen graphics context
     *
     * @return BufferedImage
     */
    protected BufferedImage getBufferedImage() {
        if (bufferedImage == null) {
            bufferedImage = new BufferedImage(Math.max(1,getPreferredWidth()),
                    Math.max(1,getPreferredHeight()),
                    BufferedImage.TYPE_INT_RGB);
            //System.out.println("Creating buffered image with size " + getPreferredWidth() + "\t" + getPreferredHeight());
            offscreenG2 = bufferedImage.createGraphics();
        }
        return bufferedImage;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getPreferredWidth(), getPreferredHeight());
    }

    public void setPreferredSize(int width, int height) {
        preferredWidth = width;
        preferredHeight = height;
        bufferedImage = null;
    }

    protected int getPreferredHeight() {
        return preferredHeight;
    }

    protected int getPreferredWidth() {
        return preferredWidth;
    }

    protected void setupAxes() {
        xAxis = view.getXAxis();
        yAxis = view.getYAxis();
        rightYAxis = view.getRightYAxis();
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        if (view != null) {
            updateScales();
            g2.drawRenderedImage(getBufferedImage(),
                                 AffineTransform.getScaleInstance(hscale, vscale));
            //drawGridLinesOnScreen(g2, getWidth(), getHeight());
            //drawVerticalLimitLabels(g2);
            //g2.setColor(new Color(72, 61, 139));
            g2.setColor(Singleton.getUserPreferences().getFrameTextColor());
            drawTitle(g2);
            drawCurrent(g2);
            drawRsIdSearchResults(g2);
            //drawYAxisTicks(g2);
            //drawXAxisTicks(g2);
            //drawXAxisTitle(g2);
            //drawYAxisTitle(g2);
            //drawYAxisTicks(g2);
            //drawXAxisTicks(g2);
            g2.setColor(Singleton.getUserPreferences().getBorderColor());
            g2.drawLine(0, 0, getWidth(), 0);
        }
    }

    /**
     * Recomputes the hscale and vscale and submits them to update view scales
     */
    protected void updateScales() {
        //System.out.println("Updating scales Manhattan [" + getWidth() + "\t" + getHeight() + "]\t[" + getBufferedImage().getWidth() + "\t" + getBufferedImage().getHeight() + "]");
        hscale = (getWidth() * 1f) / (getBufferedImage().getWidth() * 1f);
        vscale = (getHeight() * 1f) / (getBufferedImage().getHeight() * 1f);
        view.setHscale(hscale);
        view.setVscale(vscale);
    }

    /**
     * Draws the position numbers of the ticks across the horizontal axis
     *
     * @param g2 Graphics2D
     * @param xCenter int
     */
    protected void drawTitle(Graphics2D g2) {
        //int xloc = leftDisplay + (rightDisplay - leftDisplay)*xCenter/getWidth();
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = new Font("Arial", Font.BOLD, 16);
        //String str = dataSet.getGeneRange().getName() + "    Model:Chromosome " + dataSet.getChromosome();
        String str = dataSet.getGeneRange().getName();
        //System.out.println("Number of models: " + dataSet.getModels().size());
        if (view.getModels().size() == 1) {
            str = str + "      " + view.getModels().get(0).toString();
        } 
        if(str.length()==0) { // kluge to prevent 0-length strings for default empty sets
            str = " ";
        }
        TextLayout tl = new TextLayout(str, font, frc);
        int x = getWidth() / 2 - (int) Math.round(tl.getBounds().getWidth() / 2.0);
        int y = 14;
        tl.draw(g2, x, y);
    }

    public View getView() {
        return view;
    }

    protected void drawRecombinationRate(Graphics2D offscreenG2) {
        ArrayList<SnpRecombRate> snpRecombRates = dataSet.getSnpRecombRates();
        int prevImagex = -1;
        int prevImagey = -1;
        // offscreenG2.setColor(new Color(244, 164, 96));
        offscreenG2.setColor(Singleton.getUserPreferences().getRecombinationColor());
        int minX = (int) xAxis.getMinDisplayValue();
        int maxX = (int) xAxis.getMaxDisplayValue();
        //System.out.print("Drawing recombination rate " + dataSet.getMaxRecombinationRate() + "\t");
        //view.resetRightYAxis();   PVH 2/18/2013 removed this line that made no sense
        //System.out.println("RightYAxis stats " + rightYAxis.getMaxDisplayValue() + "\t" + rightYAxis.getOrigMaxValue());
        //System.out.println("rightYAxis = " + rightYAxis);
        //offscreenG2.setColor(Color.BLACK);
        for (SnpRecombRate currSnpRecomb : snpRecombRates) {
            int x = currSnpRecomb.getSnp();
            if (x > minX && x < maxX) {
                float y = currSnpRecomb.getRecombRate();
                int imagex = xAxis.getImagePixelFromValue(x, bufferedImage);
                int imagey = rightYAxis.getImagePixelFromValue(y, bufferedImage);
                //System.out.println("Recomb " + x + "\t" + y + "\t-->" + imagex + "\t" + imagey);
                if (prevImagey > -1) {
                    //offscreenG2.setColor(new Color(189,252,201));
                    //offscreenG2.setColor(new Color(100, 149, 237));
                    offscreenG2.drawLine(prevImagex, prevImagey, imagex, imagey);
                }
                //offscreenG2.setColor(Color.RED);
                offscreenG2.drawOval(imagex-1, imagey-1, 2, 2);
                prevImagex = imagex;
                prevImagey = imagey;
            }
        }
    }

    /**
     * Draws a circle around the selected entry that it obtains from State
     *
     * @param g2
     */
    protected void drawCurrent(Graphics2D g2) {
        DataPointEntry dataEntry = Singleton.getState().getCurrenDataEntry();
        if (dataEntry != null) {
            SNP snp = dataEntry.getSnp();
            Model model = dataEntry.getModel();
            int x = snp.getLoc();
            double y = dataEntry.getDataSet().getPvalFromSnpModel(snp, model);
            int yPixLoc = yAxis.getRawPixelFromValue(y, this) + DOT_WIDTH / 2;
            int xPixLoc = xAxis.getRawPixelFromValue(x, this) + DOT_WIDTH / 2;
            g2.setColor(Singleton.getUserPreferences().getCurrentColor());
            g2.drawLine(xPixLoc, yPixLoc - CURR_LENGTH, xPixLoc, yPixLoc + CURR_LENGTH);
            g2.drawLine(xPixLoc - CURR_LENGTH, yPixLoc, xPixLoc + CURR_LENGTH, yPixLoc);
            //g2.drawLine(0, yPixLoc, xPixLoc + CURR_LENGTH, yPixLoc);
        }
    }
    
    protected void drawHorizontalLines(Graphics2D offscreenG2) {
        String gene = view.getDataSet().getGeneRange().getName();
        ArrayList<Model> models = view.getViewData().getModels();
        ArrayList<HLine> hLines = Singleton.getState().getLines(gene, models);
        Stroke currStroke = offscreenG2.getStroke();
        for (HLine hLine : hLines) {
            float[] strokeSeq = getLineStyleStrokeSequence(hLine.getLineStyle());
            if (strokeSeq.length == 0) {
                offscreenG2.setStroke(new BasicStroke(2.0f));
            } else {
                offscreenG2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, strokeSeq, 0.0f));
            }
            offscreenG2.setColor(hLine.getLineColor());
            int yLoc = yAxis.getRawPixelFromValue(hLine.getyValue(), this);
            offscreenG2.drawLine(0, yLoc, bufferedImage.getWidth(), yLoc);
        }
        offscreenG2.setStroke(currStroke);
    }

    /**
     * Returns the stroke sequence for the given lineStyle
     *
     * @param lineStyle
     * @return
     */
    private float[] getLineStyleStrokeSequence(int lineStyle) {
        float[] seq;
        switch (lineStyle) {
            case HLine.SOLID:
                seq = new float[0];
                return seq;
            case HLine.DASHED:
                seq = new float[1];
                seq[0] = 10f;
                return seq;
            case HLine.DOTTED:
                seq = new float[1];
                seq[0] = 4f;
                return seq;
            case HLine.DASH_DOT:
                seq = new float[4];
                seq[0] = 10f;
                seq[1] = 4f;
                seq[2] = 2f;
                seq[3] = 4f;
                return seq;
            default:
                return new float[0];
        }
    }

    public void processMainPlotChanged() {
        ManhattanPlot.this.view = Singleton.getState().getMainView();
        ManhattanPlot.this.dataSet = view.getDataSet();
        reStartGraphics();
        setupAxes();
        render();
        repaint();
    }

    /**
     * Draws a circle around the selected entry that it obtains from State
     *
     * @param g2
     */
    protected void drawRsIdSearchResults(Graphics2D g2) {
        ArrayList<DataPointEntry> rsIdSearchResults = Singleton.getState().getRsIdSearchResults();
        if (!rsIdSearchResults.isEmpty()) {
            int CIRCLE_RADIUS = 10;
            Stroke origStroke = g2.getStroke();
            g2.setColor(Singleton.getUserPreferences().getSelectionColor());
            g2.setStroke(new BasicStroke(2.0f));
            for (DataPointEntry result : rsIdSearchResults) {
                SNP snp = result.getSnp();
                Model model = result.getModel();
                int x = snp.getLoc();
                double y = result.getDataSet().getPvalFromSnpModel(snp, model);
                int yPixLoc = yAxis.getRawPixelFromValue(y, this) + DOT_WIDTH / 2;
                int xPixLoc = xAxis.getRawPixelFromValue(x, this) + DOT_WIDTH / 2;
                g2.drawOval(xPixLoc - CIRCLE_RADIUS, yPixLoc - CIRCLE_RADIUS, CIRCLE_RADIUS * 2, CIRCLE_RADIUS * 2);
            }
            g2.setStroke(origStroke);
        }
    }

    /**
     * Resizes the bufferPanel to the current image size and then restarts the
     * graphics
     */
    public void adjustImage() {
        setPreferredSize(this.getWidth(), this.getHeight());
        reStartGraphics();
        render();
        repaint();
    }

    public class ViewController implements ViewListener {

        public void zoomChanged(AxisChangeEvent ce) {
            System.out.println("Re-rendering Manhattan Plot");
            render();
            repaint();
        }
    }

    /**
     * This is a major routine that guts the current plot, replacing it with a
     * different view and data set
     *
     * @param newview
     */
    public void setView(View view) {
        this.view = view;
        this.dataSet = view.getDataSet();
        setupAxes();
        setPreferredSize(this.getWidth(), this.getHeight());
        reStartGraphics();
        render();
        repaint();
    }

    public class StateController implements StateListener {

        @Override
        public void currentChanged(ChangeEvent ce) {
            repaint();
        }

        @Override
        public void mainPlotChanged(ChangeEvent ce) {
            //processMainPlotChanged();
        }

        @Override
        public void thumbnailsChanged(ChangeEvent ce) {
        }

        @Override
        public void currentAnnotationChanged(ChangeEvent ce) {
        }

        @Override
        public void selectedAnnotationChanged(ChangeEvent ce) {
            render();
            repaint();
        }

        @Override
        public void averagingWindowChanged(ChangeEvent ce) {
            render();
            repaint();
        }
        
        @Override
        public void legendSelectedRowChanged(ChangeEvent ce) { }
      public void heatmapChanged(ChangeEvent ce) { }
        
    }

    public class UserPrefController implements UserPrefListener {

        public void colorChanged(ChangeEvent ce) {
            render();
            repaint();
        }
    }

    public class MouseController implements MouseListener, MouseMotionListener {

        public void mouseEntered(MouseEvent me) {
        }

        public void mouseExited(MouseEvent me) {
            Singleton.getState().clearCurrDataEntry();
            //System.out.println("Cleared due to mouse exited");
        }

        public void mouseClicked(MouseEvent me) {
            Singleton.getState().clearRsIdSearchResults();
        }

        public void mousePressed(MouseEvent me) {
        }

        public void mouseReleased(MouseEvent me) {
        }

        public void mouseDragged(MouseEvent me) {
        }

        public void mouseMoved(MouseEvent me) {
            updateCurrent(me.getX(), me.getY());
        }

        private void updateCurrent(int x, int y) {
            /*
             * double xval = xAxis.getValueFromRawPixel(x,
             * getWidth()); double yval =
             * yAxis.getValueFromRawPixel(y, getWidth());
             */
            int setIndex = 0;
            Model closestModel = null;
            SNP closestSNP = null;
            double closestDist = Double.POSITIVE_INFINITY;

            if(Singleton.getState().getSnpLineChoice() == SnpLineChoice.CONNECTING) {
                render();
                repaint();
            }

            CopyOnWriteArrayList<SNP> snps = dataSet.getSnps();
            ArrayList<Model> models = view.getModels();
            for (Model model : models) {
                for (SNP snp : snps) {
                    Double logpval = dataSet.getPvalFromSnpModel(snp, model);
                    if (logpval != null) {
                        double imagex = xAxis.getRawPixelFromValue(snp.getLoc() * 1.0, ManhattanPlot.this);
                        //double imagex = xAxis.getImagePixelFromValue(snp.getLoc(), bufferedImage);
                        //double imagey = yAxis.getImagePixelFromValue(logpval, bufferedImage);
                        double imagey = yAxis.getRawPixelFromValue(logpval, ManhattanPlot.this);
                        double oneDist = Math.pow(imagey - y, 2.0)
                                + Math.pow(imagex - x, 2.0);
                        if (oneDist < closestDist) {
                            closestDist = oneDist;
                            closestSNP = snp;
                            closestModel = model;
                        }
                    }
                }
                setIndex++;
            }
            if(closestSNP != null) {
                int closestLoc = closestSNP.getLoc();
                double imagex = xAxis.getImagePixelFromValue(closestLoc, bufferedImage);
                double closestLogp = dataSet.getPvalFromSnpModel(closestSNP, closestModel);
                double imagey = yAxis.getImagePixelFromValue(closestLogp, bufferedImage);
            }
            
            if (closestDist < DIST_SQUARED_THRESHOLD) {
                DataPointEntry currEntry = new DataPointEntry(dataSet, closestModel, closestSNP);
                Singleton.getState().setCurrDataEntry(currEntry);
                //System.out.println("Setting closest " + closestDist);
            } else {
                Singleton.getState().clearCurrDataEntry();
                //System.out.println("Cleared too far");
                //System.out.println("Clearing closest");
            }
        }
    }
    
}
