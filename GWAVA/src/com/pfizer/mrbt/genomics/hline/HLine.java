/*
 * Horizontal line that the user can add to different types of versions of the
 * main view
 */
package com.pfizer.mrbt.genomics.hline;

import java.awt.Color;

/**
 *
 * @author henstockpv
 */
public class HLine {
    private String  lineName;
    private int     lineStyle;
    private int     lineScope;
    private Color   lineColor;
    private float   yValue;
    
    public final static String[] lineStyleOptions = {"Solid", "Dashed", "Dotted", "Dash Dot"};
    public final static String[] scopeOptions = {"This plot only", "All plots with same gene", "All plots with [any of] the same model(s)", "All plots "};
    public final static int SOLID       = 0;
    public final static int DASHED      = 1;
    public final static int DOTTED      = 2;
    public final static int DASH_DOT    = 3;
    
    public final static int SCOPE_THIS_PLOT  = 0;
    public final static int SCOPE_SAME_GENE  = 1;
    public final static int SCOPE_SAME_MODEL = 2;
    public final static int SCOPE_GLOBAL     = 3;

    
    public HLine(String lineName, float yValue, Color color, int style, int scope) {
        this.lineName   = lineName;
        this.yValue     = yValue;
        this.lineColor  = color;
        this.lineStyle  = style;
        this.lineScope  = scope;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public int getLineScope() {
        return lineScope;
    }

    public void setLineScope(int lineScope) {
        this.lineScope = lineScope;
    }

    public int getLineStyle() {
        return lineStyle;
    }

    public void setLineStyle(int lineStyle) {
        this.lineStyle = lineStyle;
    }

    public float getyValue() {
        return yValue;
    }

    public void setyValue(float yValue) {
        this.yValue = yValue;
    }
}
