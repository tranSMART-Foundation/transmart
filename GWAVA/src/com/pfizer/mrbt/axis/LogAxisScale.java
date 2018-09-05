/*
 * Linear axis scaling and conversion of values
 */

package com.pfizer.mrbt.axis;


import com.pfizer.mrbt.axis.AxisScale;
import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.state.State;
import com.pfizer.mrbt.genomics.state.View;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import javax.swing.JComponent;

/**
 *
 * @author Peter V. Henstock
 */
public class LogAxisScale extends AxisScale {
    private double minDisplayValue;
    private double maxDisplayValue;
    private double origMin;
    private double origMax;
    private double[] majorTicks;
    private double[] minorTicks;
    private double majorTickSpacing;
    private boolean isVertical;
    private State state;
    private View view;
    
    public LogAxisScale(double minValue, double maxValue, int orientation, View view) {
        setMinMaxValue(minValue, maxValue);
        this.view = view;
        state = Singleton.getState();
        isVertical = (orientation == AxisScale.VERTICAL);
    }

    /**
     * Sets the range of values outside of which the display values are computed
     * @param minValue
     * @param maxValue
     */
    public void setMinMaxValue(double minValue, double maxValue) {
        computeMajorLogTickSpacing(minValue, maxValue);
        maxDisplayValue = Math.pow(10.0, majorTickSpacing * (float) Math.ceil(Math.log10(maxValue) / majorTickSpacing));
        minDisplayValue = Math.pow(10.0, majorTickSpacing * (float) Math.floor(Math.log10(minValue) / majorTickSpacing));
        origMin = minDisplayValue;
        origMax = maxDisplayValue;
        int numTicks = (int) Math.round((Math.log10(maxDisplayValue) - Math.log10(minDisplayValue)) / majorTickSpacing) + 1;
        majorTicks = new double[numTicks];
        for(int i = 0; i < numTicks; i++) {
            majorTicks[i] = Math.pow(10.0, Math.log10(minDisplayValue) + majorTickSpacing * i);
        }
    }

    /*
     * Sets the absolute display values inside which the tick spacings are
     * computed including the end points
     */
    public void setDisplayMinMaxValue(double minValue, double maxValue) {
        computeMajorLogTickSpacing(minValue, maxValue);
        this.maxDisplayValue = maxValue;
        this.minDisplayValue = minValue;
        double minInteriorTick = Math.pow(10.0, majorTickSpacing * (float) Math.floor(Math.log10(maxValue) / majorTickSpacing));
        double maxInteriorTick = Math.pow(10.0, minDisplayValue = majorTickSpacing * (float) Math.ceil(Math.log10(minValue) / majorTickSpacing));
        int numTicks = (int) Math.round((Math.log10(maxInteriorTick) - Math.log10(maxInteriorTick))/majorTickSpacing) + 1;
        double fraction = (Math.log10(maxValue) - Math.log10(minValue)) / 200.0;
        boolean minSameAsInteriorMin = Math.abs(Math.log10(minValue) - Math.log10(minInteriorTick)) < fraction;
        boolean maxSameAsInteriorMax = Math.abs(Math.log10(maxValue) - Math.log10(maxInteriorTick)) < fraction;
        majorTicks = new double[numTicks];
        if(! minSameAsInteriorMin) {
           numTicks++;
        }
        if(! maxSameAsInteriorMax) {
            numTicks++;
        }
        int index = 0;
        if(! minSameAsInteriorMin) {
            majorTicks[0] = minValue;
            index = 1;
        }
        for(int i = 0; i < numTicks; i++) {
            majorTicks[index++] = Math.pow(10.0, Math.log10(minInteriorTick) + majorTickSpacing * i);
        }
        if(! maxSameAsInteriorMax) {
            majorTicks[index] = maxValue;
        }
    }

    /**
     * Computes tickSpacing, maxDisplayValue, and minDisplayValue so that it
     * finds a nice range of values to plot that span the max/min range and
     * have effective tick spacing.  The tick spacing is in the log scale
     */
    private void computeMajorLogTickSpacing(double minValue, double maxValue) {
      double difference = Math.log10(maxValue) - Math.log10(minValue);
      int logTick = (int) Math.floor(Math.log10(difference));
      majorTickSpacing = Math.pow(10.0, logTick * 1.0);
      if (difference / majorTickSpacing < 6.0) {
        majorTickSpacing = majorTickSpacing / 2.0;
        if (difference / majorTickSpacing < 6.0) {
          majorTickSpacing = majorTickSpacing / 2.0;
          if (difference / majorTickSpacing < 6.0) {
            majorTickSpacing = majorTickSpacing * 4.0 / 1.0;
          }
        }
      }
    }



