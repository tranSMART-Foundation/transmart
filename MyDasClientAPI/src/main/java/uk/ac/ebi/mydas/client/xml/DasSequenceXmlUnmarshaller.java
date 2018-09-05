package uk.ac.ebi.mydas.client.xml;

import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import uk.ac.ebi.mydas.client.QueryAwareDasSequence;
import uk.ac.ebi.mydas.exceptions.DataSourceException;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * <p/>
 * Unmarshaller for DAS 1.6 Sequence XML.
 * Unmarshalls the XML into the MyDas sequence class structure.
 * <p/>
 * Format:
 * <p/>
 * <?xml version="1.0" standalone="no"?>
 * <!DOCTYPE DASSEQUENCE SYSTEM "http://www.biodas.org/dtd/dassequence.dtd">
 * <DASSEQUENCE>
 * <SEQUENCE id="id" start="start" stop="stop"
 * label="alternative_id" version="X.XX">
 * atttcttggcgtaaataagagtctcaatgagactctcagaagaaaattgataaatattat
 * </SEQUENCE>
 * </DASSEQUENCE>
 *
 * @author Phil Jones
 * Date: 19-May-2008
 * Time: 15:51:42
 *
 */
public class DasSequenceXmlUnmarshaller extends AbstractXmlUnmarshaller {
    /**
     * Define a static LOGGER variable so that it references the
     * Logger instance named "DasSequenceXmlUnmarshaller".
     */
    private static Logger LOGGER = Logger.getLogger(DasSequenceXmlUnmarshaller.class);


    /**
     * String defining the namespace for the feature XML.  At present there is no namespace,
     * so this is set to null.
     */
    private static final String NAMESPACE = null;
    private static final String ELEMENT_DASSEQUENCE = "DASSEQUENCE";
    private static final String ELEMENT_SEQUENCE = "SEQUENCE";
    private static final String ATTRIBUTE_VERSION = "version";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_START = "start";
    private static final String ATTRIBUTE_LABEL = "label";

    public DasSequenceXmlUnmarshaller() {
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
     * This method unmarshalls an XML document from the specified Reader into a Collection of
     * QueryAwareDasSequence objects.
     *
     * @param aReader Reader which reads from the XML file.
     * @return a Collection of DasSequence objects, holding the details of the sequence.
     * @throws java.io.IOException when the reading or parsing failed.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *                             if there is a problem building a valid DasAnnotatedSegment.
     */
    public Collection<QueryAwareDasSequence> unMarshall(Reader aReader) throws IOException, DataSourceException {
        Collection<QueryAwareDasSequence> dasSequences = null;
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
                        if (ELEMENT_DASSEQUENCE.equals(start)) {
                            dasSequences = processDasSequence(xpp);
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
        return dasSequences;
    }

    private Collection<QueryAwareDasSequence> processDasSequence(XmlPullParser xpp) throws XmlPullParserException, IOException, DataSourceException {
        Collection<QueryAwareDasSequence> sequences = new ArrayList<QueryAwareDasSequence>();

        if (xpp.isEmptyElementTag()) {
            throw new XmlPullParserException("There is no content in this DASSEQUENCE element.  This is an error.  Xml file line " + xpp.getLineNumber());
        }
        while (!(xpp.next() == XmlPullParser.END_TAG && ELEMENT_DASSEQUENCE.equals(xpp.getName()))) {
            if (xpp.getEventType() == XmlPullParser.START_TAG) {
                if (ELEMENT_SEQUENCE.equals(xpp.getName())) {
                    sequences.add(processSequence(xpp));
                }
            }
        }
        if (sequences.size() == 0) {
            throw new XmlPullParserException("No sequences have been returned from this remote DAS service");
        }
        return sequences;
    }

    private QueryAwareDasSequence processSequence(XmlPullParser xpp) throws XmlPullParserException, IOException, DataSourceException {
        String segmentId = failIfEmptyTrimmedString(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_ID), xpp.getLineNumber(), "/DASSEQUENCE/SEQUENCE/@id");
        Integer startCoordinate = parseStringToInt(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_START), true, null, xpp.getLineNumber(), "/DASSEQUENCE/SEQUENCE/@start");
        String version = failIfEmptyTrimmedString(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_VERSION), xpp.getLineNumber(), "/DASSEQUENCE/SEQUENCE/@version");
        String label = trimmedStringOrNull(xpp.getAttributeValue(NAMESPACE, ATTRIBUTE_LABEL));
        String sequence = getElementText(xpp, ELEMENT_SEQUENCE);

        return new QueryAwareDasSequence(segmentId, sequence, startCoordinate, version, label);
	}
}
