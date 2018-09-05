/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.data;

import javax.swing.event.ChangeEvent;

/**
 *
 * @author henstockpv
 */
public interface DataListener {
    /**
     * Modified existing data by replacing values
     * @param ce 
     */
    public void dataChanged(ChangeEvent ce);
    
    /**
     * Added existing data only--old data unmodified.
     * @param ce 
     */
    public void dataAdded(ChangeEvent ce);
}