    /**
     * Returns the number of ticks recommended
     * @return
     */
    public int getNumMajorTicks() {
        return majorTicks.length;
    }

    /**
     * Returns the minimum value of the displayed axis
     * @return
     */
    public double getMinDisplayValue() {
        return minDisplayValue;
    }

    public double getMaxDisplayValue() {
        return maxDisplayValue;
    }

    /**
     * Returns a vector of the major tick values
     * @return
     */
    public double[] getMajorTickLocations() {
        return majorTicks;
    }

    /**
     * Returns a vector of the minor tick values
     * @return
     */
    public double[] getMinorTickLocations() {
        return minorTicks;
    }

    /**
     * Returns the pretty-printed tick value
     * @param index
     * @return
     */
    public String getMajorTickString(int index) {
        String returnStr = formatString(majorTicks[index]);
        return returnStr;
    }

    public String formatString(double value) {
        String returnStr = "";
        if(value > 10000 || value < 1E-4) {
            DecimalFormat sciformat = new DecimalFormat("0.0E0");
            returnStr = sciformat.format(value);
        } else if(value < 0.001) {
            DecimalFormat format = new DecimalFormat("0.0000");
            returnStr = format.format(value);
        } else if(value < 0.01) {
            DecimalFormat format = new DecimalFormat("0.000");
            returnStr = format.format(value);
        } else if(value < 0.1) {
            DecimalFormat format = new DecimalFormat("0.00");
            returnStr = format.format(value);
        } else if(value > 10) {
            DecimalFormat format = new DecimalFormat("0");
            returnStr = format.format(value);
        } else {
            DecimalFormat format = new DecimalFormat("0.0");
            returnStr = format.format(value);
        }
        return returnStr;
    }

    /**
     * Returns whether linear or log scale is used
     * @return
     */
    @Override
    public boolean useLogScale() {
        return true;
    }

    /**
     * If the image is on the buffered image, returns the representing pixel
     * Know the min and max vlaue of the range.  From state, we can get the
     * padding.  The dimPixels provides the number of pixels between the
     * padding.  We know in the class the rnage of display values so we can
     * compute the pixel
     * @param value
     * @return
     */
    @Override
    public int getImagePixelFromValue(double value, BufferedImage image) {
        int pixelRange = 0;
        if(isVertical) {
            double fraction = (Math.log10(maxDisplayValue) - Math.log10(value)) / (Math.log10(maxDisplayValue) - Math.log10(minDisplayValue));
            pixelRange = image.getHeight() - state.getTopPadding() - state.getBottomPadding();
            int pixel = state.getTopPadding() + (int) Math.round(fraction * pixelRange);
            return pixel;
        } else {
            double fraction = (Math.log10(value) - Math.log10(minDisplayValue)) / (Math.log10(maxDisplayValue) - Math.log10(minDisplayValue));
            pixelRange = image.getWidth() - state.getLeftPadding() - state.getRightPadding();
            int pixel = state.getLeftPadding() + (int) Math.round(fraction * pixelRange);
            return pixel;
        }
    }

    /**
     * If the image is drawn on the final screen, returns the representing pixel
     * @param value
     * @return
     */
    @Override
    public int getRawPixelFromValue(double value, JComponent image) {
        double pixelRange = 0.0;
        if(isVertical) {
            double fraction = (Math.log10(maxDisplayValue) - Math.log10(value)) / (Math.log10(maxDisplayValue) - Math.log10(minDisplayValue));
            double vscale = view.getVscale();
            pixelRange =  image.getHeight()*1.0 - state.getTopPadding()*vscale - state.getBottomPadding()*vscale;
            int pixel = (int) Math.round(vscale * state.getTopPadding() + fraction * pixelRange);
            return pixel;
        } else {
            double fraction = (Math.log10(value) - Math.log10(minDisplayValue)) / (Math.log10(maxDisplayValue) - Math.log10(minDisplayValue));
            double hscale = view.getHscale();
            pixelRange = image.getWidth()*1.0 - state.getLeftPadding()*hscale - state.getRightPadding()*hscale;
            int pixel = (int) Math.round(hscale * state.getLeftPadding() + fraction * pixelRange);
            return pixel;
        }
    }

        /**
     * Not implemented for the logAxisScale
     * @param value
     * @param image
     * @param startPadSize
     * @param endPadSize
     * @return 
     */
    @Override
    public int getPaddedRawPixelFromValue(double value, JComponent image, int startPadSize, int endPadSize) {
        System.out.println("This function is not implemented for the log axis scale!!!");
        return getRawPixelFromValue(value, image);
    }
    

