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
  
package com.recomdata.transmart.data.export;

import com.recomdata.transmart.data.export.util.FTPUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author SMunikuntla
 */
public class ExportDataProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ExportDataProcessor.class);

    private final String tempFolderDirectory;

    public ExportDataProcessor(String directory) {
        tempFolderDirectory = directory;
    }

    public InputStream getExportJobFileStream(String fileToGet) {
        InputStream inputStream = null;
        try {
            if (StringUtils.isEmpty(fileToGet)) {
                return null;
            }

            inputStream = FTPUtil.downloadFile(true, fileToGet);
            //If the file was not found at the FTP location try to download it from the server Temp dir
            if (null == inputStream) {
                File jobZipFile = new File(tempFolderDirectory, fileToGet);
                if (jobZipFile.isFile()) {
                    inputStream = new FileInputStream(jobZipFile);
                }
            }
        }
        catch (FileNotFoundException | RuntimeException e) {
            logger.error("Failed to SFTP GET the ZIP file");
        }

        return inputStream;
    }

    @SuppressWarnings("unused")
    private String getZipFileName(String studyName) {
        return tempFolderDirectory + studyName + new SimpleDateFormat("MMddyyyyHHmmss").format(new Date()) + ".zip";
    }
}
