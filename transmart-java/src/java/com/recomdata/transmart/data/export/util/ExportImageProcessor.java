/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
package com.recomdata.transmart.data.export.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.List;

/**
 * @author SMunikuntla
 */
public class ExportImageProcessor {
	
    private static final ThreadGroup imagesThreadGroup = new ThreadGroup("Images");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String getFilename(URI imageURI) {
        String filename = null;
        if (StringUtils.equalsIgnoreCase("file", imageURI.getScheme())) {
            filename = new File(imageURI.toString()).getName();
        }
        else {
            String imageURIStr = imageURI.toString();
            if (StringUtils.isNotEmpty(imageURIStr)) {
                int loc = imageURIStr.lastIndexOf("/");
                if (loc == imageURIStr.length() - 1) {
                    loc = (imageURIStr.substring(0, loc - 1)).lastIndexOf("/");
                }
                filename = imageURIStr.substring(loc+1, imageURIStr.length());
            }
        }
		
        return filename;
    }
	
    public void getImageFromURI(String imageURIStr, String filename) throws URISyntaxException {
        if (StringUtils.isEmpty(filename)) {
            filename = getFilename(new URI(imageURIStr));
        }
        new Thread(imagesThreadGroup, new ExportImageThread(imageURIStr, filename)).start();
    }
	
    public void getImages(List<String> imageURIs) {
        for (String imageURI : imageURIs) {
            try {
                getImageFromURI(imageURI, null);
            }
            catch (URISyntaxException e) {
                logger.error("Invalid URI for image :: " + imageURI, e);
            }
        }
    }
}

class ExportImageThread extends Thread {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String imageURI;
    private String filename;
	
    /**
     * change to configurable from property file
     */
    private static final String imagesTempDir = "C://images";

    public ExportImageThread(String imageURI, String filename) {
        this.imageURI = imageURI;
        this.filename = filename;
    }
	
    public void run() {
        FileOutputStream fos = null;
        try {
            if (StringUtils.isEmpty(imageURI)) {
                return;
            }

            fos = new FileOutputStream(new File(imagesTempDir, filename));
            fos.getChannel().transferFrom(
                Channels.newChannel(new URL(imageURI).openStream()), 0, 1 << 24);
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        finally {
            if (null != fos) {
                try {
                    fos.close();
                }
                catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
}
