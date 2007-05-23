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

package uk.ac.ebi.mydas.example;

import uk.ac.ebi.mydas.controller.DataSourceConfiguration;
import uk.ac.ebi.mydas.controller.CacheManager;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.*;
import uk.ac.ebi.mydas.datasource.ReferenceDataSource;

import javax.servlet.ServletContext;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;

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

    CacheManager cacheManager = null;
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
        // In this case, nothing to do.
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
    public DasAnnotatedSegment getFeatures(String segmentReference) throws BadReferenceObjectException, DataSourceException {
        try{
            if (segmentReference.equals ("one")){
                Collection<DasFeature> oneFeatures = new ArrayList<DasFeature>(2);
                DasTarget target = new DasTarget("oneTargetId", 20, 30, "oneTargetName");
                DasGroup group = new DasGroup(
                        "oneGroupId",
                        "one Group Label",
                        "onegrouptype",
                        Collections.singleton("A note on the group for reference one."),
                        Collections.singletonMap(new URL("http://code.google.com/p/mydas/"), "mydas project home page."),
                        Collections.singleton(target)
                );
                oneFeatures.add(new DasFeature(
                        "oneFeatureIdOne",
                        "one Feature Label One",
                        "oneFeatureTypeIdOne",
                        "oneFeatureCategoryOne",
                        "one Feature DasType Label One",
                        "oneFeatureMethodIdOne",
                        "one Feature Method Label One",
                        5,
                        10,
                        123.45,
                        DasFeature.ORIENTATION_NOT_APPLICABLE,
                        DasFeature.PHASE_NOT_APPLICABLE,
                        Collections.singleton("This is a note relating to feature one of segment one."),
                        Collections.singletonMap(new URL("http://code.google.com/p/mydas/"), "mydas project home page."),
                        Collections.singleton(target),
                        Collections.singleton(group)
                ));
                oneFeatures.add(new DasFeature(
                        "oneFeatureIdTwo",
                        "one Feature Label Two",
                        "oneFeatureTypeIdTwo",
                        "oneFeatureCategoryTwo",
                        "one Feature DasType Label Two",
                        "oneFeatureMethodIdTwo",
                        "one Feature Method Label Two",
                        18,
                        25,
                        96.3,
                        DasFeature.ORIENTATION_NOT_APPLICABLE,
                        DasFeature.PHASE_NOT_APPLICABLE,
                        Collections.singleton("This is a note relating to feature two of segment one."),
                        Collections.singletonMap(new URL("http://code.google.com/p/mydas/"), "mydas project home page."),
                        null,
                        null
                ));
                return new DasAnnotatedSegment("one", 1, 34, "Up-to-date", "one_label", oneFeatures);
            }
            else if (segmentReference.equals("two")){

                Collection<DasFeature> twoFeatures = new ArrayList<DasFeature>(2);
                twoFeatures.add(new DasFeature(
                        "twoFeatureIdOne",
                        "two Feature Label One",
                        "twoFeatureTypeIdOne",
                        "twoFeatureCategoryOne",
                        "two Feature DasType Label One",
                        "twoFeatureMethodIdOne",
                        "two Featur eMethod Label One",
                        9,
                        33,
                        1000.01,
                        DasFeature.ORIENTATION_SENSE_STRAND,
                        DasFeature.PHASE_READING_FRAME_0,
                        Collections.singleton("This is a note relating to feature one of segment two."),
                        Collections.singletonMap(new URL("http://code.google.com/p/mydas/"), "mydas project home page."),
                        null,
                        null
                ));
                DasAnnotatedSegment segmentTwo = new DasAnnotatedSegment("two", 1, 48, "Up-to-date", "two_label", twoFeatures);
                DasComponentFeature selfComponent = segmentTwo.getSelfComponentFeature();
                selfComponent.addSubComponent(
                        "Contig:A",
                        1,
                        200,
                        20,
                        30,
                        null,
                        "Contig",
                        "a contig",
                        "contigA_id",
                        "Contig A",
                        "component",
                        null,
                        0.0,
                        DasFeature.ORIENTATION_SENSE_STRAND,
                        DasFeature.PHASE_READING_FRAME_0,
                        Collections.singleton("This is a sub-component with a different coordinate system."),
                        null);
                selfComponent.addSubComponent(
                        "Contig:B",
                        20,620,
                        400,1000,
                        null,
                        "Contig",
                        null,
                        "B",
                        null,
                        "component",
                        null,
                        0.00,
                        DasFeature.ORIENTATION_SENSE_STRAND,
                        DasFeature.PHASE_READING_FRAME_0,
                        null,
                        null
                );
                DasComponentFeature c = selfComponent.addSubComponent(
                        "Contig:C",
                        80,280,
                        200,400,
                        null,
                        "Contig",
                        null,
                        "C",
                        null,
                        "component",
                        null,
                        0.00,
                        DasFeature.ORIENTATION_SENSE_STRAND,
                        DasFeature.PHASE_READING_FRAME_0,
                        null,
                        null
                );
                c.addSubComponent(
                        "Contig:C.1",
                        80,280,
                        200,400,
                        null,
                        "Contig",
                        null,
                        "C.1",
                        null,
                        "component",
                        null,
                        0.00,
                        DasFeature.ORIENTATION_SENSE_STRAND,
                        DasFeature.PHASE_READING_FRAME_0,
                        null,
                        null
                );

                // And a super component
                selfComponent.addSuperComponent("ParentChromosome",
                        1,1000,
                        1,34,
                        null,
                        "Chromosome",
                        null,
                        "Parent",
                        null,
                        "supercomponent",
                        null,
                        0.00,
                        DasFeature.ORIENTATION_SENSE_STRAND,
                        DasFeature.PHASE_READING_FRAME_0,
                        null,
                        null
                );
                return segmentTwo;
            }
            else throw new BadReferenceObjectException(segmentReference, "Not found");
        }
        catch (MalformedURLException e) {
            throw new DataSourceException("Tried to create an invalid URL for a LINK element.", e);
        }
    }

    /**
     * This method is used to implement the DAS types command.  (See <a href="http://biodas.org/documents/spec.html#types">
     * DAS 1.53 Specification : types command</a>.  This method should return a Collection containing <b>all</b> the
     * types described by the data source (one DasType object for each type ID).
     * <p/>
     * For some data sources it may be desirable to populate this Collection from a configuration file or to
     *
     * @return a Collection of DasType objects - one for each type id described by the data source.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          should be thrown if there is any
     *          fatal problem with loading this data source.  <bold>It is highly desirable
     *          for the implementation to test itself in this init method and throw
     *          a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     *          and read a record.</bold>
     */
    public Collection<DasType> getTypes() throws DataSourceException {
        Collection<DasType> types = new ArrayList<DasType>(5);
        types.add (new DasType("oneFeatureTypeIdOne", "oneFeatureCategoryOne", "oneFeatureMethodIdOne"));
        types.add (new DasType("oneFeatureTypeIdTwo", "oneFeatureCategoryTwo", "oneFeatureMethodIdTwo"));
        types.add (new DasType("twoFeatureTypeIdOne", "twoFeatureCategoryOne", "twoFeatureMethodIdOne"));
        types.add (new DasType("Chromosome", null, null));
        types.add (new DasType("Contig", null, null));
        return types;
    }

    /**
     * <b>For some Datasources, especially ones with many entry points, this method may be hard or impossible
     * to implement.  If this is the case, you should just throw an {@link uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException} as your
     * implementation of this method, so that a suitable error HTTP header
     * (X-DAS-Status: 501 Unimplemented feature) is returned to the DAS client as
     * described in the DAS 1.53 protocol.</b><br/><br/>
     * <p/>
     * This method is used by the features command when no segments are included, but feature_id and / or
     * group_id filters have been included, to meet the following specification:<br/><br/>
     * <p/>
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
     * <p/>
     * Note that if segments are included in the request, this method is not used, so feature_id and group_id
     * filters accompanying a list of segments will work, even if your implementation of this method throws an
     * {@link uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException}.
     *
     * @param featureIdCollection a Collection&lt;String&gt; of feature_id values included in the features command / request.
     *                            May be a <code>java.util.Collections.EMPTY_LIST</code> but will <b>not</b> be null.
     * @param groupIdCollection   a Collection&lt;String&gt; of group_id values included in the features command / request.
     *                            May be a <code>java.util.Collections.EMPTY_LIST</code> but will <b>not</b> be null.
     * @return A Collection of {@link uk.ac.ebi.mydas.model.DasAnnotatedSegment} objects. These describe the segments that is annotated, limited
     *         to the information required for the /DASGFF/GFF/SEGMENT element.  Each References a Collection of
     *         DasFeature objects.   Note that this is a basic Collection - this gives you complete control over the details
     *         of the Collection type - so you can create your own comparators etc.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          should be thrown if there is any
     *          fatal problem with loading this data source.  <bold>It is highly desirable for the implementation to test itself in this init method and throw
     *          a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     *          and read a record.</bold>
     * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
     *          Throw this if you cannot
     *          provide a working implementation of this method.
     */
    public Collection<DasAnnotatedSegment> getFeatures(Collection<String> featureIdCollection, Collection<String> groupIdCollection) throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("\"This test class, it is not ready yet!\" (in a Swedish accent)");
    }

    /**
     * This method allows the DAS server to report a total count for a particular type ID
     * for all annotations across the entire data source.  If it is not possible to retrieve this value from your dsn, you
     * should throw a {@link uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException} to indicate this.
     *
     * @param typeId as a String.  Please see <a href="http://biodas.org/documents/spec.html#types">
     *               DAS 1.53 Specification : types command</a>
     * @return The total count <i>across the entire data source</i> (not
     *         just for one segment) for the specified type id.
     */
    public int getTotalCountForType(String typeId) throws UnimplementedFeatureException, DataSourceException {
        return 0;
    }

    /**
     * The mydas DAS server implements caching within the server.  This method passes your datasource a reference
     * to a {@link uk.ac.ebi.mydas.controller.CacheManager} object.  To implement this method, you should simply retain a reference to this object.
     * In your code you can then make use of this object to manipulate caching in the mydas servlet.
     * <p/>
     * At present the {@link uk.ac.ebi.mydas.controller.CacheManager} class provides you with a single method public void emptyCache() that
     * you can call if (for example) the underlying data source has changed.
     *
     * @param cacheManager a reference to a {@link uk.ac.ebi.mydas.controller.CacheManager} object that the data source can use to empty
     *                     the cache for this data source.
     */
    public void registerCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * This method returns a URL, based upon a request built as part of the DAS 'link' command.
     * The nature of this URL is entirely up to the data source implementor.
     * <p/>
     * The mydas servlet will redirect to the URL provided.  This command is intended for use in an internet browser,
     * so the URL returned should be a valid internet address.  The page can return content of any MIME type and
     * is intended to be 'human readable' rather than material for consumption by a DAS client.
     * <p/>
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
     * See <a href="http://biodas.org/documents/spec.html#feature_linking">DAS 1.53 Specification: Linking to a Feature</a>
     * for details.
     * <p/>
     * If your data source does not implement this method, an UnimplementedFeatureException should be thrown.
     *
     * @param field one of 'feature', 'type', 'method', 'category' or 'target' as documented in the DAS 1.53
     *              specification
     * @param id    being the ID of the indicated annotation field
     * @return a valid URL.
     * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
     *          in the event that the DAS data source
     *          does not implement the link command
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          should be thrown if there is any
     *          fatal problem with loading this data source.  <bold>It is highly desirable for the implementation
     *          to test itself in this init method and throw
     *          a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     *          and read a record.</bold>
     */
    public URL getLinkURL(String field, String id) throws UnimplementedFeatureException, DataSourceException {
        try{
        return new URL("http://code.google.com/p/mydas/");
        } catch (MalformedURLException e) {
            throw new DataSourceException("Invalid URL!", e);
        }
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
        // Arbitrarily empty the cache when the entry_points command is called for testing purposes.
        this.cacheManager.emptyCache();
        return "Version 1.1";
    }
}
