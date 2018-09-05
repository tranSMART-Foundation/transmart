/*
 * This contains the specific information on how to create a single view of
 * at least one DataSet within the model.  The idea is that the view is a
 * fixed box in the panel that can substitute in/out different data sets, models
 * or zooms.  These are done with the internal ViewData.  This way the listeners
 * don't have to worry about being turned on and off.
 */
package com.pfizer.mrbt.genomics.state;

import com.pfizer.mrbt.genomics.data.DataSet;
import com.pfizer.mrbt.genomics.data.Model;
import com.pfizer.mrbt.axis.AxisScale;
import java.util.ArrayList;

/**
 *
 * @author henstockpv
 */
public class View {
    private ViewData viewData;
    private ArrayList<ViewListener> listeners = new ArrayList<ViewListener>();

    
    public View(ViewData viewData) {
        this.viewData = viewData;
    }
    
    public View() {
    }
    
    public void setViewData(ViewData viewData) {
        this.viewData = viewData;
    }
    
    public AxisScale getXAxis() {
        return viewData.getXAxis();
    }

    /**
     * Creates a y-axis based on the maximum y-range of the modelIndices in the
     * view
     * @param dataSet
     * @return 
     */
    public AxisScale getYAxis() {
        return viewData.getYAxis();
    }
    
    public AxisScale getRightYAxis() {
        return viewData.getRightYAxis();
    }
    
    public void resetRightYAxis() {
        viewData.setRightYAxis(null);
    }
    
    public boolean containsModel(Model model) {
        return viewData.getModels().contains(model);
    }
    
    public void addModel(Model model) {
        viewData.addModel(model);
    }
    
    /**
     * Returns the list of models that are being displayed in the current view
     * @return 
     */
    public ArrayList<Model> getModels() {
        return viewData.getModels();
    }
    
    public Model getModel(int index) {
            return viewData.getModels().get(index);
    }
    
    public void removeModel(Model model) {
        viewData.removeModel(model);
    }
    
    /**
     * Changes the zoom to the original zoom for the x-axis  and firesZoomChanged
     */
    public void xZoomToOriginal() {
        viewData.getXAxis().zoomToOriginal();
        fireZoomChanged(AxisChangeEvent.XAXIS);
    }
    
    /**
     * Changes the zoom to the original zoom for the y-axis and firesZoomChanged
     */
    public void yZoomToOriginal() {
        viewData.getYAxis().zoomToOriginal();
        fireZoomChanged(AxisChangeEvent.YAXIS);
    }
    
    /**
     * Changes the zoom to the original zoom for the y-axis and firesZoomChanged
     */
    public void rightYZoomToOriginal() {
        viewData.getRightYAxis().zoomToOriginal();
        fireZoomChanged(AxisChangeEvent.RIGHT_YAXIS);
    }
    
    /**
     * Zooms out from the current setting for the x-axis and firesZoomChanged
     */
    public void xZoomOut() {
        viewData.getXAxis().zoomOut();
        fireZoomChanged(AxisChangeEvent.XAXIS);
    }
    
    /**
     * Zooms out from the current setting for the y-axis and firesZoomChanged
     */
    public void yZoomOut() {
        viewData.getYAxis().zoomOut();
        fireZoomChanged(AxisChangeEvent.YAXIS);
    }
    
    /**
     * Zooms out from the current setting for the y-axis and firesZoomChanged
     */
    public void rightYZoomOut() {
        viewData.getRightYAxis().zoomOut();
        fireZoomChanged(AxisChangeEvent.RIGHT_YAXIS);
    }
    
    /**
     * Zooms in from the current setting for the x-axis and firesZoomChanged
     */
    public void xZoomIn() {
        viewData.getXAxis().zoomIn();
        fireZoomChanged(AxisChangeEvent.XAXIS);
    }
    
    /**
     * Zooms in from the current setting for the y-axis and firesZoomChanged
     */
    public void yZoomIn() {
        viewData.getYAxis().zoomIn();
        fireZoomChanged(AxisChangeEvent.YAXIS);
    }
    
    /**
     * Zooms in from the current setting for the y-axis and firesZoomChanged
     */
    public void rightYZoomIn() {
        viewData.getRightYAxis().zoomIn();
        fireZoomChanged(AxisChangeEvent.RIGHT_YAXIS);
    }
    
    /**
     * Zooms to smaller selection from the current setting for the x-axis and firesZoomChanged
     */
    public void xZoomToRange(double left, double right) {
        viewData.getXAxis().zoomToRange(left, right);
        fireZoomChanged(AxisChangeEvent.XAXIS);
    }
    
    /**
     * Zooms to smaller selection from the current setting for the x-axis and firesZoomChanged
     */
    public void yZoomToRange(double bottom, double top) {
        viewData.getYAxis().zoomToRange(bottom, top);
        fireZoomChanged(AxisChangeEvent.YAXIS);
    }
    
    /**
     * Zooms to smaller selection from the current setting for the x-axis and firesZoomChanged
     */
    public void rightYZoomToRange(double bottom, double top) {
        viewData.getRightYAxis().zoomToRange(bottom, top);
        System.out.println("RightYZoom " + bottom + " to " + top);
        fireZoomChanged(AxisChangeEvent.RIGHT_YAXIS);
    }
    
    /**
     * Shifts the x-axis by 20% to the left
     */
    public void shiftLeft() {
        viewData.getXAxis().shiftLeft();
        fireZoomChanged(AxisChangeEvent.XAXIS);
    }
    
    /**
     * Shifts the x-axis by 20% tot he right
     */
    public void shiftRight() {
        viewData.getXAxis().shiftRight();
        fireZoomChanged(AxisChangeEvent.XAXIS);
    }
    
    public void addListener(ViewListener viewListener) {
        if(listeners.contains(viewListener)) {
            System.out.println("Adding same listener twice");
        } else {
            listeners.add(viewListener);
        }
    }
    
    public void removeListener(ViewListener viewListener) {
        if(! listeners.contains(viewListener)) {
            System.out.println("Trying to remove non-existent listener");
        } else {
            listeners.remove(viewListener);
        }
    }
    
    public void fireZoomChanged(int axisChanged) {
        AxisChangeEvent changeEvent = new AxisChangeEvent(this, axisChanged);
        System.out.println("Firing zoom Changed to " + listeners.size() + " listeners");
        for(ViewListener viewListener : listeners) {
            viewListener.zoomChanged(changeEvent);
        }
    }
    
    public double getHscale() {
        return viewData.getHscale();
    }

    public void setHscale(double hScale) {
        viewData.setHscale(hScale);
    }

    public double getVscale() {
        return viewData.getVscale();
    }

    public void setVscale(double vScale) {
        viewData.setVscale(vScale);
    }

    public DataSet getDataSet() {
        return viewData.getDataSet();
    }

    public int getViewId() {
        return viewData.getViewId();
    }
    
    public ViewData getViewData() {
        return viewData;
    }
}
