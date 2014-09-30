/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.annotation;

import com.pfizer.mrbt.genomics.ManhattanPlot;
import com.pfizer.mrbt.genomics.PlotPanel;
import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.axisregion.AxisRegionYLeft;
import com.pfizer.mrbt.genomics.state.View;
import com.pfizer.mrbt.genomics.state.ViewListener;
import com.pfizer.mrbt.genomics.state.StateListener;
import com.pfizer.mrbt.genomics.data.DataPointEntry;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.GeneAnnotation;
import com.pfizer.mrbt.genomics.data.SnpRangeY;
import com.pfizer.mrbt.genomics.state.AxisChangeEvent;
import com.pfizer.mrbt.genomics.state.SelectedGeneAnnotation;
import com.pfizer.mrbt.genomics.userpref.UserPrefListener;
import com.pfizer.mrbt.axis.AxisScale;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;

/**
 *
 * @author henstockpv
 */
public class AnnotationPanelWide extends JComponent {

    public final static int PREFERRED_HEIGHT = 90;
    public final static int Y_LOC_SPACING = 14;
    public final static int Y_TOP_PADDING = 7;
    public final static int DIST_THRESHOLD = 30;
    public final static float TOP_PADDING_FACTOR = 0.25f;
    public final static int PADDING = 10; // pixels between annotations horizontally
    private final int MAX_Y = 6; // max number of y positions
    //private int maximumY = 1; // maximum Y-level found in current view
    public final int PREFERRED_WIDTH = ManhattanPlot.PREFERRED_WIDTH;
    public final static double DIST_SQUARED_THRESHOLD = 500.0;
    private View view;
    private DataSet dataSet;
    private AxisScale xAxis;
    private double hscale, vscale;
    private Graphics2D offscreenG2;
    private BufferedImage bufferedImage;
    private int preferredHeight = PREFERRED_HEIGHT;
    private int preferredWidth = PREFERRED_WIDTH;
    private List<GeneAnnotation> geneAnnotationList;
    private ArrayList<GeneAnnotation> displayedAnnotations = new ArrayList<GeneAnnotation>();
    private ArrayList<SnpRangeY> snpRangeYList = new ArrayList<SnpRangeY>();
    private HashMap<String, Integer> gene2yloc = new HashMap<String, Integer>();
    private GeneAnnotation closestGeneAnnotation = null;
    private boolean closetGeneAnnotationInternal = false;
    public final static Color PURPLE3 = new Color(125,38,205);
    public final static Color EMERALD_GREEN = new Color(0, 201, 87);
    public final static Color ORANGE4 = new Color(139, 90, 0);
    public final static Color TEXT_COLOR = new Color(72, 61, 139);
    public final static Color LIGHT_GREEN = new Color(210, 255, 210);
    private int yLocSpacing = Y_LOC_SPACING;
    private final static int MAX_FONT_SIZE = 13;
    private final static int DEFAULT_TEXT_LINE_SPACE = 1;
    private final static int DEFAULT_ARROW_HEIGHT = 4;
    private int textLineSpace = DEFAULT_TEXT_LINE_SPACE;
    private int arrowHeight   = DEFAULT_ARROW_HEIGHT;
    private int fontSize = MAX_FONT_SIZE;
    public final static String ucscUrl = "http://genome.ucsc.edu/cgi-bin/hgTracks?org=human&db=hg19&singleSearch=knownCanonical&position=";
    private int maximumY = 1;
    private PlotPanel plotPanel;
    //private ArrayList<Integer> yLoc = new ArrayList<Integer>();

    public AnnotationPanelWide(View view, PlotPanel plotPanel) {
        super();
        this.view = view;
        this.dataSet   = view.getDataSet();
        this.plotPanel = plotPanel;
        reStartGraphics();
        render();
        repaint();

        ViewController viewController = new ViewController();
        view.addListener(viewController);

        StateController stateController = new StateController();
        Singleton.getState().addListener(stateController);
        
        ResizeController resizeController = new ResizeController();
        this.addComponentListener(resizeController);
        
        UserPrefController userPrefController = new UserPrefController();
        Singleton.getUserPreferences().addListener(userPrefController);
        
        MouseController mouseController = new MouseController();
        this.addMouseMotionListener(mouseController);
        this.addMouseListener(mouseController);
    }

