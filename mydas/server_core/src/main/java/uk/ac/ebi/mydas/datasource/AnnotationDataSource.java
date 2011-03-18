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

package uk.ac.ebi.mydas.datasource;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.controller.CacheManager;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasType;
import uk.ac.ebi.mydas.model.Range;

import javax.servlet.ServletContext;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 13:18:22
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * This interface should be implemented to create a {@link uk.ac.ebi.mydas.controller.MydasServlet} plugin.
 *
 * All related configuration can be entered in the ServerConfig.xml file
 * including the fully qualified name of the AnnotationDataSource class, so it can be registered with the
 * MydasServlet.
 */
public interface AnnotationDataSource {

    /*
     Valid link command field parameters

     (Note that the mydas servlet validates the link command for you - it is
     guaranteed that any calls to the getLinkURL() method will include valid
     field values.)
     */
    public static final String LINK_FIELD_FEATURE = "feature";
    public static final String LINK_FIELD_TYPE = "type";
    public static final String LINK_FIELD_METHOD = "method";
    public static final String LINK_FIELD_CATEGORY = "category";
    public static final String LINK_FIELD_TARGET = "target";


    /**
     * This method is called by the MydasServlet class at Servlet initialisation.
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
     * Mydas servlet is running in.
     * @param globalParameters being a Map &lt;String, String&gt; of keys and values
     * as defined in the ServerConfig.xml file.
     * @param dataSourceConfig containing the pertinent information frmo the ServerConfig.xml
     * file for this datasource, including (optionally) a Map of datasource specific configuration.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException should be thrown if there is any
     * fatal problem with loading this data source.  <bold>It is highly desirable
     * for the implementation to test itself in this init method and throw
     * a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     * and read a record.</bold>
     */
    public void init (ServletContext servletContext,
                      Map<String, PropertyType> globalParameters,
                      DataSourceConfiguration dataSourceConfig)
            throws DataSourceException;

    /**
     * This method is called when the DAS server is shut down and should be used
     * to clean up resources such as database connections as required.
     */
    public void destroy ();

