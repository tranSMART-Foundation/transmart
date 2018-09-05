/*
 * Copyright (C) 2016 ITTM S.A.
 *
 * Written by Nils Christian <nils.christian@ittm-solutions.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.ittm_solutions.ipacore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Caching wrapper around the {@code IpaApi} assuming analysis names are unique.
 * <p>
 * This wraps calls to {@link IpaApi} assuming that analysis names are unique
 * (eg. a hash of the serialised data and parameters) within the project
 * {@code projectName}. When a new analysis is launched, the dataset name will
 * always be set to the analysis name.
 */


public class IpaApiCached {
    /**
     * name of the project folder in IPA
     */
    private String projectName;
    /**
     * instance querying the IPA server
     */
    private IpaApi ipa = null;
    /**
     * maps analysis names to analysis ids
     */
    private Map<String, String> analysisNameToId = new HashMap<String, String>();
    private final Log log = LogFactory.getLog(IpaApi.class);

    /**
     * Constructor.
     *
     * @param username
     *          the username used to connect to the IPA server
     * @param password
     *          the password used to connect to the IPA server
     * @param serverUrl
     *          the URL of the the IPA server
     * @param projectName
     *          project name that will be used for creating and searching analyses
     */
    public IpaApiCached(String username, String password, String serverUrl, String projectName) {
        this.projectName = projectName;
        ipa = new IpaApi(username, password, serverUrl);
    }

    /**
     * Constructor using the default URL for the IPA server.
     *
     * @param username
     *          the username used to connect to the IPA server
     * @param password
     *          the password used to connect to the IPA server
     * @param projectName
     *          project name that will be used for creating and searching analyses
     */
    public IpaApiCached(String username, String password, String projectName) {
        this(username, password, "https://analysis.ingenuity.com", projectName);
    }

    /**
     * Exports the given analysis.
     * <p>
     * This method calls the export endpoint of the IPA API, allowing to export
     * one analysis by its id.
     *
     * @param analysisId
     *          the analysis id
     * @return exported results wrapped in a class
     */
    public synchronized IpaAnalysisResults exportAnalysis(String analysisId) throws IpaApiException {
        try {
            return ipa.exportAnalysis(analysisId);
        } catch (IpaApiException e) {
            // information stored about this analysis (if any) probably outdated,
            // remove it
            while (analysisNameToId.values().remove(analysisId));
            if (e.getMessage().equals("analysis id does not exist")) {
                log.warn("no analysis with id " + analysisId + " stored on server");
            } else {
                throw e;
            }
        }
        return null;
    }

    /**
     * Uploads a dataset and initiates an analysis on the server.
     * <p>
     * This method calls the {@code dataanalysis} endpoint of the IPA API,
     * allowing to upload a dataset and starting an analysis.
     * <p>
     * For details of the meaning of the parameters refer to the IPA Integration
     * Module documentation.
     *
     * @param analysisName
     *          name of the analysis and dataset saved in IPA
     * @param geneIdType
     *          gene or protein identifier type (eg. {@code entrezgene},
     *          {@code affymetrix}, ...)
     * @param geneId
     *          gene or protein ids
     * @param expValueType
     *          expression value type (eg. {@code foldchange}, {@code pvalue}, ...
     * @param expValue
     *          expression values corresponding to the {@code geneId} entries
     * @param expValueType2
     *          expression value type of {@code expValue}
     * @param expValue2
     *          second expression values corresponding to the {@code geneId}
     *          entries
     * @param applicationName
     *          the name of the application calling the API (this will be saved in
     *          an analysis's metadata)
     * @return true if a new analysis ws launched, false if analysis already
     *         exists
     */
    public synchronized boolean dataAnalysis(String analysisName, String geneIdType, List<String> geneId,
            String expValueType, List<? extends Number> expValue, String expValueType2,
            List<? extends Number> expValue2, String applicationName) throws IpaApiException {
        if (analysisNameToId.containsKey(analysisName)) {
            // this name already exists
            return false;
        }
        if (this.analysisId(analysisName) != null) {
            return false;
        }
        try {
            ipa.dataAnalysis(projectName, analysisName, analysisName, geneIdType, geneId, expValueType, expValue, expValueType2, expValue2, applicationName);
        } catch (IpaApiException e) {
            if (e.getMessage().equals("dataset name exists") || e.getMessage().equals("analysis name exists")) {
                // this name already exists
                return false;
            } else {
                throw e;
            }
        }
        return true;
    }

    /**
     * Returns the analysis name to id mapping for all analyses of this project.
     * <p>
     * All analyses of this project (with the name {@code projectName}, as this
     * class was initialised) are exported and their name to id mapping is
     * returned.
     *
     * @return the mapping from analysis name to id
     */
    public synchronized Map<String, String> analysisNameToId() throws IpaApiException {
        analysisNameToId = ipa.analysisNameToIdForProjectName(projectName);
        return analysisNameToId;
    }

    /**
     * Returns the id of the analysis with the given name if it exists.
     *
     * @param analysisName
     *          name of analysis to be looked up
     * @return null or analysis id
     */
    public synchronized String analysisId(String analysisName) throws IpaApiException {
        // first check whether the analysis id corresponding to the name is already
        // know
        if (analysisNameToId.containsKey(analysisName)) {
            return analysisNameToId.get(analysisName);
        }

        // id is not known, so get it from the server
        Map<String, String> anameToId = ipa.analysisNameToIdForProjectName(projectName, analysisName);
        for (Map.Entry<String, String> nameId : anameToId.entrySet()) {
            // update internal state
            analysisNameToId.put(nameId.getKey(), nameId.getValue());
        }

        return analysisNameToId.get(analysisName);
    }
}