    /**
     * If the image is on the buffered image, returns the value from the pixel.
     * Useful for mouse click interpretation
     * @param value
     * @return
     */
    @Override
    public double getValueFromImagePixel(int pixel, BufferedImage image) {
        return 0;
    }

    /**
     * If the image is on the non-buffered final image, returns the value from
     * the pixel.  Useful for mouse click interpretation
     * @param value
     * @return
     */
    @Override
    public double getValueFromRawPixel(int pixel, BufferedImage image) {
        if(isVertical) {
            double xformed = pixel / view.getVscale();
            double fromTop = xformed - state.getTopPadding();
            int pixHeight = image.getHeight() - state.getTopPadding() - state.getBottomPadding();
            double fraction = fromTop / pixHeight;
            double value = Math.log10(maxDisplayValue) - (Math.log10(maxDisplayValue) - Math.log10(minDisplayValue)) * fraction;
            return Math.pow(10.0, value);
        } else {
            double xformed = pixel / view.getHscale();
            double fromLeft = xformed - state.getLeftPadding();
            int pixWidth = image.getWidth() - state.getLeftPadding() - state.getRightPadding();
            double fraction = fromLeft / pixWidth;
            double value = Math.log10(minDisplayValue) + (Math.log10(maxDisplayValue) - Math.log10(minDisplayValue)) * fraction;
            return Math.pow(10.0, value);
        }
    }
    
    /**
     * Zooms out by 20% on each side
     */
    @Override
    public void zoomOut() {
        double range = maxDisplayValue - minDisplayValue;
        double newMaxValue = maxDisplayValue + range / 5.0;
        double newMinValue = minDisplayValue - range / 5.0;
        setDisplayMinMaxValue(newMinValue, newMaxValue);
    }
        
    /**
     * Zooms in by 20% on each side
     */
    @Override
    public void zoomIn() {
        double range = maxDisplayValue - minDisplayValue;
        double newMaxValue = maxDisplayValue - range / 5.0;
        double newMinValue = minDisplayValue + range / 5.0;
        setDisplayMinMaxValue(newMinValue, newMaxValue);
    }
    
    /**
     * Zooms to the original value when constructed
     */
    @Override
    public void zoomToOriginal() {
        setDisplayMinMaxValue(origMin, origMax);
        lastShiftQuantity = NULL_QUANTITY;
        lastScaleQuantity = NULL_QUANTITY;
    }
    
    /**
     * Zooms to the specified min/max value
     */
    @Override
    public void zoomToRange(double min, double max) {
        setDisplayMinMaxValue(min, max);
    }
    
    /**
     * Shifts axis by 20% on each side
     */
    @Override
    public void shiftLeft() {
        double range = maxDisplayValue - minDisplayValue;
        double newMaxValue = maxDisplayValue - range / 5.0;
        double newMinValue = minDisplayValue - range / 5.0;
        setDisplayMinMaxValue(newMinValue, newMaxValue);
    }
    
    /**
     * Shifts axis by 20% on each side
     */
    @Override
    public void shiftRight() {
        double range = maxDisplayValue - minDisplayValue;
        double newMaxValue = maxDisplayValue + range / 5.0;
        double newMinValue = minDisplayValue + range / 5.0;
        setDisplayMinMaxValue(newMinValue, newMaxValue);
    }
    

    /**
     * If the image is on the non-buffered final image, returns the value from
     * the pixel.  Useful for mouse click interpretation
     * Copied this over from linearAxisScale so it would compile.
     * @param value
     * @return
     */
    public double getValueFromRawPixel(int pixel, int widthOrHeight) {
        if(isVertical) {
            double xformed = pixel / view.getVscale();
            double fromTop = xformed - state.getTopPadding();
            int pixHeight = widthOrHeight - state.getTopPadding() - state.getBottomPadding();
            double fraction = fromTop / pixHeight;
            double value = maxDisplayValue - (maxDisplayValue - minDisplayValue) * fraction;
            return value;
        } else {
            double xformed = pixel / view.getHscale();
            double fromLeft = xformed - state.getLeftPadding();
            int pixWidth = widthOrHeight - state.getLeftPadding() - state.getRightPadding();
            double fraction = fromLeft / pixWidth;
            double value = minDisplayValue + (maxDisplayValue - minDisplayValue) * fraction;
            return value;
        }
    }
    
    @Override
    public double getOrigMinValue() {
        return origMin;
    }
    
    @Override
    public double getOrigMaxValue() {
        return origMax;
    }


}
