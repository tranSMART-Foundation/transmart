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

import com.opensymphony.oscache.general.GeneralCacheAdministrator;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.exceptions.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
@SuppressWarnings("serial")
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

	private DasCommandManager dasCommands=null;

	private static final Pattern REQUEST_URI_PATTERN = Pattern.compile ("/das/([^\\s/?]+)/?([^\\s/?]*)$");
//	private static final Pattern REQUEST_URI_PATTERN = Pattern.compile ("/([^\\s/?]+)/?([^\\s/?]*)$");

	private static final Pattern DAS_ONLY_URI_PATTERN = Pattern.compile ("/das[/]?$");
//	private static final Pattern DAS_ONLY_URI_PATTERN = Pattern.compile ("[/]?$");

	/**
	 * Pattern used to parse a segment range, as used for the dna and sequenceString commands.
	 * This can be used based on the assumption that the segments have already been split
	 * into indidual Strings (i.e. by splitting on the ; character).
	 * Three groups are returned from a match as follows:
	 * Group 1: segment name
	 * Group 3: start coordinate
	 * Group 4: stop coordinate
	 */

	private static DataSourceManager DATA_SOURCE_MANAGER = null;

	static final String RESOURCE_FOLDER = "/";
//	private static final String RESOURCE_FOLDER = "/WEB-INF/classes/";

	private static final String CONFIGURATION_FILE_NAME = RESOURCE_FOLDER + "MydasServerConfig.xml";
	/*
	Response Header line keys
	 */
	private static final String HEADER_KEY_X_DAS_VERSION = "X-DAS-Version";
	private static final String HEADER_KEY_X_DAS_STATUS = "X-DAS-Status";
	private static final String HEADER_KEY_X_DAS_SERVER = "X-DAS-Server";
	private static final String HEADER_KEY_X_DAS_CAPABILITIES = "X-DAS-Capabilities";
    private static final String HEADER_KEY_CORS = "Access-Control-Allow-Origin";
    private static final String HEADER_KEY_CORS_EXPOSE = "Access-Control-Expose-Headers";
    private static final String HEADER_KEY_CORS_METHODS = "Access-Control-Allow-Methods";
    private static final String HEADER_KEY_CORS_HEADERS = "Access-Control-Allow-Headers";
    private static final String HEADER_KEY_CORS_AGE = "Access-Control-Max-Age";

    /*
	Response Header line values
	 */
