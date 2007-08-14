/*
 * Copyright 2007 Philip Jones, EMBL-European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * For further details of the mydas project, including source code,
 * downloads and documentation, please see:
 *
 * http://code.google.com/p/mydas/
 *
 */

package uk.ac.ebi.mydas.controller;

import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import java.io.PrintWriter;

/**
 * Created Using IntelliJ IDEA.
 * Date: 24-Jul-2007
 * Time: 15:54:35
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class EmptyCache extends javax.servlet.http.HttpServlet{

    /**
     * Define a static logger variable so that it references the
     * Logger instance named "EmptyCache".
     */
    private static final Logger logger = Logger.getLogger(EmptyCache.class);

    /**
     * This method will ensure that all the plugins are registered and call
     * the corresonding init() method on all of the plugins.
     *
     * Also initialises the XMLPullParser factory.
     * @throws javax.servlet.ServletException
     */
    public void init() throws ServletException {
        super.init();
    }


    protected void doGet(javax.servlet.http.HttpServletRequest httpServletRequest, javax.servlet.http.HttpServletResponse httpServletResponse)
            throws javax.servlet.ServletException, java.io.IOException {

        PrintWriter out = httpServletResponse.getWriter();
        try{
            if (httpServletRequest.getParameter("datasource") != null && httpServletRequest.getParameter("datasource").length() > 0){
                String dataSource = httpServletRequest.getParameter("datasource");
                logger.info ("Flushing cache for dsn " + dataSource);
                MydasServlet.CACHE_MANAGER.flushGroup(dataSource);
                out.println("<html><head><title>Empty UniProt DAS Server Cache</title></head><body><h3>The cache for datasource '" + dataSource + "' has been flushed.</h3></body></html>");
            }
            else {
                logger.info ("Flushing entire cache.");
                MydasServlet.CACHE_MANAGER.flushAll();
                out.println("<html><head><title>Empty UniProt DAS Server Cache</title></head><body><h3>The entire UniProt DAS cache has been emptied.</h3></body></html>");
            }
        }
        finally{
            out.flush();
            out.close();
        }
    }

    protected void doPost(javax.servlet.http.HttpServletRequest httpServletRequest, javax.servlet.http.HttpServletResponse httpServletResponse) throws javax.servlet.ServletException, java.io.IOException {
        doGet(httpServletRequest, httpServletResponse);
    }
}
