/*
 * The interface to define the linear and log values of the range of values
 */
package com.pfizer.mrbt.axis;

import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 *
 * @author henstockpv
 */
public abstract class AxisScale {
    public final static int HORIZONTAL = 20;
    public final static int VERTICAL   = 21;
    public final static double NULL_QUANTITY = -999.0;
    private static int axisNumber = 0;
    protected int axisId;
    protected double lastShiftQuantity = NULL_QUANTITY;
    protected double lastScaleQuantity = NULL_QUANTITY;

    public AxisScale() {
        axisId = ++axisNumber;
    }

    /**
     * Sets the range of values outside of which the display values are computed
     * @param minValue
     * @param maxValue
     */
    public abstract void setMinMaxValue(double minValue, double maxValue);

    /*
     * Sets the absolute display values inside which the tick spacings are
     * computed including the end points
     */
    public abstract void setDisplayMinMaxValue(double minValue, double maxValue);

    /**
     * Returns the number of ticks recommended
     * @return
     */
    public abstract int getNumMajorTicks();

    /**
     * Returns the minimum value of the displayed axis
     * @return
     */
    public abstract double getMinDisplayValue();

    public abstract double getMaxDisplayValue();

    /**
     * Returns a vector of the major tick values
     * @return
     */
    public abstract double[] getMajorTickLocations();

    /**
     * Returns a vector of the minor tick values that includes the major ticks
     * @return
     */
    public abstract double[] getMinorTickLocations();

    /**
     * Returns the pretty-printed tick value
     * @param index
     * @return
     */
    public abstract String getMajorTickString(int index);

    /**
     * Returns whether linear or log scale is used
     * @return
     */
    public abstract boolean useLogScale();

    /**
     * If the image is on the buffered image, returns the representing pixel
     * @param value
     * @return
     */
    public abstract int getImagePixelFromValue(double value, BufferedImage image);

    /**
     * If the image is drawn on the final screen, returns the representing pixel
     * @param value
     * @return
     */
    public abstract int getRawPixelFromValue(double value, JComponent image);

    /**
     * If the image is drawn on the final screen, returns the representing pixel.
     * This takes into account the fact that the first startPadSize and endPadSize
     * are not included in the mapping and are outside the range of values.  
     * This is for the annotation panel extends beyond the plot
     * @param value
     * @return
     */
    public abstract int getPaddedRawPixelFromValue(double value, JComponent image, int startPadSize, int endPadSize);

    /**
     * If the image is on the buffered image, returns the value from the pixel.
     * Useful for mouse click interpretation
     * @param value
     * @return
     */
    public abstract double getValueFromImagePixel(int pixel, BufferedImage image);

    /**
     * If the image is on the non-buffered final image, returns the value from
     * the pixel.  Useful for mouse click interpretation
     * @param value
     * @return
     */
    public abstract double getValueFromRawPixel(int pixel, BufferedImage image);

    public abstract String formatString(double value);
    
    public abstract void zoomIn();
    
    public abstract void zoomOut();
    
    public abstract void zoomToOriginal();
    
    public abstract void zoomToRange(double min, double max);
    
    public abstract void shiftLeft();
    public abstract void shiftRight();
    
    public abstract double getValueFromRawPixel(int pixel, int widthOrHeight);
    
    /**
     * Returns the axisID which is useful for keeping track of unique axes that
     * has implications for updating axes.
     * @return 
     */
    public int getAxisId() {
        return axisId;
    }
    
    public abstract double getOrigMinValue();
    
    public abstract double getOrigMaxValue();

}
