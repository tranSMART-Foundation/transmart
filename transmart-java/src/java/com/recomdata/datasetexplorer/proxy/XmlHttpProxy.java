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
package com.recomdata.datasetexplorer.proxy;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/* Copyright 2007 You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at:
 http://developer.sun.com/berkeley_license.html
*/
public class XmlHttpProxy {
	
    private static final String USAGE = "Usage:  -url service_URL  -id service_key [-url or -id required] " +
        "-xslurl xsl_url [optional] -format json|xml [optional] -callback[optional] -config [optional] " +
        "-resources base_directory_containing XSL stylesheets [optional]";

    private static Logger logger = LoggerFactory.getLogger("jmaki.xhp.Log");

    private String userName;
    private String password;
    private String proxyHost = "";
    int proxyPort = -1;
    private JSONObject config;
    
    public XmlHttpProxy() {
    }

    public XmlHttpProxy(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }
    
    public XmlHttpProxy(String proxyHost, int proxyPort, String userName, String password) {
        this(proxyHost, proxyPort);
        this.userName = userName;
        this.password = password;
    }

    /**
     * Makes the call and applies an XSLT Transformation with the set of parameters provided.
     * @param urlString - The URL which you are looking up
     * @param out - The OutputStream to which the resulting document is written
     *
     */
    public void doGet(String urlString, OutputStream out) throws IOException {
        doProcess(urlString, out, null, null, null, null,null,null);
    }
    
    /**
     * Makes the call and applies an XSLT Transformation with the set of parameters provided.
     *
     * @param urlString - The URL which you are looking up
     * @param out - The OutputStream to which the resulting document is written
     * @param xslInputStream - An input Stream to an XSL style sheet that is provided to the XSLT processor.
     *                        If set to null there will be no transformation
     * @param paramsMap - parameters that are feed to the XSLT Processor.
     *                    These params may be used when generating content.
     *                    This may be set to null if no parameters are necessary.
     */
    public void doGet(String urlString, OutputStream out, InputStream xslInputStream, Map<String, String> paramsMap) throws IOException {
        doProcess(urlString, out, xslInputStream, paramsMap, null,null, null,null);
    }
         
    public void doGet(String urlString, OutputStream out, InputStream xslInputStream, Map<String, String> paramsMap,
                      String userName, String password) throws IOException {
        doProcess(urlString, out, xslInputStream, paramsMap, null,null, userName,password);
    }

    /**
     * Makes the call and applies an XSLT Transformation with the set of parameters provided.
     * @param urlString - The URL which you are looking up
     * @param out - The OutputStream to which the resulting document is written
     *
     */
    public void doPost(String urlString, OutputStream out, InputStream xslInputStream, Map<String, String> paramsMap,
                       String postData, String postContentType) throws IOException {
        doPost(urlString, out, xslInputStream, paramsMap, postData, postContentType, null, null);
    }

    public void doPost(String urlString, OutputStream out, InputStream xslInputStream, Map<String, String> paramsMap, String postData,
                       String postContentType, String userName, String password) throws IOException {
        doProcess(urlString, out, xslInputStream, paramsMap, postData, postContentType, userName, password);
    }
     
