/*
 * Single container for the parameters that drive the heatmap update
 */
package com.pfizer.mrbt.genomics.heatmap;

import com.pfizer.mrbt.genomics.Singleton;

/**
 *
 * @author henstockpv
 */
public class HeatmapParameters {
    public final static int FUNCTION_MAXIMUM = 0;
    public final static int FUNCTION_MINIMUM = 1;
    public final static int FUNCTION_AVERAGE = 2;
    public final static int FUNCTION_MEDIAN  = 3;
    public final static String[] METHOD_OPTIONS = {"Maximum","Minimum","Mean","Median"};
    
    public final static String[] TOP_OPTIONS    = {"Top 1", "Top 2", "Top 3", "Top 5", "Top 10"};
    private final static int[] topNoptions       = {1, 2, 3, 5, 10};
    
    
    private int function;
    //private int topn;
    private int radius;
    private int topNindex;
    
    public HeatmapParameters() {
        function  = Singleton.getUserPreferences().getHeatmapFunction();
        topNindex = Singleton.getUserPreferences().getHeatmapTopNindex();
        //topn      = topOptions[topNindex];
        radius    = Singleton.getUserPreferences().getHeatmapRadius();
    }
    
    public HeatmapParameters(int methodIndex, int topOptionIndex, int radius) {
        this.function  = methodIndex;
        this.topNindex = topOptionIndex;
        //this.topn      = topOptions[topOptionIndex];
        this.radius    = radius;
    }

    public int getFunction() {
        return function;
    }

    public void setFunction(int function) {
        this.function = function;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getTopn() {
        return topNoptions[topNindex];
    }

/*    public void setTopn(int topn) {
        this.topn = topn;
    }*/
    
    public int getTopNindex() {
        return topNindex;
    }
    
}
