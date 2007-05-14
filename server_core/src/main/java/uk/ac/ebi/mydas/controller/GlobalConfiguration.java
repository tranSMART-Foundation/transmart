package uk.ac.ebi.mydas.controller;

import java.util.*;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 17:51:18
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class GlobalConfiguration {

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
     * A Map of arbitrary key / value pairs that can be passed into the
     * server from the MydasServerConfig.xml file.
     *
     * Accessible from the DataSources, so may be useful as global parameters
     * by the plugin implementor.
     */
    private Map<String, String> globalParameters = null;


    public GlobalConfiguration(String baseURL, boolean gzipped, String defaultStyleSheet, Map<String, String> globalParameters) {
        this.baseURL = baseURL;
        this.gzipped = gzipped;
        this.globalParameters = globalParameters;
        this.defaultStyleSheet = defaultStyleSheet;
    }

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

}