    /**
     * This method returns a List of DasAnnotatedSegment objects, describing the annotated segment and the features
     * of the segmentId passed in as argument.
     * @param segmentId being the reference of the segment requested in the DAS request (not including
     * start and stop coordinates)
     * @param maxbins (optional) This argument allows a client to indicate to the server
     * the available rendering space it has for drawing features (i.e. the number of "bins").
     *
     * If your datasource implements only this interface,
     * the MydasServlet will handle restricting the features returned to
     * the start / stop coordinates in the request and you will only need to
     * implement this method to return Features.  If on the other hand, your data source
     * includes massive segments, you may wish to implement the
     * {@link uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource}
     * interface.  It will then be the responsibility of your AnnotationDataSource plugin to
     * restrict the features returned for the requested range.
     *
     * @return A DasAnnotatedSegment object.  This describes the segment that is annotated, limited
     * to the information required for the /DASGFF/GFF/SEGMENT element.  References a Collection of
     * DasFeature objects.   Note that this is a basic Collection - this gives you complete control over the details
     * of the Collection type - so you can create your own comparators etc.
     * @throws BadReferenceObjectException in the event that your server does not include information about this segment.
     * @throws DataSourceException should be thrown if there is any
     * fatal problem with loading this data source.  <bold>It is highly desirable
     * for the implementation to test itself in this init method and throw
     * a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     * and read a record.</bold>
     */
    public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins) throws BadReferenceObjectException, DataSourceException;


    /**
     * This method is used to implement the DAS types command.  (See <a href="http://biodas.org/documents/spec.html#types">
     * DAS 1.53 Specification : types command</a>.  This method should return a Collection containing <b>all</b> the
     * types described by the data source (one DasType object for each type ID).
     *
     * For some data sources it may be desirable to populate this Collection from a configuration file or to
     *
     * @return a Collection of DasType objects - one for each type id described by the data source.  
     * @throws DataSourceException should be thrown if there is any
     * fatal problem with loading this data source.  <bold>It is highly desirable
     * for the implementation to test itself in this init method and throw
     * a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     * and read a record.</bold>
     */
    public Collection<DasType> getTypes() throws DataSourceException;


    /**
     * <b>For some Datasources, especially ones with many entry points, this method may be hard or impossible
     * to implement.  If this is the case, you should just throw an {@link UnimplementedFeatureException} as your
     * implementation of this method, so that a suitable error HTTP header
     * (X-DAS-Status: 501 Unimplemented feature) is returned to the DAS client as
     * described in the DAS 1.53 protocol.</b><br/><br/>
     *
     * This method is used by the features command when no segments are included, but feature_id and / or
     * group_id filters have been included, to meet the following specification:<br/><br/>
     *
     * "<b>feature_id</b> (zero or more; new in 1.5)<br/>
     * Instead of, or in addition to, <b>segment</b> arguments, you may provide one or more <b>feature_id</b>
     * arguments, whose values are the identifiers of particular features.  If the server supports this operation,
     * it will translate the feature ID into the segment(s) that strictly enclose them and return the result in
     * the <i>features</i> response.  It is possible for the server to return multiple segments if the requested
     * feature is present in multiple locations.
     * <b>group_id</b> (zero or more; new in 1.5)<br/>
     * The <b>group_id</b> argument, is similar to <b>feature_id</b>, but retrieves segments that contain
     * the indicated feature group."  (Direct quote from the DAS 1.53 specification, available from
     * <a href="http://biodas.org/documents/spec.html#features">http://biodas.org/documents/spec.html#features</a>.)
     *
     * Note that if segments are included in the request, this method is not used, so feature_id and group_id
     * filters accompanying a list of segments will work correctly, even if your implementation of this method throws an
     * {@link UnimplementedFeatureException}.
     *
     * @param featureIdCollection a Collection&lt;String&gt; of feature_id values included in the features command / request.
     * May be a <code>java.util.Collections.EMPTY_LIST</code> but will <b>not</b> be null.
     * May be a <code>java.util.Collections.EMPTY_LIST</code> but will <b>not</b> be null.
     * @param maxbins (optional) This argument allows a client to indicate to the server 
     * the available rendering space it has for drawing features (i.e. the number of "bins").
     * @return A Collection of {@link DasAnnotatedSegment} objects. These describe the segments that is annotated, limited
     * to the information required for the /DASGFF/GFF/SEGMENT element.  Each References a Collection of
     * DasFeature objects.   Note that this is a basic Collection - this gives you complete control over the details
     * of the Collection type - so you can create your own comparators etc.
     * @throws DataSourceException should be thrown if there is any
     * fatal problem with loading this data source.  <bold>It is highly desirable for the
     *  implementation to test itself in this init method and throw
     * a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     * and read a record.</bold>
     * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException  Throw this if you cannot
     * provide a working implementation of this method.
     */
    public Collection<DasAnnotatedSegment> getFeatures (Collection<String> featureIdCollection, Integer maxbins)
            throws UnimplementedFeatureException, DataSourceException;

    /**
     * This method allows the DAS server to report a total count for a particular type
     * for all annotations across the entire data source.  If it is not possible to retrieve this value from your dsn, you
     * should return <code>null</code>.
     * @param type containing the information needed to retrieve the type count
     * (type id and optionally the method id and category id.  Note that the last two may
     * be null, which needs to be taken into account by the implementation.)
     * @return The total count <i>across the entire data source</i> (not
     * just for one segment) for the specified type.  If it is not possible to determine
     * this count, this method should return <code>null</code>.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException should be thrown if there is any
     * fatal problem with loading this data source.  <bold>It is highly desirable for the
     *  implementation to test itself in this init method and throw
     * a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     * and read a record.</bold>
     */
    public Integer getTotalCountForType(DasType type) throws DataSourceException;

    /**
     * The mydas DAS server implements caching within the server.  This method passes your datasource a reference
     * to a {@link CacheManager} object.  To implement this method, you should simply retain a reference to this object.
     * In your code you can then make use of this object to manipulate caching in the mydas servlet.
     *
     * At present the {@link CacheManager} class provides you with a single method public void emptyCache() that
     * you can call if (for example) the underlying data source has changed.
     * 
     * @param cacheManager a reference to a {@link CacheManager} object that the data source can use to empty
     * the cache for this data source.
     */
    public void registerCacheManager(CacheManager cacheManager);

    /**
     * This method returns a URL, based upon a request built as part of the DAS 'link' command.
     * The nature of this URL is entirely up to the data source implementor.
     *
     * The mydas servlet will redirect to the URL provided.  This command is intended for use in an internet browser,
     * so the URL returned should be a valid internet address.  The page can return content of any MIME type and
     * is intended to be 'human readable' rather than material for consumption by a DAS client.
     *
     * The link command takes two mandatory
     * arguments:
     * <ul>
     * <li>
     * a 'field' parameter which is limited to one of five valid values.  This method is guaranteed
     * to be called with the 'field' parameter set to one of these values (any other request will be handled as
     * an error by the mydas DAS server servlet.)  The 'field' parameter will be one of the five static String constants
     * that are members of the AnnotationDataSource interface.
     * </li>
     * <li>
     * an 'id' field.  Again, this will be validated by the mydas servlet to ensure that it
     * is a non-null, non-zero length String.
     * </li>
     * <ul>
     * See <a href="http://biodas.org/documents/spec.html#feature_linking">DAS 1.53 Specification: Linking to a Feature</a>
     * for details.
     *
     * If your data source does not implement this method, an UnimplementedFeatureException should be thrown.
     * @param field one of 'feature', 'type', 'method', 'category' or 'target' as documented in the DAS 1.53
     * specification
     * @param id being the ID of the indicated annotation field
     * @return a valid URL.
     * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException in the event that the DAS data source
     * does not implement the link command
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException should be thrown if there is any
     * fatal problem with loading this data source.  <bold>It is highly desirable for the implementation
     *  to test itself in this init method and throw
     * a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     * and read a record.</bold>
     */
    public URL getLinkURL(String field, String id) throws UnimplementedFeatureException, DataSourceException;

