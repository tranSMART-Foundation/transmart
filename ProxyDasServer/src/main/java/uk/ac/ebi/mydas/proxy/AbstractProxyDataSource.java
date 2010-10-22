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

package uk.ac.ebi.mydas.proxy;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import uk.ac.ebi.mydas.client.QueryAwareDasAnnotatedSegment;
import uk.ac.ebi.mydas.client.RegexPatterns;
import uk.ac.ebi.mydas.client.xml.DasFeatureXmlUnmarshaller;
import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.controller.CacheManager;
import uk.ac.ebi.mydas.datasource.AnnotationDataSource;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasType;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created Using IntelliJ IDEA.
 * Date: 18-Jul-2007
 * Time: 16:51:37
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *         <p/>
 *         NOTE TO DATA SOURCE DEVELOPERS:
 *         <p/>
 *         This template is based upon the AnnotationDataSource interface,
 *         there are however three other interfaces available that may be
 *         more appropriate for your needs, described here:
 *         <p/>
 *         <a href="http://code.google.com/p/mydas/wiki/HOWTO_WritePluginIntro">
 *         Writing a MyDas Data Source - Selecting the Best Inteface
 *         </a>
 *         <p/>
 *         This version of the AbstractProxyDataSource has been implemented against the new
 *         MyDAS 1.6 implementation and is therefore DAS 1.6 compliant.
 */
public abstract class AbstractProxyDataSource implements AnnotationDataSource {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named "AbstractProxyDataSource".
     */
    private static final Logger LOGGER = Logger.getLogger(AbstractProxyDataSource.class);

    /**
     * The CachedThreadPool will not restrict the maximum number of Threads
     * but will allow their reuse and prevent creation of additional threads
     * unless they are really needed.
     */
    private static final Executor EXEC = Executors.newCachedThreadPool();

    CacheManager cacheManager = null;
    ServletContext svCon;
    Map<String, String> globalParameters;
    DataSourceConfiguration config;

    // Create an instance of HttpClient.
    HttpClient httpClient = null;

    // Connection timeout set to 4 seconds by default - can be overridden in the MydasServerConfig.xml file.
    int connectionTimeout = 4000;
    final Set<String> dasServers = new HashSet<String>();

    private static final String HTTP_PROXY_SET = "http.proxySet";
    private static final String HTTP_PROXY_HOST = "http.proxyHost";
    private static final String HTTP_PROXY_PORT = "http.proxyPort";
    private static final String HTTP_PROXY_USER = "http.proxyUser";
    private static final String HTTP_PROXY_PASSWORD = "http.proxyPassword";
    private static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    /**
     * This pattern is used to test that a URL String ends with /das/datasourcename
     * (possibly with a trailing slash) and returns the following groups:
     * Group 1: data source name.
     * Group 2: A trailing slash, if there is one.
     */
    private static final Pattern PATTERN_VALID_DAS_SERVER_URL = Pattern.compile("/das/([^/?]+)/?$");
    private static final String HTTP_TIMEOUT = "http.timeout";

    private List<String> remoteDataSources = new ArrayList<String>();


