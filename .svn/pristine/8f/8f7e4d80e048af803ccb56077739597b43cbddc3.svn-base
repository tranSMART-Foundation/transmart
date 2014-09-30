/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pfizer.mrbt.genomics.utils;

import java.io.File;

/**
 *
 * @author henstock
 */
/**
 * <p>Title: TxtFiler</p> <p>Description: Filter for filenames based on suffix
 * "txt" and "well"</p>
 */
public class JpgFilter extends SuffixFileFilter {

    public boolean accept(File filename) {
        if (filename.isDirectory()) {
            return true;
        }
        String extension = getExtension(filename);
        if (extension != null) {
            if (extension.equals("jpg")) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public String getDescription() {
        return "*.jpg (JPeg Files)";
    }

    public String getSuffix() {
        return ".jpg";
    }
}
