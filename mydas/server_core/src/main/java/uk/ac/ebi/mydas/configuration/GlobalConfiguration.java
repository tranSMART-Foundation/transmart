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

package uk.ac.ebi.mydas.configuration;

import java.util.*;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 17:51:18
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class GlobalConfiguration {


    private static final String PROC_INST_START = "xml-stylesheet href=\"";

    private static final String PROC_INST_END = "\" type=\"text/xsl\"";

    /**
     * The baseURL used in the ./GFF/@href element
     */
    private String baseURL = null;

    /**
     * Default stylesheet.  Can be overridden by individual DSNs
     */
    private String defaultStyleSheet = null;

    /**
     * A boolean to indicate if the response should be gzipped or not.
     */
    private boolean gzipped = false;

    /**
     * If set to true, then if the browser points to /das or /das/ then
        the output of the dsn command should be displayed.  This is
        useful for creating a home page based upon the data source
        details.
     */
    private boolean slashDasPointsToDsn = false;

    /**
     * Gives the URI of an XSLT transformation that can be used by the browser
        to present the dsn command response in a human-readable format.
        Optional - the processing instruction will only be included if this
        element is present.
     */
    private String dsnXSLT = null;

    /**
     * Gives the URI of an XSLT transformation that can be used by the browser
        to present the dna command response in a human-readable format.
        Optional - the processing instruction will only be included if this
        element is present.
     */
    private String dnaXSLT = null;

    /**
     * Gives the URI of an XSLT transformation that can be used by the browser
        to present the entry-points command response in a human-readable format.
        Optional - the processing instruction will only be included if this
        element is present.
     */
    private String entryPointsXSLT = null;

    /**
     * Gives the URI of an XSLT transformation that can be used by the browser
      to present the sequence command response in a human-readable format.
      Optional - the processing instruction will only be included if this
      element is present.
     */
    private String sequenceXSLT = null;

    /**
     * Gives the URI of an XSLT transformation that can be used by the browser
        to present the features command response in a human-readable format.
        Optional - the processing instruction will only be included if this
        element is present.
     */
    private String featuresXSLT = null;

    /**
     * Gives the URI of an XSLT transformation that can be used by the browser
        to present the types command response in a human-readable format.
        Optional - the processing instruction will only be included if this
        element is present.
     */
    private String typesXSLT = null;

    /**
     * A Map of arbitrary key / value pairs that can be passed into the
     * server from the MydasServerConfig.xml file.
     *
     * Accessible from the DataSources, so may be useful as global parameters
     * by the plugin implementor.
     */
    private Map<String, String> globalParameters = null;

    
    public GlobalConfiguration(String baseURL, String defaultStyleSheet, boolean gzipped, boolean slashDasPointsToDsn, String dsnXSLT, String dnaXSLT, String entryPointsXSLT, String sequenceXSLT, String featuresXSLT, String typesXSLT, Map<String, String> globalParameters) {
        this.baseURL = baseURL;
        this.defaultStyleSheet = defaultStyleSheet;
        this.gzipped = gzipped;
        this.slashDasPointsToDsn = slashDasPointsToDsn;
        if (dsnXSLT != null)
            this.dsnXSLT = PROC_INST_START + dsnXSLT.trim() + PROC_INST_END;
        if (dnaXSLT != null)
            this.dnaXSLT = PROC_INST_START + dnaXSLT.trim() + PROC_INST_END;
        if (entryPointsXSLT != null)
            this.entryPointsXSLT = PROC_INST_START + entryPointsXSLT.trim() + PROC_INST_END;
        if (sequenceXSLT != null)
            this.sequenceXSLT = PROC_INST_START + sequenceXSLT.trim() + PROC_INST_END;
        if (featuresXSLT != null)
            this.featuresXSLT = PROC_INST_START + featuresXSLT.trim() + PROC_INST_END;
        if (typesXSLT != null)
            this.typesXSLT = PROC_INST_START + typesXSLT.trim() + PROC_INST_END;
        this.globalParameters = globalParameters;
    }

//    public GlobalConfiguration(String baseURL, boolean gzipped, String defaultStyleSheet, Map<String, String> globalParameters) {
//        this.baseURL = baseURL;
//        this.gzipped = gzipped;
//        this.globalParameters = globalParameters;
//        this.defaultStyleSheet = defaultStyleSheet;
//    }

    public String getBaseURL() {
        return baseURL;
    }


    public String getDefaultStyleSheet() {
        return defaultStyleSheet;
    }

    public boolean isGzipped() {
        return gzipped;
    }

    public Map<String, String> getGlobalParameters() {
        return globalParameters;
    }


    public boolean isSlashDasPointsToDsn() {
        return slashDasPointsToDsn;
    }

    public String getDsnXSLT() {
        return dsnXSLT;
    }

    public String getDnaXSLT() {
        return dnaXSLT;
    }

    public String getEntryPointsXSLT() {
        return entryPointsXSLT;
    }

    public String getSequenceXSLT() {
        return sequenceXSLT;
    }

    public String getFeaturesXSLT() {
        return featuresXSLT;
    }

    public String getTypesXSLT() {
        return typesXSLT;
    }
}
