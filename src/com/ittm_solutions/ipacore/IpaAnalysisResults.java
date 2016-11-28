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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Container for results of an Ingenuity pathway analysis.
 */
public class IpaAnalysisResults {
    /**
     * Ingenuity workspace.
     */
    private String workspace = null;

    /**
     * Name of the project where analysis is stored.
     */
    private String projectName = null;

    /**
     * Name of the analysis.
     */
    private String analysisName = null;

    /**
     * Id (Ingenuity internal) of the analysis.
     * <p>
     * This id is used to retrieve information about the analysis from the API.
     */
    private String analysisId = null;

    /**
     * Result tables, accessible by the section name.
     */
    private Map<String, SimpleTable> tables = new LinkedHashMap<String, SimpleTable>();

    @Override
    public String toString() {
        String out = "projectName\t" + projectName + "\n";
        out += "analysisName\t" + analysisName + "\n";
        out += "analysisId\t" + analysisId + "\n";
        for (SimpleTable t : tables.values()) {
            out += t.toString();
        }
        return out;
    }

    /**
     * @return the workspace
     * @see #workspace
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * @param workspace
     *          the workspace to set
     * @see #workspace
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    /**
     * @return the projectName
     * @see #projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * @param projectName
     *          the projectName to set
     * @see #projectName
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * @return the analysisName
     * @see #analysisName
     */
    public String getAnalysisName() {
        return analysisName;
    }

    /**
     * @param analysisName
     *          the analysisName to set
     * @see #analysisName
     */
    public void setAnalysisName(String analysisName) {
        this.analysisName = analysisName;
    }

    /**
     * @return the analysisId
     * @see #analysisId
     */
    public String getAnalysisId() {
        return analysisId;
    }

    /**
     * @param analysisId
     *          the analysisId to set
     * @see #analysisId
     */
    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    /**
     * @return the tables
     * @see #tables
     */
    public Map<String, SimpleTable> getTables() {
        return tables;
    }

    /**
     * @param tables
     *          the tables to set
     * @see #tables
     */
    public void setTables(Map<String, SimpleTable> tables) {
        this.tables = tables;
    }
}
