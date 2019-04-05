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
  

/* Copyright 2007 Sun Microsystems, Inc.  All rights reserved.  You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at: 
 http://developer.sun.com/berkeley_license.html
 $Id: HttpClient.java 11083 2011-12-09 06:05:13Z jliu $ 
*/
package com.recomdata.datasetexplorer.proxy;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Security;

/**
 * @author Yutaka Yoshida, Greg Murray
 * <p>
 * Minimum set of HTTPclient supporting both http and https.
 * It's aslo capable of POST, but it doesn't provide doGet because
 * the caller can just read the inputstream.
 */
public class HttpClient {

    private static final Logger logger = LoggerFactory.getLogger("jmaki.xhp.Log");

    private String proxyHost;
    private int proxyPort = -1;
    private boolean isHttps = false;
    private boolean isProxy = false;
    private URLConnection urlConnection;
    
    /**
     * @param url URL string
     */
    public HttpClient(String url) throws MalformedURLException {
        urlConnection = getURLConnection(url);
    }

    /**
     * @param phost PROXY host name
     * @param pport PROXY port string
     * @param url URL string
     */
    public HttpClient(String phost, int pport, String url) throws MalformedURLException {
        if (phost != null && pport != -1) {
            isProxy = true;
        }
        proxyHost = phost;
        proxyPort = pport;
        if (url.trim().startsWith("https:")) {
            isHttps = true;
        }
        urlConnection = getURLConnection(url);
    }

    /**
     * @param pport PROXY port string
     */
    public HttpClient(String proxyHostName, int pport, String url, String userName, String password) {
        try {            
            if (proxyHostName != null && pport != -1) {
                isProxy = true;
            }
            proxyHost = proxyHostName;
            proxyPort = pport;
            if (url.trim().startsWith("https:")) {
                isHttps = true;
            }
            urlConnection = getURLConnection(url);

            byte[] encodedBytes = Base64.encodeBase64((userName + ":" + password).getBytes());
            urlConnection.setRequestProperty("Authorization", "Basic " + new String(encodedBytes));
            urlConnection.setConnectTimeout(600000);
            urlConnection.setReadTimeout(600000);
        }
        catch (Exception e) {
            logger.error("Unable to set basic authorization for {} : ", userName, e);
        }    
    }

    private URLConnection getURLConnection(String url) throws MalformedURLException {
    	try {
            if (isHttps) {
                /* when communicating with the server which has unsigned or invalid
                 * certificate (https), SSLException or IOException is thrown.
                 * the following line is a hack to avoid that
                 */
                Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
                System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
                if (isProxy) {
                    System.setProperty("https.proxyHost", proxyHost);
                    System.setProperty("https.proxyPort", String.valueOf(proxyPort));
                }
            }
            else {
                if (isProxy) {
                    System.setProperty("http.proxyHost", proxyHost);
                    System.setProperty("http.proxyPort", String.valueOf(proxyPort));
                }
            }
            SSLUtilities.trustAllHostnames();
            SSLUtilities.trustAllHttpsCertificates();
            URLConnection uc = new URL(url).openConnection();
            // set user agent to mimic a common browser
            uc.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322)");
            return uc;
        }
        catch (MalformedURLException me) {
            throw new MalformedURLException(url + " is not a valid URL");
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
    
    public InputStream getInputStream() {
        try {
            return urlConnection.getInputStream();
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public OutputStream getOutputStream() {
        try {
            return urlConnection.getOutputStream();
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * posts data to the inputstream and returns the InputStream.
     *
     * @param postData data to be posted. must be url-encoded already.
     * @param contentType allows you to set the contentType of the request.
     * @return InputStream input stream from URLConnection
     */
    public InputStream doPost(String postData, String contentType) {
        urlConnection.setDoOutput(true);
        if (contentType != null) {
            urlConnection.setRequestProperty("Content-type", contentType);
        }

        PrintStream ps = new PrintStream(getOutputStream());
        ps.print(postData);
        ps.close(); 
        return getInputStream();
    }
    
    public String getContentEncoding() {
        return urlConnection == null ? null : urlConnection.getContentEncoding();
    }

    public int getContentLength() {
        return urlConnection == null ? -1 : urlConnection.getContentLength();
    }

    public String getContentType() {
        return urlConnection == null ? null : urlConnection.getContentType();
    }

    public long getDate() {
        return urlConnection == null ? -1 : urlConnection.getDate();
    }

    public String getHeader(String name) {
        return urlConnection == null ? null : urlConnection.getHeaderField(name);
    }

    public long getIfModifiedSince() {
        return urlConnection == null ? -1 : urlConnection.getIfModifiedSince();
    } 
}
