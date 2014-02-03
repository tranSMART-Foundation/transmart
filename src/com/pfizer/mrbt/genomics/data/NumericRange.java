/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.data;

/**
 *
 * @author henstockpv
 */
public class NumericRange {
    private double min;
    private double max;
    
    public NumericRange(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public double getMax() {
        return this.max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public double getMin() {
        return this.min;
    }

    public void setMin(double min) {
        this.min = min;
    }
    
    
}
