package uk.ac.ebi.mydas.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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


    public ServerConfiguration(GlobalConfiguration globalConfiguration, Map<String, DataSourceConfiguration> dataSourceConfigList) {
        this.globalConfiguration = globalConfiguration;
        this.dataSourceConfigMap = dataSourceConfigList;
    }


    public GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }

    public Map<String, DataSourceConfiguration> getDataSourceConfigMap() {
        return dataSourceConfigMap;
    }

    /**
     * Convenience method to return the DataSourceConfiguration by id.
     * @param dsnName being the id of the datasource.
     * @return a DataSourceConfiguration object.
     */
    public DataSourceConfiguration getDataSourceConfig (String dsnName) {
        return dataSourceConfigMap.get(dsnName);
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
