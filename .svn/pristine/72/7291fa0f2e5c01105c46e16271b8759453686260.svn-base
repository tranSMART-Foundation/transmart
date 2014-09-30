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
public class Row {
    public Vector data;
    public Row() {
        data = new Vector();
    }
    
    public String getValue(int index) {
        return (String) data.elementAt(index);
    }
    
    public void addData(String str) {
        data.addElement(str);
    }
    
    public int getNumValues() {
        return data.size();
    }
    
    public void setData(Vector data) {
        this.data = data;
    }
    
    public Vector getData() {
        return data;
    }
}
