package uk.ac.ebi.mydas.client.xml;

import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import uk.ac.ebi.mydas.client.QueryAwareDasAnnotatedSegment;
import uk.ac.ebi.mydas.client.RegexPatterns;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.extendedmodel.DasFeatureE;
import uk.ac.ebi.mydas.extendedmodel.DasMethodE;
import uk.ac.ebi.mydas.extendedmodel.DasTypeE;
import uk.ac.ebi.mydas.model.*;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.*;

/**

 * <p/>
 * Unmarshaller for DAS 1.53 Feature XML.
 * Unmarshalls the XML into the MyDas feature class structure.
 * <p/>
 * Format:
 * <p/>
 * <?xml version="1.0" standalone="no"?>
 * <!DOCTYPE DASGFF SYSTEM "http://www.biodas.org/dtd/dasgff.dtd">
 * <DASGFF>
 * <GFF version="1.0" href="url">
 * <SEGMENT id="id" start="start" stop="stop" type="type" version="X.XX" label="label">
 * <FEATURE id="id" label="label">
 * <TYPE id="id" category="category" reference="yes|no">type label</TYPE>
 * <METHOD id="id"> method label </METHOD>
 * <START> start </START>
 * <END> end </END>
 * <SCORE> [X.XX|-] </SCORE>
 * <ORIENTATION> [0|-|+] </ORIENTATION>
 * <PHASE> [0|1|2|-]</PHASE>
 * <NOTE> note text </NOTE>
 * <LINK href="url"> link text </LINK>
 * <TARGET id="id" start="x" stop="y">target name</TARGET>
 * <PARENT id=""/>
 * <PART id=""/>
 * </FEATURE>
 * ...
 * </SEGMENT>
 * </GFF>
 * </DASGFF>
 * @author Phil Jones
 * Date: 19-May-2008
 * Time: 15:51:42
 */
public class DasFeatureXmlUnmarshaller extends AbstractXmlUnmarshaller {
    /**
     * Define a static LOGGER variable so that it references the
     * Logger instance named "DasFeatureXmlUnmarshaller".
     */
    private static Logger LOGGER = Logger.getLogger(DasFeatureXmlUnmarshaller.class);

    /**
     * String defining the namespace for the feature XML.  At present there is no namespace,
     * so this is set to null.
     */
    private static final String NAMESPACE = null;
    private static final String ELEMENT_DASGFF = "DASGFF";
    private static final String ELEMENT_GFF = "GFF";
    private static final String ELEMENT_SEGMENT = "SEGMENT";
    private static final String ELEMENT_FEATURE = "FEATURE";
    private static final String ELEMENT_TYPE = "TYPE";
    private static final String ELEMENT_METHOD = "METHOD";
    private static final String ELEMENT_START = "START";
    private static final String ELEMENT_END = "END";
    private static final String ELEMENT_SCORE = "SCORE";
    private static final String ELEMENT_ORIENTATION = "ORIENTATION";
    private static final String ELEMENT_PHASE = "PHASE";
    private static final String ELEMENT_NOTE = "NOTE";
    private static final String ELEMENT_LINK = "LINK";
    private static final String ELEMENT_PARENT = "PARENT";
    private static final String ELEMENT_PART = "PART";

    private static final String ELEMENT_TARGET = "TARGET";
    private static final String ATTRIBUTE_VERSION = "version";
    private static final String ATTRIBUTE_HREF = "href";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_CVID = "cvId";
    private static final String ATTRIBUTE_START = "start";
    private static final String ATTRIBUTE_STOP = "stop";
    private static final String ATTRIBUTE_LABEL = "label";
    private static final String ATTRIBUTE_CATEGORY = "category";
//	private static final String ATTRIBUTE_REFERENCE = "reference";

    public DasFeatureXmlUnmarshaller() {
        if (FACTORY == null) {
            try {
                FACTORY = XmlPullParserFactory.newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
                FACTORY.setNamespaceAware(true);
            } catch (XmlPullParserException xppe) {
                LOGGER.warn(xppe.getMessage(), xppe);
            }
        }
    }

