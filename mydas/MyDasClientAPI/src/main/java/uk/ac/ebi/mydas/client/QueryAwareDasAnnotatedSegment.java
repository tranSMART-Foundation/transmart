package uk.ac.ebi.mydas.client;

import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasSequence;
import uk.ac.ebi.mydas.model.DasFeature;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.client.RegexPatterns;

import java.util.Collection;
import java.util.regex.Matcher;

/**
 * This class acts as a DasAnnotatedSegment, but is also able to store details
 * of the URL from which the segment originates.
 * @author Phil Jones
 * Date: 30-Jun-2008
 * Time: 15:19:56
 *
 */
public class QueryAwareDasAnnotatedSegment extends DasAnnotatedSegment {

    /**
     * The URL used to retrieve this DasAnnotatedSegment from a proxy DAS server.
     * (e.g. http://www.ebi.ac.uk/das-srv/uniprot/das/uniprot/features?segment=Q12345 )
     */
    private String queryURL;

    private String dsnName;

    public QueryAwareDasAnnotatedSegment(String segmentId, Integer startCoordinate, Integer stopCoordinate, String version, String segmentLabel, Collection<DasFeature> features)
            throws DataSourceException {
        super(segmentId, startCoordinate, stopCoordinate, version, segmentLabel, features);
    }

    /**
     * Convenience method - if you are creating a DasAnnotatedSegment, but already have a DasSequence object for the
     * same segment, you can use the sequence to build the DasAnnotatedSegment easily.
     *
     * @param sequence     being a valid DasSequence object that represents the same segment.
     * @param segmentLabel <b>Optional.</b> A human readable label for the segment.  If this is not given (null or
     *                     empty string) the segment ID will be used in its place.
     * @param features     being a Collection of zero or more {@link uk.ac.ebi.mydas.model.DasFeature} objects.  Each of these objects describes a single
     *                     feature.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          to allow you to handle problems with the data source, such as SQLExceptions,
     *          parsing errors etc.
     */
    public QueryAwareDasAnnotatedSegment(DasSequence sequence, String segmentLabel, Collection<DasFeature> features) throws DataSourceException {
        super(sequence, segmentLabel, features);
    }

    /**
     * @return The URL used to retrieve this DasAnnotatedSegment from a proxy DAS server.
     */
    public String getQueryURL() {
        return queryURL;
    }

    /**
     * @param queryURL The URL used to retrieve this DasAnnotatedSegment from a proxy DAS server.
     */
    public void setQueryURL(String queryURL) throws DataSourceException {
        this.queryURL = queryURL;
        Matcher dsnMatcher = RegexPatterns.FIND_DSN_NAME_PATTERN.matcher(queryURL);
        if (dsnMatcher.find()){
            dsnName = dsnMatcher.group(1);
        }
        else {
            throw new DataSourceException ("The queryURL: " + queryURL + " does not look like a valid DAS features query (or there is an error in the FIND_DSN_NAME_PATTERN regular expression in this class.)");
        }
    }

    public String getDsnName() {
        return dsnName;
    }
}
