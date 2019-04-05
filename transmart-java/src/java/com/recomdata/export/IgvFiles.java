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
  

package com.recomdata.export;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmcduffie
 */
public class IgvFiles {
    protected File tmpDir;
    protected String fileAccessUrl;
    protected File sampleFile;
    protected List<File> files = new ArrayList<>();
    protected File sessionFile;
	
    public IgvFiles(String gpFileDirName, String gpFileAccessUrl) {
        fileAccessUrl = gpFileAccessUrl;
        // put files in a directory 
        tmpDir = new File(gpFileDirName);
        if (! tmpDir.exists()) {
            tmpDir.mkdir();
        }
    }
	
    public File getSampleFile() throws IOException {
        if (sampleFile == null) {
            sampleFile = File.createTempFile("igv_df_", ".sample.txt", tmpDir);
        }
        return sampleFile;
    }
	
    public File createCopyNumberFile() throws IOException {
        return File.createTempFile("igv_df_", ".cn", tmpDir);
    }
		
    public void addFile(File file) {
        files.add(file);
    }
	
    public File getSessionFile() throws IOException {
        if (sessionFile == null) {
            sessionFile = File.createTempFile("igv_df_", ".xml", tmpDir);
        }
        return sessionFile;
    }
	
    public static String getFileSecurityHash(File file, String userName) throws NoSuchAlgorithmException {
        String hashWord = userName + Long.toString(file.length());
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.reset();
        md5.update(hashWord.getBytes());
        //return md5.digest().toString();
        return hashWord;
    }
	
    public String getFileUrlWithSecurityToken(File file, String userName) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String hashStr = getFileSecurityHash(file, userName);
        // The URL in the XML document need to have & escaped by &amp;
        // IGV openSession routine uses the extension of a file or a URL to determine the file type. Put the file name at the end of URL.
        return fileAccessUrl  + "?user=" + userName + 
            "&amp;hash=" + URLEncoder.encode(hashStr, "UTF-8")+ "&amp;file=" + URLEncoder.encode(file.getName(), "UTF-8");
    }
	
    public String getFileUrl(File file) throws Exception {
        return fileAccessUrl + "/" + URLEncoder.encode(file.getName(), "UTF-8");
    }

    public List<File> getCopyNumberFileList() {
        return files;
    }

    public List<File> getDataFileList() {
        return files;
    }
}