    /**
     * This method is called by the MydasServlet class at Servlet initialisation.
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
     *          fatal problem with loading this data source.  <bold>It is highly desirable
     *          for the implementation to test itself in this init method and throw
     *          a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     *          and read a record.</bold>
     */
    public void init(ServletContext servletContext, Map<String, String> globalParameters, DataSourceConfiguration dataSourceConfig) throws DataSourceException {
        this.svCon = servletContext;
        this.globalParameters = globalParameters;
        this.config = dataSourceConfig;

        // Configure a proxy server, if set in the global parameters.
        // Modify system properties
        Properties sysProperties = System.getProperties();
        Map<String, String> dataSourceProps = dataSourceConfig.getDataSourceProperties();
        // TODO Check that these proxy settings are used correctly by HttpClient.
        // Check if a proxy is required.
        if (dataSourceProps.containsKey(HTTP_PROXY_SET) && "true".equalsIgnoreCase(dataSourceProps.get(HTTP_PROXY_SET))) {
            sysProperties.put(HTTP_PROXY_SET, "true");
            // Check that the host is provided, otherwise throw an exception.
            if (!(dataSourceProps.containsKey(HTTP_PROXY_HOST) && (dataSourceProps.get(HTTP_PROXY_HOST)).length() > 0)) {
                throw new DataSourceException("MydasServerConfig.xml error: The 'http.proxySet' property has been set to 'true', but no 'http.proxyHost' value has been provided.");
            }
            sysProperties.put(HTTP_PROXY_HOST, dataSourceProps.get(HTTP_PROXY_HOST));
            setSystemProperty(sysProperties, dataSourceProps, HTTP_PROXY_PORT);
            setSystemProperty(sysProperties, dataSourceProps, HTTP_PROXY_USER);
            setSystemProperty(sysProperties, dataSourceProps, HTTP_PROXY_PASSWORD);
            setSystemProperty(sysProperties, dataSourceProps, HTTP_NON_PROXY_HOSTS);
        } else {
            sysProperties.put(HTTP_PROXY_SET, "false");
        }

        // Configure the HTTP request timeout (optional - defaults to 4 seconds).
        if (dataSourceProps.containsKey(HTTP_TIMEOUT)) {
            String timeoutString = dataSourceProps.get(HTTP_TIMEOUT);
            if (RegexPatterns.INTEGER_PATTERN.matcher(timeoutString).matches()) {
                connectionTimeout = Integer.parseInt(timeoutString);
            } else {
                throw new DataSourceException("The " + HTTP_TIMEOUT + " parameter in the MydasServerConfig.xml file must be a valid integer.  It is currently set to '" + timeoutString + "'");
            }
        }

        // Create the HttpClient for this Data Source instance.
        MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setConnectionTimeout(connectionTimeout);
        httpClient = new HttpClient(connectionManager);

        // Get the list of source DAS Servers and store.
        Set<String> keys = dataSourceProps.keySet();
        Map<Integer, String> indexToURL = new HashMap<Integer, String>();
        for (String key : keys) {
            if (key.startsWith("dasServer")) {
                Integer serverIndex = new Integer(key.substring(9).trim());
                String serverURLString = dataSourceProps.get(key);
                // Check that the URL looks good.
                try {
                    // Just attempt to make a URL out of it (immediately discarded, but just to check the URL is well formed).
                    new URL(serverURLString);
                    // Check that the URL ends with /das/datasourcename
                    Matcher match = PATTERN_VALID_DAS_SERVER_URL.matcher(serverURLString);
                    if (match.find()) {
                        if ("dsn".equals(match.group(1))) {
                            LOGGER.error("For the AbstractProxyDataSource, a source DAS Server has been configured with a URL ending /das/dsn rather than ending with a specific data source name.");
                        } else {
                            // Remove any trailing slash.
                            if (match.group(1).length() == 1) {
                                serverURLString = serverURLString.substring(0, serverURLString.length() - 1);
                                LOGGER.debug("Attempted to remove trailing space.  Ended up with: " + serverURLString);
                            }
                            // Now attempt to query the server, warn if it fails (but still add it?).
//                            checkServerRunning(httpClient, serverURLString);
//                            remoteDataSources.add(serverURLString);
                            indexToURL.put(serverIndex, serverURLString);
                        }
                    }
                } catch (MalformedURLException e) {
                    LOGGER.error("For the AbstractProxyDataSource, a source DAS Server has been configured with a malformed URL.", e);
                }
            }
        }

        // Build the list of remote data sources in the correct order.
        for (int i = 1; indexToURL.keySet().contains(i); i++) {
            remoteDataSources.add(indexToURL.get(i));
        }
        // Check that at least one remote data source has been initialised.
        if (remoteDataSources.size() == 0) {
            LOGGER.fatal("No remote DAS data sources have been successfully initialised.  Please check your settings in the MydasServerConfig.xml file.");
            throw new DataSourceException("No remote DAS data sources have been successfully initialised.  Please check your settings in the MydasServerConfig.xml file.");
        }
    }

    private void setSystemProperty(Properties sysProperties, Map<String, String> dataSourceProps, String propertyName) {
        if (dataSourceProps.containsKey(propertyName) && (dataSourceProps.get(propertyName)).length() > 0) {
            sysProperties.put(propertyName, dataSourceProps.get(propertyName));
        }
    }