/**
     * Returns an ordered Collection of DasEntryPoint objects to implement the entry_point command.
     * The DasEntryPoint object encapsulates information including the segment id, the
     * start coordinate, end coordinate, type, orientation and description of a segment.
     *
     * Reference servers should always implement this method. 
     * Annotation/Reference servers should always return entry points in the same order,
     * starting on position 1 (rather than 0). Annotation/Reference servers are responsible to
     * take care of start and stop positions, thus they should only return the collection corresponding to
     * those positions (including both limits).
     *
     * For some servers it is important to limit the number of
     * entry points actually retrieved; in this case it is recommended to the server to declare the
     * max_entry_points attribute in MydasServerConfig.xml.
     *
     * If start is greater that the collection size, the DasCommandManager will report an Exception,
     * if the number of requested entry points is greater than max_entry_points attribute, DasCommandManager
     * will modify the stop in order to complain to that restriction. When the initial request does not
     * specify any positions, DasCommandManager will send (1, max_entry_points attribute) as parameters.
     *
     * If the stop is greater than the collection size,
     * the returned collection must include only those existing entry points from the specified start position.
     * 
     * @param start Initial row position on the entry points collection for this server
     * @param stop Final row position ont the entry points collection for this server
     * @return a Collection of DasEntryPoint objects
     * @throws DataSourceException to encapsulate any exceptions thrown by the datasource
     * and allow the MydasServlet to return a decent error header to the client.
     * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException  Throw this if you cannot
     * provide a working implementation of this method.
     */
    public Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop) throws UnimplementedFeatureException, DataSourceException;

    /**
     * Returns the value to be returned from the entry_points command, specifically
     * the /DASEP/ENTRY_POINTS/@version attribute.
     *
     * When getEntryPoints method is implement, so should this method, and the lines below then apply:
     * This is a <b>mandatory</b> value so you must ensure that this method does not
     * return null or an empty String. (The MydasServlet will return an error to the
     * client if you do).
     * @return a non-null, non-zero length String, being the version number of the
     * entry points / datasource.
     * @throws DataSourceException to encapsulate any exceptions thrown by the datasource
     * and allow the MydasServlet to return a decent error header to the client.
     * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException  Throw this if you cannot
     * provide a working implementation of this method.
     */
    public String getEntryPointVersion () throws UnimplementedFeatureException, DataSourceException;

    /**
     * Returns the value to be returned from the entry_points command, specifically
     * the /DASEP/ENTRY_POINTS/@total attribute
     *
     * When getEntryPoints method is implement, so should this method, and the lines below then apply:
     * This is a <b>mandatory</b> value so you must ensure that this method is implemented.
     * @return an integer being the total number of entry points on this datasource.
     * @throws DataSourceException to encapsulate any exceptions thrown by the datasource
     * and allow the MydasServlet to return a decent error header to the client.
     * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException  Throw this if you cannot
     * provide a working implementation of this method.
     */
    public int getTotalEntryPoints () throws UnimplementedFeatureException, DataSourceException;
    
    
    
    /**
     * This method is an extension of the {@link #getFeatures(String, Integer)} to allow a customized 
     * implementation of the pagination capability. MyDas verifies if the </i>'rows-for-feature'</i> 
     * capability has been declared in the MydasServerConfig.xml file and will try to use this method. 
     * A UnimplementedFeatureException must be thrown in case this capability is not implemented in your data source.
     * @param segmentId being the reference of the segment requested in the DAS request (not including
     * start and stop coordinates)
     * @param rows (optional) a range of the desired features of the requested segment for pagination purposes. 
     * The Range may be out of the range of this segment, meaning that it wont be included in the response, an empty
     * segment is expected with the value of the total of features that is should it included for pagination purposes
     *
     *
     * @return A DasAnnotatedSegment object.  This describes the segment that is annotated, limited
     * to the information required for the /DASGFF/GFF/SEGMENT element.  It will just include the features in the range 
     * established by the attributes rowsStart and rowsStop
     * @throws BadReferenceObjectException in the event that your server does not include information about this segment.
     * @throws DataSourceException should be thrown if there is any
     * fatal problem with loading this data source.  <bold>It is highly desirable
     * for the implementation to test itself in this init method and throw
     * a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     * and read a record.</bold>
     * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException in the event that the DAS data source
     * does not implement the </i>'rows-for-feature'</i> capability.
     * 
     */
    public DasAnnotatedSegment getFeatures(String segmentId, Integer maxbins,Range rows) throws BadReferenceObjectException, DataSourceException, UnimplementedFeatureException;

    /**
     * This method is an extension of the {@link #getFeatures(Collection, Integer)} to allow a customized 
     * implementation of the pagination capability. MyDas verifies if the </i>'rows-for-feature'</i> 
     * capability has been declared in the MydasServerConfig.xml file and will try to use this method. 
     * A UnimplementedFeatureException must be thrown in case this capability is not implemented in your data source.
     *
     * @param featureIdCollection a Collection&lt;String&gt; of feature_id values included in the features command / request.
     * May be a <code>java.util.Collections.EMPTY_LIST</code> but will <b>not</b> be null.
     * @param maxbins (optional) This argument allows a client to indicate to the server 
     * the available rendering space it has for drawing features (i.e. the number of "bins"). It may be null.
     * @param rows (optional) a range of the desired features of the requested segment for pagination purposes. 
     * The Range may be out of the range of this segment, meaning that it wont be included in the response, an empty
     * segment is expected with the value of the total of features that is should it included for pagination purposes
     * @return A Collection of {@link DasAnnotatedSegment} objects. These describe the segments that is annotated, limited
     * to the information required for the /DASGFF/GFF/SEGMENT element.  Each References a Collection of
     * DasFeature objects.   Note that this is a basic Collection - this gives you complete control over the details
     * of the Collection type - so you can create your own comparators etc.
     * @throws DataSourceException should be thrown if there is any
     * fatal problem with loading this data source.  <bold>It is highly desirable for the
     *  implementation to test itself in this init method and throw
     * a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     * and read a record.</bold>
     * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException  Throw this if you cannot
     * provide a working implementation of this method.
     */
    public Collection<DasAnnotatedSegment> getFeatures (Collection<String> featureIdCollection, Integer maxbins,Range rows)
            throws UnimplementedFeatureException, DataSourceException;
    
}
