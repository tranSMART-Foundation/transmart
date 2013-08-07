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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created Using IntelliJ IDEA.
 * Date: 08-May-2007
 * Time: 11:56:58
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class ServerConfiguration {

    private List<String> liveDsnNames = null;

    private final GlobalConfiguration globalConfiguration;

    private final Map<String, DataSourceConfiguration> dataSourceConfigMap;

    private final Map<Pattern, DataSourceConfiguration> dynamicDataSourcesConfigMap;


    public ServerConfiguration(GlobalConfiguration globalConfiguration, Map<String, DataSourceConfiguration> dataSourceConfigList) {
        this.globalConfiguration = globalConfiguration;
        this.dataSourceConfigMap = dataSourceConfigList;
        this.dynamicDataSourcesConfigMap = new LinkedHashMap<Pattern, DataSourceConfiguration>();
        for (Map.Entry<String, DataSourceConfiguration> entry : dataSourceConfigList.entrySet()) {
            String pattern = entry.getValue().getConfig().getPattern();
            if (pattern == null) {
                continue;
            }

            this.dynamicDataSourcesConfigMap.put(Pattern.compile(pattern),
                    entry.getValue());
        }
    }


    public GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }

    /**
     * Return all the configured data sources.
     *
     * @return collection of configured data sources
     */
    public Collection<DataSourceConfiguration> getDataSourceConfigs() {
        if (this.dataSourceConfigMap == null) {
            return Collections.emptyList();
        }

        return this.dataSourceConfigMap.values();
    }

    /**
     * Convenience method to return the DataSourceConfiguration by id.
     * @param dsnName being the id of the datasource.
     * @return a DataSourceConfiguration object.
     */
    public DataSourceConfiguration getDataSourceConfig(String dsnName) {
        DataSourceConfiguration config = dataSourceConfigMap.get(dsnName);

        if (config == null) {
            for (Map.Entry<Pattern, DataSourceConfiguration> entry:
                    this.dynamicDataSourcesConfigMap.entrySet()) {
                Matcher matcher = entry.getKey().matcher(dsnName);
                if (matcher.matches()) {
                    return new DataSourceConfiguration(entry.getValue(), matcher);
                }
            }
        }

        return null;
    }

    /**
     * Lazy loads and returns a List of successfully initilised dsns.
     * @return a List of successfully initilised dsns.
     */
    public List<String> getDsnNames() {
        if (liveDsnNames == null){
            liveDsnNames = new ArrayList<String> (dataSourceConfigMap.size());
            // Iterate over the dsns and return only the ones that report themselves as 'ok'.
            for (String dsnName : dataSourceConfigMap.keySet()){
                if (dataSourceConfigMap.get(dsnName).isOK()){
                    liveDsnNames.add(dsnName);
                }
            }
        }
        return liveDsnNames;
    }
}