    protected void reStartGraphics() {
        bufferedImage = getBufferedImage();
        initializeXAxis();
    }

    protected void render() {
        preferredWidth = Math.max(1, plotPanel.getWidth() - 24);
        //System.out.println("PlotPanelWidth " + plotPanel.getWidth() + "\t" + getWidth());
        computeMaxHeight();
        preferredHeight = yLocSpacing * (maximumY+1);
    }
    
    /**
     * Computes the class variables:
     *   maximumY=max vertical number of positions
     *   fontSize
     *   gene2yloc, snpRangeYList, and displayedAnnotations
     * Tries the biggest font and shrinks incrementally until they will fit
     */
    protected void computeBestFontSizes() {
        fontSize = MAX_FONT_SIZE+1;
        int pixPerRow = 100;
        do {
            geneAnnotationList = dataSet.getGeneAnnotations();
            gene2yloc.clear();
            snpRangeYList.clear();
            displayedAnnotations.clear();
            int maximumY = 1; // avoid /0 errors
            fontSize--; // start with MAX_FONT_SIZE
            for (GeneAnnotation geneEntry : dataSet.getGeneAnnotations()) {
                int start = geneEntry.getStart();
                int end = geneEntry.getEnd();
                int imageStartx = xAxis.getRawPixelFromValue(start * 1.0, this);
                int imageEndx = xAxis.getRawPixelFromValue(end * 1.0, this);
                if(isGeneInView(start, end, xAxis)) {
                    displayedAnnotations.add(geneEntry);
                    int textWidth = computeTextWidth(offscreenG2, geneEntry.getGene());
                    int rightLocation = imageStartx + textWidth + PADDING;
                    int yLoc = computeYLoc(imageStartx, rightLocation);
                    System.out.println("\tGene " + geneEntry.getGene() + "\tStart " + imageStartx + "\trightLoc " + rightLocation + "\ttextWidth " + textWidth + "\tYloc=" + yLoc);
                    if(yLoc > maximumY) {
                        maximumY = yLoc;
                    }
                    gene2yloc.put(geneEntry.getGene(), yLoc);
                    snpRangeYList.add(new SnpRangeY(imageStartx, imageEndx + textWidth, yLoc));
                }
            }
            pixPerRow = (int) Math.floor(getHeight() / (maximumY+1)); // maximumY starts at 0
            System.out.println("MaximumY = " + maximumY + "\tPixPerRow = " + pixPerRow);
        } while(pixPerRow < 9 && getHeight() > 0 && fontSize > 5);
        if(pixPerRow < 11) {
            textLineSpace = 1;
            arrowHeight   = DEFAULT_ARROW_HEIGHT - 1;
            fontSize = 7;
        } else {
            textLineSpace = DEFAULT_TEXT_LINE_SPACE;
            arrowHeight   = DEFAULT_ARROW_HEIGHT;
            pixPerRow = Math.min(MAX_FONT_SIZE + DEFAULT_TEXT_LINE_SPACE + DEFAULT_ARROW_HEIGHT, pixPerRow);
            fontSize = pixPerRow - 5;
        }
        yLocSpacing = pixPerRow;
    }
    
