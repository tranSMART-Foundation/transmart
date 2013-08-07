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
import uk.ac.ebi.mydas.controller.MydasServlet;
import uk.ac.ebi.mydas.datasource.AnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.ConfigurationException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 13:21:06
 * <p/>
 * This class holds details of the datasource, limited to those set in the ServerConfig.xml
 * file.  An instance of this class is passed to implementations of AnnotationDataSource when the init() method
 * is called, allowing the AnnotationDataSource access to its configuration in a transparent manner.
 * <p/>
 * <b>Note that no action is required on the part of the datasource plugin regarding the standard
 * configuration available from this class</b> - this is handled by the MydasServlet class and is
 * for information only.
 * <p/>
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
     * For dynamic data sources, this value can also be null, meaning we never
     * tried to instantiate the annotation data source.
     */
    private Boolean datasourceOK;

    private final Mydasserver.Datasources.Datasource config;
    private int versionPosition;

    /**
     * For dynamic data sources, the data source configuration will be copied
     * on each request and this field will be populated with the matcher of the
     * data source pattern against the actual dsn given by the user
     */
    private Matcher matcherAgainstDsn;

    public DataSourceConfiguration(Mydasserver.Datasources.Datasource config, int versionPosition) {
        this.datasourceOK = false;
        this.config = config;
        this.versionPosition = versionPosition;
        if (logger.isDebugEnabled()) {
            logger.debug("New DataSourceConfiguration instantiated: \n" + this.toString());
        }
    }

    public DataSourceConfiguration(DataSourceConfiguration original, Matcher actualDsn) {
        this.config = original.config;
        this.versionPosition = original.versionPosition;
        this.matcherAgainstDsn = actualDsn;
    }


    /**
     * Returns the mandatory value for /DASDSN/DSN/SOURCE/@id
     *
     * @return the mandatory value for /DASDSN/DSN/SOURCE/@id
     */
    public String getId() {
        return config.getVersion().get(this.versionPosition).getUri();
    }

    /**
     * Returns the optional value for /DASDSN/DSN/SOURCE
     *
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
     *
     * @return the optional value for /DASDSN/DSN/MAPMASTER
     */
    public String getMapmaster() {
        //return config.getVersion().get(this.versionPosition).getCoordinates().get(0).getUri();
        return config.getMapmaster();
    }

    /**
     * Returns the optional value for /DASDSN/DSN/DESCRIPTION
     *
     * @return the optional value for /DASDSN/DSN/DESCRIPTION
     */
    public String getDescription() {
        return config.getDescription();
    }

    /**
     * Returns the optional value for /DASDSN/DSN/DESCRIPTION/@href
     *
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
     * Returns the optional value for max_entry_points
     *
     * @return the optional value for max_entry_points
     */
    public Integer getMaxEntryPoints() {
        return config.getMaxEntryPoints();
    }

    /**
     * Returns the name of the stylesheet for the plugin
     *
     * @return the name of the stylesheet for the plugin
     */
    public String getStyleSheet() {
        return config.getStylesheet();
    }

    /**
     * Returns a Map of key value pairs defined by the plugin developer.
     * Properties for data sources come from the version element and are not allowed out of it (since 1.6.1).
     *
     * @return a Map of key value pairs defined by the plugin developer.
     */
    public Map<String, PropertyType> getDataSourceProperties() {
        Map<String, PropertyType> props = new HashMap<String, PropertyType>();
        for (PropertyType pt : config.getVersion().get(this.versionPosition).getProperty())
            props.put(pt.key, pt);
        return props;
    }


    /**
     * returns a flag to indicate if the dna command is enabled.
     *
     * @return a flag to indicate if the dna command is enabled.
     */
    public boolean isDnaCommandEnabled() {
        return config.getDnaCommandEnabled().value;
    }

    /**
     * Returns a flag to indicate if the feature id should be used for the label
     * if no label is set by the data source.
     *
     * @return a flag to indicate if the feature id should be used for the label
     *         if no label is set by the data source.
     */
    public boolean isUseFeatureIdForFeatureLabel() {
        return config.getUseFeatureIdForFeatureLabel().value;
    }


    /**
     * Returns flag indicating if types with a count of zero should be included in the
     * output of the types command.
     *
     * @return flag indicating if types with a count of zero should be included in the
     *         output of the types command.
     */
    public boolean isIncludeTypesWithZeroCount() {
        return config.getIncludeTypesWithZeroCount().value;
    }


    /**
     * This method is called by the DataSourceManager to load
     * the datasource.
     *
     * @return a boolean indicating if the load was successful or not.
     *         (This value is also stored in the datasourceOK flag, for later retrieval.)
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          if a problem occurs when attempting to instantiate the DataSource class.
     */
    public boolean loadDataSource() throws DataSourceException {
        /* because we return dynamic data sources together with the non-dynamic ones
         * on ServerConfiguration::getDataSourceConfigs() and these go on to be
         * initialized on startup, this method will eventually be called for dynamic
         * data sources without any contextual information (the matcherAgainstDsn property).
         * For those instances, make do not try instantiating the actual data source.
         */
        if (this.isUnmatchedDynamic()) {
            /* this combo is actually OK: */
            datasourceOK = true;
            return false;
        }

        datasourceOK = false;    // Pessimistic start.
        String className = config.getVersion().get(this.versionPosition).getClazz();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            dataSource = (AnnotationDataSource) (classLoader.loadClass(className)).newInstance();
            if (dataSource != null) {
                datasourceOK = true;
            }
        } catch (ClassNotFoundException e) {
            datasourceOK = false;
            logger.error("ClassNotFoundException thrown when attempting to load data source " + className, e);
            throw new DataSourceException("ClassNotFoundException thrown when attempting to instantiate a '" + className + "'.  Please check that the configuration XML file and the classpath are correct.", e);
        } catch (IllegalAccessException e) {
            datasourceOK = false;
            logger.error("IllegalAccessException thrown when attempting to load data source " + className, e);
            throw new DataSourceException("IllegalAccessException thrown when attempting to instantiate a '" + className + "'.", e);
        } catch (InstantiationException e) {
            datasourceOK = false;
            logger.error("InstantiationException thrown when attempting to load data source " + className, e);
            throw new DataSourceException("InstantiationException thrown when attempting to instantiate a '" + className + "'.", e);
        }
        return datasourceOK;
    }

    /**
     * Gives the DataSourceManager access to the DataSource.
     *
     * @return A successfully initialised data source.
     * @throws DataSourceException in the event that the DataSourceManager code
     *                             attempts to access a DataSource that has not loaded successfully without
     *                             checking first! (That would be a bug, by the way).
     */
    public AnnotationDataSource getDataSource() throws DataSourceException {
        if (Boolean.FALSE.equals(this.datasourceOK)) {
            throw new DataSourceException("An attempt has been made to access an AnnotationDataSource that has not been successfully loaded.");
        }
        if (this.datasourceOK == null) {
            /* dynamic data source */
            loadDataSource();
            try {
                MydasServlet.getDataSourceManager().initializeDataSource(this);
            } catch (ConfigurationException e) {
                DataSourceException dataSourceException =
                        new DataSourceException("Could not initialize dynamic data source", e);
                throw dataSourceException;
            }
        }
        return dataSource;
    }

    /**
     * The matcher obtained from the pattern configured for the (dynamic) data
     * source matched against the actual dsn name provided by the user.
     * @return the matcher
     */
    public Matcher getMatcherAgainstDsn() {
        return matcherAgainstDsn;
    }

    /**
     * An unmatched dynamic data source configuration is the first, general
     * instance of a dynamic data source configuration,
     * not bound to any user provided dsn. If this is true, this is an
     * instance returned by {@link uk.ac.ebi.mydas.configuration.ServerConfiguration#getDataSourceConfigs()}.
     * @return true iif this object is an unmatched dynamic
     */
    public boolean isUnmatchedDynamic() {
        return this.config.getPattern() != null && this.matcherAgainstDsn == null;
    }

    /**
     * If true, this is a dynamic source configuration copied from its
     * unmatched version. It is bound a specific user provided dsn.
     * This is the instance returned by {@link ServerConfiguration#getDataSourceConfig(String)}.
     * @return
     */
    public boolean isMatchedDynamic() {
        return this.config.getPattern() != null && this.matcherAgainstDsn != null;
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
     *
     * @return false if things have gone wrong, true if all is good.
     */
    public boolean isOK() {
        return Boolean.TRUE.equals(datasourceOK);
    }

    /**
     * toString method used (so far) to report failed DSNs.
     *
     * @return a meaningful string representation of the datasource.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("DataSourceConfiguration: id: '");
        buf.append((this.getId() == null) ? "null" : this.getId())
                .append("' name: '")
                .append((this.getName() == null) ? "null" : this.getName())
                .append("' version: '")
                .append((this.getVersion() == null) ? "null" : this.getVersion())
                .append("' description: '")
                .append((this.getDescription() == null) ? "null" : this.getDescription())
                        //.append ("' features strictly enclosed: '")
                        //.append (this.isFeaturesStrictlyEnclosed())
                .append("' dna command enabled :'")
                .append(this.isDnaCommandEnabled())
                .append("' use feature id for feature label: '")
                .append(this.isUseFeatureIdForFeatureLabel())
                .append("' include types with zero count: '")
                .append(this.isIncludeTypesWithZeroCount());
        return buf.toString();
    }

    public Mydasserver.Datasources.Datasource getConfig() {
        return config;
    }

    /**
     * Returns the capabilities of the datasource.
     * Capabilities are reported according to DAS 1.6 spec:
     * error-segment/1.0; unknown-segment/1.0; unknown-feature/1.0; ...
     *
     * @return
     */
    public String getCapabilities() {
        String capabilities = "";
        for (Capability cap : config.getVersion().get(this.versionPosition).getCapability()) {
            try {
                String[] caps = cap.getType().split(":");  //das1:sources
                capabilities += " " + caps[1] + "/1.0;";
            } catch (Exception e) {
                //Just do not include that capability
            }

        }
        return capabilities.substring(0, capabilities.length() - 1);
    }

    /**
     * Destroy the dynamic data source instantiated by this object,
     * if applicable. This object should not be used after a call to
     * this method.
     */
    public void destroy() {
        if (isMatchedDynamic() && isOK()) {
            MydasServlet.getDataSourceManager().destroyDataSource(this);
        }
    }
}