    /**
     * This method unmarshalls an XML document from the specified Reader into an MzData object.
     *
     * @param aReader Reader which reads from the XML file.
     * @return an MzData instance.
     * @throws java.io.IOException when the reading or parsing failed.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *                             if there is a problem building a valid DasAnnotatedSegment.
     */
    public Collection<QueryAwareDasAnnotatedSegment> unMarshall(Reader aReader) throws IOException, DataSourceException {
        Collection<QueryAwareDasAnnotatedSegment> dasAnnotatedSegments = null;
        try {

            XmlPullParser xpp = FACTORY.newPullParser();
            xpp.setInput(aReader);
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Document start encountered.");
                        }
                        eventType = xpp.next();
                        break;
                    case XmlPullParser.START_TAG:
                        String start = xpp.getName();
                        // Note that we check the version here.
                        if (ELEMENT_DASGFF.equals(start)) {
                            dasAnnotatedSegments = processDasGff(xpp);
                            eventType = xpp.getEventType();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Found end tag " + xpp.getName() + " in namespace " + xpp.getName() + ".");
                        }
                        eventType = xpp.next();
                        break;
                    case XmlPullParser.TEXT:
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Found text '" + xpp.getText().trim() + "'.");
                        }
                        eventType = xpp.next();
                        break;
                    default:
                        eventType = xpp.next();
                        break;
                }
            }
        } catch (XmlPullParserException xppe) {
            throw new IOException(xppe.getMessage());
        }
        return dasAnnotatedSegments;
    }

    private Collection<QueryAwareDasAnnotatedSegment> processDasGff(XmlPullParser xpp) throws XmlPullParserException, IOException, DataSourceException {
        Collection<QueryAwareDasAnnotatedSegment> annotatedSegments = new ArrayList<QueryAwareDasAnnotatedSegment>();

        if (xpp.isEmptyElementTag()) {
            throw new XmlPullParserException("There is no content in this DASGFF element.  This is an error.  Xml file line " + xpp.getLineNumber());
        }
        while (!(xpp.next() == XmlPullParser.END_TAG && ELEMENT_DASGFF.equals(xpp.getName()))) {
            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                if (ELEMENT_GFF.equals(xpp.getName())) {
                    // Process GFF element.
                    while (!(xpp.next() == XmlPullParser.END_TAG && ELEMENT_GFF.equals(xpp.getName()))) {
                        if (xpp.getEventType() == XmlPullParser.START_TAG) {
                            if (ELEMENT_SEGMENT.equals(xpp.getName())) {
                                // OK - process an element and add the resulting DasAnnotatedSegment to the Collection.
                                annotatedSegments.add(processSegment(xpp));
                            }
                        }
                    }
                }
            }
        }
        if (annotatedSegments.size() == 0) {
            throw new XmlPullParserException("No segments have been returned from this remote DAS service");
        }
        return annotatedSegments;
    }

    private QueryAwareDasAnnotatedSegment processSegment(XmlPullParser xpp) throws XmlPullParserException, IOException, DataSourceException {
        String segmentId = failIfEmptyTrimmedString(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_ID), xpp.getLineNumber(), "/DASGFF/GFF/SEGMENT/@id");
        Integer startCoordinate = parseStringToInt(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_START), true, null, xpp.getLineNumber(), "/DASGFF/GFF/SEGMENT/@start");
        Integer stopCoordinate = parseStringToInt(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_STOP), false, null, xpp.getLineNumber(), "/DASGFF/GFF/SEGMENT/@stop");
        String version = failIfEmptyTrimmedString(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_VERSION), xpp.getLineNumber(), "/DASGFF/GFF/SEGMENT/@version");
        String segmentLabel = trimmedStringOrNull(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_LABEL));
        Collection<DasFeature> features = new ArrayList<DasFeature>();

        // Now parse any enclosed features (Allowed to be empty!?)
        while (!(xpp.next() == XmlPullParser.END_TAG && ELEMENT_SEGMENT.equals(xpp.getName()))) {
            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                if (ELEMENT_FEATURE.equals(xpp.getName())) {
                    features.add(processFeature(xpp));
                }
            }
        }

        return new QueryAwareDasAnnotatedSegment(segmentId, startCoordinate, stopCoordinate, version, segmentLabel, features);
    }


    private DasFeature processFeature(XmlPullParser xpp) throws XmlPullParserException, DataSourceException, IOException {
        if (xpp.isEmptyElementTag()) {
            throw new XmlPullParserException("There is no content in this FEATURE element.  This is an error.  Xml file line " + xpp.getLineNumber());
        }

        // TODO - Need to handle the creation of DasComponentFeatures. Currently detecting it, but then not using the information for anything.
//		boolean isComponentFeature = false;
        String featureId = failIfEmptyTrimmedString(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_ID), xpp.getLineNumber(), "/DASGFF/GFF/SEGMENT/FEATURE/@id");
        String featureLabel = xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_LABEL);
        String typeId = null;
        String typeCategory = null;
        String typeLabel = null;
        String typeCvId = null;
        String methodId = null;
        String methodCvId = null;
        String methodLabel = null;
        int startCoordinate = 0;
        int endCoordinate = 0;
        Double score = null;
        DasFeatureOrientation orientation = DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE;
        DasPhase phase = DasPhase.PHASE_NOT_APPLICABLE;
        Collection<String> notes = new ArrayList<String>();
        // Using a LinkedHashMap, as it maintains the keys in the order in which they were added
        // (so should maintain the order of the links in the parsed XML).
        Map<URL, String> links = new LinkedHashMap<URL, String>();
        Collection<DasTarget> targets = new ArrayList<DasTarget>();
        Collection<String> parents = new ArrayList<String>();
        Collection<String> parts = new ArrayList<String>();
        // Parse the TYPE element
        while (!(xpp.next() == XmlPullParser.END_TAG && ELEMENT_FEATURE.equals(xpp.getName()))) {
            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                final String elemName = xpp.getName();
                if (ELEMENT_TYPE.equals(elemName)) {
                    typeId = failIfEmptyTrimmedString(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_ID), xpp.getLineNumber(), "/DASGFF/GFF/SEGMENT/FEATURE/TYPE/@id");
                    typeCategory = trimmedStringOrNull(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_CATEGORY));
                    typeCvId = trimmedStringOrNull(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_CVID));
