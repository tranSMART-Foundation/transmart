/*
 * Axis change event is a change event but includes the axis that has changed
 */
package com.pfizer.mrbt.genomics.state;

import javax.swing.event.ChangeEvent;

/**
 *
 * @author henstockpv
 */
public class AxisChangeEvent extends ChangeEvent {
    public final static int YAXIS       = 10;
    public final static int XAXIS       = 11;
    public final static int RIGHT_YAXIS = 12;
    private int axisChanged;
    
    public AxisChangeEvent(Object source, int axisChanged) {
        super(source);
        this.axisChanged = axisChanged;
    }

    public int getAxisChanged() {
        return axisChanged;
    }

    public void setAxisChanged(int axisChanged) {
        this.axisChanged = axisChanged;
    }
    
}