    /**
     * Fills gene2yloc and fills maximumY with the maximum Y value allowed
     */
    protected void computeMaxHeight() {
        fontSize = MAX_FONT_SIZE;
        geneAnnotationList = dataSet.getGeneAnnotations();
        gene2yloc.clear();
        snpRangeYList.clear();
        displayedAnnotations.clear();
        maximumY = 1; // avoid /0 errors
        //String maximumYgene = "";
        fontSize--; // start with MAX_FONT_SIZE
        int scrollerWidth = plotPanel.getAnnotationPanelWideScrollWidth();
        
        
        
        for (GeneAnnotation geneEntry : dataSet.getGeneAnnotations()) {
            int start = geneEntry.getStart();
            int end = geneEntry.getEnd();
            int imageStartx = xAxis.getPaddedRawPixelFromValue(start * 1.0, this, AxisRegionYLeft.PREFERRED_WIDTH, AxisRegionYLeft.PREFERRED_WIDTH - scrollerWidth);
            int imageEndx   = xAxis.getPaddedRawPixelFromValue(end * 1.0, this, AxisRegionYLeft.PREFERRED_WIDTH, AxisRegionYLeft.PREFERRED_WIDTH - scrollerWidth);
            if(isGeneInView(start, end, xAxis)) {
                displayedAnnotations.add(geneEntry);
                int textWidth = computeTextWidth(offscreenG2, geneEntry.getGene());
                int rightLocation = imageStartx + textWidth + PADDING;
                /*if(geneEntry.getGene().equals("CLEC2D") || geneEntry.getGene().equals("CLECL1")) {
                    System.out.println("Gene " + geneEntry.getGene() + "\t[" + start + "\t" + end + "]\tImg[" + imageStartx + "\t" + textWidth + "\t" + rightLocation + "]");
                }*/
                //int yLoc = computeYLoc(imageStartx, rightLocation);
                // Says it will not overlap the left or max of the right side of arrow or where a long text extends
                int yLoc = computeYLoc(imageStartx, Math.max(imageEndx, rightLocation));
                /*if(geneEntry.getGene().equals("CLEC2D") || geneEntry.getGene().equals("CLECL1")) {
                    System.out.println("Gene " + geneEntry.getGene() + "\t[" + start + "\t" + end + "]\tImg[" + imageStartx + "\t" + textWidth + "\t" + rightLocation + "] --> y=" + yLoc);
                }*/
                //System.out.println("\tGene " + geneEntry.getGene() + "\tStart " + imageStartx + "\trightLoc " + rightLocation + "\ttextWidth " + textWidth + "\tYloc=" + yLoc);
                if(yLoc > maximumY) {
                    maximumY = yLoc;
                    //maximumYgene = geneEntry.getGene();
                }
                gene2yloc.put(geneEntry.getGene(), yLoc);
                snpRangeYList.add(new SnpRangeY(imageStartx, imageEndx + textWidth + PADDING, yLoc));
            }
        }
        yLocSpacing = MAX_FONT_SIZE + DEFAULT_TEXT_LINE_SPACE + DEFAULT_ARROW_HEIGHT;
        //System.out.println("fontSize " + fontSize + "\ttextLineSpace " + textLineSpace + "\tarrowHeight " + arrowHeight + "\tylocspacing " + yLocSpacing + "\tMaxHeight " + maximumY + "\t" + maximumYgene);
    }
    

    
    /**
     * Returns true if the start and end overlap the xAxis range.  Three checks:
     * if start is between the left and right side OR
     * if end is between the left and right side OR
     * start is left-of and end is right-of the display
     * @param start
     * @param end
     * @param xAxis
     * @return 
     */
    protected boolean isGeneInView(int start, int end, AxisScale xAxis) {
        if((start > xAxis.getMinDisplayValue() && start < xAxis.getMaxDisplayValue()) ||
           (end   > xAxis.getMinDisplayValue() && end < xAxis.getMaxDisplayValue()) ||
           (start < xAxis.getMinDisplayValue() && end > xAxis.getMaxDisplayValue())) {
               return true;
        } else {
            return false;
        }
    }
    
    /**
     * Returns the width of the str drawn with 12 point font in this g2 context
     * @param g2
     * @param str
     * @return 
     */
    protected int computeTextWidth(Graphics2D g2, String str) {
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = new Font("Arial", Font.PLAIN, fontSize);
        TextLayout tl = new TextLayout(str, font, frc);
        int txtWidth = (int) Math.round(tl.getBounds().getWidth());
        return txtWidth;
    }
    
