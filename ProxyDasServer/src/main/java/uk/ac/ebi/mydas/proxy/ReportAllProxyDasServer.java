package uk.ac.ebi.mydas.proxy;

import uk.ac.ebi.mydas.client.QueryAwareDasAnnotatedSegment;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.DasEntryPoint;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: phil
 * Date: 26-Jun-2008
 * Time: 17:12:13
 * To test the AbstractProxyDataSource - this simple implementation just reports all of the features, one after another
 * in an unintelligent way - should not be used for production purposes really!
 */
public class ReportAllProxyDasServer extends AbstractProxyDataSource {
    /**
     * This method must be implemented by a concrete subclass that will determine how the data source merges (or not!)
     * features from different data sources.
     * <p/>
     * This naive implementation just takes the first DasAnnotatedSegment and uses it as the basis for coalesced
     * DasAnnotatedSegment object, adding the features from all of the other DasAnnotatedSegments to it.
     *
     * @param annotatedSegments being all of the DasAnnotatedSegments that contribute to the final result
     * @return a single DasAnnotatedSegment comprising all of the features returned from multiple DAS sources.
     */
    public QueryAwareDasAnnotatedSegment coalesceDasAnnotatedSegments(Collection<QueryAwareDasAnnotatedSegment> annotatedSegments) {
        QueryAwareDasAnnotatedSegment coalesced = null;
        for (QueryAwareDasAnnotatedSegment segment : annotatedSegments) {
            if (coalesced == null) {
                coalesced = segment;
            } else {
                coalesced.getFeatures().addAll(segment.getFeatures());
            }
        }
        return coalesced;
    }

    /**
     * Returns an ordered Collection of DasEntryPoint objects to implement the entry_point command.
     * The DasEntryPoint object encapsulates information including the segment id, the
     * start coordinate, end coordinate, type, orientation and description of a segment.
     * <p/>
     * Reference servers should always implement this method.
     * Annotation/Reference servers should always return entry points in the same order,
     * starting on position 1 (rather than 0). Annotation/Reference servers are responsible to
     * take care of start and stop positions, thus they should only return the collection corresponding to
     * those positions (including both limits).
     * <p/>
     * For some servers it is important to limit the number of
     * entry points actually retrieved; in this case it is recommended to the server to declare the
     * max_entry_points attribute in MydasServerConfig.xml.
     * <p/>
     * If start is greater that the collection size, the DasCommandManager will report an Exception,
     * if the number of requested entry points is greater than max_entry_points attribute, DasCommandManager
     * will modify the stop in order to complain to that restriction. When the initial request does not
     * specify any positions, DasCommandManager will send (1, max_entry_points attribute) as parameters.
     * <p/>
     * If the stop is greater than the collection size,
     * the returned collection must include only those existing entry points from the specified start position.
     *
     * @param start Initial row position on the entry points collection for this server
     * @param stop  Final row position ont the entry points collection for this server
     * @return a Collection of DasEntryPoint objects
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          to encapsulate any exceptions thrown by the datasource
     *          and allow the MydasServlet to return a decent error header to the client.
     * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
     *          Throw this if you cannot
     *          provide a working implementation of this method.
     */
    @Override
    public Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop) throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("This server does not support the entry point command.");
    }

    /**
     * Returns the value to be returned from the entry_points command, specifically
     * the /DASEP/ENTRY_POINTS/@version attribute.
     * <p/>
     * When getEntryPoints method is implement, so should this method, and the lines below then apply:
     * This is a <b>mandatory</b> value so you must ensure that this method does not
     * return null or an empty String. (The MydasServlet will return an error to the
     * client if you do).
     *
     * @return a non-null, non-zero length String, being the version number of the
     *         entry points / datasource.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          to encapsulate any exceptions thrown by the datasource
     *          and allow the MydasServlet to return a decent error header to the client.
     * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
     *          Throw this if you cannot
     *          provide a working implementation of this method.
     */
    @Override
    public String getEntryPointVersion() throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("This server does not support the entry point command.");
    }

    /**
     * Returns the value to be returned from the entry_points command, specifically
     * the /DASEP/ENTRY_POINTS/@total attribute
     * <p/>
     * When getEntryPoints method is implement, so should this method, and the lines below then apply:
     * This is a <b>mandatory</b> value so you must ensure that this method is implemented.
     *
     * @return an integer being the total number of entry points on this datasource.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          to encapsulate any exceptions thrown by the datasource
     *          and allow the MydasServlet to return a decent error header to the client.
     * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
     *          Throw this if you cannot
     *          provide a working implementation of this method.
     */
    @Override
    public int getTotalEntryPoints() throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("This server does not support the entry point command.");
    }
}