//	private static final String HEADER_VALUE_CAPABILITIES = "dsn/1.0; dna/1.0; types/1.0; stylesheet/1.0; features/1.0; entry_points/1.0; error-segment/1.0; unknown-segment/1.0; feature-by-id/1.0; group-by-id/1.0; component/1.0; supercomponent/1.0; sequenceString/1.0";
	private static final String HEADER_VALUE_DAS_VERSION = "DAS/1.6";
	private static final String HEADER_VALUE_DAS_SERVER = "MyDAS1.6";
    private static final String HEADER_VALUE_X_DAS_DEFAULT_CAPABILITIES = "sources/1.0";
    private static final String HEADER_VALUE_CORS = "*";
    private static final String HEADER_VALUE_CORS_EXPOSE = HEADER_KEY_X_DAS_VERSION +
        ", " + HEADER_KEY_X_DAS_STATUS +
        ", " + HEADER_KEY_X_DAS_SERVER +
        ", " + HEADER_KEY_X_DAS_CAPABILITIES;
    private static final String HEADER_VALUE_CORS_METHODS = "GET, POST, OPTIONS";
    private static final String HEADER_VALUE_CORS_HEADERS = "X-DAS-Version, X-DAS-Client";
    private static final String HEADER_VALUE_CORS_AGE = "2592000";

	/**
	 * Private enum that is used by this class to match valid commands.
	 * The command structure was added to support the new capability in DAS1.6
	 */
	enum Commands{

		COMMAND_DSN("dsn"),
		COMMAND_DNA ("dna"),
		COMMAND_TYPES ("types"),
		COMMAND_LINK ("link"),
		COMMAND_STYLESHEET ("stylesheet"),
		COMMAND_FEATURES ("features"),
		COMMAND_ENTRY_POINTS ("entry_points"),
		COMMAND_SEQUENCE ("sequence"),
		COMMAND_ALIGNMENT ("alignment"),
		COMMAND_STRUCTURE ("structure"),
		COMMAND_SOURCES ("sources");

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



	/*
        Content encoding
	 */
	private static final String ENCODING_REQUEST_HEADER_KEY = "Accept-Encoding";
	private static final String ENCODING_RESPONSE_HEADER_KEY = "Content-Encoding";
	private static final String ENCODING_GZIPPED = "gzip";

	private static XmlPullParserFactory PULL_PARSER_FACTORY = null;

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
		dasCommands= new DasCommandManager(DATA_SOURCE_MANAGER, CACHE_MANAGER, this);

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
		DataSourceConfiguration dataSourceConfig = DATA_SOURCE_MANAGER.getServerConfiguration().getDataSourceConfigMap().get("writeback");
		try {
			dasCommands.writebackCreate(request,response,dataSourceConfig);
		} catch (WritebackException e) {
			logger.error("Writebackexception thrown", e);
			writeHeader(request, response, XDasStatus.STATUS_500_SERVER_ERROR, false,null);
			reportError(XDasStatus.STATUS_500_SERVER_ERROR, "Writeback error creating a feature.", request, response);
		}
	}

	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		dasCommands.writebackDelete(request,response);
	}

	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DataSourceConfiguration dataSourceConfig = DATA_SOURCE_MANAGER.getServerConfiguration().getDataSourceConfigMap().get("writeback");
		try {
			dasCommands.writebackupdate(request,response,dataSourceConfig);
		} catch (WritebackException e) {
			logger.error("WritebackException thrown", e);
			writeHeader(request, response, XDasStatus.STATUS_500_SERVER_ERROR, false,null);
			reportError(XDasStatus.STATUS_500_SERVER_ERROR, "Writeback error creating a feature.", request, response);
		}
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
			logger.debug("Query String2: '" + queryString + "'");
		}
		Matcher match = REQUEST_URI_PATTERN.matcher(request.getRequestURI().replaceAll("/{2,}", "/"));

        String capabilities = null; //capabilities will be reported even when there are errors.
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
						dasCommands.dsnCommand (request, response, queryString);
					}
					else {
						// Starts off looking like the dsn command, but has some other stuff after it...
						throw new BadCommandException("A bad dsn command has been sent to the server, including unrecognised additional query parameters.");
					}
					// Check for the source command (similar command to dsn).
				} else if (Commands.COMMAND_SOURCES.matches(match.group(1))){
					// Handle source command, in contrast with dsn, source can have extra info 
					dasCommands.sourceCommand (request, response, queryString, null);
				}

				// Not the dsn the source command either the source(explicit), so handle other commands (which are datasource specific)
				else {
					String dsnName = match.group(1);
					if (match.group(2) == null || match.group(2).length() == 0){
						// Source command for an specific DSN 
						// Attempt to retrieve the DataSource
						if (null!=DATA_SOURCE_MANAGER.getServerConfiguration().getDataSourceConfigMap().get(dsnName)){
							dasCommands.sourceCommand (request, response, queryString, dsnName);
							return;
						}
					}
					String command = match.group(2);
					if (logger.isDebugEnabled()){
						logger.debug("dsnName: '" + dsnName + "'");
						logger.debug("command: '" + command + "'");
					}

					// Attempt to retrieve the DataSource
					DataSourceConfiguration dataSourceConfig = DATA_SOURCE_MANAGER.getServerConfiguration().getDataSourceConfigMap().get(dsnName);
					// Check if the datasource exists.
					if (dataSourceConfig != null){
                        //Get datasource capabilities so they will be display in the headers
                        capabilities = dataSourceConfig.getCapabilities();
						// Check the datasource is alive.
						if (dataSourceConfig.isOK()){
							if      (Commands.COMMAND_DNA.matches(command)){
								dasCommands.dnaCommand (request, response, dataSourceConfig, queryString);
							}
							else if (Commands.COMMAND_TYPES.matches(command)){
								dasCommands.typesCommand (request, response, dataSourceConfig, queryString);
							}
							else if (Commands.COMMAND_STYLESHEET.matches(command)){
								dasCommands.stylesheetCommand (request, response, dataSourceConfig, queryString);
							}
							else if (Commands.COMMAND_FEATURES.matches(command)){
								dasCommands.featuresCommand (request, response, dataSourceConfig, queryString);
							}
							else if (Commands.COMMAND_ENTRY_POINTS.matches(command)){
								dasCommands.entryPointsCommand (request, response, dataSourceConfig, queryString);
							}
							else if (Commands.COMMAND_SEQUENCE.matches(command)){
								dasCommands.sequenceCommand (request, response, dataSourceConfig, queryString);
							}
							else if (Commands.COMMAND_STRUCTURE.matches(command)){ //for the command structure DAS1.6
								dasCommands.structureCommand (request, response, dataSourceConfig, queryString);
							}
							else if (Commands.COMMAND_ALIGNMENT.matches(command)){ //for the command alignment DAS1.6
								dasCommands.alignmentCommand (request, response, dataSourceConfig, queryString);
							}
							else if (Commands.COMMAND_LINK.matches(command)){
								dasCommands.linkCommand (response, dataSourceConfig, queryString);
							}
							else {
								dasCommands.otherCommand(request, response,dataSourceConfig,command,queryString);
							}
						}
						else{
							throw new BadDataSourceException("The datasource was not correctly initialised.");
						}
					}
					else {
                        capabilities = null;
						throw new BadDataSourceException("The requested datasource does not exist.");
					}
				}
			}
			else if (DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().isSlashDasPointsToDsn()
					&& DAS_ONLY_URI_PATTERN.matcher(request.getRequestURI()).find()){
				// Just /das or /das/ has been given as the URL.  This server is configured to point
				// this to the sources command, so do so.
                dasCommands.sourceCommand(request, response, queryString, null);
				//dasCommands.dsnCommand (request, response, queryString); //since 1.6.1 sources is the default command
			}
			else {
				throw new BadCommandException("The command is not recognised.");
			}
		} catch (BadCommandException bce) {
			logger.error("BadCommandException thrown", bce);
			writeHeader(request, response, XDasStatus.STATUS_400_BAD_COMMAND, false, capabilities);
			reportError(XDasStatus.STATUS_400_BAD_COMMAND, "Bad Command - Command not recognised as a valid DAS command.", request, response);
		} catch (BadDataSourceException bdse) {
			logger.error("BadDataSourceException thrown", bdse);
			writeHeader(request, response, XDasStatus.STATUS_401_BAD_DATA_SOURCE, false, capabilities);
			reportError(XDasStatus.STATUS_401_BAD_DATA_SOURCE, "Bad Data Source", request, response);
		} catch (BadCommandArgumentsException bcae) {
			logger.error("BadCommandArgumentsException thrown", bcae);
			writeHeader(request, response, XDasStatus.STATUS_402_BAD_COMMAND_ARGUMENTS, false, capabilities);
			reportError(XDasStatus.STATUS_402_BAD_COMMAND_ARGUMENTS, "Bad Command Arguments - Command not recognised as a valid DAS command.", request, response);
		} catch (BadReferenceObjectException broe) {
			logger.error("BadReferenceObjectException thrown", broe);
			writeHeader(request, response, XDasStatus.STATUS_403_BAD_REFERENCE_OBJECT, false, capabilities);
			reportError(XDasStatus.STATUS_403_BAD_REFERENCE_OBJECT, "Unrecognised reference object: the requested segment is not available from this server.", request, response);
		} catch (BadStylesheetException bse) {
			logger.error("BadStylesheetException thrown:", bse);
			writeHeader(request, response, XDasStatus.STATUS_404_BAD_STYLESHEET, false, capabilities);
			reportError(XDasStatus.STATUS_404_BAD_STYLESHEET, "Bad Stylesheet.", request, response);
		} catch (CoordinateErrorException cee) {
			logger.error("CoordinateErrorException thrown", cee);
			writeHeader(request, response, XDasStatus.STATUS_405_COORDINATE_ERROR, false, capabilities);
			reportError(XDasStatus.STATUS_405_COORDINATE_ERROR, "Coordinate error - the requested coordinates are outside the scope of the requested segment.", request, response);
		} catch (XmlPullParserException xppe) {
			logger.error("XmlPullParserException thrown when attempting to ouput XML.", xppe);
			writeHeader (request, response, XDasStatus.STATUS_500_SERVER_ERROR, false, capabilities);
			reportError(XDasStatus.STATUS_500_SERVER_ERROR, "An error has occurred when attempting to output the DAS XML.", request, response);
		} catch (DataSourceException dse){
			logger.error("DataSourceException thrown by a data source.", dse);
			writeHeader(request, response, XDasStatus.STATUS_500_SERVER_ERROR, false, capabilities);
			reportError(XDasStatus.STATUS_500_SERVER_ERROR, "The data source has thrown a 'DataSourceException' indicating a software error has occurred: " + dse.getMessage(), request, response);
		} catch (ConfigurationException ce) {
			logger.error("ConfigurationException thrown: This mydas installation was not correctly initialised.", ce);
			writeHeader(request, response, XDasStatus.STATUS_500_SERVER_ERROR, false, capabilities);
			reportError(XDasStatus.STATUS_500_SERVER_ERROR, "This installation of MyDas is not correctly configured.", request, response);
		} catch (UnimplementedFeatureException efe) {
			logger.error("UnimplementedFeatureException thrown", efe);
			writeHeader(request, response, XDasStatus.STATUS_501_UNIMPLEMENTED_FEATURE, false, capabilities);
			reportError(XDasStatus.STATUS_501_UNIMPLEMENTED_FEATURE, "Unimplemented feature: this DAS server cannot serve the request you have made.", request, response);
		}

	}

	void reportError (XDasStatus dasStatus, String errorMessage, HttpServletRequest request, HttpServletResponse response) throws IOException {
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
	 * Writes the response header with the additional DAS Http headers.
	 * @param response to which to write the headers.
	 * @param status being the status to write.
	 * @param request required to determine if the client will accept a compressed response
	 * @param compressionAllowed to indicate if the specific response should be gzipped. (e.g. an error message with
	 * no content should not set the compressed header.)
	 * @throws IOException 
	 */
	void writeHeader (HttpServletRequest request, HttpServletResponse response, XDasStatus status, boolean compressionAllowed, String capabilities) throws IOException{
		response.setHeader(HEADER_KEY_X_DAS_VERSION, HEADER_VALUE_DAS_VERSION);
		if (capabilities != null) {
            String cap = "";
            
			response.setHeader(HEADER_KEY_X_DAS_CAPABILITIES, capabilities);
        } else {
            response.setHeader(HEADER_KEY_X_DAS_CAPABILITIES, HEADER_VALUE_X_DAS_DEFAULT_CAPABILITIES);
        }
		response.setHeader(HEADER_KEY_X_DAS_STATUS, status.toString());
		response.setHeader(HEADER_KEY_X_DAS_SERVER,HEADER_VALUE_DAS_SERVER);
		if (compressionAllowed && compressResponse (request)){
			response.setHeader(ENCODING_RESPONSE_HEADER_KEY, ENCODING_GZIPPED);
		}
        //CORS headers since 1.6.1
        response.setHeader(HEADER_KEY_CORS, HEADER_VALUE_CORS);
        response.setHeader(HEADER_KEY_CORS_EXPOSE, HEADER_VALUE_CORS_EXPOSE);
        response.setHeader(HEADER_KEY_CORS_METHODS, HEADER_VALUE_CORS_METHODS);
        response.setHeader(HEADER_KEY_CORS_HEADERS, HEADER_VALUE_CORS_HEADERS);
        response.setHeader(HEADER_KEY_CORS_AGE, HEADER_VALUE_CORS_AGE);

        /*
        if ( status==XDasStatus.STATUS_400_BAD_COMMAND ||
			status==XDasStatus.STATUS_401_BAD_DATA_SOURCE ||
			status==XDasStatus.STATUS_402_BAD_COMMAND_ARGUMENTS){
			response.sendError(400);
		} else if (status==XDasStatus.STATUS_404_BAD_STYLESHEET){
			response.sendError(404);
		} else if (status==XDasStatus.STATUS_500_SERVER_ERROR || status==XDasStatus.STATUS_501_UNIMPLEMENTED_FEATURE){
			response.sendError(500);
		}*/
	}


	/**
	 * Checks in the configuration to see if the output should be gzipped and also
	 * checks if the client can accept gzipped output.
	 * @param request being the HttpServletRequest, to allow a check of the client capabilities to be checked.
	 * @return a boolean indicating if the response should be compressed.
	 */
	boolean compressResponse (HttpServletRequest request){
		String clientEncodingAbility = request.getHeader(ENCODING_REQUEST_HEADER_KEY);
		return DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().isGzipped()
		&& clientEncodingAbility != null
		&& clientEncodingAbility.contains(ENCODING_GZIPPED);
	}

	GeneralCacheAdministrator getCacheManager() {
		return CACHE_MANAGER;
	}
}
