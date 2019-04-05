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

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright 2007 You may not modify, use, reproduce, or distribute this software except in compliance with the terms of the License at:
 * http://developer.sun.com/berkeley_license.html
 *
 *   @author Greg Murray
 */
public class XmlHttpProxyServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger("jmaki.services.xhp.Log");

    private static final String XHP_LAST_MODIFIED = "xhp_last_modified_key";
    private static final String XHP_CONFIG = "xhp.json";

    private static boolean allowXDomain = true;
    private static boolean requireSession = false;
    private static String responseContentType = "text/xml;charset=UTF-8";  //changed from text/json in jmaki source
    private XmlHttpProxy xhp;
    private ServletContext servletContext;
    private JSONObject services;
    private String resourcesDir = "/resources/";
    private String classpathResourcesDir = "/META-INF/resources/";
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servletContext = config.getServletContext();
        // set the response content type
        if (servletContext.getInitParameter("responseContentType") != null) {
            responseContentType = servletContext.getInitParameter("responseContentType");
        }
        // allow for resources dir over-ride at the xhp level otherwise allow
        // for the jmaki level resources
        if (servletContext.getInitParameter("jmaki-xhp-resources") != null) {
            resourcesDir = servletContext.getInitParameter("jmaki-xhp-resources");
        }
        else if (servletContext.getInitParameter("jmaki-resources") != null) {
            resourcesDir = servletContext.getInitParameter("jmaki-resources");
        }
        // allow for resources dir over-ride
        if (servletContext.getInitParameter("jmaki-classpath-resources") != null) {
            classpathResourcesDir = servletContext.getInitParameter("jmaki-classpath-resources");
        }     

        String requireSessionString = servletContext.getInitParameter("requireSession");
        if (requireSessionString != null) {
            if ("false".equals(requireSessionString)) {
                requireSession = false;
                logger.error("XmlHttpProxyServlet: intialization. Session requirement disabled.");
            }
            else if ("true".equals(requireSessionString)) {
                requireSession = true;
                logger.error("XmlHttpProxyServlet: intialization. Session requirement enabled.");
            }
        }
        String xdomainString = servletContext.getInitParameter("allowXDomain");
        if (xdomainString != null) {
            if ("true".equals(xdomainString)) {
                allowXDomain = true;
                logger.error("XmlHttpProxyServlet: intialization. xDomain access is enabled.");
            }
            else if ("false".equals(xdomainString)) {
                allowXDomain = false;
                logger.error("XmlHttpProxyServlet: intialization. xDomain access is disabled.");
            }
        }
        // if there is a proxyHost and proxyPort specified create an HttpClient with the proxy
        String proxyHost = servletContext.getInitParameter("proxyHost");
        String proxyPortString = servletContext.getInitParameter("proxyPort");
        if (proxyHost != null && proxyPortString != null) {
            int proxyPort;
            try {
                proxyPort = Integer.valueOf(proxyPortString);
                xhp = new XmlHttpProxy(proxyHost, proxyPort);
            }
            catch (NumberFormatException e) {
                logger.error("XmlHttpProxyServlet: intialization error. The proxyPort must be a number");
                throw new ServletException("XmlHttpProxyServlet: intialization error. The proxyPort must be a number");
            }
        }
        else {
            xhp = new XmlHttpProxy();
        }
    }
    
    private void getServices(HttpServletResponse res) {
        InputStream inputStream = null;
        try {
            URL url = servletContext.getResource(resourcesDir + XHP_CONFIG);
            // use classpath if not found locally.
            if (url == null) {
                url = XmlHttpProxyServlet.class.getResource(classpathResourcesDir + XHP_CONFIG);
            }
            inputStream = url.openStream();
        }
        catch (Exception e) {
            try {
                logger.error("XmlHttpProxyServlet error loading xhp.json : {}", e.getMessage(), e);
                PrintWriter writer = res.getWriter();
                writer.write("XmlHttpProxyServlet Error: Error loading xhp.json. " +
                             "Make sure it is available in the /resources directory of your applicaton.");
                writer.flush();
            }
            catch (Exception ignored) { }
        }
        services = XmlHttpProxy.loadServices(inputStream);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) {
        doProcess(req,res, false);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) {
        doProcess(req,res, true);
    }
    
    public void doProcess(HttpServletRequest request, HttpServletResponse response, boolean post) {
        StringBuilder bodyContent = null;
        OutputStream out = null;
        PrintWriter writer = null;
        String serviceKey;

        try {
            BufferedReader in = request.getReader();
            String line = null;
            while ((line = in.readLine()) != null) {
                if (bodyContent == null) {
                    bodyContent = new StringBuilder();
                }
                bodyContent.append(line); 
            }
        }
        catch (Exception ignored) {}

        try {
            if (requireSession) {
                // check to see if there was a session created for this request
                // if not assume it was from another domain and blow up
                // Wrap this to prevent Portlet exeptions
                HttpSession session = request.getSession(false);
                if (session == null) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
            }
            serviceKey = request.getParameter("id");
            // only to preven regressions - Remove before 1.0
            if (serviceKey == null) {
                serviceKey = request.getParameter("key");
            }
            // check if the services have been loaded or if they need to be reloaded
            if (services == null || configUpdated()) {
                getServices(response);
            }
            String urlString = null;
            String xslURLString = null;
            String userName = null;
            String password = null;            
            String format = "json";
            String callback = request.getParameter("callback");
            String urlParams = request.getParameter("urlparams");
            String countString = request.getParameter("count");
            // encode the url to prevent spaces from being passed along
            if (urlParams != null) {
                urlParams = urlParams.replace(' ', '+');
            }

            try {
                if (services.has(serviceKey)) {
                    JSONObject service = services.getJSONObject(serviceKey);
                    // default to the service default if no url parameters are specified
                    if (urlParams == null && service.has("defaultURLParams")) {
                        urlParams = service.getString("defaultURLParams");
                    }
                    String serviceURL = service.getString("url");
                    // build the URL
                    if (urlParams != null && !serviceURL.contains("?")) {
                        serviceURL += "?";
                    }
                    else if (urlParams != null) {
                        serviceURL += "&";
                    }
                    String apikey = "";
                    if (service.has("username")) {
                        userName = service.getString("username");
                    }
                    if (service.has("password")) {
                        password = service.getString("password");
                    }
                    if (service.has("apikey")) {
                        apikey = service.getString("apikey");
                    }
                    urlString = serviceURL + apikey;
                    if (urlParams != null) {
                        urlString += "&" + urlParams;
                    }
                    if (service.has("xslStyleSheet")) {
                        xslURLString = service.getString("xslStyleSheet");
                    }
                }
                //code for passing the url directly through instead of using configuration file
                else if (request.getParameter("url") != null) {
                    String serviceURL = request.getParameter("url");
                    // build the URL
                    if (urlParams != null && !serviceURL.contains("?")) {
                        serviceURL += "?";
                    }
                    else if (urlParams != null) {
                        serviceURL += "&";
                    }
                    urlString = serviceURL;
                    if (urlParams != null) {
                        urlString += urlParams;
                    }
                }
                else {
                    writer = response.getWriter();
                    if (serviceKey == null) {
                        writer.write("XmlHttpProxyServlet Error: id parameter specifying serivce required.");
                    }
                    else {
                        writer.write("XmlHttpProxyServlet Error : service for id '" + serviceKey + "' not  found.");
                    }
                    writer.flush();
                    return;
                }
            }
            catch (Exception e) {
                logger.error("XmlHttpProxyServlet Error loading service: {}", e.getMessage(), e);
            }

            Map<String, String> paramsMap = new HashMap<>();
            paramsMap.put("format", format);
            // do not allow for xdomain unless the context level setting is enabled.
            if (callback != null && allowXDomain) {
                paramsMap.put("callback", callback);
            }
            if (countString != null) {
                paramsMap.put("count", countString);
            }            

            InputStream xslInputStream = null;

            if (urlString == null) {
                writer = response.getWriter();
                writer.write("XmlHttpProxyServlet parameters:  id[Required] urlparams[Optional] format[Optional] callback[Optional]");
                writer.flush();
                return;
            }
            // default to JSON
            response.setContentType(responseContentType);
            out = response.getOutputStream();
            // get the stream for the xsl stylesheet
            if (xslURLString != null) {
                // check the web root for the resource
                URL xslURL = null;
                xslURL = servletContext.getResource(resourcesDir + "xsl/" + xslURLString);
                // if not in the web root check the classpath
                if (xslURL == null) {
                    xslURL = XmlHttpProxyServlet.class.getResource(classpathResourcesDir + "xsl/" + xslURLString);
                }
                if (xslURL != null) {
                    xslInputStream  = xslURL.openStream();
                }
                else {
                    String message = "Could not locate the XSL stylesheet provided for service id " + serviceKey +
                        ". Please check the XMLHttpProxy configuration.";
                    logger.error(message);
                    try {
                        out.write(message.getBytes());
                        out.flush();
                        return;
                    }
                    catch (IOException ignored) {}
                }
            }
            if (!post) {
                xhp.doGet(urlString, out, xslInputStream, paramsMap, userName, password);
            }
            else {
                if (bodyContent == null) {
                    logger.info(
                        "XmlHttpProxyServlet attempting to post to url {} with no body content",
                        urlString);
                }
                xhp.doPost(urlString, out, xslInputStream, paramsMap, bodyContent.toString(),
                           request.getContentType(), userName, password);
            }
        }
        catch (Exception e) {
            logger.error("XmlHttpProxyServlet: caught {}", e.getMessage(), e);
            try {
                writer = response.getWriter();
                writer.write(e.toString());
                writer.flush();
            }
            catch (IOException ioe) {
                logger.error(ioe.getMessage(), ioe);
            }
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (writer != null) {
                    writer.close();
                }
            }
            catch (IOException ignored) {}
        }
    }

    /**
     * Check to see if the configuration file has been updated so that it may be reloaded.
     */
    private boolean configUpdated() {
        try {
            URL url = servletContext.getResource(resourcesDir + XHP_CONFIG);
            if (url == null) {
                return false;
            }

            URLConnection con = url.openConnection();
            long lastModified = con.getLastModified();
            long xhpLastModified;
            if (servletContext.getAttribute(XHP_LAST_MODIFIED) != null) {
                xhpLastModified = (long) servletContext.getAttribute(XHP_LAST_MODIFIED);
            }
            else {
                servletContext.setAttribute(XHP_LAST_MODIFIED, lastModified);
                return false;
            }

            if (xhpLastModified < lastModified) {
                servletContext.setAttribute(XHP_LAST_MODIFIED, lastModified);
                return true;
            }
        }
        catch (Exception e) {
            logger.error("XmlHttpProxyServlet error checking configuration: {}", e.getMessage(), e);
        }
        return false;
    }
}
