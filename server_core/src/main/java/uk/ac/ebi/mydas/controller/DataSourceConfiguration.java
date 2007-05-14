package uk.ac.ebi.mydas.controller;

import org.apache.log4j.Logger;
import uk.ac.ebi.mydas.datasource.AnnotationDataSource;
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource;
import uk.ac.ebi.mydas.datasource.ReferenceDataSource;
import uk.ac.ebi.mydas.datasource.RangeHandlingReferenceDataSource;
import uk.ac.ebi.mydas.exceptions.DataSourceException;

import java.net.URL;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 13:21:06
 *
 * This class holds details of the datasource, limited to those set int the ServerConfig.xml
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

    private static final List<Class> DATA_SOURCE_INTERFACES = new ArrayList<Class>(4);

    static{
        DATA_SOURCE_INTERFACES.add (AnnotationDataSource.class);
        DATA_SOURCE_INTERFACES.add (RangeHandlingAnnotationDataSource.class);
        DATA_SOURCE_INTERFACES.add (ReferenceDataSource.class);
        DATA_SOURCE_INTERFACES.add (RangeHandlingReferenceDataSource.class);
    }

    /**
     * Holds the mandatory value for /DASDSN/DSN/SOURCE/@id
     */
    private final String id;

    /**
     * Holds the optional value for /DASDSN/DSN/SOURCE
     */
    private final String name;

    /**
     * Holds the optional value for /DASDSN/DSN/SOURCE/@version
     */
    private final String version;

    /**
     * Holds the optional value for /DASDSN/DSN/MAPMASTER
     */
    private final String mapmaster;

    /**
     * Holds the optional value for /DASDSN/DSN/DESCRIPTION
     */
    private final String description;

    /**
     * Holds the optional value for /DASDSN/DSN/DESCRIPTION/@href
     */
    private final URL descriptionHref;

    /**
     * Holds the name of the stylesheet for the plugin
     */
    private final String styleSheet;

    /**
     * The fully qualified class of the data source plugin.
     */
    private final String className;

    /**
     * Holds a Map of key value pairs defined by the plugin developer.
     */
    private final Map<String,String> dataSourceProperties;

    /**
     * A reference to the dataSource itself, once it is loaded.
     * (If it is loaded!)
     */
    private AnnotationDataSource dataSource;

    /**
     * A boolean flag to indicate if the datasource failed on initialisation.
     */
    private boolean datasourceOK;

    /**
     * Package access only - instances of this class can be created by the MydasServlet
     * when it reads the configuration file.
     * @param id the mandatory value for /DASDSN/DSN/SOURCE/@id
     * @param name the optional value for /DASDSN/DSN/SOURCE
     * @param version the optional value for /DASDSN/DSN/SOURCE/@version
     * @param mapmaster the optional value for /DASDSN/DSN/MAPMASTER
     * @param description the optional value for /DASDSN/DSN/DESCRIPTION
     * @param descriptionHref the optional value for /DASDSN/DSN/DESCRIPTION/@href
     * @param styleSheet the name of the stylesheet for the plugin
     * @param dataSourceProperties a Map of key value pairs defined by the plugin developer.
     * @param className the fully qualified class of the data source plugin.
     */
    DataSourceConfiguration(String id,
                                   String name,
                                   String version,
                                   String mapmaster,
                                   String description,
                                   URL descriptionHref,
                                   String styleSheet,
                                   Map<String, String> dataSourceProperties,
                                   String className) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.mapmaster = mapmaster;
        this.description = description;
        this.descriptionHref = descriptionHref;
        this.styleSheet = styleSheet;
        this.dataSourceProperties = dataSourceProperties;
        this.className = className;
    }

    /**
     * Returns the mandatory value for /DASDSN/DSN/SOURCE/@id
     * @return the mandatory value for /DASDSN/DSN/SOURCE/@id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the optional value for /DASDSN/DSN/SOURCE
     * @return the optional value for /DASDSN/DSN/SOURCE
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the optional value for /DASDSN/DSN/SOURCE/@version
     * @return the optional value for /DASDSN/DSN/SOURCE/@version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the optional value for /DASDSN/DSN/MAPMASTER
     * @return the optional value for /DASDSN/DSN/MAPMASTER
     */
    public String getMapmaster() {
        return mapmaster;
    }

    /**
     * Returns the optional value for /DASDSN/DSN/DESCRIPTION
     * @return the optional value for /DASDSN/DSN/DESCRIPTION
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the optional value for /DASDSN/DSN/DESCRIPTION/@href
     * @return the optional value for /DASDSN/DSN/DESCRIPTION/@href
     */
    public URL getDescriptionHref() {
        return descriptionHref;
    }

    /**
     * Returns the name of the stylesheet for the plugin
     * @return the name of the stylesheet for the plugin
     */
    public String getStyleSheet() {
        return styleSheet;
    }

    /**
     * Returns a Map of key value pairs defined by the plugin developer.
     * @return a Map of key value pairs defined by the plugin developer.
     */
    public Map<String, String> getDataSourceProperties() {
        return dataSourceProperties;
    }

    /**
     * This method is called by the DataSourceManager to load
     * the datasource.
     * @return a boolean indicating if the load was successful or not.
     * (This value is also stored in the datasourceOK flag, for later retrieval.)
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     * if a problem occurs when attempting to instantiate the DataSource class.
     */
    boolean loadDataSource() throws DataSourceException{
        datasourceOK = false;    // Pessimistic start.
        try{
            ClassLoader classLoader = this.getClass().getClassLoader();
            Class dataSourceClass = classLoader.loadClass(className);
            dataSource = (AnnotationDataSource) dataSourceClass.newInstance();
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
    AnnotationDataSource getDataSource() throws DataSourceException {
        if (! datasourceOK){
            throw new DataSourceException ("An attempt has been made to access an AnnotationDataSource that has not been successfully loaded.");
        }
        return dataSource;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataSourceConfiguration that = (DataSourceConfiguration) o;

        if (dataSourceProperties != null ? !dataSourceProperties.equals(that.dataSourceProperties) : that.dataSourceProperties != null)
            return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (descriptionHref != null ? !descriptionHref.equals(that.descriptionHref) : that.descriptionHref != null)
            return false;
        if (!id.equals(that.id)) return false;
        if (mapmaster != null ? !mapmaster.equals(that.mapmaster) : that.mapmaster != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (styleSheet != null ? !styleSheet.equals(that.styleSheet) : that.styleSheet != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = id.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (mapmaster != null ? mapmaster.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (descriptionHref != null ? descriptionHref.hashCode() : 0);
        result = 31 * result + (styleSheet != null ? styleSheet.hashCode() : 0);
        result = 31 * result + (dataSourceProperties != null ? dataSourceProperties.hashCode() : 0);
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
        buf .append (id)
            .append ("' name: '")
            .append (name)
            .append ("' version: '")
            .append (version)
            .append ("' description: '")
            .append (description);
        return buf.toString();
    }
}
