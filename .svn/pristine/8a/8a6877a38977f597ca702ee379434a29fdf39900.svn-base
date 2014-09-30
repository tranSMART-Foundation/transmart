/*
 * Linear axis scaling and conversion of values
 */

package com.pfizerm.mrbt.axis;

import com.pfizer.mrbt.genomics.Singleton;
import com.pfizer.mrbt.genomics.state.State;
import com.pfizer.mrbt.genomics.state.ViewData;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import javax.swing.JComponent;

/**
 *
 * @author Peter V. Henstock
 */
public class LinearAxisScale extends AxisScale {
    private double minDisplayValue;
    private double maxDisplayValue;
    private double origMin;
    private double origMax;
    private double[] majorTicks;
    private double[] minorTicks;
    private double majorTickSpacing;
    private State state;
    private boolean isVertical;
    private ViewData viewData;
    private boolean hasMin0 = true; // kluge added to prevent negative numbers
    
    
    public LinearAxisScale(double minValue, double maxValue, int orientation, ViewData viewData) {
        this.viewData = viewData;
        state = Singleton.getState();        
        //System.out.println("Defining axis [" + minValue + "\t" + maxValue + "]");
        setMinMaxValue(minValue, maxValue);
        origMin = minDisplayValue;
        origMax = maxDisplayValue;
        isVertical = (orientation == AxisScale.VERTICAL);
    }

    /**
     * Sets the range of values outside of which the display values are computed
     * Currently the use of the minorTicks for the linear axis is not implemented
     * @param minValue
     * @param maxValue
     */
    public void setMinMaxValue(double minValue, double maxValue) {
        computeMajorTickSpacing(minValue, maxValue);
        maxDisplayValue = majorTickSpacing * (float) Math.ceil(maxValue / majorTickSpacing);
        minDisplayValue = majorTickSpacing * (float) Math.floor(minValue / majorTickSpacing);
        int numTicks = (int) Math.round((maxDisplayValue - minDisplayValue) / majorTickSpacing) + 1;
        majorTicks = new double[numTicks];
        for(int i = 0; i < numTicks; i++) {
            majorTicks[i] = minDisplayValue + majorTickSpacing * i;
        }
    }

    /*
     * Sets the absolute display values inside which the tick spacings are
     * computed including the end points
     */
    public void setDisplayMinMaxValue(double minValue, double maxValue) {
        if(hasMin0 && minValue < 0.0) {
            minValue = 0.0;
        }
        computeMajorTickSpacing(minValue, maxValue);
        this.maxDisplayValue = maxValue;
        this.minDisplayValue = minValue;
        System.out.println("MinValue " + minValue + "\tMaxValue " + maxValue);
        double maxInteriorTick  = majorTickSpacing * (float) Math.floor(maxValue / majorTickSpacing);
        double minInteriorTick  = majorTickSpacing * (float) Math.ceil(minValue / majorTickSpacing);
        int numTicks = (int) Math.round((maxInteriorTick - minInteriorTick)/majorTickSpacing) + 1;
        double fraction = (maxValue - minValue) / 200.0;
        //boolean minSameAsInteriorMin = Math.abs(minValue - minInteriorTick) < fraction;
        //boolean maxSameAsInteriorMax = Math.abs(maxValue - maxInteriorTick) < fraction;
        majorTicks = new double[numTicks];
        System.out.println("maxInter " + maxInteriorTick + "\tMinInterior " + minInteriorTick + "\tMajorspacing " + majorTickSpacing);
        System.out.println("NumTicks " + numTicks + "\tFraction " + fraction);
        //System.out.println("Min/MaxSame as " + minSameAsInteriorMin + "\t" + maxSameAsInteriorMax);
        /*if(! minSameAsInteriorMin) {
           numTicks++;
        }
        if(! maxSameAsInteriorMax) {
            numTicks++;
        }
        int index = 0;
        if(! minSameAsInteriorMin) {
            majorTicks[0] = minValue;
            index = 1;
        }*/
        majorTicks[0] = minInteriorTick;
        int index = 1;
        for(int i = 0; i < numTicks-1; i++) {
            majorTicks[index++] = minInteriorTick + majorTickSpacing * i;
        }
        //if(! maxSameAsInteriorMax) {
            //majorTicks[index] = maxValue;
        //}
    }
    
