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

import org.apache.log4j.Logger;

import uk.ac.ebi.mydas.configuration.Mydasserver.Datasources.Datasource.Version.Capability;
import uk.ac.ebi.mydas.datasource.AnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.DataSourceException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 13:21:06
 *
 * This class holds details of the datasource, limited to those set in the ServerConfig.xml
 * file.  An instance of this class is passed to implementations of AnnotationDataSource when the init() method
 * is called, allowing the AnnotationDataSource access to its configuration in a transparent manner.
 *
 * <b>Note that no action is required on the part of the datasource plugin regarding the standard
 * configuration available from this class</b> - this is handled by the MydasServlet class and is
 * for information only.
 *
 * You can of course use the properties Map to pass in arbitrary / data source specific configuration.
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class DataSourceConfiguration {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named "XMLUnmarshaller".
     */
    private static final Logger logger = Logger.getLogger(DataSourceConfiguration.class);

    /**
     * A reference to the dataSource itself, once it is loaded.
     * (If it is loaded!)
     */
    private AnnotationDataSource dataSource;


    /**
     * A boolean flag to indicate if the datasource failed on initialisation.
     */
    private boolean datasourceOK;

    private final Mydasserver.Datasources.Datasource config;
    private int versionPosition;
    public DataSourceConfiguration(Mydasserver.Datasources.Datasource config, int versionPosition){
    	this.config=config;
    	this.versionPosition=versionPosition;
    	this.cacheGroup[0] = config.getUri();

        if (logger.isDebugEnabled()){
            logger.debug("New DataSourceConfiguration instantiated: \n" + this.toString());
        }
    }

    /**
     * Stores the cache group for caching purposes.
     */
    private final String[] cacheGroup = new String[1];


    /**
     * Returns the mandatory value for /DASDSN/DSN/SOURCE/@id
     * @return the mandatory value for /DASDSN/DSN/SOURCE/@id
     */
    public String getId() {
        return config.getVersion().get(this.versionPosition).getUri();
    }

    /**
     * Returns the optional value for /DASDSN/DSN/SOURCE
     * @return the optional value for /DASDSN/DSN/SOURCE
     */
    public String getName() {
        return config.getTitle();
    }

    /**
     * Returns the optional value for /DASDSN/DSN/SOURCE/@version
     * 
     * @return the optional value for /DASDSN/DSN/SOURCE/@version
     */
    public String getVersion() {
        return config.getVersion().get(this.versionPosition).getCreated().toString();
    }

    /**
     * Returns the optional value for /DASDSN/DSN/MAPMASTER
     * @return the optional value for /DASDSN/DSN/MAPMASTER
     */
    public String getMapmaster() {
        return config.getVersion().get(this.versionPosition).getCoordinates().get(0).getUri();
    }

    /**
     * Returns the optional value for /DASDSN/DSN/DESCRIPTION
     * @return the optional value for /DASDSN/DSN/DESCRIPTION
     */
    public String getDescription() {
        return config.getDescription();
    }

    /**
     * Returns the optional value for /DASDSN/DSN/DESCRIPTION/@href
     * @return the optional value for /DASDSN/DSN/DESCRIPTION/@href
     */
    public URL getDescriptionHref() {
        try {
			return new URL(config.getDocHref());
		} catch (MalformedURLException e) {
			return null;
		}
    }

    /**
     * Returns the name of the stylesheet for the plugin
     * @return the name of the stylesheet for the plugin
     */
    public String getStyleSheet() {
        return config.getStylesheet();
    }

    /**
     * Returns a Map of key value pairs defined by the plugin developer.
     * @return a Map of key value pairs defined by the plugin developer.
     */
    public Map<String, String> getDataSourceProperties() {
    	Map<String, String> props = new HashMap<String, String>();
    	for (PropertyType pt:config.getProperty())
    		props.put(pt.key, pt.value);
        return props;
    }


    /**
     * returns a flag to indicate if the dna command is enabled.
     * @return a flag to indicate if the dna command is enabled.
     */
    public boolean isDnaCommandEnabled() {
        return config.getDnaCommandEnabled().value;
    }

    /**
     * Returns a flag to indicate if features should only be returned if they appear strictly
     * within the coordinates given.
     * @return a flag to indicate if features should only be returned if they appear strictly
     * within the coordinates given.
     */
    public boolean isFeaturesStrictlyEnclosed() {
        return config.getFeaturesStrictlyEnclosed().value;
    }

    /**
     * Returns a flag to indicate if the feature id should be used for the label
     * if no label is set by the data source.
     * @return a flag to indicate if the feature id should be used for the label
     * if no label is set by the data source.
     */
    public boolean isUseFeatureIdForFeatureLabel() {
        return config.getUseFeatureIdForFeatureLabel().value;
    }


    /**
     * Returns flag indicating if types with a count of zero should be included in the
     * output of the types command.
     * @return flag indicating if types with a count of zero should be included in the
     * output of the types command.
     */
    public boolean isIncludeTypesWithZeroCount() {
        return config.getIncludeTypesWithZeroCount().value;
    }



    /**
     * This method is called by the DataSourceManager to load
     * the datasource.
     * 
     * @return a boolean indicating if the load was successful or not.
     * (This value is also stored in the datasourceOK flag, for later retrieval.)
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     * if a problem occurs when attempting to instantiate the DataSource class.
     */
    public boolean loadDataSource() throws DataSourceException{
        datasourceOK = false;    // Pessimistic start.
        String className=config.getVersion().get(this.versionPosition).getClazz();
        try{
            ClassLoader classLoader = this.getClass().getClassLoader();
            dataSource = (AnnotationDataSource) (classLoader.loadClass(className)).newInstance();
            if (dataSource != null){
                datasourceOK = true;
            }
        } catch (ClassNotFoundException e) {
            datasourceOK = false;
            logger.error("ClassNotFoundException thrown when attempting to load data source " + className, e);
            throw new DataSourceException ("ClassNotFoundException thrown when attempting to instantiate a '" + className + "'.  Please check that the configuration XML file and the classpath are correct.", e);
        } catch (IllegalAccessException e) {
            datasourceOK = false;
            logger.error("IllegalAccessException thrown when attempting to load data source " + className, e);
            throw new DataSourceException ("IllegalAccessException thrown when attempting to instantiate a '" + className + "'.", e);
        } catch (InstantiationException e) {
            datasourceOK = false;
            logger.error("InstantiationException thrown when attempting to load data source " + className, e);
            throw new DataSourceException ("InstantiationException thrown when attempting to instantiate a '" + className + "'.", e);
        }
        return datasourceOK;
    }

    /**
     * Gives the DataSourceManager access to the DataSource.
     * @return A successfully initialised data source.
     * @throws DataSourceException in the event that the DataSourceManager code
     * attempts to access a DataSource that has not loaded successfully without
     * checking first! (That would be a bug, by the way).
     */
    public AnnotationDataSource getDataSource() throws DataSourceException {
        if (! datasourceOK){
            throw new DataSourceException ("An attempt has been made to access an AnnotationDataSource that has not been successfully loaded.");
        }
        return dataSource;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSourceConfiguration that = (DataSourceConfiguration) o;
        
        return (that.getConfig().equals(this.getConfig()));
//
//        if (dataSourceProperties != null ? !dataSourceProperties.equals(that.dataSourceProperties) : that.dataSourceProperties != null)
//            return false;
//        if (description != null ? !description.equals(that.description) : that.description != null) return false;
//        if (descriptionHref != null ? !descriptionHref.equals(that.descriptionHref) : that.descriptionHref != null)
//            return false;
//        if (!id.equals(that.id)) return false;
//        if (mapmaster != null ? !mapmaster.equals(that.mapmaster) : that.mapmaster != null) return false;
//        if (name != null ? !name.equals(that.name) : that.name != null) return false;
//        if (styleSheet != null ? !styleSheet.equals(that.styleSheet) : that.styleSheet != null) return false;
//        if (version != null ? !version.equals(that.version) : that.version != null) return false;
//
//        return true;
    }

    public int hashCode() {
        int result;
        result = this.getId().hashCode();
        result = 31 * result + (this.getName() != null ? this.getName().hashCode() : 0);
        result = 31 * result + (this.getVersion() != null ? this.getVersion().hashCode() : 0);
        result = 31 * result + (this.getMapmaster() != null ? this.getMapmaster().hashCode() : 0);
        result = 31 * result + (this.getDescription() != null ? this.getDescription().hashCode() : 0);
        result = 31 * result + (this.getDescriptionHref() != null ? this.getDescriptionHref().hashCode() : 0);
        result = 31 * result + (this.getStyleSheet() != null ? this.getStyleSheet().hashCode() : 0);
        result = 31 * result + (this.getDataSourceProperties() != null ? this.getDataSourceProperties().hashCode() : 0);
        return result;
    }

    /**
     * Flags up if the DataSource is OK.
     * @return false if things have gone wrong, true if all is good.
     */
    public boolean isOK() {
        return datasourceOK;
    }

     /**
     * toString method used (so far) to report failed DSNs.
     * @return a meaningful string representation of the datasource.
     */
    public String toString(){
        StringBuffer buf = new StringBuffer("DataSourceConfiguration: id: '");
        buf .append ((this.getId() == null) ? "null" : this.getId())
            .append ("' name: '")
            .append ((this.getName() == null) ? "null" : this.getName())
            .append ("' version: '")
            .append ((this.getVersion() == null) ? "null" : this.getVersion())
            .append ("' description: '")
            .append ((this.getDescription() == null) ? "null" : this.getDescription())
            .append ("' features strictly enclosed: '")
            .append (this.isFeaturesStrictlyEnclosed())
            .append ("' dna command enabled :'")
            .append (this.isDnaCommandEnabled())
            .append ("' use feature id for feature label: '")
            .append (this.isUseFeatureIdForFeatureLabel())
            .append ("' include types with zero count: '")
            .append (this.isIncludeTypesWithZeroCount());
        return buf.toString();
    }

    /**
     * Returns the cache group for use with OSCache.
     * @return the cache group for use with OSCache.
     */
    public String[] getCacheGroup(){
        return cacheGroup;
    }

	public Mydasserver.Datasources.Datasource getConfig() {
		return config;
	}

	public String getCapabilities() {
		String capabilities="";
		for (Capability cap:config.getVersion().get(this.versionPosition).getCapability())
			capabilities += " "+cap.getType()+";";
		return capabilities;
	}
}
