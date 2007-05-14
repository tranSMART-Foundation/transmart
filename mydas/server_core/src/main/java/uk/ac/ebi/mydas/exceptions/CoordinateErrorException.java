package uk.ac.ebi.mydas.exceptions;

import java.text.MessageFormat;

/**
 * Created using IntelliJ IDEA.
 * Date: 12-May-2007
 * Time: 12:03:46
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 * Exception indicating that a request for sequenceString of features over a particular range of
 * coordinates is out of range of the segment itself.
 */
public class CoordinateErrorException extends Exception{

    public CoordinateErrorException(String segmentName, int requestedStart, int requestedEnd){
        super (MessageFormat.format("A request has been made for a coordinate that is out of range, segment name {0} requested start {1} requested stop {2}", segmentName, requestedStart, requestedEnd));
    }
}
