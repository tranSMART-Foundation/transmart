package uk.ac.ebi.mydas.client.xml;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import uk.ac.ebi.mydas.client.RegexPatterns;

/**
 * Abstract class for parsing XML using PullParser API.
 * @author Phil Jones
 * Date: 07-Aug-2008
 * Time: 15:34:03
 */
public abstract class AbstractXmlUnmarshaller {


    /**
     * The Factory for the XmlPullParser. Note that it is static.
     */
    protected static XmlPullParserFactory FACTORY = null;

    protected String getElementText(XmlPullParser xpp, String elementName) throws XmlPullParserException, IOException {
        if (! xpp.isEmptyElementTag()){
            // Check for a label.
            while (! (xpp.next() == XmlPullParser.END_TAG && elementName.equals(xpp.getName()))) {
                if (xpp.getEventType() == XmlPullParser.TEXT) {
                    String currentText = xpp.getText();
                    if (currentText != null){
                        currentText = currentText.trim();
                        if (currentText.length() > 0) {
                            return currentText;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected Integer parseStringToInt(String intAsString, boolean mandatory, Integer defaultValue, int lineNumber, String xpath) throws XmlPullParserException {
        if (intAsString != null && intAsString.trim().length() > 0){
            intAsString = intAsString.trim();
            if (RegexPatterns.INTEGER_PATTERN.matcher(intAsString).matches()){
                return Integer.parseInt(intAsString);
            }
            else {
                throw new XmlPullParserException ("The String value '" + intAsString + "' should be an integer at line number "+ lineNumber + " in element / attribute " + xpath);
            }
        }
        else if (mandatory){
            throw new XmlPullParserException("An integer value is mandatory at line number " + lineNumber + " in element / attribute " + xpath);
        }
        else {
            return defaultValue;
        }
    }

    /**
     * Convenience method that throws a DataSourceException if the content being read is empty.
     * @param toCheck being the element / attribute content to check
     * @param lineNumber of the XML file being parsed
     * @param xpath of the element / attribute in question
     * @return the trimmed string, if it passes the test.
     * @throws org.xmlpull.v1.XmlPullParserException if mandatory XML content is missing.
     */
    protected String failIfEmptyTrimmedString (String toCheck, int lineNumber, String xpath) throws XmlPullParserException {
        toCheck = trimmedStringOrNull(toCheck);
        if (toCheck == null){
            throw new XmlPullParserException ("The mandatory content for " + xpath + " on line number " + lineNumber + " is empty or missing.");
        }
        return toCheck;
    }

    /**
     * Ensures that the String returned is either null, or a non-empty trimmed String.
     *
     * @param toCheck being the String to inspect
     * @return either null, or a non-empty trimmed String.
     */
    protected String trimmedStringOrNull (String toCheck){
        if (toCheck == null){
            return null;
        }
        toCheck = toCheck.trim();
        return (toCheck.length() == 0)
                ? null
                : toCheck;
    }
}
