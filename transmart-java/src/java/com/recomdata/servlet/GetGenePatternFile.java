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
package com.recomdata.servlet;

import com.recomdata.export.IgvFiles;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Provides data as remote accessed URL to IGV. Grails controller cannot handle the HttpInputStream.close() call from the client
 * IGV may issue http "head" request to get attributes of the data, without getting the data itself
 * IGV will also issue multiple "get" request for a data, each request downloading a segment of data.
 */
public class GetGenePatternFile extends HttpServlet {
	
    public static final String fileDirName = "genepattern";
	
    public void doHead(HttpServletRequest request, HttpServletResponse response) {
        String fileName = request.getParameter("file");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment; filename=" + fileName);
        response.setContentLength((int) new File(getGenePatternFileDirName(request), fileName).length());
    }
	
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        FileInputStream fileIn = null;
        OutputStream out = response.getOutputStream();
		
        try {
            String fileName = request.getParameter("file");
            String userName = request.getParameter("user");
            String secureHash = request.getParameter("hash");
			
            File file = new File(getGenePatternFileDirName(request), fileName);
			
            String requestMethod = request.getMethod().toUpperCase();
            if ("HEAD".equals(requestMethod)) {
                doHead(request, response);
                return;
            }
            else if (!"GET".equals(requestMethod)) {
                return;
            }

            String hashStr = IgvFiles.getFileSecurityHash(file, userName);
            if (!hashStr.equals(secureHash)) {
                throw new ServletException(
                    "The user name and security hash does not match those for the file " + fileName);
            }

            fileIn = new FileInputStream(file);
            int endFile = (int) file.length();

            boolean isRangeRead = false;
            int startIn = 0;
            int endIn = 0;
            int len = 0;
            String rangeStr = request.getHeader("Range");

            if (rangeStr != null && rangeStr.length() != 0) {
                int idx1 = rangeStr.indexOf("=");
                int idx2 = rangeStr.lastIndexOf("-");
                startIn = Integer.parseInt(rangeStr.substring(idx1 + 1, idx2));
                endIn = Integer.parseInt(rangeStr.substring(idx2 + 1));
                len = Math.min(endIn, endFile) - startIn + 1;
                isRangeRead = true;
            }

            // Set headers
            response.setContentType("application/octet-stream");
            response.setHeader("Content-disposition", "attachment; filename=" + fileName);

            byte[] bytes;
            if (isRangeRead) {
                if (startIn > endFile) {
                    response.setContentLength(0);
                }
                else {
                    response.setContentLength(len);
                    // formal response for ranged request, to satisfy Apache proxy
                    response.setHeader("Content-Range", "bytes " + startIn + "-" + endIn + "/" + endFile);
                    bytes = new byte[len];
                    if (startIn > 0) {
                        fileIn.skip(startIn);
                    }
                    fileIn.read(bytes);
                    out.write(bytes);
                }
            }
            else {
                // Write the file content. The file can be very large
                response.setContentLength(endFile);
                bytes = new byte[2048];
                int bytesRead;
                while ((bytesRead = fileIn.read(bytes)) != -1) {
                    out.write(bytes, 0, bytesRead);
                }
            }
        }
        catch (Exception e) {
            throw new ServletException(e);
        }
        finally {
            if (fileIn != null) {
                fileIn.close();
            }
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }
		
    protected String getGenePatternFileDirName(HttpServletRequest request) {
        String webRootName = request.getSession().getServletContext().getRealPath("/");
        if (!webRootName.endsWith(File.separator)) {
            webRootName += File.separatorChar;
        }
        return webRootName + fileDirName;
    }
}
