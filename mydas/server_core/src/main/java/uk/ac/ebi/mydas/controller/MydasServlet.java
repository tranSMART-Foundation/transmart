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

package uk.ac.ebi.mydas.controller;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;
import uk.ac.ebi.mydas.datasource.AnnotationDataSource;
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource;
import uk.ac.ebi.mydas.datasource.RangeHandlingReferenceDataSource;
import uk.ac.ebi.mydas.datasource.ReferenceDataSource;
import uk.ac.ebi.mydas.exceptions.*;
import uk.ac.ebi.mydas.model.*;
import uk.ac.ebi.mydas.extendedmodel.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * Created Using IntelliJ IDEA.
 * User: phil
 * Date: 04-May-2007
 * Time: 12:10:01
 * A DAS server allowing the easy creation of plugins to different data
 * sources that does not tie in the plugin developer to any particular API
 * (apart from the very simple interfaces defined by this API.)
 *
 * This DAS server provides a complete implementation of
 * <a href="http://biodas.org/documents/spec.html">
 *     Distributed Sequence Annotation Systems (DAS) Version 1.53
 * </a>
 *
 * @author Phil Jones, EMBL EBI, pjones@ebi.ac.uk
 */
public class MydasServlet extends HttpServlet {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named "XMLUnmarshaller".
     */
    private static final Logger logger = Logger.getLogger(MydasServlet.class);

    /**
     * This pattern is used to parse the URI part of the request.
     * Returns two groups:
     *
     * <b>dsn command</b>
     * Group 1: "dsn"
     * Group 2: ""
     *
     * <b>All other commands</b>
     * Group 1: "DSN_NAME"
     * Group 2: "command"
     *
     * The URI part of the request as returned by <code>request.getRequestURI();</code>
     * should look like one of the following examples:
     *
     [PREFIX]/das/dsn

     [PREFIX]/das/dsnname/entry_points
     [PREFIX]/das/dsnname/dna
     [PREFIX]/das/dsnname/sequenceString
     [PREFIX]/das/DSNNAME/types
     [PREFIX]/das/dsnname/features
     [PREFIX]/das/dsnname/link
     [PREFIX]/das/dsnname/stylesheet
     */
    private static final Pattern REQUEST_URI_PATTERN = Pattern.compile ("/das/([^\\s/?]+)/?([^\\s/?]*)$");
//    private static final Pattern REQUEST_URI_PATTERN = Pattern.compile ("/([^\\s/?]+)/?([^\\s/?]*)$");

    private static final Pattern DAS_ONLY_URI_PATTERN = Pattern.compile ("/das[/]?$");
//    private static final Pattern DAS_ONLY_URI_PATTERN = Pattern.compile ("[/]?$");

    /**
     * Pattern used to parse a segment range, as used for the dna and sequenceString commands.
     * This can be used based on the assumption that the segments have already been split
     * into indidual Strings (i.e. by splitting on the ; character).
     * Three groups are returned from a match as follows:
     * Group 1: segment name
     * Group 3: start coordinate
     * Group 4: stop coordinate
     */
    private static final Pattern SEGMENT_RANGE_PATTERN = Pattern.compile ("^segment=([^:\\s]*)(:(\\d+),(\\d+))?$");

    private static DataSourceManager DATA_SOURCE_MANAGER = null;

    private static final String RESOURCE_FOLDER = "/";
//    private static final String RESOURCE_FOLDER = "/WEB-INF/classes/";

    private static final String CONFIGURATION_FILE_NAME = RESOURCE_FOLDER + "MydasServerConfig.xml";

	/**
	 * Private enum that is used by this class to match valid commands.
	 */
	enum Commands{

		COMMAND_DSN("dsn"),
		COMMAND_DNA ("dna"),
		COMMAND_TYPES ("types"),
		COMMAND_LINK ("link"),
		COMMAND_STYLESHEET ("stylesheet"),
		COMMAND_FEATURES ("features"),
		COMMAND_ENTRY_POINTS ("entry_points"),
		COMMAND_SEQUENCE ("sequence");

		private String commandString;

		/**
		 * Constructor that sets the commmand string to match.
		 * @param commandString being the String signifying the command.
		 */
		Commands (String commandString){
		    this.commandString = commandString;
		}

		/**
		 * Returns true of the String passed in as argument is the command.
		 * @param command being the string parsed from the URL.
		 * @return true, if the commmand is recognised.
		 */
		boolean matches (String command){
			return this.commandString.equals(command);
		}
	}

    /**
     * List<String> of valid 'field' parameters for the link command.
     */
    public static final List<String> VALID_LINK_COMMAND_FIELDS = new ArrayList<String>(5);

	static {
        VALID_LINK_COMMAND_FIELDS.add(AnnotationDataSource.LINK_FIELD_CATEGORY);
        VALID_LINK_COMMAND_FIELDS.add(AnnotationDataSource.LINK_FIELD_FEATURE);
        VALID_LINK_COMMAND_FIELDS.add(AnnotationDataSource.LINK_FIELD_METHOD);
        VALID_LINK_COMMAND_FIELDS.add(AnnotationDataSource.LINK_FIELD_TARGET);
        VALID_LINK_COMMAND_FIELDS.add(AnnotationDataSource.LINK_FIELD_TYPE);
    }
    /*
        Response Header line keys
     */
    private static final String HEADER_KEY_X_DAS_VERSION = "X-DAS-Version";
    private static final String HEADER_KEY_X_DAS_STATUS = "X-DAS-Status";
    private static final String HEADER_KEY_X_DAS_CAPABILITIES = "X-DAS-Capabilities";

    /*
        Response Header line values
     */
    private static final String HEADER_VALUE_CAPABILITIES = "dsn/1.0; dna/1.0; types/1.0; stylesheet/1.0; features/1.0; entry_points/1.0; error-segment/1.0; unknown-segment/1.0; feature-by-id/1.0; group-by-id/1.0; component/1.0; supercomponent/1.0; sequenceString/1.0";
    private static final String HEADER_VALUE_DAS_VERSION = "DAS/1.5";


    /*
        Content encoding
     */
    private static final String ENCODING_REQUEST_HEADER_KEY = "Accept-Encoding";
    private static final String ENCODING_RESPONSE_HEADER_KEY = "Content-Encoding";
    private static final String ENCODING_GZIPPED = "gzip";

    /*
        Configuration for the output XML
     */
    private static final String DAS_XML_NAMESPACE = null;

    private static XmlPullParserFactory PULL_PARSER_FACTORY = null;
    private static final String INDENTATION_PROPERTY = "http://xmlpull.org/v1/doc/properties.html#serializer-indentation";
    private static final String INDENTATION_PROPERTY_VALUE = "  ";

    static GeneralCacheAdministrator CACHE_MANAGER = null;


    /**
     * This method will ensure that all the plugins are registered and call
     * the corresonding init() method on all of the plugins.
     *
     * Also initialises the XMLPullParser factory.
     * @throws ServletException
     */
    public void init() throws ServletException {
        super.init();

        // Initialize the GeneralCacheAdministrator.
        if (CACHE_MANAGER == null){
            CACHE_MANAGER = new GeneralCacheAdministrator();
        }

        // Initialise data sources.
        if (DATA_SOURCE_MANAGER == null){
            DATA_SOURCE_MANAGER = new DataSourceManager(this.getServletContext());
            try{
                DATA_SOURCE_MANAGER.init(CACHE_MANAGER, CONFIGURATION_FILE_NAME);
            }
            catch (Exception e){
                // Something fatal has happened.  Need to barf out at this point and warn the person who has deployed the service.
                logger.error ("Fatal Exception thrown at initialisation.  None of the datasources will be usable.", e);
                throw new IllegalStateException ("Fatal Exception thrown at initialisation.  None of the datasources will be usable.", e);
            }
        }

        // Initialize XMLPullParserFactory for marshaller.
        if (PULL_PARSER_FACTORY == null) {
            try {
                PULL_PARSER_FACTORY = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
                PULL_PARSER_FACTORY.setNamespaceAware(true);
            } catch (XmlPullParserException xppe) {
                logger.error("Fatal Exception thrown at initialisation.  Cannot initialise the PullParserFactory required to allow generation of the DAS XML.", xppe);
                throw new IllegalStateException ("Fatal Exception thrown at initialisation.  Cannot initialise the PullParserFactory required to allow generation of the DAS XML.", xppe);
            }
        }


    }

    /**
     * This method will ensure that call the corresponding destroy() method on
     * all of the registered plugins to allow them to clean up resources.
     */
    public void destroy() {
        super.destroy();

        if (DATA_SOURCE_MANAGER != null){
            DATA_SOURCE_MANAGER.destroy();
        }
    }

    /**
     * Delegates to the parseAndHandleRequest method
     * @param request containing details of the request, including the command and command arguments.
     * @param response to which the HTTP header / XML response will be written
     * @throws ServletException as defined in the HTTPServlet interface.
     * @throws IOException as defined in the HTTPServlet interface.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        parseAndHandleRequest(request, response);
    }

    /**
     * Delegates to the parseAndHandleRequest method
     * @param request containing details of the request, including the command and command arguments.
     * @param response to which the HTTP header / XML response will be written
     * @throws ServletException as defined in the HTTPServlet interface.
     * @throws IOException as defined in the HTTPServlet interface.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        parseAndHandleRequest(request, response);
    }

    /**
     * Handles requests encoded as GET or POST.
     * First of all splits up the request and then delegates to an appropriate method
     * to respond to this request.  Only basic checking of the request is done here - checking of command
     * arguments is the responsibility of the handling method.
     *
     * This method also handles all exceptions that can be reported as defined DAS errors and returns the
     * appropriate X-DAS-STATUS HTTP header in the event of a problem.
     * @param request The http request object.
     * @param response The response - normally an XML file in HTTP/1.0 protocol.
     * @throws ServletException in the event of an internal error
     * @throws IOException in the event of a low level I/O error.
     */
    private void parseAndHandleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Parse the request URI (e.g. /das/dsnname/sequenceString).
        String queryString = request.getQueryString();
        if (queryString != null){
            // Get rid of any multiple slashes in the request.
            queryString = queryString.replaceAll("/{2,}", "/");
        }