    /**
     * Makes the call and applies an XSLT Transformation with the set of parameters provided.
     *
     * @param urlString - The URL which you are looking up
     * @param out - The OutputStream to which the resulting document is written
     * @param xslInputStream - An input Stream to an XSL style sheet that is provided to the XSLT processor. If set to null there will be no transformation
     * @param paramsMap - A Map of parameters that are feed to the XSLT Processor. These params may be used when generating content. This may be set to null if no parameters are necessary.
     * @param postData - A String of the bodyContent to be posted. A doPost will be used if this is parameter is not null.
     * @param postContentType - The request contentType used when posting data. Will not be set if this parameter is null.
     * @param userName - userName used for basic authorization
     * @param password - password used for basic authorization
     */        
    public void doProcess(String urlString, OutputStream out, InputStream xslInputStream, Map<String, String> paramsMap,
                          String postData, String postContentType, String userName, String password) throws IOException {

        if (paramsMap == null) {
            paramsMap = new HashMap<>();
        }

        String format = (String)paramsMap.get("format");
        if (format == null) {
            format = "xml";
        }

        HttpClient httpclient;
        if (userName != null && password != null) {
            httpclient = new HttpClient(proxyHost, proxyPort, urlString, userName, password);
        }
        else {
            httpclient = new HttpClient(proxyHost, proxyPort, urlString);
        }

        // post data determines whether we are going to do a get or a post
        InputStream in;
        if (postData == null ||postData.trim().length()==0) {
            in = httpclient.getInputStream();
        }
        else {
            in = httpclient.doPost(postData, postContentType);
        }
        // read the encoding from the incoming document and default to 8859-1
        // if an encoding is not provided
        String ce = httpclient.getContentEncoding();
        if (ce == null) {
            String ct = httpclient.getContentType();
            if (ct != null) {
                int idx = ct.lastIndexOf("charset=");
                if (idx >= 0) {
                    ce = ct.substring(idx+8);
                }
                else {
                    ce = "UTF-8";
                }
            }
            else {
                ce = "UTF-8";
            }
        }

        try {
            byte[] buffer = new byte[1024];
            int read = 0;
            if (xslInputStream == null) {
                while (true) {
                    read = in.read(buffer);
                    if (read <= 0) {
                        break;
                    }
                    out.write(buffer, 0, read );
                }
            }
            else {
                transform(in, xslInputStream, paramsMap, out, ce);
            }
        }
        catch (Exception e) {
            logger.error("XmlHttpProxy transformation error: {}", e.getMessage(), e);
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.flush();
                    out.close();
                }
            }
            catch (Exception ignored) {}
        }
    }
    
    /**
     * Do the XSLT transformation
     */
    public void transform(InputStream xmlIS, InputStream xslIS, Map<String, String> params,
                          OutputStream result, String encoding) {
        try {
            TransformerFactory trFac = TransformerFactory.newInstance();
            Transformer transformer = trFac.newTransformer(new StreamSource(xslIS));
            for (String key : params.keySet()) {
                transformer.setParameter(key, params.get(key));
            }
            transformer.setOutputProperty("encoding", encoding);
            transformer.transform(new StreamSource(xmlIS), new StreamResult(result));
        }
        catch (Exception e) {
            logger.error("XmlHttpProxy: Exception with xslt {}", e.getMessage(), e);
        }
    }

    /**
     * CLI to the XmlHttpProxy
     */	
    public static void main(String[] args) throws IOException {

        logger.info("XmlHttpProxy 1.1");
        XmlHttpProxy xhp = new XmlHttpProxy();

        if (args.length == 0) {
            System.out.println(USAGE);
        }

        InputStream xslInputStream = null;
        String serviceKey = null;
        String urlString = null;
        String xslURLString = null;
        String format = "xml";
        String callback = null;
        String urlParams = null;
        String configURLString = "xhp.json";
        String resourceBase = "file:src/conf/META-INF/resources/xsl/";

        // read in the arguments
        int index = 0;
        while (index < args.length) {
            if (args[index].toLowerCase().equals("-url") && index + 1 < args.length) {
                urlString = args[++index];
            }
            else if (args[index].toLowerCase().equals("-key") && index + 1 < args.length) {
                serviceKey = args[++index];
            }
            else if (args[index].toLowerCase().equals("-id") && index + 1 < args.length) {
                serviceKey = args[++index];                
            }
            else if (args[index].toLowerCase().equals("-callback") && index + 1 < args.length) {
                callback = args[++index];
            }
            else if (args[index].toLowerCase().equals("-xslurl") && index + 1 < args.length) {
                xslURLString = args[++index];
            }
            else if (args[index].toLowerCase().equals("-urlparams") && index + 1 < args.length) {
                urlParams = args[++index];
            }
            else if (args[index].toLowerCase().equals("-config") && index + 1 < args.length) {
                configURLString = args[++index];
            }
            else if (args[index].toLowerCase().equals("-resources") && index + 1 < args.length) {
                resourceBase = args[++index];
            }
            index++;
        }

        if (serviceKey != null) {
            try {
                InputStream is = new URL(configURLString).openStream();
                JSONObject services = loadServices(is);
                JSONObject service = services.getJSONObject(serviceKey);
                // default to the service default if no url parameters are specified
                if (urlParams == null && service.has("defaultURLParams")) {
                    urlParams = service.getString("defaultURLParams");
                }
                String serviceURL = service.getString("url");
                // build the URL properly
                if (urlParams != null && !serviceURL.contains("?")) {
                    serviceURL += "?";
                }
                else if (urlParams != null) {
                    serviceURL += "&";
                }
                urlString = serviceURL + service.getString("apikey") +  "&" + urlParams;
                if (service.has("xslStyleSheet")) {
                    xslURLString = service.getString("xslStyleSheet");
                    // check if the url is correct of if to load from the classpath
                }
            }
            catch (Exception e) {
                logger.error("XmlHttpProxy Error loading service: {}", e.getMessage(), e);
                System.exit(1);
            }
        }
        else if (urlString == null) {
            System.out.println(USAGE);
            System.exit(1);
        }
        // The parameters are feed to the XSL Stylesheet during transformation.
        // These parameters can provided data or conditional information.
        Map<String, String> paramsMap = new HashMap<>();
        if (format != null) {
            paramsMap.put("format", format);
        }
        if (callback != null) {
            paramsMap.put("callback", callback);
        }

        if (xslURLString != null) {
            URL xslURL = new URL(xslURLString);
            if (xslURL != null) {
                xslInputStream  = xslURL.openStream();
            }
            else {
                logger.error("Error: Unable to locate XSL at URL {}", xslURLString);
            }
        }
        xhp.doGet(urlString, System.out, xslInputStream, paramsMap);
    }

    public static JSONObject loadServices(InputStream is) {
        JSONObject services = new JSONObject();
        try {
            JSONObject config = loadJSONObject(is).getJSONObject("xhp");
            JSONArray sA = config.getJSONArray("services");
            for (int l=0; l < sA.length(); l++) {
                JSONObject value = sA.getJSONObject(l);
                String key = value.getString("id");
                services.put(key,value);
            }
        }
        catch (Exception e) {
            logger.error("XmlHttpProxy error loading services: {}", e.getMessage(), e);
        }
        return services;
    }

    public static JSONObject loadJSONObject(InputStream inputStream) {
        ByteArrayOutputStream out = null;
        try {
            byte[] buffer = new byte[1024];
            out = new ByteArrayOutputStream();
            while (true) {
                int read = inputStream.read(buffer);
                if (read <= 0) {
                    break;
                }
                out.write(buffer, 0, read );
            }
            return new JSONObject(out.toString());
        }
        catch (Exception e) {
            logger.error("XmlHttpProxy error reading in json {}", e.getMessage(), e);
        }
        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (out != null) {
                    out.flush();
                    out.close();
                }
            }
            catch (Exception ignored) {}
        }
        return null;
    }
}
