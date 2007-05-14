package uk.ac.ebi.mydas.controller;

import uk.ac.ebi.mydas.model.DasSequence;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;

/**
 * Created using IntelliJ IDEA.
 * Date: 12-May-2007
 * Time: 11:49:24
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
class SequenceReporter {

    private boolean restricted;

    private int requestedStart;

    private int requestedStop;

    private DasSequence sequence;


    SequenceReporter(DasSequence sequence, int requestedStart, int requestedStop) throws CoordinateErrorException {
        if (requestedStart < sequence.getStartCoordinate() || requestedStop > sequence.getStopCoordinate()){
            throw new CoordinateErrorException(sequence.getSegmentName(), requestedStart,  requestedStop);
        }
        this.restricted = true;
        this.requestedStart = requestedStart;
        this.requestedStop = requestedStop;
        this.sequence = sequence;
    }

    SequenceReporter(DasSequence sequence){
        this.restricted = false;
        this.sequence = sequence;
    }

    String getSequenceString() throws CoordinateErrorException {
        return (restricted)
            ? sequence.getRestrictedSequenceString(requestedStart, requestedStop)
            : sequence.getSequenceString();
    }

    String getSegmentName(){
        return sequence.getSegmentName();
    }

    String getSequenceVersion(){
        return sequence.getVersion();
    }

    String getSequenceMoleculeType(){
        return sequence.getMolType();
    }

    int getStart(){
        return (restricted)
                ? requestedStart
                : sequence.getStartCoordinate();
    }

    int getStop(){
        return (restricted)
                ? requestedStop
                : sequence.getStopCoordinate();
    }
}
