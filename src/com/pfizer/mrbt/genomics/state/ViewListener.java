/*
 * Manages changes to the view
 */
package com.pfizer.mrbt.genomics.state;

import javax.swing.event.ChangeEvent;

/**
 *
 * @author henstockpv
 */
public interface ViewListener {
    public void zoomChanged(AxisChangeEvent ce);
}
