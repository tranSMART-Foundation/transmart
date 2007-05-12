package uk.ac.ebi.simpledas.datasource;

import uk.ac.ebi.simpledas.controller.DataSourceConfiguration;
import uk.ac.ebi.simpledas.exceptions.DataSourceException;
import uk.ac.ebi.simpledas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.simpledas.model.DasFeature;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.Map;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 13:18:22
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * This interface should be implemented to create a SimpleDasServlet plugin.
 *
 * All related configuration can be entered in the ServerConfig.xml file
 * including the fully qualified name of the AnnotationDataSource class, so it can be registered with the
 * SimpleDasServlet.
 */
public interface AnnotationDataSource {

    /**
     * This method is called by the SimpleDasServlet class at Servlet initialisation.
     *
     * The AnnotationDataSource is passed the servletContext, a handle to globalParameters in the
     * form of a Map &lt;String, String&gt; and a DataSourceConfiguration object.
     *
     * The latter two parameters contain all of the pertinent information in the
     * ServerConfig.xml file relating to the server as a whole and specifically to
     * this data source.  This mechanism allows the datasource author to set up
     * required configuration in one place, including AnnotationDataSource specific configuration.
     *
     * <bold>It is highly desirable for the implementation to test itself in this init method and throw
     * a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     * and read a record.</bold>
     * @param servletContext being the ServletContext of the servlet container that the
     * SimpleDAS servlet is running in.
     * @param globalParameters being a Map &lt;String, String&gt; of keys and values
     * as defined in the ServerConfig.xml file.
     * @param dataSourceConfig containing the pertinent information frmo the ServerConfig.xml
     * file for this datasource, including (optionally) a Map of datasource specific configuration.
     * @throws uk.ac.ebi.simpledas.exceptions.DataSourceException should be thrown if there is any
     * fatal problem with loading this data source.  <bold>It is highly desirable for the implementation to test itself in this init method and throw
     * a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     * and read a record.</bold>
     */
    public void init (ServletContext servletContext,
                      Map<String,String> globalParameters,
                      DataSourceConfiguration dataSourceConfig)
            throws DataSourceException;

    public DataSourceConfiguration getConfiguration();


    /**
     * This method is called when the DAS server is shut down and should be used
     * to clean up resources such as database connections as required.
     */
    public void destroy ();

    /**
     * This method returns a List of DasFeature objects, describing the Features
     * of the segmentReference passed in as argument.
     * @param segmentReference being the reference of the segment requested in the DAS request (not including
     * start and stop coordinates)
     *
     * If your datasource implements only this interface,
     * the SimpleDasServlet will handle restricting the features returned to
     * the start / stop coordinates in the request and you will only need to
     * implement this method to return Features.  If on the other hand, your data source
     * includes massive segments, you may wish to implement the {@link uk.ac.ebi.simpledas.datasource.RangeHandlingAnnotationDataSource}
     * interface.  It will then be the responsibility of your AnnotationDataSource plugin to
     * restrict the features returned for the requested range.
     *
     * @return a List of DasFeature objects.
     * @throws BadReferenceObjectException
     * @throws DataSourceException
     */
    public List<DasFeature> getFeatures(String segmentReference) throws BadReferenceObjectException, DataSourceException;


    
}
