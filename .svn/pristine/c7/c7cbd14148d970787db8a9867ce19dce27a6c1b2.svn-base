/*
 * View Data contains the components that will represent how to make a particular
 * plot but will exclude the listeners that are in View
 */
package com.pfizer.mrbt.genomics.state;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.genomics.data.NumericRange;
import com.pfizer.mrbt.genomics.hline.HLine;
import com.pfizerm.mrbt.axis.AxisScale;
import com.pfizerm.mrbt.axis.LinearAxisScale;
import java.util.ArrayList;

/**
 *
 * @author henstockpv
 */
public class ViewData {
    private static int numViews = 0;
    private ArrayList<Model> models = new ArrayList<Model>();
    private AxisScale xAxis;
    private AxisScale yAxis;
    private AxisScale rightYAxis;
    private int viewId;
    private DataSet dataSet = null;
    private double hScale;
    private double vScale;
    private ArrayList<HLine> hLines = new ArrayList<HLine>();
    
    
    public ViewData(DataSet dataSet) {
        this.dataSet = dataSet;
        this.viewId = numViews;
        numViews++;
    }
    
    /*public ViewData() {
        this.viewId = numViews;
        numViews++;
    }*/
    
    
    
    public AxisScale getXAxis() {
        if(xAxis==null) {
            NumericRange xRange = dataSet.getXRange();
            //System.out.println("create x axis " + xRange.getMin() + "\t" + xRange.getMax());
            xAxis = new LinearAxisScale(xRange.getMin(), xRange.getMax(), AxisScale.HORIZONTAL, this);
        }
        return xAxis;
    }

    /**
     * Creates a y-axis based on the maximum y-range of the modelIndices in the
     * view
     * @param dataSet
     * @return 
     */
    public AxisScale getYAxis() {
        if(yAxis==null) {
            double maxValue = Double.NEGATIVE_INFINITY;
            for(Model model : dataSet.getModels()) {
                if(models.contains(model)) {
                    maxValue = Math.max(maxValue, dataSet.getYRange(model).getMax());
                }
            }
            if(maxValue <= 0) {
                maxValue = 1.0;
            }
            if(maxValue < Singleton.getUserPreferences().getMinTopNegLogPvalAxis()) {
                maxValue = Singleton.getUserPreferences().getMinTopNegLogPvalAxis();
            }
            yAxis = new LinearAxisScale(0, maxValue, AxisScale.VERTICAL, this);
        }
        return yAxis;
    }
    
    public void setXAxis(AxisScale xAxis) {
        this.xAxis = xAxis;
    }

    public void setYAxis(AxisScale yAxis) {
        this.yAxis = yAxis;
    }
    
    public void setRightYAxis(AxisScale rightYAxis) {
        this.rightYAxis = rightYAxis;
    }
    
    public AxisScale getRightYAxis() {
        if(rightYAxis == null) {
          double maxValue = dataSet.getMaxRecombinationRate();
          if(maxValue <= 0.0) {
              maxValue = 1.0;
          }
          if(maxValue < Singleton.getUserPreferences().getMinTopRecombinationRateAxis()) {
              maxValue = Singleton.getUserPreferences().getMinTopRecombinationRateAxis();
          }
          rightYAxis = new LinearAxisScale(0, maxValue, AxisScale.VERTICAL, this);
        }
        return rightYAxis;   
    }
    
    public boolean containsModel(Model model) {
        if(models.contains(model)) {
            return true;
        } else {
            return false;
        }
    }
    
    public void addModel(Model model) {
        if(! models.contains(model)) {
            models.add(model);
        }
    }
    
    /**
     * Returns the list of models that are being displayed in the current view
     * @return 
     */
    public ArrayList<Model> getModels() {
        return models;
    }
    
    public void removeModel(Model model) {
        if(models.contains(model)) {
            models.remove(model);
        }
    }
    
    /**
     * Changes the zoom to the original zoom for the x-axis  and firesZoomChanged
     */
    public void xZoomToOriginal() {
        xAxis.zoomToOriginal();
        //fireZoomChanged();
    }
    
    /**
     * Changes the zoom to the original zoom for the y-axis and firesZoomChanged
     */
    public void yZoomToOriginal() {
        yAxis.zoomToOriginal();
        //fireZoomChanged();
    }
    
    /**
     * Changes the zoom to the original zoom for the y-axis and firesZoomChanged
     */
    public void rightYZoomToOriginal() {
        rightYAxis.zoomToOriginal();
        //fireZoomChanged();
    }
    
    /**
     * Zooms out from the current setting for the x-axis and firesZoomChanged
     */
    public void xZoomOut() {
        xAxis.zoomOut();
        //fireZoomChanged();
    }
    
    /**
     * Zooms out from the current setting for the y-axis and firesZoomChanged
     */
    public void yZoomOut() {
        yAxis.zoomOut();
        //fireZoomChanged();
    }
    
    /**
     * Zooms out from the current setting for the y-axis and firesZoomChanged
     */
    public void rightYZoomOut() {
        rightYAxis.zoomOut();
        //fireZoomChanged();
    }
    
    /**
     * Zooms in from the current setting for the x-axis and firesZoomChanged
     */
    public void xZoomIn() {
        xAxis.zoomIn();
        //fireZoomChanged();
    }
    
    /**
     * Zooms in from the current setting for the y-axis and firesZoomChanged
     */
    public void yZoomIn() {
        yAxis.zoomIn();
        //fireZoomChanged();
    }
    
    /**
     * Zooms in from the current setting for the y-axis and firesZoomChanged
     */
    public void rightYZoomIn() {
        rightYAxis.zoomIn();
        //fireZoomChanged();
    }
    
    /**
     * Zooms to smaller selection from the current setting for the x-axis and firesZoomChanged
     */
    public void xZoomToRange(double left, double right) {
        xAxis.zoomToRange(left, right);
        //fireZoomChanged();
    }
    
    /**
     * Zooms to smaller selection from the current setting for the x-axis and firesZoomChanged
     */
    public void yZoomToRange(double bottom, double top) {
        yAxis.zoomToRange(bottom, top);
        //fireZoomChanged();
    }
    
    /**
     * Zooms to smaller selection from the current setting for the x-axis and firesZoomChanged
     */
    public void rightYZoomToRange(double bottom, double top) {
        rightYAxis.zoomToRange(bottom, top);
        //fireZoomChanged();
    }
    
    /**
     * Shifts the x-axis by 20% to the left
     */
    public void shiftLeft() {
        xAxis.shiftLeft();
        //fireZoomChanged();
    }
    
    /**
     * Shifts the x-axis by 20% tot he right
     */
    public void shiftRight() {
        xAxis.shiftRight();
        //fireZoomChanged();
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public int getViewId() {
        return viewId;
    }

    public double getHscale() {
        return hScale;
    }

    public void setHscale(double hScale) {
        this.hScale = hScale;
    }

    public double getVscale() {
        return vScale;
    }

    public void setVscale(double vScale) {
        this.vScale = vScale;
    }
    
    public void addHLine(HLine hLine) {
        if(! hLines.contains(hLine)) {
            hLines.add(hLine);
        }
    }
    
    public ArrayList<HLine> getHLines() {
        return hLines;
    }
}