    /**
     * Computes tickSpacing, maxDisplayValue, and minDisplayValue so that it
     * finds a nice range of values to plot that span the max/min range and
     * have effective tick spacing.
     */
    private void computeMajorTickSpacing(double minValue, double maxValue) {
      double difference = maxValue - minValue;
      int logTick = (int) Math.floor(Math.log10(difference));
      majorTickSpacing = Math.pow(10.0, logTick * 1.0);
      if (difference / majorTickSpacing < 8.0) {
        majorTickSpacing = majorTickSpacing / 2.0;
        if (difference / majorTickSpacing < 8.0) {
          majorTickSpacing = majorTickSpacing / 2.0;
          if (difference / majorTickSpacing < 8.0) {
            majorTickSpacing = majorTickSpacing * 4.0 / 10.0;
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
        return formatString(majorTicks[index]);
    }

    /**
     * Formats the ticks as strings and includes the X-axis switch to Mb
     * @param value
     * @return 
     */
    public String formatString(double value) {
        DecimalFormat format;
        int logrange = (int) Math.ceil(Math.log10(maxDisplayValue - minDisplayValue));
        //System.out.println("Major Tick Spacing " + majorTickSpacing + "\t" + logrange);
        if (logrange >= 2 && isVertical) {
            format = new DecimalFormat("######");
            String returnStr = format.format(value);
            return returnStr;
        } else {
            double currMajorTickSpacing = majorTickSpacing;
            double currValue = value;
            if(! isVertical) {
                currMajorTickSpacing = majorTickSpacing/1000000;
                currValue = value / 1000000;
            }
            if (currMajorTickSpacing >= 1) {
                format = new DecimalFormat("#");
            } else if (currMajorTickSpacing == 0.5 || currMajorTickSpacing == 0.1) {
                format = new DecimalFormat("#0.0");
            } else if (currMajorTickSpacing == 0.25 || currMajorTickSpacing == 0.05 || currMajorTickSpacing == 0.01) {
                format = new DecimalFormat("#0.00");
            } else if (currMajorTickSpacing == 0.025 || currMajorTickSpacing == 0.005 || currMajorTickSpacing == 0.001) {
                format = new DecimalFormat("#0.000");
            } else if (currMajorTickSpacing == 0.0025 || currMajorTickSpacing == 0.0005 || currMajorTickSpacing == 0.0001) {
                format = new DecimalFormat("#0.0000");
            } else {
                format = new DecimalFormat(".00000");
            }
            String returnStr = format.format(currValue);
            return returnStr;
        }
    }

    /**
     * This was the format used where the X-axis was in integers.  We're switching
     * to millions for the X-axis so this value was redone
     * @param value
     * @deprecated
     * @return 
     */
    public String oldFormatString(double value) {
        DecimalFormat format;
        int logrange = (int) Math.ceil(Math.log10(maxDisplayValue - minDisplayValue));
        //System.out.println("Major Tick Spacing " + majorTickSpacing + "\t" + logrange);
        if (logrange >= 2) {
            format = new DecimalFormat("######");
        } else {
            /*StringBuffer zeroCnt = new StringBuffer("######0.");
            for(int i = logrange; i >= 1; i--) {
            //zeroCnt.append("0");
            zeroCnt.append("#");
            }
            format = new DecimalFormat(zeroCnt.toString());*/
            //format = new DecimalFormat("#" + majorTickSpacing);
            if (majorTickSpacing >= 1) {
                format = new DecimalFormat("#");
            } else if (majorTickSpacing == 0.5 || majorTickSpacing == 0.1) {
                format = new DecimalFormat("#0.0");
            } else if (majorTickSpacing == 0.25 || majorTickSpacing == 0.05 || majorTickSpacing == 0.01) {
                format = new DecimalFormat("#0.00");
            } else if (majorTickSpacing == 0.025 || majorTickSpacing == 0.005 || majorTickSpacing == 0.001) {
                format = new DecimalFormat("#0.000");
            } else if (majorTickSpacing == 0.0025 || majorTickSpacing == 0.0005 || majorTickSpacing == 0.0001) {
                format = new DecimalFormat("#0.0000");
            } else {
                format = new DecimalFormat(".00000");
            }
        }
        String returnStr = format.format(value);
        return returnStr;
    }



    /**
     * Returns whether linear or log scale is used
     * @return
     */
    public boolean useLogScale() {
        return false;
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
    public int getImagePixelFromValue(double value, BufferedImage image) {
        int pixelRange = 0;
        if(image == null) { // kluge for when the scale causes image to be null due to size constraints
            return 0;
        } else if(isVertical) {
            double fraction = (maxDisplayValue - value) / (maxDisplayValue - minDisplayValue);
            pixelRange = image.getHeight() - state.getTopPadding() - state.getBottomPadding();
            int pixel = state.getTopPadding() + (int) Math.round(fraction * pixelRange);
            return pixel;
        } else {
            double fraction = (value - minDisplayValue) / (maxDisplayValue - minDisplayValue);
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
        //int pixelRange = 0;
        if(isVertical) {
            double fraction = (maxDisplayValue - value) / (maxDisplayValue - minDisplayValue);
            double vscale = viewData.getVscale();
            double pixelRange =  Math.max(0.0, image.getHeight()*1.0 - state.getTopPadding()*vscale - state.getBottomPadding()*vscale);
            int pixel = (int) Math.round(vscale * state.getTopPadding() + fraction * pixelRange);
            //System.out.println("fraction " + fraction + "\tVScale " + vscale + "\tPixRange " + pixelRange + "\tpixel " + pixel);
            return pixel;
        } else {
            double fraction = (value - minDisplayValue) / (maxDisplayValue - minDisplayValue);
            double hscale = viewData.getHscale();
            //System.out.println("hscale " + hscale + "\t" + image.getWidth());
            double pixelRange = image.getWidth()*1.0 - state.getLeftPadding()*hscale - state.getRightPadding()*hscale;
            int pixel = (int) Math.round(hscale * state.getLeftPadding() + fraction * pixelRange);
            return pixel;
        }

    }

    @Override
    public int getPaddedRawPixelFromValue(double value, JComponent image, int startPadSize, int endPadSize) {
        if(isVertical) {
            double fraction = (maxDisplayValue - value) / (maxDisplayValue - minDisplayValue);
            double vscale = viewData.getVscale();
            double pixelRange =  Math.max(0.0, image.getHeight()*1.0 - state.getTopPadding()*vscale - state.getBottomPadding()*vscale);
            int pixel = (int) Math.round(vscale * state.getTopPadding() + fraction * pixelRange);
            //System.out.println("fraction " + fraction + "\tVScale " + vscale + "\tPixRange " + pixelRange + "\tpixel " + pixel);
            return pixel;
        } else {
            double fraction = (value - minDisplayValue) / (maxDisplayValue - minDisplayValue);
            //double hscale = viewData.getHscale();
            double hscale = 1.0;
            //System.out.println("hscale " + hscale + "\t" + image.getWidth());
            double pixelRange = image.getWidth()*1.0 - state.getLeftPadding()*hscale - state.getRightPadding()*hscale - startPadSize - endPadSize;
            int pixel = (int) Math.round(hscale * state.getLeftPadding() + fraction * pixelRange + startPadSize);
            return pixel;
        }
    }
    
    /**
     * If the image is on the buffered image, returns the value from the pixel.
     * Useful for mouse click interpretation
     * @param value
     * @return
     */
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
            double xformed = pixel / viewData.getVscale();
            double fromTop = xformed - state.getTopPadding();
            int pixHeight = image.getHeight() - state.getTopPadding() - state.getBottomPadding();
            double fraction = fromTop / pixHeight;
            double value = maxDisplayValue - (maxDisplayValue - minDisplayValue) * fraction;
            return value;
        } else {
            double xformed = pixel / viewData.getHscale();
            double fromLeft = xformed - state.getLeftPadding();
            int pixWidth = image.getWidth() - state.getLeftPadding() - state.getRightPadding();
            double fraction = fromLeft / pixWidth;
            double value = minDisplayValue + (maxDisplayValue - minDisplayValue) * fraction;
            return value;
        }
    }
    
    /**
     * If the image is on the non-buffered final image, returns the value from
     * the pixel.  Useful for mouse click interpretation
     * @param value
     * @return
     */
    @Override
    public double getValueFromRawPixel(int pixel, int widthOrHeight) {
        if(isVertical) {
            double xformed = pixel / viewData.getVscale();
            double fromTop = xformed - state.getTopPadding();
            int pixHeight = widthOrHeight - state.getTopPadding() - state.getBottomPadding();
            double fraction = fromTop / pixHeight;
            double value = maxDisplayValue - (maxDisplayValue - minDisplayValue) * fraction;
            return value;
        } else {
            double xformed = pixel / viewData.getHscale();
            double fromLeft = xformed - state.getLeftPadding();
            int pixWidth = widthOrHeight - state.getLeftPadding() - state.getRightPadding();
            double fraction = fromLeft / pixWidth;
            double value = minDisplayValue + (maxDisplayValue - minDisplayValue) * fraction;
            return value;
        }
    }
    
   public void zoomOut() {
        double range = maxDisplayValue - minDisplayValue;
        
        double newMaxValue = maxDisplayValue + range / 3.0;
        double newMinValue = minDisplayValue - range / 3.0;
        setDisplayMinMaxValue(newMinValue, newMaxValue);
    }
        
    public void zoomIn() {
        double range = maxDisplayValue - minDisplayValue;
        double newMaxValue = maxDisplayValue - range / 5.0;
        double newMinValue = minDisplayValue + range / 5.0;
        setDisplayMinMaxValue(newMinValue, newMaxValue);
    }
    
    public void zoomToOriginal() {
        setDisplayMinMaxValue(origMin, origMax);
        lastShiftQuantity = NULL_QUANTITY;
        lastScaleQuantity = NULL_QUANTITY;
    }
    
    public void zoomToRange(double min, double max) {
        System.out.println("LinearAxisScale zoomToRange [" + min + "\t" + max + "]");
        setDisplayMinMaxValue(min, max);
    }
    
    public void shiftLeft() {
        lastShiftQuantity = NULL_QUANTITY;
        double range = maxDisplayValue - minDisplayValue;
        double newMaxValue = maxDisplayValue - (range / 5.0);
        double newMinValue = minDisplayValue - (range / 5.0);
        setDisplayMinMaxValue(newMinValue, newMaxValue);
        range = maxDisplayValue - minDisplayValue;
    }

    public void shiftRight() {
        double range = maxDisplayValue - minDisplayValue;
        double newMaxValue = maxDisplayValue + range / 5.0;
        double newMinValue = minDisplayValue + range / 5.0;
        setDisplayMinMaxValue(newMinValue, newMaxValue);
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
