/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.utils;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author henstock
 */
public abstract class SuffixFileFilter extends FileFilter {

    public abstract String getSuffix();

    /**
     * Returns the text of filename after the last period. Stolen from the Java
     * Tutorial FileChooser
     *
     * @param filename File
     * @return String
     */
    public String getExtension(File filename) {
        String ext = null;
        String str = filename.getName();
        int i = str.lastIndexOf('.');

        if (i > 0 && i < str.length() - 1) {
            ext = str.substring(i + 1).toLowerCase();
        }
        return ext;
    }
}
