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

import uk.ac.ebi.mydas.controller.DataSourceConfiguration;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;

import javax.servlet.ServletContext;
import java.util.Map;
import java.util.Collection;

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
     * This method returns a List of DasAnnotatedSegment objects, describing the annotated segment and the features
     * of the segmentId passed in as argument.
     * @param segmentId being the reference of the segment requested in the DAS request (not including
     * start and stop coordinates)
     *
     * If your datasource implements only this interface,
     * the MydasServlet will handle restricting the features returned to
     * the start / stop coordinates in the request and you will only need to
     * implement this method to return Features.  If on the other hand, your data source
     * includes massive segments, you may wish to implement the {@link uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource}
     * interface.  It will then be the responsibility of your AnnotationDataSource plugin to
     * restrict the features returned for the requested range.
     *
     * @return A DasAnnotatedSegment object.  This describes the segment that is annotated, limited
     * to the information required for the /DASGFF/GFF/SEGMENT element.  References a Collection of
     * DasFeature objects.   Note that this is a basic Collection - this gives you complete control over the details
     * of the Collection type - so you can create your own comparators etc.
     * @throws BadReferenceObjectException in the event that your server does not include information about this segment.
     * @throws DataSourceException should be thrown if there is any
     * fatal problem with loading this data source.  <bold>It is highly desirable for the implementation to test itself in this init method and throw
     * a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     * and read a record.</bold>
     */
    public DasAnnotatedSegment getFeatures(String segmentId) throws BadReferenceObjectException, DataSourceException;


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
     * <a href="http://biodas.org/documents/spec.html">http://biodas.org/documents/spec.html</a>.)
     *
     * Note that if segments are included in the request, this method is not used, so feature_id and group_id
     * filters accompanying a list of segments will work correctly, even if your implementation of this method throws an
     * {@link UnimplementedFeatureException}.
     *
     * @param featureIdCollection a Collection&lt;String&gt; of feature_id values included in the features command / request.
     * May be a <code>java.util.Collections.EMPTY_LIST</code> but will <b>not</b> be null.
     * @param groupIdCollection a Collection&lt;String&gt; of group_id values included in the features command / request.
     * May be a <code>java.util.Collections.EMPTY_LIST</code> but will <b>not</b> be null.
     * @return A Collection of {@link DasAnnotatedSegment} objects. These describe the segments that is annotated, limited
     * to the information required for the /DASGFF/GFF/SEGMENT element.  Each References a Collection of
     * DasFeature objects.   Note that this is a basic Collection - this gives you complete control over the details
     * of the Collection type - so you can create your own comparators etc.
     * @throws DataSourceException should be thrown if there is any
     * fatal problem with loading this data source.  <bold>It is highly desirable for the implementation to test itself in this init method and throw
     * a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     * and read a record.</bold>
     * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException  Throw this if you cannot
     * provide a working implementation of this method.
     */
    public Collection<DasAnnotatedSegment> getFeatures (Collection<String> featureIdCollection, Collection<String> groupIdCollection)
            throws UnimplementedFeatureException, DataSourceException;
    
}
