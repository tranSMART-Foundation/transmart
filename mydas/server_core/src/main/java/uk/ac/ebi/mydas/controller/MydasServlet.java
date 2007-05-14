package uk.ac.ebi.mydas.controller;

import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;
import uk.ac.ebi.mydas.datasource.ReferenceDataSource;
import uk.ac.ebi.mydas.datasource.RangeHandlingReferenceDataSource;
import uk.ac.ebi.mydas.exceptions.*;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasSequence;
import uk.ac.ebi.mydas.model.DasFeature;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * Created Using IntelliJ IDEA.
 * User: phil
 * Date: 04-May-2007
 * Time: 12:10:01
 * A simple DAS server allowing the easy creation of plugins to different data
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

    /**
     * Pattern used to parse a segment range, as used for the dna and sequenceString commands.
     * This can be used based on the assumption that the segments have already been split
     * into indidual Strings (i.e. by splitting on the ; character).
     * Three groups are returned from a match as follows:
     * Group 1: segment name
     * Group 3: start coordinate
     * Group 4: stop coordinate
     */
    private static final Pattern SEGMENT_RANGE_PATTERN = Pattern.compile ("^segment=([^:\\s]*)(:(\\d+),(\\d+))?$"
);

    private static DataSourceManager DATA_SOURCE_MANAGER = null;

    private static final String RESOURCE_FOLDER = "/WEB-INF/classes/";

    private static final String CONFIGURATION_FILE_NAME = RESOURCE_FOLDER + "MydasServerConfig.xml";

    /*
     Status codes for the DAS server.
     */
    private static final String STATUS_200_OK = "200";
    private static final String STATUS_400_BAD_COMMAND = "400";
    private static final String STATUS_401_BAD_DATA_SOURCE = "401";
    private static final String STATUS_402_BAD_COMMAND_ARGUMENTS = "402";
    private static final String STATUS_403_BAD_REFERENCE_OBJECT = "403";
    private static final String STATUS_404_BAD_STYLESHEET = "404";
    private static final String STATUS_405_COORDINATE_ERROR = "405";
    private static final String STATUS_500_SERVER_ERROR = "500";
    private static final String STATUS_501_UNIMPLEMENTED_FEATURE = "501";

    /*
     Commands handled by the servlet.
     */
    private static final String COMMAND_DSN = "dsn";
    private static final String COMMAND_DNA = "dna";
    private static final String COMMAND_TYPES = "types";
    private static final String COMMAND_STYLESHEET = "stylesheet";
    private static final String COMMAND_FEATURES = "features";
    private static final String COMMAND_ENTRY_POINTS = "entry_points";
    private static final String COMMAND_SEQUENCE = "sequence";

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


    /**
     * This method will ensure that all the plugins are registered and call
     * the corresonding init() method on all of the plugins.
     *
     * Also initialises the XMLPullParser factory.
     * @throws ServletException
     */
    public void init() throws ServletException {
        super.init();

        // Initialise data sources.
        if (DATA_SOURCE_MANAGER == null){
            DATA_SOURCE_MANAGER = new DataSourceManager(this.getServletContext());
            try{
                DATA_SOURCE_MANAGER.init(CONFIGURATION_FILE_NAME);
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
     * Delegates to the doHandle method
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doHandle(request, response);
    }

    /**
     * Delegates to the doHandle method
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doHandle(request, response);
    }

    /**
     * Handles requests encoded as GET or POST.
     * First of all splits up the request and then delegates to appropriate method
     * to feed this request.
     * @param request The http request object.
     * @param response The response - normally an XML file in HTTP/1.0 protocol.
     * @throws ServletException in the event of an internal error
     * @throws IOException in the event of a low level I/O error.
     */
    private void doHandle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Parse the request URI (e.g. /das/dsnname/sequenceString).
        String queryString = request.getQueryString();

        if (logger.isDebugEnabled()){
            logger.debug("RequestURI: '" + request.getRequestURI() + "'");
            logger.debug("Query String: '" + queryString + "'");
        }
        
        Matcher match = REQUEST_URI_PATTERN.matcher(request.getRequestURI());
        if (match.find()){
            try{
                // Check first for the dsn command
                if (COMMAND_DSN.equals(match.group(1))){
                    // Handle dsn command, after checking there is no guff in the URI after it.
                    if (match.group(2) == null || match.group(2).length() == 0){
                        // All good, send command.
                        dsnCommand (request, response, queryString);
                    }
                    else {
                        // Starts off looking like the dsn command, but has some other stuff after it...
                        writeHeader (request, response, STATUS_400_BAD_COMMAND, false);
                    }
                }

                // Not the dsn command, so handle other commands (which are datasource specific)
                else {
                    String dsnName = match.group(1);
                    String command = match.group(2);
                    // Attempt to retrieve the DataSource
                    DataSourceConfiguration dataSourceConfig = DATA_SOURCE_MANAGER.getServerConfiguration().getDataSourceConfigMap().get(dsnName);
                    // Check if the datasource exists.
                    if (dataSourceConfig != null){
                        // Check the datasource is alive.
                        if (dataSourceConfig.isOK()){
                            if (COMMAND_DNA.equals(command)){
                                dnaCommand (request, response, dataSourceConfig, queryString);
                            }
                            else if (COMMAND_TYPES.equals(command)){
                                typesCommand (request, response, dataSourceConfig, queryString);
                            }
                            else if (COMMAND_STYLESHEET.equals(command)){
                                stylesheetCommand (request, response, dataSourceConfig, queryString);
                            }
                            else if (COMMAND_FEATURES.equals(command)){
                                featuresCommand (request, response, dataSourceConfig, queryString);
                            }
                            else if (COMMAND_ENTRY_POINTS.equals(command)){
                                entryPointsCommand (request, response, dataSourceConfig, queryString);
                            }
                            else if (COMMAND_SEQUENCE.equals(command)){
                                sequenceCommand (request, response, dataSourceConfig, queryString);
                            }
                            else {
                                // The command is not recognised.
                                writeHeader (request, response, STATUS_400_BAD_COMMAND, false);
                            }
                        }
                        else{
                            // The dataSource requested did not initialise successfully.
                            logger.error(command + " called on a DataSource that did not initialise successfully.");
                            writeHeader(request, response, STATUS_401_BAD_DATA_SOURCE, false);
                        }
                    }
                    else {
                        // The datasource requested does not exist.
                        logger.error(command + " called on a non-existent data source.");
                        writeHeader(request, response, STATUS_401_BAD_DATA_SOURCE, false);
                    }
                }
            } catch (XmlPullParserException xppe) {
                // A problem has occurred when attempting to marshall out XML.
                logger.error("XmlPullParserException thrown when attempting to ouput XML.", xppe);
                writeHeader (request, response, STATUS_500_SERVER_ERROR, false);
            } catch (DataSourceException dse){
                // A data source has thrown a DataSourceException.
                logger.error("DataSourceException thrown by a data source.", dse);
                writeHeader(request, response, STATUS_500_SERVER_ERROR, false);
            }
        }
        else {
            // The command is not recognised.
            writeHeader (request, response, STATUS_400_BAD_COMMAND, false);
        }
    }

    /**
     * Implements the dsn command.  Only reports dsns that have initialised successfully.
     * @param request
     * @param response
     * @param queryString
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void dsnCommand(HttpServletRequest request, HttpServletResponse response, String queryString)
            throws XmlPullParserException, IOException{
        // Check the configuration has been loaded successfully
        if (DATA_SOURCE_MANAGER.getServerConfiguration() == null){
            writeHeader (request, response, STATUS_500_SERVER_ERROR, false);
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
                writeHeader (request, response, STATUS_500_SERVER_ERROR, false);
                logger.error("The dsn command has been called, but no dsns have been initialised successfully.");
            }
            else{
                // At least one dsn is OK.
                writeHeader (request, response, STATUS_200_OK, true);
                // Build the XML.
                XmlSerializer serializer;
                serializer = PULL_PARSER_FACTORY.newSerializer();
                PrintWriter out = null;
                try{
                    out = getResponseWriter(request, response);
                    serializer.setOutput(out);
                    serializer.setProperty(INDENTATION_PROPERTY, INDENTATION_PROPERTY_VALUE);
                    serializer.startDocument(null, false);
                    serializer.text("\n");
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
            writeHeader (request, response, STATUS_402_BAD_COMMAND_ARGUMENTS, true);
        }
    }

    private void dnaCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
            throws XmlPullParserException, IOException, DataSourceException{
        // Is this a reference source?
        if (dsnConfig.getDataSource() instanceof ReferenceDataSource){
            // Fine - process command.
            try{
                Collection<SequenceReporter> sequences = getSequences(dsnConfig, queryString);
                // Got some sequences, so all is OK.
                writeHeader (request, response, STATUS_200_OK, true);
                // Build the XML.
                XmlSerializer serializer;
                serializer = PULL_PARSER_FACTORY.newSerializer();
                PrintWriter out = null;
                try{
                    out = getResponseWriter(request, response);
                    serializer.setOutput(out);
                    serializer.setProperty(INDENTATION_PROPERTY, INDENTATION_PROPERTY_VALUE);
                    serializer.startDocument(null, false);
                    serializer.text("\n");
                    serializer.docdecl(" DASDNA SYSTEM \"http://www.biodas.org/dtd/dasdna.dtd\"");
                    serializer.text("\n");
                    // Now the body of the DASDNA xml.
                    serializer.startTag (DAS_XML_NAMESPACE, "DASDNA");
                    for (SequenceReporter sequenceReporter : sequences){
                        serializer.startTag(DAS_XML_NAMESPACE, "SEQUENCE");
                        serializer.attribute(DAS_XML_NAMESPACE, "id", sequenceReporter.getSegmentName());
                        serializer.attribute(DAS_XML_NAMESPACE, "start", Integer.toString(sequenceReporter.getStart()));
                        serializer.attribute(DAS_XML_NAMESPACE, "stop", Integer.toString(sequenceReporter.getStop()));
                        serializer.attribute(DAS_XML_NAMESPACE, "version", sequenceReporter.getSequenceVersion());
                            serializer.startTag(DAS_XML_NAMESPACE, "DNA");
                            serializer.attribute(DAS_XML_NAMESPACE, "length", Integer.toString(sequenceReporter.getSequenceString().length()));
                            serializer.text(sequenceReporter.getSequenceString());
                            serializer.endTag(DAS_XML_NAMESPACE, "DNA");
                        serializer.endTag(DAS_XML_NAMESPACE, "SEQUENCE");
                    }
                    serializer.endTag (DAS_XML_NAMESPACE, "DASDNA");
                }
                finally{
                    if (out != null){
                        out.close();
                    }
                }
            } catch (CoordinateErrorException e) {
                logger.error("The requested coordinates were out of range", e);
                writeHeader(request, response, STATUS_405_COORDINATE_ERROR, false);
            } catch (BadReferenceObjectException e) {
                logger.error("At least one of the requested segments was not found by the dsn.", e);
                writeHeader(request, response, STATUS_403_BAD_REFERENCE_OBJECT, false);
            } catch (BadCommandArgumentsException e) {
                logger.error("The format of the command arguments was not recognised");
                writeHeader (request, response, STATUS_402_BAD_COMMAND_ARGUMENTS, false);
            }
        }
        else {
            // Not a reference source.
            logger.error ("Attempt to call the dna command on the " + dsnConfig.getId() + " dsn.  This is not a reference source.");
            writeHeader (request, response, STATUS_501_UNIMPLEMENTED_FEATURE, false);
        }
    }

    private void typesCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
            throws XmlPullParserException, IOException, DataSourceException{
    }

    private void stylesheetCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
            throws XmlPullParserException, IOException, DataSourceException{
    }

    private void featuresCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
            throws XmlPullParserException, IOException, DataSourceException{
    }

    private void entryPointsCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
            throws XmlPullParserException, IOException, DataSourceException{

        if (dsnConfig.getDataSource() instanceof ReferenceDataSource){
            // Fine - process command.
            ReferenceDataSource refDsn = (ReferenceDataSource) dsnConfig.getDataSource();
            Collection<DasEntryPoint> entryPoints = refDsn.getEntryPoints();
            // Check that an entry point version has been set.
            if (refDsn.getEntryPointVersion() == null){
                throw new DataSourceException("The dsn " + dsnConfig.getId() + "is returning null for the entry point version, which is invalid.");
            }
            // Looks like all is OK.
            writeHeader (request, response, STATUS_200_OK, true);
            //OK, got our entry points, so write out the XML.
            XmlSerializer serializer;
            serializer = PULL_PARSER_FACTORY.newSerializer();
            PrintWriter out = null;
            try{
                out = getResponseWriter(request, response);
                serializer.setOutput(out);
                serializer.setProperty(INDENTATION_PROPERTY, INDENTATION_PROPERTY_VALUE);
                serializer.startDocument(null, false);
                serializer.text("\n");
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
                        serializer.attribute(DAS_XML_NAMESPACE, "stop", Integer.toString(entryPoint.getEndCoordinate()));
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
            logger.error ("Attempt to call the entry_points command on the " + dsnConfig.getId() + " dsn.  This is not a reference source.");
            writeHeader (request, response, STATUS_501_UNIMPLEMENTED_FEATURE, false);
        }
    }



    private void sequenceCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
            throws XmlPullParserException, IOException, DataSourceException {
        // Is this a reference source?
        if (dsnConfig.getDataSource() instanceof ReferenceDataSource){
            // Fine - process command.
            try{
                Collection<SequenceReporter> sequences = getSequences(dsnConfig, queryString);
                // Got some sequences, so all is OK.
                writeHeader (request, response, STATUS_200_OK, true);
                // Build the XML.
                XmlSerializer serializer;
                serializer = PULL_PARSER_FACTORY.newSerializer();
                PrintWriter out = null;
                try{
                    out = getResponseWriter(request, response);
                    serializer.setOutput(out);
                    serializer.setProperty(INDENTATION_PROPERTY, INDENTATION_PROPERTY_VALUE);
                    serializer.startDocument(null, false);
                    serializer.text("\n");
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
            } catch (CoordinateErrorException e) {
                logger.error("The requested coordinates were out of range", e);
                writeHeader(request, response, STATUS_405_COORDINATE_ERROR, false);
            } catch (BadReferenceObjectException e) {
                logger.error("At least one of the requested segments was not found by the dsn.", e);
                writeHeader(request, response, STATUS_403_BAD_REFERENCE_OBJECT, false);
            } catch (BadCommandArgumentsException e) {
                logger.error("The format of the command arguments was not recognised");
                writeHeader (request, response, STATUS_402_BAD_COMMAND_ARGUMENTS, false);
            }
        }
        else {
            // Not a reference source.
            logger.error ("Attempt to call the dna command on the " + dsnConfig.getId() + " dsn.  This is not a reference source.");
            writeHeader (request, response, STATUS_501_UNIMPLEMENTED_FEATURE, false);
        }
    }


    /**
     * Helper method used by both the featuresCommand and typesCommand.
     * @param request
     * @param response
     * @param dsnConfig
     * @param queryString
     */
    private Collection<DasFeature> getFeatureCollection(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
            throws XmlPullParserException, IOException, DataSourceException{
        return Collections.EMPTY_LIST;
    }

    /**
     * Helper method used by both the dnaCommand and the sequenceCommand
     * @param dsnConfig
     * @param queryString
     */
    private Collection<SequenceReporter> getSequences(DataSourceConfiguration dsnConfig, String queryString)
            throws BadReferenceObjectException, CoordinateErrorException, DataSourceException, BadCommandArgumentsException {

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
                String segmentName = referenceStringMatcher.group(1);
                String startString = referenceStringMatcher.group(3);
                String stopString = referenceStringMatcher.group(4);
                Integer start = null;
                Integer stop = null;
                if (startString != null && stopString.length() > 0){
                    // Don't need to check stopString - the regex looks after that.
                    start = new Integer(startString);
                    stop = new Integer(stopString);
                }
                if (start != null){
                    // Getting a restricted sequenceString - and the data source will handle the restriction.
                    DasSequence sequence;
                    if (refDsn instanceof RangeHandlingReferenceDataSource){
                        sequence = ((RangeHandlingReferenceDataSource)refDsn).getSequence(segmentName, start, stop);
                    }
                    else {
                        sequence = refDsn.getSequence(segmentName);
                    }
                    if (sequence == null) throw new BadReferenceObjectException(segmentName, "Segment cannot be found.");
                    sequenceCollection.add (new SequenceReporter(sequence, start, stop));
                }
                else {
                    // Request for a complete sequenceString
                    DasSequence sequence = refDsn.getSequence(segmentName);
                    if (sequence == null) throw new BadReferenceObjectException(segmentName, "Segment cannot be found.");
                    sequenceCollection.add (new SequenceReporter(sequence));
                }
            }
            else {
                throw new BadCommandArgumentsException("The query string format is not recognized.");
            }
        }
        if (sequenceCollection.size() ==0){
            // The query string did not include any segment references.
            throw new BadCommandArgumentsException("The query string format is not recognized.");
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
    private void writeHeader (HttpServletRequest request, HttpServletResponse response, String status, boolean compressionAllowed){
        response.setHeader(HEADER_KEY_X_DAS_VERSION, HEADER_VALUE_DAS_VERSION);
        response.setHeader(HEADER_KEY_X_DAS_CAPABILITIES, HEADER_VALUE_CAPABILITIES);
        response.setHeader(HEADER_KEY_X_DAS_STATUS, status);
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
    private PrintWriter getResponseWriter (HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (compressResponse(request)){
            // Wrap the response writer in a Zipstream.
            GZIPOutputStream zipStream = new GZIPOutputStream(response.getOutputStream());
            return new PrintWriter(zipStream);
        }
        else {
            return response.getWriter();
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


    /*

    // Build the XML.
            XmlSerializer serializer;
            serializer = PULL_PARSER_FACTORY.newSerializer();
            PrintWriter out = null;
            try{
                out = getResponseWriter(request, response);
                serializer.setOutput(out);
                serializer.setProperty(INDENTATION_PROPERTY, INDENTATION_PROPERTY_VALUE);
                serializer.startDocument(null, false);
                serializer.text("\n");
                serializer.docdecl(" DASDSN SYSTEM \"http://www.biodas.org/dtd/dasdsn.dtd\"");
                serializer.text("\n");

                // Rest of the XML.

                serializer.flush();
            }
            finally{
                if (out != null){
                    out.close();
                }
            }

     */

}