    /**
     * Performs a greedy search to find the lowest y that corresponds to an unoccupied
     * zone in the 
     * @param startx
     * @param endx
     * @return 
     */
    protected int computeYLoc(int startx, int endx) {
        // put list of all Y taken for this region in takenYLoc
        ArrayList<Integer> takenYLoc = new ArrayList<Integer>();
        for(SnpRangeY oneGene : snpRangeYList) {
            /*if(startx==539) {
                System.out.println("Matching (" + startx + "\t" + endx + ") against [" + oneGene.getStart() + "\t" + oneGene.getEnd() + "\t" + oneGene.getY() + "]\t-->" + oneGene.overlaps(startx, endx));
            }*/
            if(oneGene.overlaps(startx, endx)) {
                int oneTaken = oneGene.getY();
                takenYLoc.add(oneTaken);
            }
        }
        //System.out.print("Computing Y [" + startx + "\t" + endx + "] size=" + takenYLoc.size());
        // start low and see which are available
        /*for(int i = 0; i < MAX_Y; i++) {
            if(! takenYLoc.contains(i)) {
                return i;
            }
        }*/
        int ylevel = 0;
        while(takenYLoc.contains(ylevel)) {
            ylevel++;
        }
        //System.out.println(" --> -1");
        return ylevel;
    }
        

/**
     * Creates a buffered image of the PREFERRED_WIDTH and PREFERRED_HEIGHT
     * and also an offscreen graphics context
     *
     * @return BufferedImage
     */
    protected BufferedImage getBufferedImage() {
        if (bufferedImage == null) {
            bufferedImage = new BufferedImage(getPreferredWidth(),
                                              getPreferredHeight(),
                                              BufferedImage.TYPE_INT_RGB);
            offscreenG2 = bufferedImage.createGraphics();
        }
        return bufferedImage;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getPreferredWidth(), getPreferredHeight());
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, getPreferredHeight());
    }

    public void setPreferredSize(int width, int height) {
        preferredWidth = width;
        preferredHeight = height;
        //bufferedImage = null;
    }

    public int getPreferredHeight() {
        return preferredHeight;
    }

    public int getPreferredWidth() {
        return preferredWidth;
    }

    protected void initializeXAxis() {
        xAxis = view.getXAxis();
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Singleton.getUserPreferences().getAnnotationColor());
        g2.fillRect(0, 0, getWidth(), getHeight());
        //System.out.println("dimensions of annotation " + getWidth() + "\t" + getHeight());
        
        g2.setColor(Singleton.getUserPreferences().getAnnotationTextColor());
        //System.out.println("gene2yloc has " + gene2yloc.keySet().size() + " entries and displaAnnotations has " + displayedAnnotations.size());
        for(GeneAnnotation geneEntry : displayedAnnotations) {
            drawAnnotationTextWithArrow(g2, geneEntry);
        }
        drawCurrentText(g2);
        if(Singleton.getState().getCurrentGeneAnnotation() != null) {
            drawRightClickMessageInLowerLeft(g2);
        }
        
        drawGeneAnnotation(g2, Singleton.getState().getCurrentGeneAnnotation(),  Singleton.getUserPreferences().getCurrentAnnotationColor());
        drawGeneAnnotation(g2, Singleton.getState().getSelectedGeneAnnotation(), Singleton.getUserPreferences().getSelectedAnnotationColor());
        
        //drawMajorAxes(g2);
    }

    /**********************************************************************
     * Draws the geneEntry text gene name with the associated arrow.  The
     * color is not altered
     * @param g2
     * @param geneEntry 
     */
    protected void drawAnnotationTextWithArrow(Graphics2D g2, GeneAnnotation geneEntry) {
            int start   = geneEntry.getStart();
            int end     = geneEntry.getEnd();
            String gene = geneEntry.getGene();
            //int yloc    = yLocSpacing-5 + (int) Math.round(gene2yloc.get(gene) * yLocSpacing);
            //int yloc    = Math.round(1+TOP_PADDING_FACTOR + gene2yloc.get(gene)) * yLocSpacing;
            int yloc = 0;
            if(gene==null) {
                System.out.println("Null gene");
            } else if(gene2yloc.get(gene)==null) {
                System.out.println("Null gene2yloc for " + gene);
                yloc = fontSize/2;
            } else if(gene2yloc.get(gene) < 0) {
                System.out.println("Null gene2yloc for " + gene);
                yloc = fontSize/2;
            } else {
                yloc = fontSize + (int) Math.round(gene2yloc.get(gene) * yLocSpacing);
            }
            int scrollerWidth = plotPanel.getAnnotationPanelWideScrollWidth();
            
            int imageStartx = xAxis.getPaddedRawPixelFromValue(start * 1.0, this, AxisRegionYLeft.PREFERRED_WIDTH, AxisRegionYLeft.PREFERRED_WIDTH - scrollerWidth);
            int imageEndx   = xAxis.getPaddedRawPixelFromValue(end   * 1.0, this, AxisRegionYLeft.PREFERRED_WIDTH, AxisRegionYLeft.PREFERRED_WIDTH - scrollerWidth);
            //System.out.println("Drawing annotation for " + gene + " at (" + imageStartx + "\t" + yloc + ")");
            drawXGeneLabel(g2, imageStartx, yloc, gene, fontSize);
            g2.drawLine(imageStartx, yloc + textLineSpace, imageEndx, yloc + textLineSpace);
            if(geneEntry.isRightDirection()) {
                g2.drawLine(imageEndx-6,    yloc + arrowHeight, imageEndx, yloc + textLineSpace);
            } else {
                g2.drawLine(imageStartx+6,  yloc + arrowHeight, imageStartx, yloc + textLineSpace);
            }
    }
    
    /**
     * Returns the y value of the geneName specified that is a geneAnnotation in
     * this panel.  Note that the gene2yloc is not the ylocation position but the count
     * of the levels starting at the top.  We have to flip and scale to the text size
     * @param geneName
     * @return 
     */
    public int getYLoc(String geneName) {
       int yloc = fontSize + (int) Math.round(gene2yloc.get(geneName) * yLocSpacing);        
        return yloc;
    }
    
    /**
     * This is a test routine to see if the lines match up with the ticks or what's screwed up
     * @param g2 
     */
    protected void drawMajorAxes(Graphics2D g2) {
        AxisScale xAxis = this.view.getXAxis();
        double[] loc = xAxis.getMajorTickLocations();
        g2.setColor(Color.BLUE);
        int scrollerWidth = plotPanel.getAnnotationPanelWideScrollWidth();
        for(int i = 0; i < loc.length; i++) {
            int x = xAxis.getPaddedRawPixelFromValue(loc[i], this, AxisRegionYLeft.PREFERRED_WIDTH, AxisRegionYLeft.PREFERRED_WIDTH - scrollerWidth);
            g2.drawLine(x, 0, x, getHeight());
        }
    }
    
    /*private void updateAnnotationParameters() {
        Singleton.getState().updateAnnotationParameters(arrowHeight, textLineSpace, fontSize yLocSpacing, gene2yloc, geneEntries);
    }*/
    /**
     * The getRawPixelFromValue is out of date and should be replaced with the padded version
     * @deprecated  Use the drawAnnotationTextWithArrow?
     * */
    protected void drawAnnotationText(Graphics2D g2) {
        for(GeneAnnotation geneEntry : displayedAnnotations) {
            int start   = geneEntry.getStart();
            int end     = geneEntry.getEnd();
            String gene = geneEntry.getGene();
            //int yloc    = yLocSpacing-5 + (int) Math.round(gene2yloc.get(gene) * yLocSpacing);
            //int yloc    = Math.round(1+TOP_PADDING_FACTOR + gene2yloc.get(gene)) * yLocSpacing;
            int yloc = 0 + (int) Math.round(gene2yloc.get(gene) * yLocSpacing);
            int imageStartx = xAxis.getRawPixelFromValue(start * 1.0, this);
            int imageEndx   = xAxis.getRawPixelFromValue(end   * 1.0, this);
            //System.out.println("Drawing annotation for " + gene + " at (" + imageStartx + "\t" + yloc + ")");
            drawXGeneLabel(g2, imageStartx, yloc, gene, fontSize);
            g2.drawLine(imageStartx, yloc + textLineSpace, imageEndx, yloc + textLineSpace);
            if(geneEntry.isRightDirection()) {
                g2.drawLine(imageEndx-6,    yloc + arrowHeight, imageEndx, yloc + textLineSpace);
            } else {
                g2.drawLine(imageStartx+6,  yloc + arrowHeight, imageStartx, yloc + textLineSpace);
            }
            
        }
    }
    
    private void drawRightClickMessageInLowerLeft(Graphics2D g2) {
        FontRenderContext frc = g2.getFontRenderContext();
        g2.setColor(Color.BLACK);
        Font font = new Font("Arial", Font.PLAIN, 11);
        TextLayout tl = new TextLayout("R-click gene for UCSC Browser", font, frc);
        tl.draw(g2, 2, getHeight()-3);
    }
    
    /**
     * Draws the geneEntry's text and arrow in the particular color to g2.  This is a
     * general purpose drawing.  The color changes are slow so it shouldn't
     * be used for drawing everything.  If geneEntry is null, it does nothing.
     * @param g2
     * @param geneEntry
     * @param color 
     */
    protected void drawGeneAnnotation(Graphics2D g2, GeneAnnotation geneEntry, Color color) {
        if(geneEntry != null) {
            g2.setColor(color);
            drawAnnotationTextWithArrow(g2, geneEntry);
        }
    }
    
    /**
     * Displays the closest gene in Orange and if it overlaps, then in red.
     * These are the current position in the plot
     * @param g2 
     */
    protected void drawCurrentText(Graphics2D g2) {
        if(closestGeneAnnotation != null) {
            if(closetGeneAnnotationInternal) {
                g2.setColor(Singleton.getUserPreferences().getInteriorAnnotationColor());
            } else {
                g2.setColor(Singleton.getUserPreferences().getClosestAnnotationColor());
            }
            drawAnnotationTextWithArrow(g2, closestGeneAnnotation);
        }
        
            /*GeneAnnotation geneEntry = closestGeneAnnotation;
            int start   = geneEntry.getStart();
            int end     = geneEntry.getEnd();
            String gene = geneEntry.getGene();
           // int yloc    = gene2yloc.get(gene) * Y_LOC_SPACING + Y_TOP_PADDING;
            //int yloc    = Math.round(1+TOP_PADDING_FACTOR + gene2yloc.get(gene)) * yLocSpacing;
            int yloc    = yLocSpacing-5 + (int) Math.round(gene2yloc.get(gene) * yLocSpacing);
            
            int imageStartx = xAxis.getRawPixelFromValue(start*1.0, this);
            int imageEndx = xAxis.getRawPixelFromValue(end*1.0, this);
            //System.out.println("Drawing annotation for " + gene + " at (" + imageStartx + "\t" + yloc + ")");
            drawXGeneLabel(g2, imageStartx, yloc, gene, fontSize);
            g2.drawLine(imageStartx, yloc+1, imageEndx, yloc+1);
            if(geneEntry.isRightDirection()) {
                g2.drawLine(imageEndx-6, yloc+4, imageEndx, yloc+1);
            } else {
                g2.drawLine(imageStartx+6, yloc+4, imageStartx, yloc+1);
            }
        }*/
    }


    /**
     * Draws the position numbers of the ticks across the horizontal axis
     * @param g2 Graphics2D
     * @param xCenter int
     */
    protected void drawXGeneLabel(Graphics2D g2, int x, int y, String str, int fontSize) {
        //int xloc = leftDisplay + (rightDisplay - leftDisplay)*xCenter/getWidth();
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = new Font("Arial", Font.BOLD, fontSize);
        TextLayout tl = new TextLayout(str, font, frc);

        //int txtWidth = (int) Math.round(tl.getBounds().getWidth());
        //int y = yAxis.getRawPixelFromValue(Singleton.getState().getYmin(), this) + TICK_LENGTH + 1;
        //int y = yline + TICK_LENGTH + 2 + (int) Math.round(tl.getBounds().getHeight());
        //int y = (int) Math.round(getHeight() - state.getVScale() * state.getBottomPadding() + TICK_LENGTH + 1);
        tl.draw(g2, x, y);
    }


    /*
     * Draws the position numbers of the ticks across the horizontal axis
     *
     * @param g2 Graphics2D
     * @param xCenter int
     *
    protected void drawTitleInformation(Graphics2D g2) {
        //int xloc = leftDisplay + (rightDisplay - leftDisplay)*xCenter/getWidth();
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = new Font("Arial", Font.BOLD, 16);
        DataModel dataModel = Singleton.getDataModel();
        String delimiter1 = "";
        String delimiter2 = "";
        String str = "XAxisLabel";
        if (str.length() > 0) {
            TextLayout tl = new TextLayout(str, font, frc);
            int x = 5;
            int y = getHeight() - 4;
            tl.draw(g2, x, y);
        }
    }*/
    
    public View getView() {
        return view;
    }
    
    /**
     * Figures out which gene the current position in State overlaps with if
     * any.
     */
    protected void computeCurrentGeneOverlap() {
        DataPointEntry dpe = Singleton.getState().getCurrenDataEntry();
        if(dpe == null) {
            closestGeneAnnotation = null;
            return;
        }
        
        int snpLoc = dpe.getSnp().getLoc();
        int closestDist = Integer.MAX_VALUE;
        closestGeneAnnotation = null;
        int dist;
        for(GeneAnnotation geneAnnotation : displayedAnnotations) {
            if(snpLoc >= geneAnnotation.getStart() && snpLoc <= geneAnnotation.getEnd()) {
                dist = 0;
            } else if(snpLoc < geneAnnotation.getStart()) {
                dist = geneAnnotation.getStart() - snpLoc;
            } else {
                dist = snpLoc - geneAnnotation.getEnd();
            }
            if(dist < closestDist) {
                closestDist = dist;
                closestGeneAnnotation = geneAnnotation;
            }
        }        
        if(closestGeneAnnotation != null) {
            //System.out.println("Closest dist " + closestDist + "\t" + closestGeneAnnotation.getGene());
        }
        if(closestDist == 0) {
            closetGeneAnnotationInternal = true;
        } else {
            closetGeneAnnotationInternal = false;
        }
    }
    
    public void processMainPlotChanged() {
            AnnotationPanelWide.this.view = Singleton.getState().getMainView();
            AnnotationPanelWide.this.dataSet = view.getDataSet();
            initializeXAxis();
            //System.out.println("Render from mainPlotchaged");
            render();
            repaint();        
    }
    
    /**
     * Returns the geneAnnotation that is closest or reasonably close to the
     * coordinates x, y
     * @param x
     * @param y
     * @return 
     */
    protected GeneAnnotation findClosestAnnotationInPanel(int x, int y) {
        int closestDist = Integer.MAX_VALUE;
        GeneAnnotation closestAnnotation = null;

        int scrollerWidth = plotPanel.getAnnotationPanelWideScrollWidth();
        
        for(GeneAnnotation geneEntry : displayedAnnotations) {
            int start   = geneEntry.getStart();
            int end     = geneEntry.getEnd();
            String gene = geneEntry.getGene();
            int yloc    = (int) Math.round((gene2yloc.get(gene)+0.5) * yLocSpacing);
            int imageStartx = xAxis.getPaddedRawPixelFromValue(start*1.0, this, AxisRegionYLeft.PREFERRED_WIDTH, AxisRegionYLeft.PREFERRED_WIDTH - scrollerWidth);
            
            int dist = Math.abs(x - imageStartx) + Math.abs(y - yloc);
            if(dist < closestDist) {
                closestDist = dist;
                closestAnnotation = geneEntry;
            }
        }
        if(closestDist > DIST_THRESHOLD) {
            closestAnnotation = null;
        }
        return closestAnnotation;
        
    }

    /**
     * Draws a circle around the selected entry that it obtains from State
     *
     * @param g2
     */
    protected void drawCurrent(Graphics2D g2) {
    }

    public class ViewController implements ViewListener {
        @Override
        public void zoomChanged(AxisChangeEvent ce) {
            if(ce.getAxisChanged() == AxisChangeEvent.XAXIS) {
                System.out.println("Render from zoomChanged");
                render();
                repaint();
            }
        }
    }
    
    public class StateController implements StateListener {
        @Override
        public void currentChanged(ChangeEvent ce) {
            computeCurrentGeneOverlap();
            //System.out.println("Render from currentChanged");
            //render();
            repaint();
        }
        @Override
        public void mainPlotChanged(ChangeEvent ce) {
        }
        @Override
        public void thumbnailsChanged(ChangeEvent ce) { }
        
        @Override
        public void selectedAnnotationChanged(ChangeEvent ce) {
            repaint();
        }
        @Override
        public void currentAnnotationChanged(ChangeEvent ce) { 
            repaint();
        }
        @Override
        public void averagingWindowChanged(ChangeEvent ce) {    }
           
        @Override
        public void legendSelectedRowChanged(ChangeEvent ce) { }
        @Override
        public void heatmapChanged(ChangeEvent ce) { }
           

    }
    
    public class MouseController implements MouseListener, MouseMotionListener {
        @Override
        public void mouseEntered(MouseEvent me) {         }
        @Override
        public void mouseExited(MouseEvent me) { 
            Singleton.getState().setCurrentGeneAnnotation(null);
        }
        @Override
        public void mouseClicked(MouseEvent me) { 
            GeneAnnotation closestAnnotation = findClosestAnnotationInPanel(me.getX(), me.getY());
            if(closestAnnotation == null) {
                Singleton.getState().setSelectedGeneAnnotation(null);
            } else {
                if(SwingUtilities.isLeftMouseButton(me)) {
                    int currChromosome = AnnotationPanelWide.this.dataSet.getChromosome();
                    SelectedGeneAnnotation selected = new SelectedGeneAnnotation(closestAnnotation, currChromosome);
                    Singleton.getState().setSelectedGeneAnnotation(selected);
                } else if(SwingUtilities.isRightMouseButton(me)) {
                    try {
                        String geneName = closestAnnotation.getGene();
                        java.awt.Desktop.getDesktop().browse(java.net.URI.create(ucscUrl + geneName));
                    } catch(IOException ioe) {
                        System.out.println("IOException in reading " + ucscUrl);
                    }
                }
                
            }
        }
        @Override
        public void mousePressed(MouseEvent me) {      }
        @Override 
        public void mouseReleased(MouseEvent me) {     }
        @Override
        public void mouseDragged(MouseEvent me) {        }
        @Override
        public void mouseMoved(MouseEvent me) { 
            GeneAnnotation closestAnnotation = findClosestAnnotationInPanel(me.getX(), me.getY());
            Singleton.getState().setCurrentGeneAnnotation(closestAnnotation);
        }
        
    }
    
    public class ResizeController implements ComponentListener {
        @Override
        public void componentHidden(ComponentEvent ce) { }
        @Override
        public void componentMoved(ComponentEvent ce) { }
        @Override
        public void componentResized(ComponentEvent ce) { 
            //System.out.println("Annotation panel resized!!!");
            render();
            repaint();
        }
        @Override
        public void componentShown(ComponentEvent ce) { }
    }
    
    public class UserPrefController implements UserPrefListener {
        @Override
        public void colorChanged(ChangeEvent ce) {
            render();
            repaint();
        }
    }
}
