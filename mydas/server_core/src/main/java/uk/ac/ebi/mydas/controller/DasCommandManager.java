package uk.ac.ebi.mydas.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

import uk.ac.ebi.mydas.configuration.DataSourceConfiguration;
import uk.ac.ebi.mydas.configuration.PropertyType;
import uk.ac.ebi.mydas.configuration.Mydasserver.Datasources.Datasource;
import uk.ac.ebi.mydas.configuration.Mydasserver.Datasources.Datasource.Version;
import uk.ac.ebi.mydas.configuration.Mydasserver.Datasources.Datasource.Version.Coordinates;
import uk.ac.ebi.mydas.configuration.Mydasserver.Datasources.Datasource.Version.Capability;
import uk.ac.ebi.mydas.datasource.AlignmentDataSource;
import uk.ac.ebi.mydas.datasource.AnnotationDataSource;
import uk.ac.ebi.mydas.datasource.CommandExtender;
import uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource;
import uk.ac.ebi.mydas.datasource.RangeHandlingReferenceDataSource;
import uk.ac.ebi.mydas.datasource.ReferenceDataSource;
import uk.ac.ebi.mydas.datasource.StructureDataSource;
import uk.ac.ebi.mydas.datasource.WritebackDataSource;
import uk.ac.ebi.mydas.exceptions.BadCommandArgumentsException;
import uk.ac.ebi.mydas.exceptions.BadCommandException;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.BadStylesheetException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;
import uk.ac.ebi.mydas.exceptions.WritebackException;
import uk.ac.ebi.mydas.extendedmodel.DasEntryPointE;
import uk.ac.ebi.mydas.extendedmodel.DasTypeE;
import uk.ac.ebi.mydas.extendedmodel.DasUnknownFeatureSegment;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.model.DasSequence;
import uk.ac.ebi.mydas.model.DasType;
import uk.ac.ebi.mydas.model.alignment.DasAlignment;
import uk.ac.ebi.mydas.model.structure.DasStructure;
import uk.ac.ebi.mydas.writeback.MyDasParser;

public class DasCommandManager {
	/**
	 * Define a static logger variable so that it references the
	 * Logger instance named "XMLUnmarshaller".
	 */
	private static final Logger logger = Logger.getLogger(DasCommandManager.class);

	private MydasServlet mydasServlet=null;

	private static DataSourceManager DATA_SOURCE_MANAGER = null;

	private static XmlPullParserFactory PULL_PARSER_FACTORY = null;
	static GeneralCacheAdministrator CACHE_MANAGER = null;


	private static final String DAS_XML_NAMESPACE = null;
	private static final String INDENTATION_PROPERTY = "http://xmlpull.org/v1/doc/properties.html#serializer-indentation";
	private static final String INDENTATION_PROPERTY_VALUE = "  ";

	/**
	 * Pattern used to parse a segment range, as used for the dna and sequenceString commands.
	 * This can be used based on the assumption that the segments have already been split
	 * into individual Strings (i.e. by splitting on the ; character).
	 * Three groups are returned from a match as follows:
	 * Group 1: segment name
	 * Group 3: start coordinate
	 * Group 4: stop coordinate
	 */
    //pattern can include negative numbers (since 1.6.1)
	private static final Pattern SEGMENT_RANGE_PATTERN = Pattern.compile ("^segment=([^:\\s]*)(:([-]?(\\d+)),([-]?(\\d+)))?$");
    private static final Pattern ROWS_RANGE_PATTERN = Pattern.compile ("^rows=([-]?(\\d+))-([-]?(\\d+))$");

