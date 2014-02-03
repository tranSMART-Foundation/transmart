/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.TransmartClient;

import java.util.Vector;

/**
 *
 * @author henstockpv
 */
public class Rows {
    private Vector rows;
    public Rows() {
        rows = new Vector();
    }
    
    public void addRow(Row row) {
        rows.addElement(row);
    }
    
    public Row getRow(int index) {
        return (Row) rows.get(index);
    }
    
    public int getNumRows() {
        return rows.size();
    }
    
    public Vector getRows() {
        return rows;
    }
    
    public void setRows(Vector rows) {
        this.rows = rows;
    }
}
