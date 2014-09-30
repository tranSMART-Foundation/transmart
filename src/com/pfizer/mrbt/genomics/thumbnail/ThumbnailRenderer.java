/**
 *
 */
package com.pfizer.mrbt.genomics.thumbnail;

import com.pfizer.mrbt.genomics.ManhattanPlot;
import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.data.SNP;
import com.pfizer.mrbt.genomics.data.SnpRecombRate;
import com.pfizer.mrbt.genomics.state.SelectedGeneAnnotation;
import com.pfizer.mrbt.genomics.state.State;
import com.pfizer.mrbt.genomics.state.ViewData;

import com.pfizer.mrbt.axis.AxisScale;
import java.awt.Color;
import java.awt.Component;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * @author Shreyas Dube; modified by Peter V. Henstock
 * @date Feb 19, 2009
 * @time 11:30:07 AM
 */
public class ThumbnailRenderer extends JComponent implements TableCellRenderer {
    private ViewData viewData = null;
    public static final int PREFERRED_WIDTH = 14;
    public static final int PREFERRED_HEIGHT = 14;    
    public final static int DOT_WIDTH   = 2;
    public final static Color BAND_COLOR = new Color(230, 250, 255);

    private AxisScale xAxis;
    private AxisScale yAxis;
    private AxisScale rightYAxis;
    private float hscale, vscale;
    private DataSet dataSet;
    private boolean isSelected = false;
    
  public ThumbnailRenderer(ViewData viewData) {
        super();
        this.viewData = viewData;
        this.dataSet = viewData.getDataSet();
  }

  /**
   * Initializes the xAxis and yAxis based on viewData
   */
  protected void setupAxes() {
    xAxis = viewData.getXAxis();
    yAxis = viewData.getYAxis();
    rightYAxis = viewData.getRightYAxis();
  }
  

  public ThumbnailRenderer() {
        super();
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus, int row,
                                                 int column) {
      if(value != null) {
          viewData = (ViewData) value;
          dataSet = viewData.getDataSet();
          JComponent jc = (JComponent) this;
          this.isSelected = isSelected;

          String toolTipStr = dataSet.getGeneRange().getName() + " " +
                              viewData.getModels().get(0).toString();
          
          jc.setToolTipText(toolTipStr);
      } else {
          viewData = null;
      }
    return this;
  }
  

    protected void render(Graphics2D g2) {
        System.out.println("Rendering thumbnail");
        updateScales();
        if(this.isSelected) {
            g2.setColor(Singleton.getUserPreferences().getThumbnailSelectionColor());
        } else {
            g2.setColor(Singleton.getUserPreferences().getThumbnailColor());
        }
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.DARK_GRAY);
        State state = Singleton.getState();
        /*g2.drawRect(state.getLeftPadding(), state.getThumbnailTopPadding(),
                             getWidth() - state.getLeftPadding() - state.getRightPadding() - 1,
                             getHeight() - state.getThumbnailTopPadding() - state.getBottomPadding() - 1);*/
        g2.drawRect(state.getLeftPadding(), 0,
                             getWidth() - state.getLeftPadding() - state.getRightPadding() - 1,
                             getHeight() - 0 - state.getBottomPadding() - 1);
        xAxis = viewData.getXAxis();
        yAxis = viewData.getYAxis();
        rightYAxis = viewData.getRightYAxis();
        //ArrayList<Integer> xPoints = dataSet.getSNPLoc();
        //int numPoints = xPoints.size();
        int topPadding = state.getThumbnailTopPadding();
        
        
        // horizontalband colors
        int thumbnailBandLevel = Singleton.getState().getThumbnailBandLevel();
        System.out.println("ThumbnailBandLevel " + thumbnailBandLevel);
        g2.setColor(Singleton.getUserPreferences().getThumbnailHorizontalBandColor());
        if(thumbnailBandLevel > 0) {
            for(int yLevel = thumbnailBandLevel; yLevel < yAxis.getMaxDisplayValue(); yLevel += thumbnailBandLevel*2) {
                int boxLowerY = yAxis.getRawPixelFromValue(yLevel, this);
                int boxUpperY = yAxis.getRawPixelFromValue(Math.min(yLevel+thumbnailBandLevel, yAxis.getMaxDisplayValue()), this);
                g2.fillRect(state.getLeftPadding()+1, 
                            boxUpperY, 
                            getWidth() - state.getLeftPadding() - state.getRightPadding() - 2,
                            boxLowerY - boxUpperY);                          
            }
        }
        
        // selected gene annotation drawn as vertical bar
        SelectedGeneAnnotation selectedGeneAnnotation = Singleton.getState().getSelectedGeneAnnotation();
        if (selectedGeneAnnotation != null
            && selectedGeneAnnotation.getChromosome() == viewData.getDataSet().getChromosome()) {
            int start = selectedGeneAnnotation.getStart();
            int end = selectedGeneAnnotation.getEnd();
            int startx = xAxis.getRawPixelFromValue(start, this);
            int endx = Math.max(startx + 1, xAxis.getRawPixelFromValue(end, this));
            if (selectedGeneAnnotation.overlaps(xAxis.getMinDisplayValue(), xAxis.getMaxDisplayValue())) {
                g2.setColor(Singleton.getUserPreferences().getThumbnailSelectionBandColor());
                g2.fillRect(startx,
                            state.getTopPadding(),
                            endx - startx,
                            getHeight() - state.getTopPadding() - state.getBottomPadding());
            }
        }
        

        
        int studySetModelIndex = 0;
        
