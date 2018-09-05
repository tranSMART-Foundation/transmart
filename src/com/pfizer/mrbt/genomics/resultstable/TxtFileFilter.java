/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.resultstable;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * Class to export the .txt tab-delimited file as a file filter
 */
public class TxtFileFilter extends FileFilter {

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        } else if (file.getAbsolutePath().toLowerCase().endsWith(".txt")) {
            return true;
        } else {
            return false;
        }
    }

    public String getDescription() {
        return "Tab-delimited txt file";
    }

    public String getSuffix() {
        return ".txt";
    }
}