	public DasCommandManager(DataSourceManager dsm, GeneralCacheAdministrator cache, MydasServlet mydasServlet){
		this.mydasServlet=mydasServlet;
		CACHE_MANAGER=cache;
		DATA_SOURCE_MANAGER=dsm;
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


	/**
	 * Implements the dsn command.  Only reports dsns that have initialised successfully.
	 * @param request to allow writing of the HTTP header
	 * @param response to which the HTTP header and DASDSN XML are written
	 * @param queryString to check no spurious arguments have been passed to the command
	 * @throws XmlPullParserException in the event of an error being thrown when writing out the XML
	 * @throws IOException in the event of an error being thrown when writing out the XML
	 */
	void dsnCommand(HttpServletRequest request, HttpServletResponse response, String queryString)
	throws XmlPullParserException, IOException{
		// Check the configuration has been loaded successfully
		if (DATA_SOURCE_MANAGER.getServerConfiguration() == null){
            //No configuration, just report the default capabilities
			writeHeader (request, response, XDasStatus.STATUS_500_SERVER_ERROR, false, null);
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
                //No dsn's, just report the default capabilities
				writeHeader (request, response, XDasStatus.STATUS_500_SERVER_ERROR, false, null);
				logger.error("The dsn command has been called, but no dsns have been initialised successfully.");
			} else{
				// At least one dsn is OK.
                //DSN is a server command, just report the default capabilities
				writeHeader (request, response, XDasStatus.STATUS_200_OK, true, null);
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
            //DSN is a server command, just report the default capabilities
			writeHeader (request, response, XDasStatus.STATUS_402_BAD_COMMAND_ARGUMENTS, true, null);
		}
	}
	void dnaCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
	throws 	XmlPullParserException, IOException, DataSourceException, UnimplementedFeatureException,
	BadReferenceObjectException, BadCommandArgumentsException, CoordinateErrorException {
		//	Is the dna command enabled?
		if (dsnConfig.isDnaCommandEnabled()){
			// Is this a reference source?
			if (dsnConfig.getDataSource() instanceof ReferenceDataSource){
				// All good - process command.
				Collection<SequenceReporter> sequences = getSequences(dsnConfig, queryString, true);
				// Got some sequences, so all is OK.
				writeHeader (request, response, XDasStatus.STATUS_200_OK, true,dsnConfig.getCapabilities());
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
                        if (sequenceReporter instanceof FoundSequenceReporter) {
						    FoundSequenceReporter foundSequenceReporter = (FoundSequenceReporter)sequenceReporter;
                            foundSequenceReporter.serialize(DAS_XML_NAMESPACE,serializer,true);
                        } else if (sequenceReporter instanceof ErrorSequenceReporter) {
						    ErrorSequenceReporter errorSequenceReporter = (ErrorSequenceReporter)sequenceReporter;
                            errorSequenceReporter.serialize(DAS_XML_NAMESPACE,serializer);
                        }
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

	/**
	 * Helper method used by both the dnaCommand and the sequenceCommand
	 *
     * DAS1.6: The exception for a bad reference object (reference sequence unknown) [deprecated in favour of Exception Handling]
     *
	 * @param dsnConfig holding configuration of the dsn and the data source object itself.
	 * @param queryString to be parsed, which includes details of the requested segments
     * @param unknownSegmentsHandled Indicates whether or not the unknown segment is handled
	 * @return a Collection of SequenceReporter objects.  The SequenceReporter wraps the DasSequence object
	 * to provide additional functionality that is hidden (for simplicity) from the dsn developer.
	 * @throws CoordinateErrorException if the requested coordinates fall outside those of the requested segment id
	 * @throws DataSourceException to capture any error returned from the data source.
	 * @throws BadCommandArgumentsException if the arguments to the command are not recognised.
     * @throws BadReferenceObjectException if the segments to the command are not recognised.
	 */
	private Collection<SequenceReporter> getSequences(DataSourceConfiguration dsnConfig, String queryString, boolean unknownSegmentsHandled) throws DataSourceException, BadCommandArgumentsException, BadReferenceObjectException, CoordinateErrorException {
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
                try {
                    DasSequence sequence;

                    // Build the key name for the cache.
                    StringBuffer cacheKeyBuffer = new StringBuffer(dsnConfig.getId());
                    cacheKeyBuffer.append("_SEQUENCE_");
                    if (refDsn instanceof RangeHandlingReferenceDataSource){
                        // May return DasSequence objects containing partial sequences, so include segment id, start and stop coordinates in the key:
                        cacheKeyBuffer.append(segmentQuery.toString());
                    } else {
                        // Otherwise will only return complete sequences, so store on segment id only:
                        cacheKeyBuffer.append(segmentQuery.getSegmentId());
                    }
                    String cacheKey = cacheKeyBuffer.toString();

                    try{
                        // flushCache checks with the data source if the cache needs emptying, and does so if required.
                        sequence = (DasSequence) CACHE_MANAGER.getFromCache(cacheKey);
                        if (logger.isDebugEnabled()) {
                            logger.debug("SEQUENCE RETRIEVED FROM CACHE: " + sequence.getSegmentId());
                        }
                        // Belt and braces - the various getSequence methods throw BadReferenceObjectException -
                        // but just in case the dsn
                        // fails to throw this appropriately and instead return a null sequence object...
                        if (sequence == null) {
                            throw new BadReferenceObjectException(segmentQuery.getSegmentId(), "Segment cannot be found.");
                        } else {
                            //Segment exist and was retrieved from cache
                            //If segment query start < sequence start or  query stop > sequence stop an ERRORSEGMENT should be reported (since 1.6.1)
                            if ((segmentQuery.getStartCoordinate() != null) && (segmentQuery.getStopCoordinate() != null)) {
                                boolean error = false;
                                if ((segmentQuery.getStartCoordinate() != null) && (segmentQuery.getStopCoordinate() != null)) {
                                    if ( (segmentQuery.getStartCoordinate() <= 0) || (segmentQuery.getStopCoordinate() <= 0) ) {
                                        //0 or negative values in range are not allowed: ERROR
                                        error = true;
                                    } else if (segmentQuery.getStartCoordinate() > segmentQuery.getStopCoordinate()) {
                                        //start cannot be greater that stop: ERROR
                                        error = true;
                                    } else if ( ( (sequence.getStartCoordinate() <= segmentQuery.getStartCoordinate()) &&
                                                  (segmentQuery.getStartCoordinate() <= sequence.getStopCoordinate()) )
                                               && (sequence.getStartCoordinate() <= segmentQuery.getStopCoordinate()) )  {
                                        //start is completely bounded, stop is greater or equal to real init: OK
                                        error = false;
                                    } else {
                                        error = true;
                                    }
                                }
                                if (error) {
                                    if (logger.isDebugEnabled()){
                                        logger.debug("SEGMENT START & STOP OUT OF BOUNDS: " +
                                            "query(" + segmentQuery.getStartCoordinate() + ", " + segmentQuery.getStopCoordinate() + ") " +
                                            "vs bounds(" + sequence.getStartCoordinate() + ", " + sequence.getStopCoordinate() + ")");
                                    }
                                    throw new BadReferenceObjectException(segmentQuery.getSegmentId(), "start and stop out of segment bounds", new IndexOutOfBoundsException("start and stop out of segment bounds"));
                                }
                            }
                        }
                    } catch (NeedsRefreshException nre){
                        try{
                            if (segmentQuery.getStartCoordinate() == null){
                                // Request for a complete sequenceString
                                sequence = refDsn.getSequence(segmentQuery.getSegmentId());
                            } else {
                                // Getting a restricted sequenceString - and the data source will handle the restriction.
                                if (refDsn instanceof RangeHandlingReferenceDataSource){
                                    sequence = ((RangeHandlingReferenceDataSource)refDsn).getSequence(
                                            segmentQuery.getSegmentId(),
                                            segmentQuery.getStartCoordinate(),
                                            segmentQuery.getStopCoordinate()
                                    );
                                } else {
                                    sequence = refDsn.getSequence(segmentQuery.getSegmentId());
                                }
                            }
                            if (logger.isDebugEnabled()){
                                logger.debug("FEATURES NOT IN CACHE: " + sequence.getSegmentId());
                            }
                            // These putInCache calls include a group, being the dsn name to allow a DSN to
                            // remove all cached data if it requires.
                            CACHE_MANAGER.putInCache(cacheKey, sequence, dsnConfig.getCacheGroup());
                            //If segment query start < sequence start or  query stop > sequence stop an ERRORSEGMENT should be reported (since 1.6.1)
                            if ((segmentQuery.getStartCoordinate() != null) && (segmentQuery.getStopCoordinate() != null)) {
                                boolean error = false;
                                if ((segmentQuery.getStartCoordinate() != null) && (segmentQuery.getStopCoordinate() != null)) {
                                    if ( (segmentQuery.getStartCoordinate() <= 0) || (segmentQuery.getStopCoordinate() <= 0) ) {
                                        //0 or negative values in range are not allowed: ERROR
                                        error = true;
                                    } else if (segmentQuery.getStartCoordinate() > segmentQuery.getStopCoordinate()) {
                                        //start cannot be greater that stop: ERROR
                                        error = true;
                                    } else if ( ( (sequence.getStartCoordinate() <= segmentQuery.getStartCoordinate()) &&
                                                  (segmentQuery.getStartCoordinate() <= sequence.getStopCoordinate()) )
                                               && (sequence.getStartCoordinate() <= segmentQuery.getStopCoordinate()) )  {
                                        //start is completely bounded, stop is greater or equal to real init: OK
                                        error = false;
                                    } else {
                                        error = true;
                                    }
                                }
                                if (error) {
                                    if (logger.isDebugEnabled()){
                                        logger.debug("SEGMENT START & STOP OUT OF BOUNDS: " +
                                            "query(" + segmentQuery.getStartCoordinate() + ", " + segmentQuery.getStopCoordinate() + ") " +
                                            "vs bounds(" + sequence.getStartCoordinate() + ", " + sequence.getStopCoordinate() + ")");
                                    }
                                    throw new BadReferenceObjectException(segmentQuery.getSegmentId(), "start and stop out of segment bounds", new IndexOutOfBoundsException("start and stop out of segment bounds"));
                                }
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

                    sequenceCollection.add (new FoundSequenceReporter(sequence, segmentQuery));
                } catch (BadReferenceObjectException broe) {
                    if (unknownSegmentsHandled){ //Sequences are handled only by reference servers, so report an ERRORSEGEMENT
                        sequenceCollection.add(new ErrorSequenceReporter(segmentQuery));
                    }
                    else {
                        throw broe;
                    }
                } catch (CoordinateErrorException cee) {
                    if (unknownSegmentsHandled){  //Sequences are handled only by reference servers, so report an ERRORSEGEMENT
                        sequenceCollection.add(new ErrorSequenceReporter(segmentQuery));
                    }
                    else {
                        throw cee;
                    }
                } catch (DataSourceException dse) {
                    throw dse;
                }
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

	@SuppressWarnings("unchecked")
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
					.append ("_CVID_")
					.append ((type.getCvId() == null ) ? "null" : type.getCvId());
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

		writeHeader (request, response, XDasStatus.STATUS_200_OK, true,dsnConfig.getCapabilities());
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
//			serializer.docdecl(" DASTYPES SYSTEM \"http://www.biodas.org/dtd/dastypes.dtd\"");
//			serializer.text("\n");
			// Now the body of the DASTYPES xml.
			serializer.startTag (DAS_XML_NAMESPACE, "DASTYPES");
			serializer.startTag (DAS_XML_NAMESPACE, "GFF");
			serializer.attribute(DAS_XML_NAMESPACE, "version", "1.0");
			serializer.attribute(DAS_XML_NAMESPACE, "href", this.buildRequestHref(request));
			serializer.startTag(DAS_XML_NAMESPACE, "SEGMENT");
			// No id, start, stop, type attributes.
			//serializer.attribute(DAS_XML_NAMESPACE, "version", dsnConfig.getVersion());
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
        Map <SegmentReporter, Map<DasType, Integer>> typesReport =
			new HashMap<SegmentReporter, Map<DasType, Integer>>(requestedSegments.size());
		// For each segment, populate the typesReport with 'all types' if necessary and then add types and counts.
        // Always handle error/unknown segments: (since 1.6)
		Collection<SegmentReporter> segmentReporters = this.getFeatureCollection(dsnConfig, requestedSegments, true, null);
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
                //Since 1.6.1 overlapping features are always retrieved
				for (DasFeature feature : segmentReporter.getFeatures()){
					// (Filtering as requested for type ids)
					if (typeFilter.size() == 0 || typeFilter.contains(feature.getType().getId())){
						DasType featureType = feature.getType();
						if (segmentTypes.keySet().contains(featureType)){
							segmentTypes.put(featureType, segmentTypes.get(featureType) + 1);
						}
						else {
							segmentTypes.put(featureType, 1);
						}
					}
				}
			} else if (uncastReporter instanceof UnknownSegmentReporter) {
                UnknownSegmentReporter segmentReporter = (UnknownSegmentReporter) uncastReporter;
                Map<DasType, Integer> segmentTypes = new HashMap<DasType, Integer>();
				// Add these objects to the typesReport.
				typesReport.put(segmentReporter, segmentTypes);
            }  else if (uncastReporter instanceof ErrorSegmentReporter) {
                ErrorSegmentReporter segmentReporter = (ErrorSegmentReporter) uncastReporter;
                Map<DasType, Integer> segmentTypes = new HashMap<DasType, Integer>();
				// Add these objects to the typesReport.
				typesReport.put(segmentReporter, segmentTypes);
            }
			// Finished with actual features
			/////////////////////////////////////////////////////////////////////////////////////////////
		}

		// OK, successfully built a Map of the types for all the requested segments, so iterate over this and report.
		writeHeader (request, response, XDasStatus.STATUS_200_OK, true, dsnConfig.getCapabilities());
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
			for (SegmentReporter segmentReporter : typesReport.keySet()){
                if (segmentReporter instanceof UnknownSegmentReporter){
                    boolean referenceSource = dsnConfig.getDataSource() instanceof ReferenceDataSource;
					((UnknownSegmentReporter)segmentReporter).serialize(DAS_XML_NAMESPACE, serializer, referenceSource);
                } else if (segmentReporter instanceof ErrorSegmentReporter){ //since 1.6.1
					((ErrorSegmentReporter)segmentReporter).serialize(DAS_XML_NAMESPACE, serializer);
                } else if (segmentReporter instanceof FoundFeaturesReporter){
                    FoundFeaturesReporter featureReporter = (FoundFeaturesReporter) segmentReporter;
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

	/**
	 * Given that this command just return a copy of a predefined stylesheet, there is nothing to modify for DAS1.6
	 * @param request HTTP request
	 * @param response HTTP response
	 * @param dsnConfig Datasource configuration
	 * @param queryString Query string
	 * @throws BadCommandArgumentsException
	 * @throws IOException
	 * @throws BadStylesheetException
	 */
	void stylesheetCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
	throws BadCommandArgumentsException, IOException, BadStylesheetException {
//		Check the queryString is empty (as it should be).
		if (queryString != null && queryString.trim().length() > 0){
			throw new BadCommandArgumentsException("Arguments have been passed to the stylesheet command, which does not expect any.");
		}
//		Get the name of the stylesheet.
		String stylesheetFileName;
		if (dsnConfig.getStyleSheet() != null && dsnConfig.getStyleSheet().trim().length() > 0){
			stylesheetFileName = dsnConfig.getStyleSheet().trim();
		}
//		These next lines look like potential null-pointer hell - but note that this has been checked robustly in the
//		calling method, so all OK.
		else if (DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getDefaultStyleSheet() != null
				&& DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getDefaultStyleSheet().trim().length() > 0){
			stylesheetFileName = DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getDefaultStyleSheet().trim();
		}
		else {
			throw new BadStylesheetException("This data source has not defined a stylesheet.");
		}

//		Need to create a FileReader to read in the stylesheet, wrapped by a PrintStream to stream it out to the browser.
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try{
			reader = new BufferedReader(
					new InputStreamReader (
							this.mydasServlet.getServletContext().getResourceAsStream(MydasServlet.RESOURCE_FOLDER + stylesheetFileName)
					)
			);

			if (reader.ready()){
				//OK, managed to open an input reader from the stylesheet, so output the success header.
				writeHeader (request, response, XDasStatus.STATUS_200_OK, true,dsnConfig.getCapabilities());
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
	 * can throw this exception under some circumstances (but not when called by the featureCommand method!)
	 * @throws CoordinateErrorException will not be thrown, but a helper method used by this method
	 * can throw this exception under some circumstances (but not when called by the featureCommand method!)
	 */
	@SuppressWarnings("unchecked")
	void featuresCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
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
		Integer maxbins=null;
		for (String queryPart : queryParts){
			// Now determine what each part is, and construct the query.
			Matcher segmentRangeMatcher = SEGMENT_RANGE_PATTERN.matcher(queryPart);
			if (segmentRangeMatcher.find()){
				requestedSegments.add (new SegmentQuery (segmentRangeMatcher));
			} else{
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
				// else check for maxbean restriction
				else if ("maxbins".equals (key)){
					try{
						maxbins = new Integer(value);
					}catch(NumberFormatException nfe){
						throw new BadCommandArgumentsException("Bad command arguments to the features command. the maxbeans attribute should be Numeric: " + queryString);
					}
				}
				// DAS1.6: The groups have been eliminated, and with it the filter by group
				//else check for groupId restriction
//				else if ("group_id".equals (key)){
//					filter.addGroupId(value);
//				}
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
			segmentReporterCollections = getFeatureCollection(dsnConfig, requestedSegments, true, maxbins);
		} else {
			// No segments have been requested, so instead check for either feature_id or group_id filters.
			// (If neither of these are present, then throw a BadCommandArgumentsException)
			if (filter.containsFeatureIds() ){
				Collection<DasAnnotatedSegment> annotatedSegments =
					dsnConfig.getDataSource().getFeatures(filter.getFeatureIds(),maxbins);
				if (annotatedSegments != null){
					segmentReporterCollections = new ArrayList<SegmentReporter>(annotatedSegments.size());
					for (DasAnnotatedSegment segment : annotatedSegments){
						if (segment instanceof DasUnknownFeatureSegment){
							segmentReporterCollections.add (new UnknownFeatureSegmentReporter(segment.getSegmentId()));
						} else
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
		writeHeader (request, response, XDasStatus.STATUS_200_OK, true,dsnConfig.getCapabilities());

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
//			serializer.docdecl(" DASGFF SYSTEM \"http://www.biodas.org/dtd/dasgff.dtd\"");
//			serializer.text("\n");

			// Rest of the XML.
			serializer.startTag(DAS_XML_NAMESPACE, "DASGFF");
			serializer.startTag(DAS_XML_NAMESPACE, "GFF");
			//the version has been deprecated from
			serializer.attribute(DAS_XML_NAMESPACE, "href", buildRequestHref(request));
			for (SegmentReporter segmentReporter : segmentReporterCollections){
				if (segmentReporter instanceof UnknownSegmentReporter){
					((UnknownSegmentReporter)segmentReporter).serialize(DAS_XML_NAMESPACE, serializer, referenceSource);
				} else if (segmentReporter instanceof ErrorSegmentReporter){ //since 1.6.1
					((ErrorSegmentReporter)segmentReporter).serialize(DAS_XML_NAMESPACE, serializer);
                } else if (segmentReporter instanceof UnknownFeatureSegmentReporter){
					((UnknownFeatureSegmentReporter)segmentReporter).serialize(DAS_XML_NAMESPACE, serializer);
				} else {
                    //Overlaps are always allowed (since 1.6.1, according to DAS spec 1.6, draft 6)
                    //featuresStrictlyEnclosed set to false means that overlaps are allowed
                    ((FoundFeaturesReporter) segmentReporter).serialize(DAS_XML_NAMESPACE, serializer, filter, categorize, false, dsnConfig.isUseFeatureIdForFeatureLabel());
					//((FoundFeaturesReporter) segmentReporter).serialize(DAS_XML_NAMESPACE, serializer, filter, categorize, dsnConfig.isFeaturesStrictlyEnclosed(), dsnConfig.isUseFeatureIdForFeatureLabel());
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
	void linkCommand(HttpServletResponse response, DataSourceConfiguration dataSourceConfig, String queryString)
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
	 * Helper method used by both the featuresCommand and typesCommand to return a Collection of SegmentReporter objects.
	 *
	 * The SegmentReporter interface is implemented to allow both correctly returned segments and missing segments
	 * to be returned.
	 * @param dsnConfig holding configuration of the dsn and the data source object itself.
	 * @param requestedSegments being a List of SegmentQuery objects, which encapsulate the segment request (including
	 * the segment id and optional start / stop coordinates)
	 * @param unknownSegmentsHandled to indicate if the calling method is able to report missing segments (i.e.
	 * the feature command can return errorsegment / unknownsegment).
	 * the segment id is not known to the DSN.
     * @param maxbins
	 * @return a Collection of FeatureReporter objects that wrap the DasFeature objects returned from the data source
	 * @throws uk.ac.ebi.mydas.exceptions.DataSourceException to capture any error returned from the data source that cannot be handled in a more
	 * elegant manner.
	 * @throws uk.ac.ebi.mydas.exceptions.CoordinateErrorException thrown if unknownSegmentsHandled is false and
	 * the segment coordinates are out of scope for the provided segment id.
     * @throws uk.ac.ebi.mydas.exceptions.BadReferenceObjectException
	 */
	private Collection<SegmentReporter> getFeatureCollection(DataSourceConfiguration dsnConfig,
			List <SegmentQuery> requestedSegments,
			boolean unknownSegmentsHandled,Integer maxbins
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
				if (maxbins!=null) {
					cacheKeyBuffer.append("_MAXBEANS_"+maxbins+"_");
                }
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
					} else {
                        //Segment exist and was retrieved from cache
                        //If segment query start and stop are completely out of limits an ERRORSEGMENT should be reported (since 1.6.1)
                        boolean error = false;
                        if ((segmentQuery.getStartCoordinate() != null) && (segmentQuery.getStopCoordinate() != null)) {
                            if ( (segmentQuery.getStartCoordinate() <= 0) || (segmentQuery.getStopCoordinate() <= 0) ) {
                                //0 or negative values in range are not allowed: ERROR
                                error = true;
                            } else if (segmentQuery.getStartCoordinate() > segmentQuery.getStopCoordinate()) {
                                //start cannot be greater that stop: ERROR
                                error = true;
                            } else if ( ( (annotatedSegment.getStartCoordinate() <= segmentQuery.getStartCoordinate()) &&
                                          (segmentQuery.getStartCoordinate() <= annotatedSegment.getStopCoordinate()) )
                                       && (annotatedSegment.getStartCoordinate() <= segmentQuery.getStopCoordinate()) )  {
                                //start is completely bounded, stop is greater or equal to real init: OK
                                error = false;
                            } else {
                                error = true;
                            }
                        }
                        if (error) {
                            if (logger.isDebugEnabled()){
                                logger.debug("SEGMENT START & STOP OUT OF BOUNDS: " +
                                    "query(" + segmentQuery.getStartCoordinate() + ", " + segmentQuery.getStopCoordinate() + ") " +
                                    "vs bounds(" + annotatedSegment.getStartCoordinate() + ", " + annotatedSegment.getStopCoordinate() + ")");
                            }
                            throw new BadReferenceObjectException(segmentQuery.getSegmentId(), "start and stop out of segment bounds", new IndexOutOfBoundsException("start and stop out of segment bounds"));
                        }
                    }
				}
				catch (NeedsRefreshException nre){
					try{
						if (segmentQuery.getStartCoordinate() == null){
							// Easy request - just want all the features on the segment.
							annotatedSegment = dataSource.getFeatures(segmentQuery.getSegmentId(),maxbins);
						}
						else {

							// Restricted to coordinates.
							if (dataSource instanceof RangeHandlingAnnotationDataSource){
								annotatedSegment = ((RangeHandlingAnnotationDataSource)dataSource).getFeatures(
										segmentQuery.getSegmentId(),
										segmentQuery.getStartCoordinate(),
										segmentQuery.getStopCoordinate(),
										maxbins);
							}
							else if (dataSource instanceof RangeHandlingReferenceDataSource){
								annotatedSegment = ((RangeHandlingReferenceDataSource)dataSource).getFeatures(
										segmentQuery.getSegmentId(),
										segmentQuery.getStartCoordinate(),
										segmentQuery.getStopCoordinate(),
										maxbins);
							}
							else {
								annotatedSegment = dataSource.getFeatures(
										segmentQuery.getSegmentId(),
										maxbins);
							}
						}
                        if (logger.isDebugEnabled()){
							logger.debug("FEATURES NOT IN CACHE: " + annotatedSegment.getSegmentId());
						}
						CACHE_MANAGER.putInCache(cacheKey, annotatedSegment, dsnConfig.getCacheGroup());
                        //If segment query start and stop are completely out of limits an ERRORSEGMENT should be reported (since 1.6.1)
                        boolean error = false;
                        if ((segmentQuery.getStartCoordinate() != null) && (segmentQuery.getStopCoordinate() != null)) {
                            if ( (segmentQuery.getStartCoordinate() <= 0) || (segmentQuery.getStopCoordinate() <= 0) ) {
                                //0 or negative values in range are not allowed: ERROR
                                error = true;
                            } else if (segmentQuery.getStartCoordinate() > segmentQuery.getStopCoordinate()) {
                                //start cannot be greater that stop: ERROR
                                error = true;
                            } else if ( ( (annotatedSegment.getStartCoordinate() <= segmentQuery.getStartCoordinate()) &&
                                          (segmentQuery.getStartCoordinate() <= annotatedSegment.getStopCoordinate()) )
                                       && (annotatedSegment.getStartCoordinate() <= segmentQuery.getStopCoordinate()) )  {
                                //start is completely bounded, stop is greater or equal to real init: OK
                                error = false;
                            } else {
                                error = true;
                            }
                        }
                        if (error) {
                            if (logger.isDebugEnabled()){
                                logger.debug("SEGMENT START & STOP OUT OF BOUNDS: " +
                                    "query(" + segmentQuery.getStartCoordinate() + ", " + segmentQuery.getStopCoordinate() + ") " +
                                    "vs bounds(" + annotatedSegment.getStartCoordinate() + ", " + annotatedSegment.getStopCoordinate() + ")");
                            }
                            throw new BadReferenceObjectException(segmentQuery.getSegmentId(), "start and stop out of segment bounds", new IndexOutOfBoundsException("start and stop out of segment bounds"));
                        }
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
                    if (broe.getCause() != null) { //For both annotation and reference servers, limits out of bounds should report an ERRORSEGEMENT (since 1.6.1)
                        segmentReporterLists.add(new ErrorSegmentReporter(segmentQuery));
                    } else {
                        segmentReporterLists.add(new UnknownSegmentReporter(segmentQuery));
                    }
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
		if (this.mydasServlet.compressResponse(request)){
			// Wrap the response writer in a Zipstream.
			GZIPOutputStream zipStream = new GZIPOutputStream(response.getOutputStream());
			return new BufferedWriter (new PrintWriter(zipStream));
		} else {
			return new BufferedWriter (response.getWriter());
		}
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
	private void writeHeader (HttpServletRequest request, HttpServletResponse response, XDasStatus status, boolean compressionAllowed,String capabilities) throws IOException{
		this.mydasServlet.writeHeader(request, response, status, compressionAllowed,capabilities);
	}



//DAS 1.6 commands:

	/**
	 * Implements the source command.  Only reports sources that have initialized successfully.
	 * @param request to allow writing of the HTTP header
	 * @param response to which the HTTP header and DASDSN XML are written
	 * @param queryString to check no spurious arguments have been passed to the command
	 * @throws XmlPullParserException in the event of an error being thrown when writing out the XML
	 * @throws IOException in the event of an error being thrown when writing out the XML
	 */
	void sourceCommand(HttpServletRequest request, HttpServletResponse response, String queryString, String source)
	throws XmlPullParserException, IOException{
		// Check the configuration has been loaded successfully
		if (DATA_SOURCE_MANAGER.getServerConfiguration() == null){
            //No configuration, just report the default capabilities
			writeHeader (request, response, XDasStatus.STATUS_500_SERVER_ERROR, false,null);
			logger.error("A request has been made to the das server, however initialisation failed - possibly the mydasserverconfig.xml file was not found.");
			return;
		}
		// All fine.
		// Get the list of dsn from the DataSourceManager
		List<String> dsns = DATA_SOURCE_MANAGER.getServerConfiguration().getDsnNames();
		// Check there is at least one dsn.  (Mandatory in the dsn XML output).
		if (dsns == null || dsns.size() == 0){
            //No dsn's, just report the default capabilities
			writeHeader (request, response, XDasStatus.STATUS_500_SERVER_ERROR, false,null);
			logger.error("The source command has been called, but no sources have been initialised successfully.");
		} else{
			// At least one dsn is OK.
            if (source == null) {
                //server sources command, just report the default capabilities
                writeHeader (request, response, XDasStatus.STATUS_200_OK, true,null);
            } else {
                //datasource sources command, report capabilities
                DataSourceConfiguration dataSourceConfig = DATA_SOURCE_MANAGER.getServerConfiguration().getDataSourceConfigMap().get(source);
                writeHeader (request, response, XDasStatus.STATUS_200_OK, true, dataSourceConfig.getCapabilities());
            }
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
				if (DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getSourcesXSLT() != null){
					serializer.processingInstruction(DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getSourcesXSLT());
					serializer.text("\n");
				}
				serializer.startTag (DAS_XML_NAMESPACE, "SOURCES");
				List<String> versionsadded= new ArrayList<String>();

				for (String dsn : dsns){
					if (source==null || source.equals(dsn)){
						if (!versionsadded.contains(dsn)){
							Datasource dsnConfig2 = DATA_SOURCE_MANAGER.getServerConfiguration().getDataSourceConfig(dsn).getConfig();
							serializer.startTag (DAS_XML_NAMESPACE, "SOURCE");
							serializer.attribute(DAS_XML_NAMESPACE, "uri", dsnConfig2.getUri());
							if (dsnConfig2.getDocHref() != null && dsnConfig2.getDocHref().length() > 0){
								serializer.attribute(DAS_XML_NAMESPACE, "doc_href", dsnConfig2.getDocHref());
							}
							serializer.attribute(DAS_XML_NAMESPACE, "title", dsnConfig2.getTitle());
							serializer.attribute(DAS_XML_NAMESPACE, "description", dsnConfig2.getDescription());

							serializer.startTag (DAS_XML_NAMESPACE, "MAINTAINER");
							serializer.attribute(DAS_XML_NAMESPACE, "email", dsnConfig2.getMaintainer().getEmail());
							serializer.endTag (DAS_XML_NAMESPACE, "MAINTAINER");

							for(Version version:dsnConfig2.getVersion()){
								versionsadded.add(version.getUri());
								serializer.startTag (DAS_XML_NAMESPACE, "VERSION");
								serializer.attribute(DAS_XML_NAMESPACE, "uri", version.getUri());
								serializer.attribute(DAS_XML_NAMESPACE, "created", version.getCreated().toString());
								for(Coordinates coordinates:version.getCoordinates()){
									serializer.startTag (DAS_XML_NAMESPACE, "COORDINATES");
									serializer.attribute(DAS_XML_NAMESPACE, "uri", coordinates.getUri());
									serializer.attribute(DAS_XML_NAMESPACE, "source", coordinates.getSource());
									serializer.attribute(DAS_XML_NAMESPACE, "authority", coordinates.getAuthority());
									if ( (coordinates.getTaxid()!=null) && (coordinates.getTaxid().length()>0) )
										serializer.attribute(DAS_XML_NAMESPACE, "taxid", coordinates.getTaxid());
									if ( (coordinates.getVersion()!=null) && (coordinates.getVersion().length()>0) )
										serializer.attribute(DAS_XML_NAMESPACE, "version", coordinates.getVersion());
									serializer.attribute(DAS_XML_NAMESPACE, "test_range", coordinates.getTestRange());
									serializer.text(coordinates.getValue());
									serializer.endTag (DAS_XML_NAMESPACE, "COORDINATES");
								}
								for(Capability capability:version.getCapability()){
									serializer.startTag (DAS_XML_NAMESPACE, "CAPABILITY");
									serializer.attribute(DAS_XML_NAMESPACE, "type", capability.getType());
									if ( (capability.getQueryUri()!=null) && (capability.getQueryUri().length()>0) )
										serializer.attribute(DAS_XML_NAMESPACE, "query_uri", capability.getQueryUri());
									serializer.endTag (DAS_XML_NAMESPACE, "CAPABILITY");
								}
                                //1.6.1 Properties come from version and are not allowed in data sources (not out of the version anyway)
                                //1.61. Only properties with visibility true will be reported in source command response
                                for(PropertyType pt:version.getProperty()){
                                    if (pt.isVisibility()) {
                                        serializer.startTag (DAS_XML_NAMESPACE, "PROPERTY");
                                        serializer.attribute(DAS_XML_NAMESPACE, "name", pt.getKey());
                                        serializer.attribute(DAS_XML_NAMESPACE, "value", pt.getValue());
                                        serializer.endTag (DAS_XML_NAMESPACE, "PROPERTY");
                                    }
                                }
								serializer.endTag (DAS_XML_NAMESPACE, "VERSION");
							}

							serializer.endTag (DAS_XML_NAMESPACE, "SOURCE");
						}
					}
				}
				serializer.endTag (DAS_XML_NAMESPACE, "SOURCES");
				serializer.flush();
			}
			finally{
				if (out != null){
					out.close();
				}
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
	void entryPointsCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
	throws XmlPullParserException, IOException, DataSourceException, UnimplementedFeatureException, BadCommandArgumentsException {

		Integer start = null;
		Integer stop = null;
        Integer expectedSize = null;
		if (queryString != null && queryString.trim().length() > 0){
            Matcher rowsRangeMatcher = ROWS_RANGE_PATTERN.matcher(queryString);
			if (rowsRangeMatcher.find()){
                start = new Integer(rowsRangeMatcher.group(1));
                stop = new Integer(rowsRangeMatcher.group(3));
			}  else {
                throw new BadCommandArgumentsException("Unexpected arguments have been passed to the entry_points command.");
            }
            if ( (start <= 0) || (stop <= 0) || (stop < start) ) {
                throw new BadCommandArgumentsException("Unexpected arguments(stop lower than start or negative values) have been passed to the entry_points command.");
            }
            expectedSize = stop - start + 1;
            /*
			String[] queryParts = queryString.split("=");
			if (!queryParts[0].equals("rows"))
				throw new BadCommandArgumentsException("Unexpected arguments have been passed to the entry_points command.");
			String[] rows = queryParts[1].split("-");
			try{
				start=Integer.parseInt(rows[0]);
			} catch (NumberFormatException nfe){
				throw new BadCommandArgumentsException("Unexpected arguments(not numeric) have been passed to the entry_points command.");
			}
			try{
				stop=Integer.parseInt(rows[1]);
			} catch (NumberFormatException nfe){
				throw new BadCommandArgumentsException("Unexpected arguments(not numeric) have been passed to the entry_points command.");
			}
			if (stop<start)
				throw new BadCommandArgumentsException("Unexpected arguments(stop lower than start) have been passed to the entry_points command.");
            */
		}

		if (dsnConfig.getDataSource() instanceof AnnotationDataSource){
			// Fine - process command.
			AnnotationDataSource refDsn = dsnConfig.getDataSource();
            //If a client requests an invalid range of rows (completely beyond the range offered by the server)
            //the server responds with an X-DAS-Status of 402: BadCommandArgumentsException
            start = start == null ? 1 : start;
            stop = stop == null ? refDsn.getTotalEntryPoints() : stop;
            expectedSize = stop - start + 1;
            if ( start > refDsn.getTotalEntryPoints() ) {
                throw new BadCommandArgumentsException("Unexpected arguments (both start ans stop out of bounds) have been passed to the entry_points command.");
            }
            //Reference data sources return only valid entry points from start to stop (since 1.6.1)
			Collection<DasEntryPoint> entryPoints = refDsn.getEntryPoints(start, stop);
			// Check that an entry point version has been set.
			if (refDsn.getEntryPointVersion() == null){
				throw new DataSourceException("The dsn " + dsnConfig.getId() + "is returning null for the entry point version, which is invalid.");
			}
			// Looks like all is OK.
			writeHeader (request, response, XDasStatus.STATUS_200_OK, true, dsnConfig.getCapabilities());
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
//				serializer.docdecl(" DASEP SYSTEM \"http://www.biodas.org/dtd/dasep.dtd\"");
//				serializer.text("\n");

				// Rest of the XML.
				serializer.startTag(DAS_XML_NAMESPACE, "DASEP");
				serializer.startTag(DAS_XML_NAMESPACE, "ENTRY_POINTS");
				serializer.attribute(DAS_XML_NAMESPACE, "href", buildRequestHref(request));
				if (refDsn.getEntryPointVersion()!=null)
					serializer.attribute(DAS_XML_NAMESPACE, "version", refDsn.getEntryPointVersion());
				serializer.attribute(DAS_XML_NAMESPACE, "total", "" + refDsn.getTotalEntryPoints());

				if (start != null) {
					serializer.attribute(DAS_XML_NAMESPACE, "start", ""+start);
                } else {
					start = 1;
                    serializer.attribute(DAS_XML_NAMESPACE, "start", ""+start);
                }

				if (stop != null) {
                    if (expectedSize == entryPoints.size()) { //From start to stop was returned, check that it is in accordance to max_entry_points
                        if (dsnConfig.getMaxEntryPoints() != null) {
                            if (dsnConfig.getMaxEntryPoints() < entryPoints.size()) {
                                stop = start + dsnConfig.getMaxEntryPoints() - 1;
                            }
                        }
                    } else { //Less elements were returned, could be because less elements actually exist or because of max_entry_points
                        if (dsnConfig.getMaxEntryPoints() != null) {
                            if (dsnConfig.getMaxEntryPoints() < entryPoints.size()) {
                                stop = start + dsnConfig.getMaxEntryPoints() - 1;
                            } else if (entryPoints.size() == 0) {
                                //Both start ans stop where out of limits, do not change the stop variable
                            } else {
                                stop = start + entryPoints.size() - 1;
                            }
                        } else {
                            if (entryPoints.size() != 0) {
                                stop = start + entryPoints.size() - 1;
                            }
                        }
                    }
					serializer.attribute(DAS_XML_NAMESPACE, "end", ""+stop);
                } else {
                    stop = dsnConfig.getMaxEntryPoints() == null ? entryPoints.size() : Math.min(dsnConfig.getMaxEntryPoints(), entryPoints.size());
                    serializer.attribute(DAS_XML_NAMESPACE, "end", ""+stop);
                }

				Iterator<DasEntryPoint> iterator = entryPoints.iterator();
				/*for (int i=1; i<start ;i++)
					iterator.next();*/
				//DasEntryPoint[] entryPointsA = (DasEntryPoint[]) entryPoints.toArray();
				// Now for the individual segments.
				for (int i = start; (i <= stop) && (iterator.hasNext()); i++){
                    DasEntryPoint entryPoint=iterator.next();
					if (entryPoint != null){
						(new DasEntryPointE(entryPoint)).serialize(DAS_XML_NAMESPACE, serializer);
					}
				}
				serializer.endTag(DAS_XML_NAMESPACE, "ENTRY_POINTS");
				/* RelaxNG schema for entry_points does not include any QUERY element (since 1.6.1)
				serializer.startTag(DAS_XML_NAMESPACE, "QUERY");
				serializer.text(""+queryString);
				serializer.endTag(DAS_XML_NAMESPACE, "QUERY");
                 */
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
			throw new UnimplementedFeatureException("An attempt to request entry_point information from an non-annotation server has been detected.");
		}
	}

	/**
	 * Implements the sequence command.  Delegates to the getSequences method to return the requested sequences.
	 *
     * DAS1.6: The exception for a bad reference object (reference sequence unknown) [deprecated in favour of Exception Handling]
     *
	 * @param request to allow the writing of the http header
	 * @param response to which the http header and the XML are written.
	 * @param dsnConfig holding configuration of the dsn and the data source object itself.
	 * @param queryString from which the requested segments are parsed.
	 * @throws XmlPullParserException in the event of a problem with writing out the DASSEQUENCE XML file.
	 * @throws IOException during writing of the XML
	 * @throws DataSourceException to capture any error returned from the data source.
	 * @throws UnimplementedFeatureException if the dsn reports that it cannot return sequence.
	 * @throws BadCommandArgumentsException if the arguments to the sequence command are not as specified in the
	 * DAS 1.53 specification
	 * @throws CoordinateErrorException if the requested coordinates are outside those of the segment id requested.
	 */
	void sequenceCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
			throws XmlPullParserException, IOException, DataSourceException, UnimplementedFeatureException, BadReferenceObjectException, BadCommandArgumentsException, CoordinateErrorException {

		// Is this a reference source?
		if (dsnConfig.getDataSource() instanceof ReferenceDataSource){
			// Fine - process command.
            //Always handle unknown segments (since 1.6.1)
			Collection<SequenceReporter> sequences = getSequences(dsnConfig, queryString, true);
			// Got some sequences, so all is OK.
			writeHeader (request, response, XDasStatus.STATUS_200_OK, true,dsnConfig.getCapabilities());
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

				// Now the body of the DASDNA xml.
				serializer.startTag (DAS_XML_NAMESPACE, "DASSEQUENCE");
				for (SequenceReporter sequenceReporter : sequences){
                    if (sequenceReporter instanceof FoundSequenceReporter) {
                        FoundSequenceReporter foundSequenceReporter = (FoundSequenceReporter) sequenceReporter;
                        foundSequenceReporter.serialize(DAS_XML_NAMESPACE,serializer);
                    } else if (sequenceReporter instanceof ErrorSequenceReporter) {
                        ErrorSequenceReporter errorSequenceReporter = (ErrorSequenceReporter) sequenceReporter;
                        errorSequenceReporter.serialize(DAS_XML_NAMESPACE,serializer);
                    }
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

	void typesCommand(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dsnConfig, String queryString)
	throws BadCommandArgumentsException, BadReferenceObjectException, DataSourceException, CoordinateErrorException, IOException, XmlPullParserException {
//		Parse the queryString to retrieve the individual parts of the query.

		List<SegmentQuery> requestedSegments = new ArrayList<SegmentQuery>();
		List<String> typeFilter = new ArrayList<String>();
		/************************************************************************\
		 * Parse the query string                                               *
		 ************************************************************************/
//		It is legal for the query string to be empty for the types command.
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
						throw new BadCommandArgumentsException("Bad command arguments to the types command: " + queryString);
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

	/**
	 * Implements the structure command.  Delegates to the getStructure method to return the requested structure.
	 *
     * DAS1.6: The exception for a bad reference object (reference sequence unknown) [deprecated in favour of Exception Handling]
     *
	 * @param request to allow the writing of the http header
	 * @param response to which the http header and the XML are written.
	 * @param dsnConfig holding configuration of the dsn and the data source object itself.
	 * @param queryString from which the requested structures are parsed.
	 * @throws XmlPullParserException in the event of a problem with writing out the DASSEQUENCE XML file.
	 * @throws IOException during writing of the XML
	 * @throws DataSourceException to capture any error returned from the data source.
	 * @throws UnimplementedFeatureException if the dsn reports that it cannot return sequence.
	 * @throws BadCommandArgumentsException if the arguments to the sequence command are not as specified in the
	 * DAS 1.6 specification
	 * @throws CoordinateErrorException if the requested coordinates are outside those of the segment id requested.
	 */
	void structureCommand(HttpServletRequest request,
			HttpServletResponse response,
			DataSourceConfiguration dsnConfig, String queryString) throws DataSourceException, UnimplementedFeatureException, BadCommandArgumentsException, XmlPullParserException, IOException, BadReferenceObjectException {
		// Is this a structure source?
		if (dsnConfig.getDataSource() instanceof StructureDataSource){
			List<String> chains = null;
			List<String> models = new ArrayList<String>();
			String query =null;
			if (queryString != null && queryString.length() > 0){
				//divide the query String
				String[] queryParts = queryString.split(";");
				for (String queryPart : queryParts){
					String[] queryPartKeysValues = queryPart.split("=");
					if (queryPartKeysValues.length != 2){
						// All of the remaining query parts are key=value pairs, so this is a bad argument.
						throw new BadCommandArgumentsException("Bad command arguments to the structure command: " + queryString);
					}
					String key = queryPartKeysValues[0];
					String value = queryPartKeysValues[1];

					//Structure to query
					if ("query".equals (key)){
						query = value;
					//Filter by Chain
					}else if ("chain".equals (key)){
						if (chains==null)
							chains = new ArrayList<String>();
						chains.add(value);
					//Filter by Model
					}else if ("model".equals (key)){
						if (models==null)
							models = new ArrayList<String>();
						models.add(value);
					}
				}
				//query is mandatory
				if (query==null)
					throw new BadCommandArgumentsException("Bad command arguments - missing the query argument of the command structure: " + queryString);
				// Looks like all is OK.
				writeHeader (request, response, XDasStatus.STATUS_200_OK, true,dsnConfig.getCapabilities());

				XmlSerializer serializer;
				serializer = PULL_PARSER_FACTORY.newSerializer();
				BufferedWriter out = null;
				DasStructure structure = ((StructureDataSource)dsnConfig.getDataSource()).getStructure(query, chains, models);
				try{
					out = getResponseWriter(request, response);
					serializer.setOutput(out);
					serializer.setProperty(INDENTATION_PROPERTY, INDENTATION_PROPERTY_VALUE);
					serializer.startDocument(null, false);
					serializer.text("\n");
					structure.serialize(DAS_XML_NAMESPACE, serializer);
					serializer.flush();
				}
				finally{
					if (out != null){
						out.close();
					}
				}

			}

		} else {
			// Not an structure source.
			throw new UnimplementedFeatureException("An attempt to request structure information from a non-structure server has been detected.");
		}
	}
	/**
	 * Implements the alignment command.  Delegates to the getAlignment method to return the requested alignment.
	 *
	 * @param request to allow the writing of the http header
	 * @param response to which the http header and the XML are written.
	 * @param dsnConfig holding configuration of the dsn and the data source object itself.
	 * @param queryString from which the requested structures are parsed.
	 * @throws XmlPullParserException in the event of a problem with writing out the DASSEQUENCE XML file.
	 * @throws IOException during writing of the XML
	 * @throws DataSourceException to capture any error returned from the data source.
	 * @throws UnimplementedFeatureException if the dsn reports that it cannot return sequence.
	 * @throws BadCommandArgumentsException if the arguments to the sequence command are not as specified in the
	 * DAS 1.6 specification
	 * @throws CoordinateErrorException if the requested coordinates are outside those of the segment id requested.
	 */
	void alignmentCommand(HttpServletRequest request,
			HttpServletResponse response,
			DataSourceConfiguration dsnConfig, String queryString) throws DataSourceException, UnimplementedFeatureException, BadCommandArgumentsException, XmlPullParserException, IOException, BadReferenceObjectException {
		// Is this an alignment source?
		if (dsnConfig.getDataSource() instanceof AlignmentDataSource){
			List<String> subjects = null;
			String query =null;
			String subjectcoordsys =null;
			Integer rowStart =null;
			Integer rowEnd =null;
			Integer colStart =null;
			Integer colEnd =null;
			if (queryString != null && queryString.length() > 0){
				//divide the query String
				String[] queryParts = queryString.split(";");
				for (String queryPart : queryParts){
					String[] queryPartKeysValues = queryPart.split("=");
					if (queryPartKeysValues.length != 2){
						// All of the remaining query parts are key=value pairs, so this is a bad argument.
						throw new BadCommandArgumentsException("Bad command arguments to the alignment command: " + queryString);
					}
					String key = queryPartKeysValues[0];
					String value = queryPartKeysValues[1];

					//Structure to query
					if ("query".equals (key)){
						query = value;
					//Filter by Chain
					}else if ("subject".equals (key)){
						if (subjects==null)
							subjects = new ArrayList<String>();
						subjects.add(value);
					//Filter by Model
					}else if ("subjectcoordsys".equals (key)){
						subjectcoordsys=value;
					}else if ("rows".equals (key)){
						String[] rowParts = queryString.split("-");
						if (rowParts.length != 2)
							throw new BadCommandArgumentsException("Bad command arguments to the alignment command: " + queryString);
						try {
							rowStart = new Integer(rowParts[0]);
							rowEnd = new Integer(rowParts[1]);
						} catch(NumberFormatException nfe){
							throw new BadCommandArgumentsException("Bad command arguments to the alignment command: " + queryString,nfe);
						}
					}else if ("cols".equals (key)){
						String[] colParts = queryString.split("-");
						if (colParts.length != 2)
							throw new BadCommandArgumentsException("Bad command arguments to the alignment command: " + queryString);
						try {
							colStart = new Integer(colParts[0]);
							colEnd = new Integer(colParts[1]);
						} catch(NumberFormatException nfe){
							throw new BadCommandArgumentsException("Bad command arguments to the alignment command: " + queryString,nfe);
						}
					}
				}
				//query is mandatory
				if (query==null)
					throw new BadCommandArgumentsException("Bad command arguments - missing the query argument of the command alignment: " + queryString);
				// Looks like all is OK.
				writeHeader (request, response, XDasStatus.STATUS_200_OK, true,dsnConfig.getCapabilities());

				XmlSerializer serializer;
				serializer = PULL_PARSER_FACTORY.newSerializer();
				BufferedWriter out = null;
				DasAlignment alignment = ((AlignmentDataSource)dsnConfig.getDataSource()).getAlignment(query, subjects, subjectcoordsys, rowStart, rowEnd, colStart, colEnd);
				try{
					out = getResponseWriter(request, response);
					serializer.setOutput(out);
					serializer.setProperty(INDENTATION_PROPERTY, INDENTATION_PROPERTY_VALUE);
					serializer.startDocument(null, false);
					serializer.text("\n");
					alignment.serialize(DAS_XML_NAMESPACE, serializer);
					serializer.flush();
				}
				finally{
					if (out != null){
						out.close();
					}
				}

			}

		} else {
			// Not an alignment source.
			throw new UnimplementedFeatureException("An attempt to request structure information from a non-structure server has been detected.");
		}
	}

	/**
	 * In case of a data source has implemented the CommandExtender Interface. It invokes its method to execute commands out of the spec.
	 * Otherwise a BadCommandException is thrown.
	 *
	 * @param request to allow the writing of the http header
	 * @param response to which the http header and the XML are written.
	 * @param dataSourceConfig holding configuration of the dsn and the data source object itself.
	 * @param queryString from which the requested structures are parsed.
	 * @throws BadCommandException in the case the interface Command extender haven't beeen implemented or
	 */
	public void otherCommand(	HttpServletRequest request,
								HttpServletResponse response,
								DataSourceConfiguration dataSourceConfig,
								String command,
								String queryString)
			throws BadCommandException {

		try {
			if (dataSourceConfig.getDataSource() instanceof CommandExtender){
				((CommandExtender)dataSourceConfig.getDataSource()).executeOtherCommand(request, response, dataSourceConfig, command, queryString);
			}else{
				throw new BadCommandException("The command is not recognised.");
			}
		} catch (DataSourceException e) {
			throw new BadCommandException("Data source impossible to recover for extended commands",e);
		}

	}
	public void writebackCreate(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dataSourceConfig) throws WritebackException {

				MyDasParser parser= new MyDasParser(PULL_PARSER_FACTORY);
				DasAnnotatedSegment segment=parser.parse2MyDasModel(request.getParameter("_content"));
				try {
					DasAnnotatedSegment segmentRes=((WritebackDataSource)dataSourceConfig.getDataSource()).create(segment);
					serialize(request,response,dataSourceConfig,segmentRes);
				} catch (DataSourceException e) {
					throw new WritebackException("ERROR creating the feature",e);
				} catch (IllegalArgumentException e) {
					throw new WritebackException("ERROR creating the XML response",e);
				} catch (IllegalStateException e) {
					throw new WritebackException("ERROR creating the XML response",e);
				} catch (XmlPullParserException e) {
					throw new WritebackException("ERROR creating the XML response",e);
				} catch (IOException e) {
					throw new WritebackException("ERROR creating the XML response",e);
				}

	}

	private void serialize(HttpServletRequest request, HttpServletResponse response, DataSourceConfiguration dataSourceConfig,DasAnnotatedSegment segment) throws IllegalArgumentException, IllegalStateException, DataSourceException, XmlPullParserException, IOException{
			if (dataSourceConfig.getDataSource() instanceof WritebackDataSource){
				if (PULL_PARSER_FACTORY == null) {
					try {
						PULL_PARSER_FACTORY = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
						PULL_PARSER_FACTORY.setNamespaceAware(true);
					} catch (XmlPullParserException xppe) {
						logger.error("Fatal Exception thrown at initialisation.  Cannot initialise the PullParserFactory required to allow generation of the DAS XML.", xppe);
						throw new IllegalStateException ("Fatal Exception thrown at initialisation.  Cannot initialise the PullParserFactory required to allow generation of the DAS XML.", xppe);
					}
				}

				List<SegmentReporter> segmentReporterLists = new ArrayList<SegmentReporter>();
				segmentReporterLists.add(new FoundFeaturesReporter(segment));

				XmlSerializer serializer;
				serializer = PULL_PARSER_FACTORY.newSerializer();
				BufferedWriter out = null;
				try {
					out = getResponseWriter(request, response);
					serializer.setOutput(out);
					serializer.setProperty(INDENTATION_PROPERTY, INDENTATION_PROPERTY_VALUE);
					serializer.startDocument(null, false);
					serializer.text("\n");
					if (DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getFeaturesXSLT() != null){
						serializer.processingInstruction(DATA_SOURCE_MANAGER.getServerConfiguration().getGlobalConfiguration().getFeaturesXSLT());
						serializer.text("\n");
					}

					// Rest of the XML.
					serializer.startTag(DAS_XML_NAMESPACE, "DASGFF");
					serializer.startTag(DAS_XML_NAMESPACE, "GFF");
					serializer.attribute(DAS_XML_NAMESPACE, "href", buildRequestHref(request));
					for (SegmentReporter segmentReporter : segmentReporterLists){
						if (segmentReporter instanceof UnknownSegmentReporter){
							((UnknownSegmentReporter)segmentReporter).serialize(DAS_XML_NAMESPACE, serializer, false);
						} else if (segmentReporter instanceof UnknownFeatureSegmentReporter){
							((UnknownFeatureSegmentReporter)segmentReporter).serialize(DAS_XML_NAMESPACE, serializer);
						} else {
							((FoundFeaturesReporter) segmentReporter).serialize(DAS_XML_NAMESPACE, serializer, new DasFeatureRequestFilter (), true,true, dataSourceConfig.isUseFeatureIdForFeatureLabel());
						}
					}
					serializer.endTag(DAS_XML_NAMESPACE, "GFF");
					serializer.endTag(DAS_XML_NAMESPACE, "DASGFF");

					serializer.flush();
				}finally{
					if (out != null){
						out.close();
					}
				}

			}
	}
	public void writebackDelete(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub

	}
	public void writebackupdate(HttpServletRequest request,	HttpServletResponse response, DataSourceConfiguration dataSourceConfig) throws WritebackException {

		MyDasParser parser= new MyDasParser(PULL_PARSER_FACTORY);
		BufferedReader reader;
		String content="",temp="";
		try {
			reader = request.getReader();
			while ((temp = reader.readLine()) != null) {
				content+=temp;
			}
		} catch (IOException e) {
			throw new WritebackException("ERROR reading the PUT content",e);
		}
		DasAnnotatedSegment segment=parser.parse2MyDasModel(content);
		try {
			DasAnnotatedSegment segmentRes=((WritebackDataSource)dataSourceConfig.getDataSource()).update(segment);
			serialize(request,response,dataSourceConfig,segmentRes);
		} catch (DataSourceException e) {
			throw new WritebackException("ERROR updating the feature",e);
		} catch (IllegalArgumentException e) {
			throw new WritebackException("ERROR updating the XML response",e);
		} catch (IllegalStateException e) {
			throw new WritebackException("ERROR updating the XML response",e);
		} catch (XmlPullParserException e) {
			throw new WritebackException("ERROR updating the XML response",e);
		} catch (IOException e) {
			throw new WritebackException("ERROR updating the XML response",e);
		}

	}




}