        //drawRecombinationRate(g2);
        
        CopyOnWriteArrayList<SNP> snps = dataSet.getSnps();
        int modelIndex = 0;
        int dotSize = Singleton.getState().getThumbnailDotSize();
        for (Model model : viewData.getModels()) {
            System.out.println("Rendering with model " + model.toString() + "\thaving #snps: "  + snps.size());
            Color modelColor = Singleton.getState().getPlotColor(modelIndex);
            g2.setColor(Singleton.getUserPreferences().getThumbnailPointColor());
            for (SNP snp : snps) {
                Double logpval = dataSet.getPvalFromSnpModel(snp, model);
                if (logpval != null) {
                    double imagex = xAxis.getRawPixelFromValue(snp.getLoc(), this);
                    double imagey = yAxis.getRawPixelFromValue(logpval, this);
                    if (imagey < topPadding) { // top points stay on image
                        imagey = topPadding;
                    }
                    g2.fillRect((int) Math.round(imagex),
                                         (int) Math.round(imagey), dotSize, dotSize);
                }
            }
            modelIndex++;
        }
    }
    
    /**
     * Recomputes the hscale and vscale and submits them to update view scales
     */
    protected void updateScales() {
        hscale = 1f;
        vscale = 1f;
        viewData.setHscale(hscale);
        viewData.setVscale(vscale);
    }



  
  @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      System.out.println("Painting thumbnail component with viewdata = " + viewData);
        if(viewData != null) {
            Graphics2D g2 = (Graphics2D) g;
            render(g2);
            drawTitle(g2);
            //g2.setColor(Color.cyan);
            setOpaque(true);
            /*int height = getHeight();
            int width  = getWidth();
            g2.fillRect(0, 0, width, height);
            g2.setColor(Color.RED);
            
            int vdid = viewData.getViewId();
            FontRenderContext frc = g2.getFontRenderContext();
            Font font = new Font("Arial", Font.PLAIN, 12);
            TextLayout tl = new TextLayout(vdid + "", font, frc);
            tl.draw(g2, 5, 5);*/

        }
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
        Font font = new Font("Arial", Font.BOLD, 9);
        String str = dataSet.getGeneRange().getName() + "  Chr" + dataSet.getChromosome();
        g2.setColor(Singleton.getUserPreferences().getThumbnailTextColor());
        TextLayout tl = new TextLayout(str, font, frc);
        int x = 2;
        int y = 10;
        tl.draw(g2, x, y);
        
        /*for (Model model : viewData.getModels()) {
            System.out.println("Rendering with model " + model.toString() + "\thaving #snps: "  + snps.size());*/
        String modelStr = viewData.getModels().get(0).toString();
        //String modelStr = dataSet.getModels().get(0).toString();
        TextLayout modelTl = new TextLayout(modelStr, font, frc);
        int INTER_TITLE_BREAK = 20;
        int modelX = x;
        int modelY = 20;
        if(tl.getBounds().getWidth() + modelTl.getBounds().getWidth() + INTER_TITLE_BREAK < getWidth()) {
            modelY = y;
            modelX = x + (int) Math.round(tl.getBounds().getWidth()) + INTER_TITLE_BREAK;
        }
        modelTl.draw(g2, modelX, modelY);
    }


  
      
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PREFERRED_WIDTH, PREFERRED_HEIGHT);
    }
    
    /*protected void drawRecombinationRate(Graphics2D offscreenG2) {
        ArrayList<SnpRecombRate> snpRecombRates = dataSet.getSnpRecombRates();
        int prevImagex  = -1;
        int prevImagey  = -1;
        //offscreenG2.setColor(new Color(189,252,201));
        offscreenG2.setColor(new Color(244,164,96));
        
        //offscreenG2.setColor(new Color(100, 149, 237));
        int minX = (int) xAxis.getMinDisplayValue();
        int maxX = (int) xAxis.getMaxDisplayValue();
        for(SnpRecombRate currSnpRecomb : snpRecombRates) {
            int x = currSnpRecomb.getSnp();
            if(x > minX && x < maxX) {
                float y = currSnpRecomb.getRecombRate();
                int imagex = xAxis.getRawPixelFromValue(x, this);
                int imagey = rightYAxis.getRawPixelFromValue(y, this);
                if(prevImagey > -1) {
                    //offscreenG2.setColor(new Color(189,252,201));
                    //offscreenG2.setColor(new Color(100, 149, 237));
                    offscreenG2.drawLine(prevImagex, prevImagey, imagex, imagey);
                }
                //offscreenG2.setColor(Color.RED);
                offscreenG2.drawOval(imagex, imagey, 1,1);
                prevImagex = imagex;
                prevImagey = imagey;
            }
        }
    }*/


}