    private void checkServerRunning(HttpClient client, String urlString) {
        GetMethod method = null;
        // Query the types command
        urlString = urlString + "/types";
        try {
            LOGGER.debug("connecting to " + urlString);
            URL url = new URL(urlString);
            // Create a method instance.
            method = new GetMethod(url.toString());
            // Execute the method.
            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                LOGGER.warn("Remote DAS Service at '" + url + "' failed: Returned HTTP status code :" + statusCode + " with status :" + method.getStatusLine());
            } else {
                // Read the response body.
                String responseString = new String(method.getResponseBody());
                // Check for a valid </DASTYPES> element.
                if (responseString.contains("</DASTYPES>")) {
                    LOGGER.info("The types request\n\n" + urlString + "\n\nwas successful and returned the XML:\n\n" + responseString);
                } else {
                    LOGGER.warn("The types request\n\n" + urlString + "\n\nFAILED and returned:\n\n" + responseString);
                }
            }
        } catch (MalformedURLException e) {
            LOGGER.error("Could not form a valid URL from " + urlString, e);
        } catch (IOException e) {
            LOGGER.error("IOException thrown when requesting URL " + urlString, e);
        }
        finally {
            if (method != null) {
                method.releaseConnection();
            }
        }
    }

    /**
     * This method is called when the DAS server is shut down and should be used
     * to clean up resources such as database connections as required.
     */
    public void destroy() {
        // Nothing to do - no resources tied up.
    }


    /**
     * This method returns a List of DasAnnotatedSegment objects, describing the annotated segment and the features
     * of the segmentId passed in as argument.
     *
     * @param segmentId being the reference of the segment requested in the DAS request (not including
     *                  start and stop coordinates)
     *                  <p/>
     *                  If your datasource implements only this interface,
     *                  the MydasServlet will handle restricting the features returned to
     *                  the start / stop coordinates in the request and you will only need to
     *                  implement this method to return Features.  If on the other hand, your data source
     *                  includes massive segments, you may wish to implement the
     *                  {@link uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource}
     *                  interface.  It will then be the responsibility of your AnnotationDataSource plugin to
     *                  restrict the features returned for the requested range.
     * @return A DasAnnotatedSegment object.  This describes the segment that is annotated, limited
     *         to the information required for the /DASGFF/GFF/SEGMENT element.  References a Collection of
     *         DasFeature objects.   Note that this is a basic Collection - this gives you complete control over the details
     *         of the Collection type - so you can create your own comparators etc.
     * @throws uk.ac.ebi.mydas.exceptions.BadReferenceObjectException
     *          in the event that your server does not include information about this segment.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          should be thrown if there is any
     *          fatal problem with loading this data source.  <bold>It is highly desirable
     *          for the implementation to test itself in this init method and throw
     *          a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     *          and read a record.</bold>
     */
    @Override
    public DasAnnotatedSegment getFeatures(String segmentId, Integer maxBins) throws BadReferenceObjectException, DataSourceException {
        Collection<QueryAwareDasAnnotatedSegment> annotatedSegments = new ArrayList<QueryAwareDasAnnotatedSegment>();
        LOGGER.debug("Data sources: " + remoteDataSources);
        List<DasQueryRunnerThread> proxies = new ArrayList<DasQueryRunnerThread>(remoteDataSources.size());
        // Request features from all the attached DAS sources... then do something clever with them...

        // Run all the queries in separate Threads...
        for (String dsnUrlString : remoteDataSources) {
            StringBuilder queryURL = new StringBuilder(dsnUrlString)
                    .append("/features?segment=")
                    .append(segmentId);
            if (maxBins != null) {
                queryURL.append(";maxbins=").append(maxBins);
            }
            DasQueryRunnerThread runner = new DasQueryRunnerThread(httpClient, queryURL.toString());
            proxies.add(runner);
            EXEC.execute(runner);
        }

        // Loop until they have all completed. (Check every 20 milliseconds)
        while (true) {
            boolean allFinished = true;
            for (DasQueryRunnerThread runner : proxies) {
                if (!runner.isFinished()) {
                    allFinished = false;
                }
            }
            if (allFinished) break;
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                LOGGER.error("The main thread has been interrupted while waiting for the worker threads to complete.");
                throw new DataSourceException("The main thread has been interrupted while waiting for the worker threads to complete.", e);
            }
        }

        // Request features from all the attached DAS sources... then do something clever with them...
        for (DasQueryRunnerThread runner : proxies) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Processing response from " + runner.getUrlQueryString());
            }
            String responseString = runner.getResponseString();
            // Check that this is a valid DASGFF containing a complete segment (not an errorsegment or unknownsegment)
            // Do this the simple way for the moment...
            if (runner.isSuccessful() && responseString != null && responseString.contains("</SEGMENT>") && responseString.contains("</DASGFF>")) {
                // OK, looks like this is a good file and complete, so parse and collect the resulting DasAnnotatedSegments.
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new StringReader(responseString));
                    DasFeatureXmlUnmarshaller unmarshaller = new DasFeatureXmlUnmarshaller();
                    Collection<QueryAwareDasAnnotatedSegment> segments = unmarshaller.unMarshall(reader);
                    for (QueryAwareDasAnnotatedSegment segment : segments) {
                        segment.setQueryURL(runner.getUrlQueryString());
                    }
                    annotatedSegments.addAll(segments);
                }
                catch (IOException e) {
                    // Don't barf out here - the other proxy data sources may work.
                    LOGGER.error("An IOException was thrown when attempting to xml the XML from " + runner.getUrlQueryString(), e);
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            // Don't barf out here - the other proxy data sources may work.
                            LOGGER.error("An IOException was thrown when attempting to close the StringReader for the response from " + runner.getUrlQueryString(), e);
                        }
                    }
                }
            }
        }

        // Delegate to the subclass method to work out how to coalesce multiple annotated segments.
        if (annotatedSegments.size() == 0) {
            throw new BadReferenceObjectException(segmentId, "None of the data sources queried by this proxy DAS service recognise this segment.");
        }

        return coalesceDasAnnotatedSegments(annotatedSegments);
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
     * <a href="http://biodas.org/documents/spec.html#features">http://biodas.org/documents/spec.html#features</a>.)
     * <p/>
     * Note that if segments are included in the request, this method is not used, so feature_id and group_id
     * filters accompanying a list of segments will work correctly, even if your implementation of this method throws an
     * {@link uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException}.
     *
     * @param featureIdCollection a Collection&lt;String&gt; of feature_id values included in the features command / request.
     *                            May be a <code>java.util.Collections.EMPTY_LIST</code> but will <b>not</b> be null.
     * @return A Collection of {@link uk.ac.ebi.mydas.model.DasAnnotatedSegment} objects. These describe the segments that is annotated, limited
     *         to the information required for the /DASGFF/GFF/SEGMENT element.  Each References a Collection of
     *         DasFeature objects.   Note that this is a basic Collection - this gives you complete control over the details
     *         of the Collection type - so you can create your own comparators etc.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          should be thrown if there is any
     *          fatal problem with loading this data source.  <bold>It is highly desirable for the
     *          implementation to test itself in this init method and throw
     *          a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     *          and read a record.</bold>
     * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
     *          Throw this if you cannot
     *          provide a working implementation of this method.
     */
    @Override
    public Collection<DasAnnotatedSegment> getFeatures(Collection<String> featureIdCollection, Integer maxbins) throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("This proxy DAS server cannot query by featureIdCollection.");
    }

    /**
     * This method must be implemented by a concrete subclass that will determine how the data source merges (or not!)
     * features from different data sources.
     *
     * @param annotatedSegments being all of the DasAnnotatedSegments that contribute to the final result
     * @return a single DasAnnotatedSegment comprising all of the features returned from multiple DAS sources.
     */
    public abstract QueryAwareDasAnnotatedSegment coalesceDasAnnotatedSegments(Collection<QueryAwareDasAnnotatedSegment> annotatedSegments) throws DataSourceException;

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
        // TODO: Implement this by returning the types for all the source DSNs?  (Will require a types unmarshaller).
        return null;
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
     * <a href="http://biodas.org/documents/spec.html#features">http://biodas.org/documents/spec.html#features</a>.)
     * <p/>
     * Note that if segments are included in the request, this method is not used, so feature_id and group_id
     * filters accompanying a list of segments will work correctly, even if your implementation of this method throws an
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
     *          fatal problem with loading this data source.  <bold>It is highly desirable for the
     *          implementation to test itself in this init method and throw
     *          a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     *          and read a record.</bold>
     * @throws uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
     *          Throw this if you cannot
     *          provide a working implementation of this method.
     */
    public Collection<DasAnnotatedSegment> getFeatures(Collection<String> featureIdCollection, Collection<String> groupIdCollection) throws UnimplementedFeatureException, DataSourceException {
        throw new UnimplementedFeatureException("getFeatures (featureIdCollection, groupIdCollectoin) not implemented.");
    }

    /**
     * This method allows the DAS server to report a total count for a particular type
     * for all annotations across the entire data source.  If it is not possible to retrieve this value from your dsn, you
     * should return <code>null</code>.
     *
     * @param type containing the information needed to retrieve the type count
     *             (type id and optionally the method id and category id.  Note that the last two may
     *             be null, which needs to be taken into account by the implementation.)
     * @return The total count <i>across the entire data source</i> (not
     *         just for one segment) for the specified type.  If it is not possible to determine
     *         this count, this method should return <code>null</code>.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          should be thrown if there is any
     *          fatal problem with loading this data source.  <bold>It is highly desirable for the
     *          implementation to test itself in this init method and throw
     *          a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     *          and read a record.</bold>
     */
    public Integer getTotalCountForType(DasType type) throws DataSourceException {
        return null;
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
     * <ul>
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
        throw new UnimplementedFeatureException("getLinkURL not implemented.");
    }
}
