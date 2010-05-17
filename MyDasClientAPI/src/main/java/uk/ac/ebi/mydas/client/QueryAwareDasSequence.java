package uk.ac.ebi.mydas.client;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasSequence;

import java.util.regex.Matcher;

/**
 * This class acts as a DasSequence, but is also able to store details
 * of the URL from which the sequence originates.
 * @author Phil Jones
 * Date: 30-Jun-2008
 * Time: 15:19:56
 *
 */
public class QueryAwareDasSequence extends DasSequence {

    /**
     * The URL used to retrieve this DasAnnotatedSegment from a proxy DAS server.
     * (e.g. http://www.ebi.ac.uk/das-srv/uniprot/das/uniprot/features?segment=Q12345 )
     */
    private String queryURL;

    private String dsnName;

    /**
     * @param sequence        <b>Mandatory</b> sequence String used to populate the
     *                        /DASSEQUENCE/SEQUENCE element (sequence command) or the
     *                        /DASDNA/SEQUENCE/DNA element (dna command).
     * @param startCoordinate <b>Mandatory</b> start coordinate of the sequence.
     * @param version         <b>Mandatory</b> version of the sequence.  Typically may be
     *                        a date, version number or checksum.  Used to populate the
     *                        /DASSEQUENCE/SEQUENCE/@version attribute or the /DASDNA/SEQUENCE/@version
     *                        attribute.
     * @param label
     * @param segmentId       <b>Mandatory</b> id of the segment.
     *                        Representing the /DASSEQUENCE/SEQUENCE/@id attribute or the /DASDNA/SEQUENCE/@id
     *                        attribute.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          in the event that
     *          there is a problem with the information used to instantiate this object.
     */
    public QueryAwareDasSequence(String segmentId, String sequence, int startCoordinate, String version, String label) throws DataSourceException {
        super(segmentId, sequence, startCoordinate, version, label);
    }


    /**
     * @return The URL used to retrieve this DasAnnotatedSegment from a proxy DAS server.
     */
    public String getQueryURL() {
        return queryURL;
    }

    /**
     * @param queryURL The URL used to retrieve this DasAnnotatedSegment from a proxy DAS server.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          in the event of the URL being invalid.
     */
    public void setQueryURL(String queryURL) throws DataSourceException {
        this.queryURL = queryURL;
        Matcher dsnMatcher = RegexPatterns.FIND_DSN_NAME_PATTERN.matcher(queryURL);
        if (dsnMatcher.find()) {
            dsnName = dsnMatcher.group(1);
        } else {
            throw new DataSourceException("The queryURL: " + queryURL + " does not look like a valid DAS features query (or there is an error in the FIND_DSN_NAME_PATTERN regular expression in this class.)");
        }
    }

    public String getDsnName() {
        return dsnName;
    }
}
