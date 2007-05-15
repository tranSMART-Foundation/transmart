package uk.ac.ebi.mydas.example;

import uk.ac.ebi.mydas.controller.DataSourceConfiguration;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasSequence;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.datasource.ReferenceDataSource;

import javax.servlet.ServletContext;
import java.util.*;

/**
 * Created Using IntelliJ IDEA.
 * Date: 09-May-2007
 * Time: 14:46:59
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * This test data source is used in conjunction with the WebIntegrationTest test class to test
 * the running web application.
 */
public class TESTDataSource implements ReferenceDataSource {

    ServletContext svCon;
    Map<String, String> globalParameters;
    DataSourceConfiguration config;
    /**
     * This method is called by the {@link uk.ac.ebi.mydas.controller.MydasServlet} class at Servlet initialisation.
     * <p/>
     * The AnnotationDataSource is passed the servletContext, a handle to globalParameters in the
     * form of a Map &lt;String, String&gt; and a DataSourceConfiguration object.
     * <p/>
     * The latter two parameters contain all of the pertinent information in the
     * ServerConfig.xml file relating to the server as a whole and specifically to
     * this data source.  This mechanism allows the datasource author to set up
     * required configuration in one place, including AnnotationDataSource specific configuration.
     * <p/>
     * <bold>It is highly desirable for the implementation to test itself in this init method and throw
     * a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     * and read a record.</bold>
     *
     * @param servletContext   being the ServletContext of the servlet container that the
     *                         Mydas servlet is running in.
     * @param globalParameters being a Map &lt;String, String&gt; of keys and values
     *                         as defined in the ServerConfig.xml file.
     * @param dataSourceConfig containing the pertinent information frmo the ServerConfig.xml
     *                         file for this datasource, including (optionally) a Map of datasource specific configuration.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          should be thrown if there is any
     *          fatal problem with loading this data source.  <bold>It is highly desirable for the implementation to test itself in this init method and throw
     *          a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     *          and read a record.</bold>
     */
    public void init(ServletContext servletContext, Map<String, String> globalParameters, DataSourceConfiguration dataSourceConfig) throws DataSourceException {
        this.svCon = servletContext;
        this.globalParameters = globalParameters;
        this.config = dataSourceConfig;
    }

    public DataSourceConfiguration getConfiguration() {
        return config;
    }

    /**
     * This method is called when the DAS server is shut down and should be used
     * to clean up resources such as database connections as required.
     */
    public void destroy() {
    }

    /**
     * This method returns a List of DasFeature objects, describing the Features
     * of the segmentReference passed in as argument.
     *
     * @param segmentReference being the reference of the segment requested in the DAS request (not including
     *                         start and stop coordinates)
     *                         <p/>
     *                         If your datasource implements only this interface,
     *                         the MydasServlet will handle restricting the features returned to
     *                         the start / stop coordinates in the request and you will only need to
     *                         implement this method to return Features.  If on the other hand, your data source
     *                         includes massive segments, you may wish to implement the {@link uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource}
     *                         interface.  It will then be the responsibility of your AnnotationDataSource plugin to
     *                         restrict the features returned for the requested range.
     * @return a List of DasFeature objects.
     * @throws uk.ac.ebi.mydas.exceptions.BadReferenceObjectException
     *
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *
     */
    public List<DasAnnotatedSegment> getFeatures(String segmentReference) throws BadReferenceObjectException, DataSourceException {
        return Collections.EMPTY_LIST;
    }

    /**
     * Returns a Collection of {@link DasEntryPoint} objects to implement the entry_point command.
     *
     * @return a Collection of {@link DasEntryPoint} objects
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          to encapsulate any exceptions thrown by the datasource
     *          and allow the {@link uk.ac.ebi.mydas.controller.MydasServlet} to return a decent error header to the client.
     */
    public Collection<DasEntryPoint> getEntryPoints() throws DataSourceException {
        List<DasEntryPoint> entryPoints = new ArrayList<DasEntryPoint>();
        entryPoints.add (new DasEntryPoint("one", 1, 34, "Protein", null, "Its a protein!", false));
        entryPoints.add (new DasEntryPoint("two", 1, 48, "DNA", DasEntryPoint.POSITIVE_ORIENTATION, "Its a chromosome!", false));
        return entryPoints;
    }

    /**
     * Extends the {@link uk.ac.ebi.mydas.datasource.ReferenceDataSource} inteface to allow the creation of an Annotation
     * data source.  The only significant difference is that a Reference data source can also
     * serve the sequenceString of the requested segment.
     *
     * @param segmentReference being the name of the sequenceString being requested.
     * @return a {@link DasSequence} object, holding the sequenceString and start / end coordinates of the sequenceString.
     * @throws uk.ac.ebi.mydas.exceptions.BadReferenceObjectException
     *          to inform the {@link uk.ac.ebi.mydas.controller.MydasServlet} that the
     *          segment requested is not available from this DataSource.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          to encapsulate any exceptions thrown by the datasource
     *          and allow the MydasServlet to return a decent error header to the client.
     */
    public DasSequence getSequence(String segmentReference) throws BadReferenceObjectException, DataSourceException {
        if (segmentReference.equals ("one")){
            return new DasSequence("one", "FFDYASTDFYASDFAUFDYFVSHCVYTDASVCYT", 1, "Up-to-date", DasSequence.TYPE_PROTEIN);
        }
        else if (segmentReference.equals("two")){
            return new DasSequence("two", "cgatcatcagctacgtacgatcagtccgtacgatcgatcagcatcaca", 1, "Up-to-date", DasSequence.TYPE_DNA);
        }
        else throw new BadReferenceObjectException(segmentReference, "Not found");
    }

    /**
     * Returns the value to be returned from the entry_points command, specifically
     * the /DASEP/ENTRY_POINTS/@version attribute.
     * <p/>
     * This is a <b>mandatory</b> value so you must ensure that this method does not
     * return null or an empty String. (The {@link uk.ac.ebi.mydas.controller.MydasServlet} will return an error to the
     * client if you do).
     *
     * @return a non-null, non-zero length String, being the version number of the
     *         entry points / datasource.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          to encapsulate any exceptions thrown by the datasource
     *          and allow the {@link uk.ac.ebi.mydas.controller.MydasServlet} to return a decent error header to the client.
     */
    public String getEntryPointVersion() throws DataSourceException {
        return "Version 1.1";
    }


    public List<DasFeature> getFeatures(String segmentReference, int start, int stop) throws BadReferenceObjectException, DataSourceException {
        return Collections.EMPTY_LIST;
    }
}