//					isComponentFeature = "yes".equalsIgnoreCase(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_REFERENCE));
                    typeLabel = trimmedStringOrNull(getElementText(xpp, ELEMENT_TYPE));
                } else if (ELEMENT_METHOD.equals(elemName)) {
                    methodId = trimmedStringOrNull(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_ID));
                    methodCvId = trimmedStringOrNull(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_CVID));
                    methodLabel = trimmedStringOrNull(getElementText(xpp, ELEMENT_METHOD));
                } else if (ELEMENT_START.equals(elemName)) {
                    startCoordinate = parseStringToInt(getElementText(xpp, ELEMENT_START), true, 0, xpp.getLineNumber(), "/DASGFF/GFF/SEGMENT/FEATURE/START");
                } else if (ELEMENT_END.equals(elemName)) {
                    endCoordinate = parseStringToInt(getElementText(xpp, ELEMENT_END), true, 0, xpp.getLineNumber(), "/DASGFF/GFF/SEGMENT/FEATURE/END");
                } else if (ELEMENT_SCORE.equals(elemName)) {
                    String scoreAsString = trimmedStringOrNull(getElementText(xpp, ELEMENT_SCORE));
                    // If the score is anything other than a floating point value, set the score to null. (Will be displayed as a '-').
                    score = (RegexPatterns.FLOAT_PATTERN.matcher(scoreAsString).matches())
                            ? new Double(scoreAsString)
                            : null;
                } else if (ELEMENT_ORIENTATION.equals(elemName)) {
                    String orientationString = trimmedStringOrNull(getElementText(xpp, ELEMENT_ORIENTATION));
                    if (orientationString == null || orientationString.length() != 1) {
                        throw new XmlPullParserException("Found an empty <ORIENTATION/> element - not valid DASGFF at line " + xpp.getLineNumber());
                    }
                    switch (orientationString.charAt(0)) {
                        case '0':
                            orientation = DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE;
                            break;
                        case '+':
                            orientation = DasFeatureOrientation.ORIENTATION_SENSE_STRAND;
                            break;
                        case '-':
                            orientation = DasFeatureOrientation.ORIENTATION_ANTISENSE_STRAND;
                            break;
                        default:
                            throw new XmlPullParserException("Found an unexpected character '" + orientationString + " in an <ORIENTATION/> element - not valid DASGFF at line " + xpp.getLineNumber());
                    }
                } else if (ELEMENT_PHASE.equals(elemName)) {
                    String phaseString = trimmedStringOrNull(getElementText(xpp, ELEMENT_PHASE));
                    if (phaseString == null || phaseString.length() != 1) {
                        throw new XmlPullParserException("Found an empty <PHASE/> element - not valid DASGFF at line " + xpp.getLineNumber());
                    }
                    switch (phaseString.charAt(0)) {
                        case '-':
                            phase = DasPhase.PHASE_NOT_APPLICABLE;
                            break;
                        case '0':
                            phase = DasPhase.PHASE_READING_FRAME_0;
                            break;
                        case '1':
                            phase = DasPhase.PHASE_READING_FRAME_1;
                            break;
                        case '2':
                            phase = DasPhase.PHASE_READING_FRAME_2;
                            break;
                        default:
                            throw new XmlPullParserException("Unexpected character in <PHASE/> element - not valid DASGFF at line " + xpp.getLineNumber());
                    }
                } else if (ELEMENT_NOTE.equals(elemName)) {
                    final String noteText = getElementText(xpp, ELEMENT_NOTE);
                    if (noteText != null){
                        notes.add(noteText);
                    }
                } else if (ELEMENT_LINK.equals(elemName)) {
                    processLinkElement(xpp, links);
                } else if (ELEMENT_TARGET.equals(elemName)) {
                    processTargetElement(xpp, targets);
                } else if (ELEMENT_PARENT.equals(elemName)) {
                    final String id = trimmedStringOrNull(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_ID));
                    if (id != null) {
                        parents.add(id);
                    }
                } else if (ELEMENT_PART.equals(elemName)) {
                    final String id = trimmedStringOrNull(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_ID));
                    if (id != null) {
                        parts.add(id);
                    }
                }
            }
        }
        final DasType type = new DasTypeE(typeId, typeCategory, typeCvId, typeLabel);
        final DasMethod method = new DasMethodE(methodId, methodLabel, methodCvId);

        return new DasFeatureE(
                featureId,
                featureLabel,
                type,
                method,
                startCoordinate,
                endCoordinate,
                score,
                orientation,
                phase,
                notes,
                links,
                targets,
                parents,
                parts);
    }

    private void processLinkElement(XmlPullParser xpp, Map<URL, String> links) throws IOException, XmlPullParserException {
        String urlString = failIfEmptyTrimmedString(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_HREF), xpp.getLineNumber(), ".../LINK/@href");
        String humanReadable = getElementText(xpp, ELEMENT_LINK);
        if (humanReadable == null) {
            humanReadable = urlString;
        }
        links.put(new URL(urlString), humanReadable);
    }

    private void processTargetElement(XmlPullParser xpp, Collection<DasTarget> targets) throws XmlPullParserException, IOException, DataSourceException {
        String id = failIfEmptyTrimmedString(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_ID), xpp.getLineNumber(), "../TARGET/@id");
        String startString = failIfEmptyTrimmedString(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_START), xpp.getLineNumber(), "../TARGET/@start");
        String stopString = failIfEmptyTrimmedString(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_STOP), xpp.getLineNumber(), ".../TARGET/@stop");
        String targetName = getElementText(xpp, ELEMENT_TARGET);
        int start = parseStringToInt(startString, true, 0, xpp.getLineNumber(), "../TARGET/@start");
        int stop = parseStringToInt(stopString, true, 0, xpp.getLineNumber(), "../TARGET/@stop");
        targets.add(new DasTarget(id, start, stop, targetName));
	}
}
