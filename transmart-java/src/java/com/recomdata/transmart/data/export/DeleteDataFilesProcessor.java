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

public class DeleteDataFilesProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DeleteDataFilesProcessor.class);
	
    public boolean deleteDataFile(String fileToDelete, String directoryToDelete, String tempFolderDirectory) {
        if (StringUtils.isEmpty(fileToDelete)||StringUtils.isEmpty(directoryToDelete)){
            logger.error("Invalid file or directory name. Both are needed to delete data for an export job");
            return false;
        }

        boolean fileDeleted = false;
        try {
            deleteDirectoryStructure(new File(tempFolderDirectory, directoryToDelete));
            fileDeleted = FTPUtil.deleteFile(fileToDelete);
            //If the file was not found at the FTP location try to delete it from the server Temp dir
            if (!fileDeleted) {
                File jobZipFile = new File(tempFolderDirectory, fileToDelete);
                if (jobZipFile.isFile()) {
                    jobZipFile.delete();
                    fileDeleted=true;
                }
            }
        }
        catch (RuntimeException e) {
            logger.error("Failed to delete the data for job " + directoryToDelete + " : " + e.getMessage());
        }
        return fileDeleted;
    }
	
    private boolean deleteDirectoryStructure(File directory){
        if (directory.exists()){
            for (File f : directory.listFiles()) {
                if (f.isDirectory()) {
                    deleteDirectoryStructure(f);
                }
                else {
                    f.delete();
                }
            }
        }
        return directory.delete();
    }
}
