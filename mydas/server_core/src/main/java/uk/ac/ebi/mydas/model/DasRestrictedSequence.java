package uk.ac.ebi.mydas.model;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;

/**
 * Created using IntelliJ IDEA.
 * Date: 12-May-2007
 * Time: 11:26:01
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class DasRestrictedSequence extends DasSequence{

    public DasRestrictedSequence(String segmentName, String sequence, int startCoordinate, String version, String molType) throws DataSourceException {
        super(segmentName, sequence, startCoordinate, version, molType);
    }

    public String getRestrictedSequenceString(int requestedStart, int requestedStop)
            throws CoordinateErrorException {
        return sequenceString;
    }
}