        if (logger.isDebugEnabled()){
            logger.debug("RequestURI: '" + request.getRequestURI() + "'");
            logger.debug("Query String: '" + queryString + "'");
        }

        Matcher match = REQUEST_URI_PATTERN.matcher(request.getRequestURI().replaceAll("/{2,}", "/"));

        try{
            // Belt and braces to ensure that no null pointers are thrown later.
            if (DATA_SOURCE_MANAGER == null ||
                    DATA_SOURCE_MANAGER.getServerConfiguration() == null ||
                    DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration() == null ||
                    DATA_SOURCE_MANAGER.getServerConfiguration().getDataSourceConfigMap() == null){

                throw new ConfigurationException("The datasources were not initialized successfully.");
            }

            if (match.find()){
                // Check first for the dsn command (has a different format to all the others, so start here).
                if (Commands.COMMAND_DSN.matches(match.group(1))){
                    // Handle dsn command, after checking there is no guff in the URI after it.
                    if (match.group(2) == null || match.group(2).length() == 0){
                        // All good, send command.
                        dsnCommand (request, response, queryString);
                    }
                    else {
                        // Starts off looking like the dsn command, but has some other stuff after it...
                        throw new BadCommandException("A bad dsn command has been sent to the server, including unrecognised additional query parameters.");
                    }
                }

                // Not the dsn command, so handle other commands (which are datasource specific)
                else {
                    String dsnName = match.group(1);
                    String command = match.group(2);
                    if (logger.isDebugEnabled()){
                        logger.debug("dsnName: '" + dsnName + "'");
                        logger.debug("command: '" + command + "'");
                    }

                    // Attempt to retrieve the DataSource
                    DataSourceConfiguration dataSourceConfig = DATA_SOURCE_MANAGER.getServerConfiguration().getDataSourceConfigMap().get(dsnName);
                    // Check if the datasource exists.
                    if (dataSourceConfig != null){
                        // Check the datasource is alive.
                        if (dataSourceConfig.isOK()){
                            if      (Commands.COMMAND_DNA.matches(command)){
                                dnaCommand (request, response, dataSourceConfig, queryString);
                            }
                            else if (Commands.COMMAND_TYPES.matches(command)){
                                typesCommand (request, response, dataSourceConfig, queryString);
                            }
                            else if (Commands.COMMAND_STYLESHEET.matches(command)){
                                stylesheetCommand (request, response, dataSourceConfig, queryString);
                            }
                            else if (Commands.COMMAND_FEATURES.matches(command)){
                                featuresCommand (request, response, dataSourceConfig, queryString);
                            }
                            else if (Commands.COMMAND_ENTRY_POINTS.matches(command)){
                                entryPointsCommand (request, response, dataSourceConfig, queryString);
                            }
                            else if (Commands.COMMAND_SEQUENCE.matches(command)){
                                sequenceCommand (request, response, dataSourceConfig, queryString);
                            }
                            else if (Commands.COMMAND_LINK.matches(command)){
                                linkCommand (response, dataSourceConfig, queryString);
                            }
                            else {
                                throw new BadCommandException("The command is not recognised.");
                            }
                        }
                        else{
                            throw new BadDataSourceException("The datasource was not correctly initialised.");
                        }
                    }
                    else {
                        throw new BadDataSourceException("The requested datasource does not exist.");
                    }
                }
            }
            else if (DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().isSlashDasPointsToDsn()
                    && DAS_ONLY_URI_PATTERN.matcher(request.getRequestURI()).find()){
                // Just /das or /das/ has been given as the URL.  This server is configured to point
                // this to the dsn command, so do so.
                dsnCommand (request, response, queryString);
            }
            else {
                throw new BadCommandException("The command is not recognised.");
            }
        } catch (BadCommandException bce) {
            logger.error("BadCommandException thrown", bce);
            writeHeader(request, response, XDasStatus.STATUS_400_BAD_COMMAND, false);
            reportError(XDasStatus.STATUS_400_BAD_COMMAND, "Bad Command - Command not recognised as a valid DAS command.", request, response);
        } catch (BadDataSourceException bdse) {
            logger.error("BadDataSourceException thrown", bdse);
            writeHeader(request, response, XDasStatus.STATUS_401_BAD_DATA_SOURCE, false);
            reportError(XDasStatus.STATUS_401_BAD_DATA_SOURCE, "Bad Data Source", request, response);
        } catch (BadCommandArgumentsException bcae) {
            logger.error("BadCommandArgumentsException thrown", bcae);
            writeHeader(request, response, XDasStatus.STATUS_402_BAD_COMMAND_ARGUMENTS, false);
            reportError(XDasStatus.STATUS_402_BAD_COMMAND_ARGUMENTS, "Bad Command Arguments - Command not recognised as a valid DAS command.", request, response);
        } catch (BadReferenceObjectException broe) {
            logger.error("BadReferenceObjectException thrown", broe);
            writeHeader(request, response, XDasStatus.STATUS_403_BAD_REFERENCE_OBJECT, false);
            reportError(XDasStatus.STATUS_403_BAD_REFERENCE_OBJECT, "Unrecognised reference object: the requested segment is not available from this server.", request, response);
        } catch (BadStylesheetException bse) {
            logger.error("BadStylesheetException thrown:", bse);
            writeHeader(request, response, XDasStatus.STATUS_404_BAD_STYLESHEET, false);
            reportError(XDasStatus.STATUS_404_BAD_STYLESHEET, "Bad Stylesheet.", request, response);
        } catch (CoordinateErrorException cee) {
            logger.error("CoordinateErrorException thrown", cee);
            writeHeader(request, response, XDasStatus.STATUS_405_COORDINATE_ERROR, false);
            reportError(XDasStatus.STATUS_405_COORDINATE_ERROR, "Coordinate error - the requested coordinates are outside the scope of the requested segment.", request, response);
        } catch (XmlPullParserException xppe) {
            logger.error("XmlPullParserException thrown when attempting to ouput XML.", xppe);
            writeHeader (request, response, XDasStatus.STATUS_500_SERVER_ERROR, false);
            reportError(XDasStatus.STATUS_500_SERVER_ERROR, "An error has occurred when attempting to output the DAS XML.", request, response);
        } catch (DataSourceException dse){
            logger.error("DataSourceException thrown by a data source.", dse);
            writeHeader(request, response, XDasStatus.STATUS_500_SERVER_ERROR, false);
            reportError(XDasStatus.STATUS_500_SERVER_ERROR, "The data source has thrown a 'DataSourceException' indicating a software error has occurred: " + dse.getMessage(), request, response);
        } catch (ConfigurationException ce) {
            logger.error("ConfigurationException thrown: This mydas installation was not correctly initialised.", ce);
            writeHeader(request, response, XDasStatus.STATUS_500_SERVER_ERROR, false);
            reportError(XDasStatus.STATUS_500_SERVER_ERROR, "This installation of MyDas is not correctly configured.", request, response);
        } catch (UnimplementedFeatureException efe) {
            logger.error("UnimplementedFeatureException thrown", efe);
            writeHeader(request, response, XDasStatus.STATUS_501_UNIMPLEMENTED_FEATURE, false);
            reportError(XDasStatus.STATUS_501_UNIMPLEMENTED_FEATURE, "Unimplemented feature: this DAS server cannot serve the request you have made.", request, response);
        }

    }

    private void reportError (XDasStatus dasStatus, String errorMessage, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Writer out = response.getWriter();
        out.write("<html><head><title>DAS Error</title></head><body><h2>MyDas Error Message</h2><h4>Request: <code>");
        out.write(request.getRequestURI());
        if (request.getQueryString() != null){
            out.write('?');
            out.write(request.getQueryString());
        }
        out.write("</code></h4>");
        if (dasStatus != null){
            out.write("<h4>");
            out.write(HEADER_KEY_X_DAS_STATUS);
            out.write(": ");
            out.write(dasStatus.toString());
            out.write("</h4>");
        }
        out.write("<h4>Error: <span style='color:red'>");
        out.write(errorMessage);
        out.write("</span></h4></body></html>");
        out.flush();
        out.close();
    }

    /**
     * Implements the dsn command.  Only reports dsns that have initialised successfully.
     * @param request to allow writing of the HTTP header
     * @param response to which the HTTP header and DASDSN XML are written
     * @param queryString to check no spurious arguments have been passed to the command
     * @throws XmlPullParserException in the event of an error being thrown when writing out the XML
     * @throws IOException in the event of an error being thrown when writing out the XML
     */
    private void dsnCommand(HttpServletRequest request, HttpServletResponse response, String queryString)
            throws XmlPullParserException, IOException{
        // Check the configuration has been loaded successfully
        if (DATA_SOURCE_MANAGER.getServerConfiguration() == null){
            writeHeader (request, response, XDasStatus.STATUS_500_SERVER_ERROR, false);
            logger.error("A request has been made to the das server, however initialisation failed - possibly the mydasserverconfig.xml file was not found.");
            return;
        }
        // Check there is nothing in the query string.
        if (queryString == null || queryString.length() == 0){
            // All fine.
            // Get the list of dsn from the DataSourceManager
            List<String> dsns = DATA_SOURCE_MANAGER.getServerConfiguration().getDsnNames();
            // Check there is at least one dsn.  (Mandatory in the dsn XML output).
            if (dsns == null || dsns.size() == 0){
                writeHeader (request, response, XDasStatus.STATUS_500_SERVER_ERROR, false);
                logger.error("The dsn command has been called, but no dsns have been initialised successfully.");
            }
            else{
                // At least one dsn is OK.
                writeHeader (request, response, XDasStatus.STATUS_200_OK, true);
                // Build the XML.
                XmlSerializer serializer;
                serializer = PULL_PARSER_FACTORY.newSerializer();
                BufferedWriter out = null;
                try{
                    out = getResponseWriter(request, response);
                    serializer.setOutput(out);
                    serializer.setProperty(INDENTATION_PROPERTY, INDENTATION_PROPERTY_VALUE);
                    serializer.startDocument(null, false);
                    serializer.text("\n");
                    if (DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getDsnXSLT() != null){
                        serializer.processingInstruction(DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getDsnXSLT());
                        serializer.text("\n");
                    }
                    serializer.docdecl(" DASDSN SYSTEM \"http://www.biodas.org/dtd/dasdsn.dtd\"");
                    serializer.text("\n");
                    serializer.startTag (DAS_XML_NAMESPACE, "DASDSN");
                    for (String dsn : dsns){
                        DataSourceConfiguration dsnConfig = DATA_SOURCE_MANAGER.getServerConfiguration().getDataSourceConfig(dsn);
                        serializer.startTag (DAS_XML_NAMESPACE, "DSN");
                        serializer.startTag (DAS_XML_NAMESPACE, "SOURCE");
                        serializer.attribute(DAS_XML_NAMESPACE, "id", dsnConfig.getId());

                        // Optional version attribute.
                        if (dsnConfig.getVersion() != null && dsnConfig.getVersion().length() > 0){
                            serializer.attribute(DAS_XML_NAMESPACE, "version", dsnConfig.getVersion());
                        }

                        // If a name has been set, this is used for the element text.  Otherwise, the id is used.
                        if (dsnConfig.getName() != null && dsnConfig.getName().length() > 0){
                            serializer.text(dsnConfig.getName());
                        }
                        else {
                            serializer.text(dsnConfig.getId());
                        }
                        serializer.endTag (DAS_XML_NAMESPACE, "SOURCE");
                        serializer.startTag (DAS_XML_NAMESPACE, "MAPMASTER");
                        serializer.text(dsnConfig.getMapmaster());
                        serializer.endTag (DAS_XML_NAMESPACE, "MAPMASTER");

                        // Optional description element.
                        if (dsnConfig.getDescription() != null && dsnConfig.getDescription().length() > 0){
                            serializer.startTag(DAS_XML_NAMESPACE, "DESCRIPTION");
                            serializer.text(dsnConfig.getDescription());
                            serializer.endTag(DAS_XML_NAMESPACE, "DESCRIPTION");
                        }
                        serializer.endTag (DAS_XML_NAMESPACE, "DSN");
                    }
                    serializer.endTag (DAS_XML_NAMESPACE, "DASDSN");
                    serializer.flush();
                }
                finally{
                    if (out != null){
                        out.close();
                    }
                }
            }
        }
        else {
            // If fallen through to here, then the dsn command is not recognised
            // as it has rubbish in the query string.
            writeHeader (request, response, XDasStatus.STATUS_402_BAD_COMMAND_ARGUMENTS, true);
        }
    }

    private void dnaCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
            throws XmlPullParserException, IOException, DataSourceException, UnimplementedFeatureException,
            BadReferenceObjectException, BadCommandArgumentsException, CoordinateErrorException {
        // Is the dna command enabled?
        if (dsnConfig.isDnaCommandEnabled()){
            // Is this a reference source?
            if (dsnConfig.getDataSource() instanceof ReferenceDataSource){
                // All good - process command.
                Collection<SequenceReporter> sequences = getSequences(dsnConfig, queryString);
                // Got some sequences, so all is OK.
                writeHeader (request, response, XDasStatus.STATUS_200_OK, true);
                // Build the XML.
                XmlSerializer serializer;
                serializer = PULL_PARSER_FACTORY.newSerializer();
                BufferedWriter out = null;
                try{
                    out = getResponseWriter(request, response);
                    serializer.setOutput(out);
                    serializer.setProperty(INDENTATION_PROPERTY, INDENTATION_PROPERTY_VALUE);
                    serializer.startDocument(null, false);
                    serializer.text("\n");
                    if (DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getDnaXSLT() != null){
                        serializer.processingInstruction(DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getDnaXSLT());
                        serializer.text("\n");
                    }
                    serializer.docdecl(" DASDNA SYSTEM \"http://www.biodas.org/dtd/dasdna.dtd\"");
                    serializer.text("\n");
                    // Now the body of the DASDNA xml.
                    serializer.startTag (DAS_XML_NAMESPACE, "DASDNA");
                    for (SequenceReporter sequenceReporter : sequences){
                    	sequenceReporter.serialize(DAS_XML_NAMESPACE,serializer);
                    }
                    serializer.endTag (DAS_XML_NAMESPACE, "DASDNA");
					serializer.flush();
				}
                finally{
                    if (out != null){
                        out.close();
                    }
                }
            }
            else {
                // Not a reference source.
                throw new UnimplementedFeatureException("The dna command has been called on an annotation server.");
            }
        }
        else{
            // dna command disabled
            throw new UnimplementedFeatureException("The dna command has been disabled for this data source.");
        }
    }

    private void typesCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
            throws BadCommandArgumentsException, BadReferenceObjectException, DataSourceException, CoordinateErrorException, IOException, XmlPullParserException {
        // Parse the queryString to retrieve the individual parts of the query.

        List<SegmentQuery> requestedSegments = new ArrayList<SegmentQuery>();
        List<String> typeFilter = new ArrayList<String>();
        /************************************************************************\
         * Parse the query string                                               *
         ************************************************************************/
        // It is legal for the query string to be empty for the types command.
        if (queryString != null && queryString.length() > 0){
            // Split on the ; (delineates the separate parts of the query)
            String[] queryParts = queryString.split(";");
            for (String queryPart : queryParts){
                // Now determine what each part is, and construct the query.
                Matcher segmentRangeMatcher = SEGMENT_RANGE_PATTERN.matcher(queryPart);
                if (segmentRangeMatcher.find()){
                    requestedSegments.add (new SegmentQuery (segmentRangeMatcher));
                }
                else{
                    // Split the queryPart on "=" and see if the result is parsable.
                    String[] queryPartKeysValues = queryPart.split("=");
                    if (queryPartKeysValues.length != 2){
                        // All of the remaining query parts are key=value pairs, so this is a bad argument.
                        throw new BadCommandArgumentsException("Bad command arguments to the features command: " + queryString);
                    }
                    String key = queryPartKeysValues[0];
                    String value = queryPartKeysValues[1];
                    // Check for typeId restriction
                    if ("type".equals (key)){
                        typeFilter.add(value);
                    }
                }
                // Previously a check was included here for unparsable parameters.  This has now
				// been removed, so that MyDas will be less fussy about new parameters, e.g. those included
				// in the DAS 1.53E spec.  (Unknown parameters will just be ignored.)
			}
        }
        if (requestedSegments.size() == 0){
            // Process the types command for all types - not segment specific.
            typesCommandAllTypes(request, response, dsnConfig, typeFilter);
        }
        else {
            // Process the types command for specific segments.
            typesCommandSpecificSegments(request, response, dsnConfig, requestedSegments, typeFilter);
        }
    }

    private Collection<DasType> getAllTypes (DataSourceConfiguration dsnConfig) throws DataSourceException {
        Collection<DasType> allTypes;
        String cacheKey = dsnConfig.getId() + "_ALL_TYPES";

        try{
            allTypes = (Collection<DasType>) CACHE_MANAGER.getFromCache(cacheKey);
            if (logger.isDebugEnabled()){
                logger.debug("ALL TYPES RETRIEVED FROM CACHE.");
            }
        } catch (NeedsRefreshException nre) {
            try{
                allTypes = dsnConfig.getDataSource().getTypes();
                CACHE_MANAGER.putInCache(cacheKey, allTypes, dsnConfig.getCacheGroup());
                if (logger.isDebugEnabled()){
                    logger.debug("ALL TYPES RETRIEVED FROM DSN (Not in Cache).");
                }
            }
            catch (DataSourceException dse){
                CACHE_MANAGER.cancelUpdate(cacheKey);
                throw dse;
            }
        }
        return (allTypes == null) ? Collections.EMPTY_LIST : allTypes;
    }

    private void typesCommandAllTypes (HttpServletRequest request, HttpServletResponse response,
                                       DataSourceConfiguration dsnConfig, List<String> typeFilter)
            throws DataSourceException, XmlPullParserException, IOException {
        // Handle no segments indicated - just give a single 'dummy' segment that describes the types for the
        // whole dsn.

        // Build a Map of Types to DasType counts. (the counts being Integer objects set to 'null' until
        // a count is retrieved.
        Map<DasType, Integer> allTypesReport;
        Collection<DasType> allTypes = getAllTypes (dsnConfig);
        allTypesReport = new HashMap<DasType, Integer>(allTypes.size());
        for (DasType type : allTypes){
            if (type != null){
                // Check if the type_ids have been filtered in the request.
                if (typeFilter.size() == 0 || typeFilter.contains(type.getId())){
                    // Attempt to get a count of the types from the dsn. (May not be implemented.)
                    Integer typeCount;
                    StringBuffer keyBuf = new StringBuffer(dsnConfig.getId());
                    keyBuf  .append("_TYPECOUNT_ID_")
                            .append (type.getId())
                            .append ("_CAT_")
                            .append ((type.getCategory() == null) ? "null" : type.getCategory())
                            .append ("_METHOD_")
                            .append ((type.getMethod() == null ) ? "null" : type.getMethod());
                    String cacheKey = keyBuf.toString();

                    try{
                        typeCount = (Integer) CACHE_MANAGER.getFromCache(cacheKey);
                    } catch (NeedsRefreshException nre) {
                        try{
                            typeCount = dsnConfig.getDataSource().getTotalCountForType (type);
                            CACHE_MANAGER.putInCache(cacheKey, typeCount, dsnConfig.getCacheGroup());
                        } catch (DataSourceException dse){
                            CACHE_MANAGER.cancelUpdate(cacheKey);
                            throw dse;
                        }
                    }
                    allTypesReport.put (type, typeCount);
                }
            }
        }

        writeHeader (request, response, XDasStatus.STATUS_200_OK, true);
        // Build the XML.
        XmlSerializer serializer;
        serializer = PULL_PARSER_FACTORY.newSerializer();
        BufferedWriter out = null;
        try{
            out = getResponseWriter(request, response);
            serializer.setOutput(out);
            serializer.setProperty(INDENTATION_PROPERTY, INDENTATION_PROPERTY_VALUE);
            serializer.startDocument(null, false);
            serializer.text("\n");
            if (DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getTypesXSLT() != null){
                serializer.processingInstruction(DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getTypesXSLT());
                serializer.text("\n");
            }
            serializer.docdecl(" DASTYPES SYSTEM \"http://www.biodas.org/dtd/dastypes.dtd\"");
            serializer.text("\n");
            // Now the body of the DASTYPES xml.
            serializer.startTag (DAS_XML_NAMESPACE, "DASTYPES");
            serializer.startTag (DAS_XML_NAMESPACE, "GFF");
            serializer.attribute(DAS_XML_NAMESPACE, "version", "1.0");
            serializer.attribute(DAS_XML_NAMESPACE, "href", this.buildRequestHref(request));
            serializer.startTag(DAS_XML_NAMESPACE, "SEGMENT");
            // No id, start, stop, type attributes.
            serializer.attribute(DAS_XML_NAMESPACE, "version", dsnConfig.getVersion());
            serializer.attribute(DAS_XML_NAMESPACE, "label", "Complete datasource summary");
            // Iterate over the allTypeReport for the TYPE elements.
            for (DasType type : allTypesReport.keySet()){
            	(new DasTypeE (type)).serialize(DAS_XML_NAMESPACE, serializer, allTypesReport.get(type));
            }
            serializer.endTag(DAS_XML_NAMESPACE, "SEGMENT");
            serializer.endTag (DAS_XML_NAMESPACE, "GFF");
            serializer.endTag (DAS_XML_NAMESPACE, "DASTYPES");
			serializer.flush();
		}
        finally{
            if (out != null){
                out.close();
            }
        }
    }

    private void typesCommandSpecificSegments(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, List<SegmentQuery> requestedSegments, List<String> typeFilter)
            throws DataSourceException, BadReferenceObjectException, XmlPullParserException, IOException, CoordinateErrorException {
        Map <FoundFeaturesReporter, Map<DasType, Integer>> typesReport =
                new HashMap<FoundFeaturesReporter, Map<DasType, Integer>>(requestedSegments.size());
        // For each segment, populate the typesReport with 'all types' if necessary and then add types and counts.
        Collection<SegmentReporter> segmentReporters = this.getFeatureCollection(dsnConfig, requestedSegments, false);
        for (SegmentReporter uncastReporter : segmentReporters){
            // Try to get the features for this segment
            if (uncastReporter instanceof FoundFeaturesReporter){
                FoundFeaturesReporter segmentReporter = (FoundFeaturesReporter) uncastReporter;
                Map<DasType, Integer> segmentTypes = new HashMap<DasType, Integer>();
                // Add these objects to the typesReport.
                typesReport.put(segmentReporter, segmentTypes);
                /////////////////////////////////////////////////////////////////////////////////////////////
                // If required in configuration, add all the types from the server to the segmentTypes map
                if (dsnConfig.isIncludeTypesWithZeroCount()){
                    Collection<DasType> allTypes = getAllTypes (dsnConfig);
                    // Iterate over allTypes and add each type to the segment types report with a count of zero.
                    for (DasType type : allTypes){
                        // (Filtering as requested for type ids)
                        if (type != null && (typeFilter.size() == 0 || typeFilter.contains(type.getId())))
                            segmentTypes.put(type, 0);
                    }
                }
                // Handled 'include types with zero count'.
                /////////////////////////////////////////////////////////////////////////////////////////////

                /////////////////////////////////////////////////////////////////////////////////////////////
                // Now iterate over the features of the segment and update the types report.
                for (DasFeature feature : segmentReporter.getFeatures(dsnConfig.isFeaturesStrictlyEnclosed())){
                    // (Filtering as requested for type ids)
                    if (typeFilter.size() == 0 || typeFilter.contains(feature.getTypeId())){
                        DasType featureType = new DasType(feature.getTypeId(), feature.getTypeCategory(), feature.getMethodId());
                        if (segmentTypes.keySet().contains(featureType)){
                            segmentTypes.put(featureType, segmentTypes.get(featureType) + 1);
                        }
                        else {
                            segmentTypes.put(featureType, 1);
                        }
                    }
                }
            }
            // Finished with actual features
            /////////////////////////////////////////////////////////////////////////////////////////////
        }

        // OK, successfully built a Map of the types for all the requested segments, so iterate over this and report.
        writeHeader (request, response, XDasStatus.STATUS_200_OK, true);
        // Build the XML.
        XmlSerializer serializer;
        serializer = PULL_PARSER_FACTORY.newSerializer();
        BufferedWriter out = null;
        try{
            out = getResponseWriter(request, response);
            serializer.setOutput(out);
            serializer.setProperty(INDENTATION_PROPERTY, INDENTATION_PROPERTY_VALUE);
            serializer.startDocument(null, false);
            serializer.text("\n");
            if (DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getTypesXSLT() != null){
                serializer.processingInstruction(DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getTypesXSLT());
                serializer.text("\n");
            }
            serializer.docdecl(" DASTYPES SYSTEM \"http://www.biodas.org/dtd/dastypes.dtd\"");
            serializer.text("\n");
            // Now the body of the DASTYPES xml.
            serializer.startTag (DAS_XML_NAMESPACE, "DASTYPES");
            serializer.startTag (DAS_XML_NAMESPACE, "GFF");
            serializer.attribute(DAS_XML_NAMESPACE, "version", "1.0");
            serializer.attribute(DAS_XML_NAMESPACE, "href", this.buildRequestHref(request));
            for (FoundFeaturesReporter featureReporter : typesReport.keySet()){
                serializer.startTag(DAS_XML_NAMESPACE, "SEGMENT");
                serializer.attribute(DAS_XML_NAMESPACE, "id", featureReporter.getSegmentId());
                serializer.attribute(DAS_XML_NAMESPACE, "start", Integer.toString(featureReporter.getStart()));
                serializer.attribute(DAS_XML_NAMESPACE, "stop", Integer.toString(featureReporter.getStop()));
                if (featureReporter.getType() != null && featureReporter.getType().length() > 0){
                    serializer.attribute(DAS_XML_NAMESPACE, "type", featureReporter.getType());
                }
                serializer.attribute(DAS_XML_NAMESPACE, "version", featureReporter.getVersion());
                if (featureReporter.getSegmentLabel() != null && featureReporter.getSegmentLabel().length() > 0){
                    serializer.attribute(DAS_XML_NAMESPACE, "label", featureReporter.getSegmentLabel());
                }
                // Now for the types.
                Map<DasType, Integer> typeMap = typesReport.get(featureReporter);
                for (DasType type : typeMap.keySet()){
                	(new DasTypeE (type)).serialize(DAS_XML_NAMESPACE, serializer, typeMap.get(type));
                }
                serializer.endTag(DAS_XML_NAMESPACE, "SEGMENT");
            }
            serializer.endTag (DAS_XML_NAMESPACE, "GFF");
            serializer.endTag (DAS_XML_NAMESPACE, "DASTYPES");
			serializer.flush();
		}
        finally{
            if (out != null){
                out.close();
            }
        }
    }

    private void stylesheetCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
            throws BadCommandArgumentsException, IOException, BadStylesheetException {
        // Check the queryString is empty (as it should be).
        if (queryString != null && queryString.trim().length() > 0){
            throw new BadCommandArgumentsException("Arguments have been passed to the stylesheet command, which does not expect any.");
        }
        // Get the name of the stylesheet.
        String stylesheetFileName;
        if (dsnConfig.getStyleSheet() != null && dsnConfig.getStyleSheet().trim().length() > 0){
            stylesheetFileName = dsnConfig.getStyleSheet().trim();
        }
        // These next lines look like potential null-pointer hell - but note that this has been checked robustly in the
        // calling method, so all OK.
        else if (DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getDefaultStyleSheet() != null
                && DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getDefaultStyleSheet().trim().length() > 0){
            stylesheetFileName = DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getDefaultStyleSheet().trim();
        }
        else {
            throw new BadStylesheetException("This data source has not defined a stylesheet.");
        }

        // Need to create a FileReader to read in the stylesheet, wrapped by a PrintStream to stream it out to the browser.
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try{
            reader = new BufferedReader(
                    new InputStreamReader (
                            getServletContext().getResourceAsStream(RESOURCE_FOLDER + stylesheetFileName)
                    )
            );

            if (reader.ready()){
                //OK, managed to open an input reader from the stylesheet, so output the success header.
                writeHeader (request, response, XDasStatus.STATUS_200_OK, true);
                writer = getResponseWriter(request, response);
                while (reader.ready()){
                    writer.write(reader.readLine());
                }
				writer.flush();
			}
            else {
                throw new BadStylesheetException("A problem has occurred reading in the stylesheet from the open stream");
            }
        }
        finally{
            if (reader != null){
                reader.close();
            }
            if (writer != null){
                writer.close();
            }
        }
    }

    /**
     * Implements the link command.  This is done using a simple mechanism - the request is parsed and checked for
     * correctness, then the 'field' and 'id' are passed to the DSN that should return a well formed URL.  This method
     * then redirects the browser to the URL specified.  This mechanism gets around any problems with odd MIME types
     * in the results page.
     * @param response which is redirected to the URL specified (unless there is a problem, in which case the
     * appropriate X-DAS-STATUS will be sent instead)
     * @param dataSourceConfig holding configuration of the dsn and the data source object itself.
     * @param queryString from which the 'field' and 'id' parameters are parsed.
     * @throws IOException during handling of the response
     * @throws BadCommandArgumentsException if the arguments to the link command are not as specified in the
     * DAS 1.53 specification
     * @throws DataSourceException to handle problems from the DSN.
     * @throws UnimplementedFeatureException if the DSN reports that it does not implement this command.
     */
    private void linkCommand(HttpServletResponse response, DataSourceConfiguration dataSourceConfig, String queryString)
            throws IOException, BadCommandArgumentsException, DataSourceException, UnimplementedFeatureException {
        // Parse the request
        if (queryString == null || queryString.length() == 0){
            throw new BadCommandArgumentsException("The link command has been called with no arguments.");
        }
        String[] queryParts = queryString.split(";");
        if (queryParts.length < 2){
            throw new BadCommandArgumentsException("Not enough arguments have been passed to the link command.");
        }
        String field = null;
        String id = null;
        for (String keyValuePair : queryParts){
            // Split the key=value pairs
            String[] queryPartKeysValues = keyValuePair.split("=");
            if (queryPartKeysValues.length != 2){
                throw new BadCommandArgumentsException("keys and values cannot be extracted from the arguments to the link command");
            }
            if ("field".equals(queryPartKeysValues[0])){
                field = queryPartKeysValues[1];
            }
            else if ("id".equals(queryPartKeysValues[0])){
                id = queryPartKeysValues[1];
            }
            // Was previously checking religiously for arguments that are not supported and throwing exceptions.
			// Now just ignoring them, to prevent problems with new DAS parameters, e.g. from DAS 1.53E.
		}
        if (field == null || ! VALID_LINK_COMMAND_FIELDS.contains(field) || id == null){
            throw new BadCommandArgumentsException("The link command must be passed a valid field and id argument.");
        }
        URL url;

        // Build the key name for the cache.
        StringBuffer cacheKeyBuffer = new StringBuffer(dataSourceConfig.getId());
        cacheKeyBuffer.append("_LINK_")
                .append(field)
                .append('_')
                .append(id);
        String cacheKey = cacheKeyBuffer.toString();

        try{
            url = (URL) CACHE_MANAGER.getFromCache(cacheKey);
            if (logger.isDebugEnabled()){
                logger.debug("LINK RETRIEVED FROM CACHE: " + url.toString());
            }
        } catch (NeedsRefreshException e) {
            try{
                url = dataSourceConfig.getDataSource().getLinkURL(field, id);
                CACHE_MANAGER.putInCache(cacheKey, url, dataSourceConfig.getCacheGroup());
            }
            catch (UnimplementedFeatureException ufe){
                CACHE_MANAGER.cancelUpdate(cacheKey);
                throw ufe;
            }
            catch (DataSourceException dse){
                CACHE_MANAGER.cancelUpdate(cacheKey);
                throw dse;
            }
            if (logger.isDebugEnabled()){
                logger.debug("LINK RETRIEVED FROM DSN (NOT CACHED): " + url.toString());
            }
        }

        response.sendRedirect(response.encodeRedirectURL(url.toString()));
    }

    /**
     * This method handles the complete features command, including all variants as specified in DAS 1.53.
     *
     * @param request to allow the writing of the http header
     * @param response to which the http header and the XML are written.
     * @param dsnConfig holding configuration of the dsn and the data source object itself.
     * @param queryString from which the requested segments and other allowed parameters are parsed.
     * @throws XmlPullParserException in the event of a problem with writing out the DASFEATURE XML file.
     * @throws IOException during writing of the XML
     * @throws DataSourceException to capture any error returned from the data source.
     * @throws BadCommandArgumentsException if the arguments to the feature command are not as specified in the
     * DAS 1.53 specification
     * @throws UnimplementedFeatureException if the dsn reports that it cannot handle an aspect of the feature
     * command (although all dsns are required to implement at least the basic feature command).
     * @throws BadReferenceObjectException will not be thrown, but a helper method used by this method
     * can throw this exception under some circumstances (but not when called by the featureCommand method!)
     * @throws CoordinateErrorException will not be thrown, but a helper method used by this method
     * can throw this exception under some circumstances (but not when called by the featureCommand method!)
     */
    private void featuresCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
            throws XmlPullParserException, IOException, DataSourceException, BadCommandArgumentsException,
            UnimplementedFeatureException, BadReferenceObjectException, CoordinateErrorException {
        // Parse the queryString to retrieve the individual parts of the query.
        if (queryString == null || queryString.length() == 0){
            throw new BadCommandArgumentsException("Expecting at least one reference in the query string, but found nothing.");
        }

        List<SegmentQuery> requestedSegments = new ArrayList<SegmentQuery>();
        /************************************************************************\
         * Parse the query string                                               *
         ************************************************************************/

        // Split on the ; (delineates the separate parts of the query)
        String[] queryParts = queryString.split(";");
        DasFeatureRequestFilter filter = new DasFeatureRequestFilter ();
        boolean categorize = true;
        for (String queryPart : queryParts){
            // Now determine what each part is, and construct the query.
            Matcher segmentRangeMatcher = SEGMENT_RANGE_PATTERN.matcher(queryPart);
            if (segmentRangeMatcher.find()){
                requestedSegments.add (new SegmentQuery (segmentRangeMatcher));
            }
            else{
                // Split the queryPart on "=" and see if the result is parsable.
                String[] queryPartKeysValues = queryPart.split("=");
                if (queryPartKeysValues.length != 2){
                    // All of the remaining query parts are key=value pairs, so this is a bad argument.
                    throw new BadCommandArgumentsException("Bad command arguments to the features command: " + queryString);
                }
                String key = queryPartKeysValues[0];
                String value = queryPartKeysValues[1];
                // Check for typeId restriction
                if ("type".equals (key)){
                    filter.addTypeId(value);
                }
                // else check for categoryId restriction
                else if ("category".equals (key)){
                    filter.addCategoryId(value);
                }
                // else check for categorize restriction
                else if ("categorize".equals (key)){
                    if ("no".equals(value)){
                        categorize = false;
                    }
                }
                // else check for featureId restriction
                else if ("feature_id".equals (key)){
                    filter.addFeatureId(value);
                }
                // else check for groupId restriction
                else if ("group_id".equals (key)){
                    filter.addGroupId(value);
                }
				// Any command parameters that are not recognised should be ignored
				// This is a change from version 1.01 - some 1.53E commands were causing
				// service failure.
			}

		}

        /************************************************************************\
         * Query the DataSource                                                 *
         ************************************************************************/

        // if segments have been included in the request, use the getFeatureCollection method to retrieve them
        // from the data source.  (getFeatureCollection method shared with the 'types' command.)
        Collection<SegmentReporter> segmentReporterCollections;
        if (requestedSegments.size() > 0){
            segmentReporterCollections = getFeatureCollection(dsnConfig, requestedSegments, true);
        }
        else {
            // No segments have been requested, so instead check for either feature_id or group_id filters.
            // (If neither of these are present, then throw a BadCommandArgumentsException)
            if (filter.containsFeatureIds() || filter.containsGroupIds()){
                Collection<DasAnnotatedSegment> annotatedSegments =
                        dsnConfig.getDataSource().getFeatures(filter.getFeatureIds(), filter.getGroupIds());
                if (annotatedSegments != null){
                    segmentReporterCollections = new ArrayList<SegmentReporter>(annotatedSegments.size());
                    for (DasAnnotatedSegment segment : annotatedSegments){
                        segmentReporterCollections.add (new FoundFeaturesReporter(segment));
                    }
                }
                else {
                    // Nothing returned from the datasource.
                    segmentReporterCollections = Collections.EMPTY_LIST;
                }
            }
            else {
                throw new BadCommandArgumentsException("Bad command arguments to the features command: " + queryString);
            }
        }
        // OK - got a Collection of FoundFeaturesReporter objects, so get on with marshalling them out.
        writeHeader (request, response, XDasStatus.STATUS_200_OK, true);

        /************************************************************************\
         * Build the XML                                                        *
         ************************************************************************/

        XmlSerializer serializer;
        serializer = PULL_PARSER_FACTORY.newSerializer();
        BufferedWriter out = null;
        try{
            boolean referenceSource = dsnConfig.getDataSource() instanceof ReferenceDataSource;
            out = getResponseWriter(request, response);
            serializer.setOutput(out);
            serializer.setProperty(INDENTATION_PROPERTY, INDENTATION_PROPERTY_VALUE);
            serializer.startDocument(null, false);
            serializer.text("\n");
            if (DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getFeaturesXSLT() != null){
                serializer.processingInstruction(DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getFeaturesXSLT());
                serializer.text("\n");
            }
            serializer.docdecl(" DASGFF SYSTEM \"http://www.biodas.org/dtd/dasgff.dtd\"");
            serializer.text("\n");

            // Rest of the XML.
            serializer.startTag(DAS_XML_NAMESPACE, "DASGFF");
            serializer.startTag(DAS_XML_NAMESPACE, "GFF");
            serializer.attribute(DAS_XML_NAMESPACE, "version", "1.0");
            serializer.attribute(DAS_XML_NAMESPACE, "href", buildRequestHref(request));
            for (SegmentReporter segmentReporter : segmentReporterCollections){
                if (segmentReporter instanceof UnknownSegmentReporter){
                	((UnknownSegmentReporter)segmentReporter).serialize(DAS_XML_NAMESPACE, serializer, referenceSource);
//                    serializer.startTag(DAS_XML_NAMESPACE, (referenceSource) ? "ERRORSEGMENT" : "UNKNOWNSEGMENT");
//                    serializer.attribute(DAS_XML_NAMESPACE, "id", segmentReporter.getSegmentId());
//                    if (segmentReporter.getStart() != null){
//                        serializer.attribute(DAS_XML_NAMESPACE, "start", Integer.toString(segmentReporter.getStart()));
//                    }
//                    if (segmentReporter.getStop() != null){
//                        serializer.attribute(DAS_XML_NAMESPACE, "stop", Integer.toString(segmentReporter.getStop()));
//                    }
//                    serializer.endTag(DAS_XML_NAMESPACE, (referenceSource) ? "ERRORSEGMENT" : "UNKNOWNSEGMENT");
                }
                else {
                    FoundFeaturesReporter foundFeaturesReporter = (FoundFeaturesReporter) segmentReporter;
                    serializer.startTag(DAS_XML_NAMESPACE, "SEGMENT");
                    serializer.attribute(DAS_XML_NAMESPACE, "id", foundFeaturesReporter.getSegmentId());
                    serializer.attribute(DAS_XML_NAMESPACE, "start", (foundFeaturesReporter.getStart() == null)
                            ? ""
                            : Integer.toString(foundFeaturesReporter.getStart()));
                    serializer.attribute(DAS_XML_NAMESPACE, "stop", (foundFeaturesReporter.getStop() == null)
                            ? ""
                            : Integer.toString(foundFeaturesReporter.getStop()));
                    if (foundFeaturesReporter.getType() != null && foundFeaturesReporter.getType().length() > 0){
                        serializer.attribute(DAS_XML_NAMESPACE, "type", foundFeaturesReporter.getType());
                    }
                    serializer.attribute(DAS_XML_NAMESPACE, "version", foundFeaturesReporter.getVersion());
                    if (foundFeaturesReporter.getSegmentLabel() != null && foundFeaturesReporter.getSegmentLabel().length() > 0){
                        serializer.attribute(DAS_XML_NAMESPACE, "label", foundFeaturesReporter.getSegmentLabel());
                    }
                    for (DasFeature feature : foundFeaturesReporter.getFeatures(dsnConfig.isFeaturesStrictlyEnclosed())){
                        // Check the feature passes the filter.
                        if (filter.featurePasses(feature)){
                            serializer.startTag(DAS_XML_NAMESPACE, "FEATURE");
                            serializer.attribute(DAS_XML_NAMESPACE, "id", feature.getFeatureId());
                            if (feature.getFeatureLabel() != null && feature.getFeatureLabel().length() > 0){
                                serializer.attribute(DAS_XML_NAMESPACE, "label", feature.getFeatureLabel());
                            }
                            else if (dsnConfig.isUseFeatureIdForFeatureLabel()){
                                serializer.attribute(DAS_XML_NAMESPACE, "label", feature.getFeatureId());
                            }

                            // TYPE element
                            serializer.startTag(DAS_XML_NAMESPACE, "TYPE");
                            serializer.attribute(DAS_XML_NAMESPACE, "id", feature.getTypeId());

                            // Handle DasReferenceFeatures.
                            if (feature instanceof DasComponentFeature){
                                DasComponentFeature refFeature = (DasComponentFeature) feature;
                                serializer.attribute(DAS_XML_NAMESPACE, "reference", "yes");
                                serializer.attribute(DAS_XML_NAMESPACE, "superparts", (refFeature.hasSuperParts()) ? "yes" : "no");
                                serializer.attribute(DAS_XML_NAMESPACE, "subparts", (refFeature.hasSubParts()) ? "yes" : "no");
                            }
                            if (categorize){
                                if (feature.getTypeCategory() != null && feature.getTypeCategory().length() > 0){
                                    serializer.attribute(DAS_XML_NAMESPACE, "category", feature.getTypeCategory());
                                }
                                else {
                                    // To prevent the DAS server from dying, if no category has been set, but
                                    // a category is required, spit out the type ID again as the category.
                                    serializer.attribute(DAS_XML_NAMESPACE, "category", feature.getTypeId());
                                }
                            }
                            if (feature.getTypeLabel() != null && feature.getTypeLabel().length() > 0){
                                serializer.text(feature.getTypeLabel());
                            }
                            serializer.endTag(DAS_XML_NAMESPACE, "TYPE");

                            // METHOD element
                            serializer.startTag(DAS_XML_NAMESPACE, "METHOD");
                            if (feature.getMethodId() != null && feature.getMethodId().length() > 0){
                                serializer.attribute(DAS_XML_NAMESPACE, "id", feature.getMethodId());
                            }
                            if (feature.getMethodLabel() != null && feature.getMethodLabel().length() > 0){
                                serializer.text(feature.getMethodLabel());
                            }
                            serializer.endTag(DAS_XML_NAMESPACE, "METHOD");

                            // START element
                            serializer.startTag(DAS_XML_NAMESPACE, "START");
                            serializer.text(Integer.toString(feature.getStartCoordinate()));
                            serializer.endTag(DAS_XML_NAMESPACE, "START");

                            // END element
                            serializer.startTag(DAS_XML_NAMESPACE, "END");
                            serializer.text(Integer.toString(feature.getStopCoordinate()));
                            serializer.endTag(DAS_XML_NAMESPACE, "END");

                            // SCORE element
                            serializer.startTag(DAS_XML_NAMESPACE, "SCORE");
                            serializer.text ((feature.getScore() == null) ? "-" : Double.toString(feature.getScore()));
                            serializer.endTag(DAS_XML_NAMESPACE, "SCORE");

                            // ORIENTATION element
                            serializer.startTag(DAS_XML_NAMESPACE, "ORIENTATION");
                            serializer.text (feature.getOrientation().toString());
                            serializer.endTag(DAS_XML_NAMESPACE, "ORIENTATION");

                            // PHASE element
                            serializer.startTag(DAS_XML_NAMESPACE, "PHASE");
                            serializer.text (feature.getPhase().toString());
                            serializer.endTag(DAS_XML_NAMESPACE, "PHASE");

                            // NOTE elements
                            serializeFeatureNoteElements(feature.getNotes(), serializer);

                            // LINK elements
                            serializeFeatureLinkElements(feature.getLinks(), serializer);

                            // TARGET elements
                            serializeFeatureTargetElements(feature.getTargets(), serializer);

                            // GROUP elements
                            if (feature.getGroups() != null){
                                for (DasGroup group : feature.getGroups()){
                                    serializer.startTag(DAS_XML_NAMESPACE, "GROUP");
                                    serializer.attribute(DAS_XML_NAMESPACE, "id", group.getGroupId());
                                    if (group.getGroupLabel() != null && group.getGroupLabel().length() > 0){
                                        serializer.attribute(DAS_XML_NAMESPACE, "label", group.getGroupLabel());
                                    }
                                    if (group.getGroupType() != null && group.getGroupType().length() > 0){
                                        serializer.attribute(DAS_XML_NAMESPACE, "type", group.getGroupType());
                                    }
                                    // GROUP/NOTE elements
                                    serializeFeatureNoteElements(group.getNotes(), serializer);

                                    // GROUP/LINK elements
                                    serializeFeatureLinkElements(group.getLinks(), serializer);

                                    // GROUP/TARGET elements
                                    serializeFeatureTargetElements(group.getTargets(), serializer);

                                    serializer.endTag(DAS_XML_NAMESPACE, "GROUP");
                                }
                            }

                            serializer.endTag(DAS_XML_NAMESPACE, "FEATURE");
                        }
                    }
                    serializer.endTag(DAS_XML_NAMESPACE, "SEGMENT");
                }
            }
            serializer.endTag(DAS_XML_NAMESPACE, "GFF");
            serializer.endTag(DAS_XML_NAMESPACE, "DASGFF");

            serializer.flush();
        }
        finally{
            if (out != null){
                out.close();
            }
        }
    }

    /**
     * Helper method - serializes out the NOTE element which is used in two places in the DASFEATURE XML file.
     * (Hence factored out).
     * @param notes being a Collection of Strings, each of which is a note to be serialized.
     * @param serializer to write out the XML
     * @throws IOException during writing of the XML.
     */
    private void serializeFeatureNoteElements(Collection<String> notes, XmlSerializer serializer) throws IOException {
        if (notes != null){
            for (String note : notes){
                serializer.startTag(DAS_XML_NAMESPACE, "NOTE");
                serializer.text (note);
                serializer.endTag(DAS_XML_NAMESPACE, "NOTE");
            }
        }
    }

    /**
     * Helper method - serializes out the LINK element which is used in two places in the DASFEATURE XML file.
     * (Hence factored out).
     * @param links being a Map of URL to String, with the String being an optional human-readable form of the URL.
     * @param serializer to write out the XML
     * @throws IOException during writing of the XML.
     */
    private void serializeFeatureLinkElements(Map<URL, String> links, XmlSerializer serializer) throws IOException {
        if (links != null){
            for (URL url : links.keySet()){
                if (url != null){
                    serializer.startTag(DAS_XML_NAMESPACE, "LINK");
                    serializer.attribute(DAS_XML_NAMESPACE, "href", url.toString());
                    String linkText = links.get(url);
                    if (linkText != null && linkText.length() > 0){
                        serializer.text(linkText);
                    }
                    serializer.endTag(DAS_XML_NAMESPACE, "LINK");
                }
            }
        }
    }

    /**
     * Helper method - serializes out the TARGET element which is used in two places in the DASFEATURE XML file.
     * (Hence factored out).
     * @param targets being a Collection of DasTarget objects, encapsulating the details of the targets.
     * @param serializer to write out the XML
     * @throws IOException during writing of the XML.
     */
    private void serializeFeatureTargetElements(Collection<DasTarget> targets, XmlSerializer serializer) throws IOException {
        if (targets != null){
            for (DasTarget target : targets){
                serializer.startTag(DAS_XML_NAMESPACE, "TARGET");
                serializer.attribute(DAS_XML_NAMESPACE, "id", target.getTargetId());
                serializer.attribute(DAS_XML_NAMESPACE, "start", Integer.toString(target.getStartCoordinate()));
                serializer.attribute(DAS_XML_NAMESPACE, "stop", Integer.toString(target.getStopCoordinate()));
                if (target.getTargetName() != null && target.getTargetName().length() > 0){
                    serializer.text(target.getTargetName());
                }
                serializer.endTag(DAS_XML_NAMESPACE, "TARGET");
            }
        }
    }

    /**
     * Implements the entry_points command.
     * @param request to allow the writing of the http header
     * @param response to which the http header and the XML are written.
     * @param dsnConfig holding configuration of the dsn and the data source object itself.
     * @param queryString to be checked for bad arguments (there should be no arguments to this command)
     * @throws XmlPullParserException in the event of a problem with writing out the DASENTRYPOINT XML file.
     * @throws IOException during writing of the XML
     * @throws DataSourceException to capture any error returned from the data source.
     * @throws UnimplementedFeatureException if the dsn reports that it cannot return entry_points.
     * @throws BadCommandArgumentsException in the event that spurious arguments have been passed in the queryString.
     */
    private void entryPointsCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
            throws XmlPullParserException, IOException, DataSourceException, UnimplementedFeatureException, BadCommandArgumentsException {

        // Check not spurious arguments have been passed to this command.
        if (queryString != null && queryString.trim().length() > 0){
            throw new BadCommandArgumentsException("Unexpected arguments have been passed to the entry_points command.");
        }

        if (dsnConfig.getDataSource() instanceof ReferenceDataSource){
            // Fine - process command.
            ReferenceDataSource refDsn = (ReferenceDataSource) dsnConfig.getDataSource();
            Collection<DasEntryPoint> entryPoints = refDsn.getEntryPoints();
            // Check that an entry point version has been set.
            if (refDsn.getEntryPointVersion() == null){
                throw new DataSourceException("The dsn " + dsnConfig.getId() + "is returning null for the entry point version, which is invalid.");
            }
            // Looks like all is OK.
            writeHeader (request, response, XDasStatus.STATUS_200_OK, true);
            //OK, got our entry points, so write out the XML.
            XmlSerializer serializer;
            serializer = PULL_PARSER_FACTORY.newSerializer();
            BufferedWriter out = null;
            try{
                out = getResponseWriter(request, response);
                serializer.setOutput(out);
                serializer.setProperty(INDENTATION_PROPERTY, INDENTATION_PROPERTY_VALUE);
                serializer.startDocument(null, false);
                serializer.text("\n");
                if (DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getEntryPointsXSLT() != null){
                    serializer.processingInstruction(DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getEntryPointsXSLT());
                    serializer.text("\n");
                }
                serializer.docdecl(" DASEP SYSTEM \"http://www.biodas.org/dtd/dasep.dtd\"");
                serializer.text("\n");

                // Rest of the XML.
                serializer.startTag(DAS_XML_NAMESPACE, "DASEP");
                serializer.startTag(DAS_XML_NAMESPACE, "ENTRY_POINTS");
                serializer.attribute(DAS_XML_NAMESPACE, "href", buildRequestHref(request));
                serializer.attribute(DAS_XML_NAMESPACE, "version", refDsn.getEntryPointVersion());

                // Now for the individual segments.
                for (DasEntryPoint entryPoint : entryPoints){
                    if (entryPoint != null){
                        serializer.startTag(DAS_XML_NAMESPACE, "SEGMENT");
                        serializer.attribute(DAS_XML_NAMESPACE, "id", entryPoint.getSegmentId());
                        serializer.attribute(DAS_XML_NAMESPACE, "start", Integer.toString(entryPoint.getStartCoordinate()));
                        serializer.attribute(DAS_XML_NAMESPACE, "stop", Integer.toString(entryPoint.getStopCoordinate()));
                        if (entryPoint.getType() != null && entryPoint.getType().length() > 0){
                            serializer.attribute(DAS_XML_NAMESPACE, "type", entryPoint.getType());
                        }
                        serializer.attribute(DAS_XML_NAMESPACE, "orientation", entryPoint.getOrientation().toString());
                        if (entryPoint.hasSubparts()){
                            serializer.attribute(DAS_XML_NAMESPACE, "subparts", "yes");
                        }
                        if (entryPoint.getDescription() != null && entryPoint.getDescription().length() > 0){
                            serializer.text(entryPoint.getDescription());
                        }
                        serializer.endTag(DAS_XML_NAMESPACE, "SEGMENT");
                    }
                }
                serializer.endTag(DAS_XML_NAMESPACE, "ENTRY_POINTS");
                serializer.endTag(DAS_XML_NAMESPACE, "DASEP");

                serializer.flush();
            }
            finally{
                if (out != null){
                    out.close();
                }
            }
        }
        else {
            // Not a reference source.
            throw new UnimplementedFeatureException("An attempt to request entry_point information from an annotation server has been detected.");
        }
    }


    /**
     * Implements the sequence command.  Delegates to the getSequences method to return the requested sequences.
     * @param request to allow the writing of the http header
     * @param response to which the http header and the XML are written.
     * @param dsnConfig holding configuration of the dsn and the data source object itself.
     * @param queryString from which the requested segments are parsed.
     * @throws XmlPullParserException in the event of a problem with writing out the DASSEQUENCE XML file.
     * @throws IOException during writing of the XML
     * @throws DataSourceException to capture any error returned from the data source.
     * @throws UnimplementedFeatureException if the dsn reports that it cannot return sequence.
     * @throws BadReferenceObjectException in the event that the segment id is not known to the dsn
     * @throws BadCommandArgumentsException if the arguments to the sequence command are not as specified in the
     * DAS 1.53 specification
     * @throws CoordinateErrorException if the requested coordinates are outside those of the segment id requested.
     */
    private void sequenceCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
            throws XmlPullParserException, IOException, DataSourceException, UnimplementedFeatureException,
            BadReferenceObjectException, BadCommandArgumentsException, CoordinateErrorException {
        // Is this a reference source?
        if (dsnConfig.getDataSource() instanceof ReferenceDataSource){
            // Fine - process command.
            Collection<SequenceReporter> sequences = getSequences(dsnConfig, queryString);
            // Got some sequences, so all is OK.
            writeHeader (request, response, XDasStatus.STATUS_200_OK, true);
            // Build the XML.
            XmlSerializer serializer;
            serializer = PULL_PARSER_FACTORY.newSerializer();
            BufferedWriter out = null;
            try{
                out = getResponseWriter(request, response);
                serializer.setOutput(out);
                serializer.setProperty(INDENTATION_PROPERTY, INDENTATION_PROPERTY_VALUE);
                serializer.startDocument(null, false);
                serializer.text("\n");
                if (DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getSequenceXSLT() != null){
                    serializer.processingInstruction(DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getSequenceXSLT());
                    serializer.text("\n");
                }
                serializer.docdecl(" DASSEQUENCE SYSTEM \"http://www.biodas.org/dtd/dassequence.dtd\"");
                serializer.text("\n");
                // Now the body of the DASDNA xml.
                serializer.startTag (DAS_XML_NAMESPACE, "DASSEQUENCE");
                for (SequenceReporter sequenceReporter : sequences){
                    serializer.startTag(DAS_XML_NAMESPACE, "SEQUENCE");
                    serializer.attribute(DAS_XML_NAMESPACE, "id", sequenceReporter.getSegmentName());
                    serializer.attribute(DAS_XML_NAMESPACE, "start", Integer.toString(sequenceReporter.getStart()));
                    serializer.attribute(DAS_XML_NAMESPACE, "stop", Integer.toString(sequenceReporter.getStop()));
                    serializer.attribute(DAS_XML_NAMESPACE, "moltype", sequenceReporter.getSequenceMoleculeType());
                    serializer.attribute(DAS_XML_NAMESPACE, "version", sequenceReporter.getSequenceVersion());
                    serializer.text(sequenceReporter.getSequenceString());
                    serializer.endTag(DAS_XML_NAMESPACE, "SEQUENCE");
                }
                serializer.endTag (DAS_XML_NAMESPACE, "DASSEQUENCE");
            }
            finally{
                if (out != null){
                    out.close();
                }
            }
        }
        else {
            // Not a reference source.
            throw new UnimplementedFeatureException("An attempt to request sequence information from an anntation server has been detected.");
        }
    }


    /**
     * Helper method used by both the featuresCommand and typesCommand to return a Collection of SegmentReporter objects.
     *
     * The SegmentReporter interface is implemented to allow both correctly returned segments and missing segments
     * to be returned.
     * @param dsnConfig holding configuration of the dsn and the data source object itself.
     * @param requestedSegments being a List of SegmentQuery objects, which encapsulate the segment request (including
     * the segment id and optional start / stop coordinates)
     * @return a Collection of FeatureReporter objects that wrap the DasFeature objects returned from the data source
     * @throws DataSourceException to capture any error returned from the data source that cannot be handled in a more
     * elegant manner.
     * @param unknownSegmentsHandled to indicate if the calling method is able to report missing segments (i.e.
     * the feature command can return errorsegment / unknownsegment).
     * @throws uk.ac.ebi.mydas.exceptions.BadReferenceObjectException thrown if unknownSegmentsHandled is false and
     * the segment id is not known to the DSN.
     * @throws uk.ac.ebi.mydas.exceptions.CoordinateErrorException thrown if unknownSegmentsHandled is false and
     * the segment coordinates are out of scope for the provided segment id.
     */
    private Collection<SegmentReporter> getFeatureCollection(DataSourceConfiguration dsnConfig,
                                                             List <SegmentQuery> requestedSegments,
                                                             boolean unknownSegmentsHandled
    )
            throws DataSourceException, BadReferenceObjectException, CoordinateErrorException {
        List<SegmentReporter> segmentReporterLists = new ArrayList<SegmentReporter>(requestedSegments.size());
        AnnotationDataSource dataSource = dsnConfig.getDataSource();
        for (SegmentQuery segmentQuery : requestedSegments){
            try{
                DasAnnotatedSegment annotatedSegment;

                // Build the key name for the cache.
                StringBuffer cacheKeyBuffer = new StringBuffer(dsnConfig.getId());
                cacheKeyBuffer.append("_FEATURES_");
                if (dataSource instanceof RangeHandlingAnnotationDataSource || dataSource instanceof RangeHandlingReferenceDataSource){
                    // May return DasSequence objects containing partial sequences, so include segment id, start and stop coordinates in the key:
                    cacheKeyBuffer.append(segmentQuery.toString());
                }
                else {
                    // Otherwise will only return complete sequences, so store on segment id only:
                    cacheKeyBuffer.append(segmentQuery.getSegmentId());
                }
                String cacheKey = cacheKeyBuffer.toString();

                try{
                    annotatedSegment = (DasAnnotatedSegment) CACHE_MANAGER.getFromCache(cacheKey);
                    if (logger.isDebugEnabled()){
                        logger.debug("FEATURES RETRIEVED FROM CACHE: " + annotatedSegment.getSegmentId());
                    }
                    if (annotatedSegment == null){
                        // This should not happen - segment requests that fail are not cached.
                        throw new BadReferenceObjectException(segmentQuery.getSegmentId(), "Obtained an annotatedSegment from the cache for this segment.  It was null, so assume this is a bad segment id.");
                    }
                }
                catch (NeedsRefreshException nre){
                    try{
                        if (segmentQuery.getStartCoordinate() == null){
                            // Easy request - just want all the features on the segment.
                            annotatedSegment = dataSource.getFeatures(segmentQuery.getSegmentId());
                        }
                        else {
                            // Restricted to coordinates.
                            if (dataSource instanceof RangeHandlingAnnotationDataSource){
                                annotatedSegment = ((RangeHandlingAnnotationDataSource)dataSource).getFeatures(
                                        segmentQuery.getSegmentId(),
                                        segmentQuery.getStartCoordinate(),
                                        segmentQuery.getStopCoordinate());
                            }
                            else if (dataSource instanceof RangeHandlingReferenceDataSource){
                                annotatedSegment = ((RangeHandlingReferenceDataSource)dataSource).getFeatures(
                                        segmentQuery.getSegmentId(),
                                        segmentQuery.getStartCoordinate(),
                                        segmentQuery.getStopCoordinate());
                            }
                            else {
                                annotatedSegment = dataSource.getFeatures(
                                        segmentQuery.getSegmentId());
                            }
                        }
                        if (logger.isDebugEnabled()){
                            logger.debug("FEATURES NOT IN CACHE: " + annotatedSegment.getSegmentId());
                        }
                        CACHE_MANAGER.putInCache(cacheKey, annotatedSegment, dsnConfig.getCacheGroup());
                    }
                    catch (BadReferenceObjectException broe) {
                        CACHE_MANAGER.cancelUpdate(cacheKey);
                        throw broe;
                    }
                    catch (CoordinateErrorException cee) {
                        CACHE_MANAGER.cancelUpdate(cacheKey);
                        throw cee;
                    }
                }
                segmentReporterLists.add(new FoundFeaturesReporter(annotatedSegment, segmentQuery));
            } catch (BadReferenceObjectException broe) {
                if (unknownSegmentsHandled){
                    segmentReporterLists.add(new UnknownSegmentReporter(segmentQuery));
                }
                else {
                    throw broe;
                }
            } catch (CoordinateErrorException cee) {
                if (unknownSegmentsHandled){
                    segmentReporterLists.add(new UnknownSegmentReporter(segmentQuery));
                }
                else {
                    throw cee;
                }
            }
        }
        return segmentReporterLists;
    }

    /**
     * Helper method used by both the dnaCommand and the sequenceCommand
     * @param dsnConfig holding configuration of the dsn and the data source object itself.
     * @param queryString to be parsed, which includes details of the requested segments
     * @return a Collection of SequenceReporter objects.  The SequenceReporter wraps the DasSequence object
     * to provide additional functionality that is hidden (for simplicity) from the dsn developer.
     * @throws BadReferenceObjectException if the segment id is not available from the data source
     * @throws CoordinateErrorException if the requested coordinates fall outside those of the requested segment id
     * @throws DataSourceException to capture any error returned from the data source.
     * @throws BadCommandArgumentsException if the arguments to the command are not recognised.
     */
    private Collection<SequenceReporter> getSequences(DataSourceConfiguration dsnConfig, String queryString) throws DataSourceException, BadCommandArgumentsException, BadReferenceObjectException, CoordinateErrorException {

        ReferenceDataSource refDsn = (ReferenceDataSource) dsnConfig.getDataSource();
        if (refDsn == null){
            throw new DataSourceException ("An attempt has been made to retrieve a sequenceString from datasource " + dsnConfig.getId() + " however the DataSource object is null.");
        }
        Collection<SequenceReporter> sequenceCollection = new ArrayList<SequenceReporter>();
        // Parse the queryString to retrieve all the DasSequence objects.
        if (queryString == null || queryString.length() == 0){
            throw new BadCommandArgumentsException("Expecting at least one reference in the query string, but found nothing.");
        }
        // Split on the ; (delineates separate references in the query string)
        String[] referenceStrings = queryString.split(";");
        for (String referenceString : referenceStrings){
            Matcher referenceStringMatcher = SEGMENT_RANGE_PATTERN.matcher(referenceString);
            if (referenceStringMatcher.find()){
                SegmentQuery segmentQuery = new SegmentQuery(referenceStringMatcher);
                DasSequence sequence;

                // Build the key name for the cache.
                StringBuffer cacheKeyBuffer = new StringBuffer(dsnConfig.getId());
                cacheKeyBuffer.append("_SEQUENCE_");
                if (refDsn instanceof RangeHandlingReferenceDataSource){
                    // May return DasSequence objects containing partial sequences, so include segment id, start and stop coordinates in the key:
                    cacheKeyBuffer.append(segmentQuery.toString());
                }
                else {
                    // Otherwise will only return complete sequences, so store on segment id only:
                    cacheKeyBuffer.append(segmentQuery.getSegmentId());
                }
                String cacheKey = cacheKeyBuffer.toString();


                try{
                    // flushCache checks with the data source if the cache needs emptying, and does so if required.
                    sequence = (DasSequence) CACHE_MANAGER.getFromCache(cacheKey);
                    if (logger.isDebugEnabled()){
                        logger.debug("SEQUENCE RETRIEVED FROM CACHE: " + sequence.getSegmentId());
                    }
                } catch (NeedsRefreshException nre){
                    try{
                        if (segmentQuery.getStartCoordinate() != null){
                            // Getting a restricted sequenceString - and the data source will handle the restriction.
                            if (refDsn instanceof RangeHandlingReferenceDataSource){
                                sequence = ((RangeHandlingReferenceDataSource)refDsn).getSequence(
                                        segmentQuery.getSegmentId(),
                                        segmentQuery.getStartCoordinate(),
                                        segmentQuery.getStopCoordinate()
                                );
                                // These putInCache calls include a group, being the dsn name to allow a DSN to
                                // remove all cached data if it requires.
                                CACHE_MANAGER.putInCache(cacheKey, sequence, dsnConfig.getCacheGroup());
                            }
                            else {
                                sequence = refDsn.getSequence(segmentQuery.getSegmentId());
                                CACHE_MANAGER.putInCache(cacheKey, sequence, dsnConfig.getCacheGroup());
                            }
                        }
                        else {
                            // Request for a complete sequenceString
                            sequence = refDsn.getSequence(segmentQuery.getSegmentId());
                            CACHE_MANAGER.putInCache(cacheKey, sequence, dsnConfig.getCacheGroup());
                        }
                    } catch (BadReferenceObjectException broe) {
                        CACHE_MANAGER.cancelUpdate(cacheKey);
                        throw broe;
                    } catch (DataSourceException dse) {
                        CACHE_MANAGER.cancelUpdate(cacheKey);
                        throw dse;
                    } catch (CoordinateErrorException cee) {
                        CACHE_MANAGER.cancelUpdate(cacheKey);
                        throw cee;
                    }
                    if (logger.isDebugEnabled()){
                        logger.debug("Sequence retrieved from DSN (not cached): " + sequence.getSegmentId());
                    }
                }
                // Belt and braces - the various getSequence methods throw BadReferenceObjectException -
                // but just in case the dsn
                // fails to throw this appropriately and instead return a null sequence object...
                if (sequence == null) throw new BadReferenceObjectException(segmentQuery.getSegmentId(), "Segment cannot be found.");
                sequenceCollection.add (new SequenceReporter(sequence, segmentQuery));
            }
			// MyDas is being made less fussy about parameters that it does not recognise as new
			// DAS features are added, e.g. to DAS 1.53E, hence any parameters that do not match are just ignored.
        }
        if (sequenceCollection.size() ==0){
            // The query string did not include any segment references.
            throw new BadCommandArgumentsException("The query string did not include any segments, so no sequence can be returned.");
        }
        return sequenceCollection;
    }


    /**
     * Writes the response header with the additional DAS Http headers.
     * @param response to which to write the headers.
     * @param status being the status to write.
     * @param request required to determine if the client will accept a compressed response
     * @param compressionAllowed to indicate if the specific response should be gzipped. (e.g. an error message with
     * no content should not set the compressed header.)
     */
    private void writeHeader (HttpServletRequest request, HttpServletResponse response, XDasStatus status, boolean compressionAllowed){
        response.setHeader(HEADER_KEY_X_DAS_VERSION, HEADER_VALUE_DAS_VERSION);
        response.setHeader(HEADER_KEY_X_DAS_CAPABILITIES, HEADER_VALUE_CAPABILITIES);
        response.setHeader(HEADER_KEY_X_DAS_STATUS, status.toString());
        if (compressionAllowed && compressResponse (request)){
            response.setHeader(ENCODING_RESPONSE_HEADER_KEY, ENCODING_GZIPPED);
        }
    }

    /**
     * Returns a PrintWriter for the response. First checks if the output should / can be
     * gzipped. If so, wraps the OutputStream in a GZIPOutputStream and then returns
     * a PrintWriter to this.
     * @param request the HttpServletRequest, needed to check the capabilities of the
     * client.
     * @param response from which the OutputStream is obtained
     * @return a PrintWriter that will either produce plain or gzipped output.
     * @throws IOException due to a problem with initiating the output stream or writer.
     */
    private BufferedWriter getResponseWriter (HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (compressResponse(request)){
            // Wrap the response writer in a Zipstream.
            GZIPOutputStream zipStream = new GZIPOutputStream(response.getOutputStream());
            return new BufferedWriter (new PrintWriter(zipStream));
        }
        else {
            return new BufferedWriter (response.getWriter());
        }
    }

    /**
     * Checks in the configuration to see if the output should be gzipped and also
     * checks if the client can accept gzipped output.
     * @param request being the HttpServletRequest, to allow a check of the client capabilities to be checked.
     * @return a boolean indicating if the response should be compressed.
     */
    private boolean compressResponse (HttpServletRequest request){
        String clientEncodingAbility = request.getHeader(ENCODING_REQUEST_HEADER_KEY);
        return DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().isGzipped()
                && clientEncodingAbility != null
                && clientEncodingAbility.contains(ENCODING_GZIPPED);
    }


    /**
     * Helper method that re-constructs the URL that was used to query the service.
     * @param request to retrieve elements of the URL
     * @return the URL that was used to query the service.
     */
    private String buildRequestHref(HttpServletRequest request) {
        StringBuffer requestURL = new StringBuffer(DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getBaseURL());
        String requestURI = request.getRequestURI();
        // The /das/ part of the URL comes from the baseurl configuration, so need to add on the request after this point.
        requestURL.append (requestURI.substring(5 + requestURI.indexOf("/das/")));
        String queryString = request.getQueryString();
        if (queryString != null && queryString.length() > 0){
            requestURL.append ('?')
                    .append (queryString);
        }
        return requestURL.toString();
    }


    GeneralCacheAdministrator getCacheManager() {
        return CACHE_MANAGER;
    }
}
