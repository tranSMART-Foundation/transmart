package uk.ac.ebi.mydas.controller;

import uk.ac.ebi.mydas.model.DasAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasFeature;
import java.util.Collection;

/**
 * Created using IntelliJ IDEA.
 * Date: 12-May-2007
 * Time: 11:49:57
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * Wraps a DasAnnotatedSegment object with details of the request encapsulated, i.e. the requested
 * start and stop coordinates.
 */
public class FeaturesReporter {

    private boolean restricted;

    private int requestedStart;

    private int requestedStop;

    private DasAnnotatedSegment annotatedSegment;

    FeaturesReporter(DasAnnotatedSegment annotatedSegment, SegmentQuery segmentQuery){
        this.restricted = true;
        this.requestedStart = segmentQuery.getStartCoordinate();
        this.requestedStop = segmentQuery.getStopCoordinate();
        this.annotatedSegment = annotatedSegment;
    }

    FeaturesReporter(DasAnnotatedSegment annotatedSegment){
        this.restricted = false;
        this.annotatedSegment = annotatedSegment;
    }

    Collection<DasFeature> getFeatures() {
        return (restricted)
                ? annotatedSegment.getFeatures(requestedStart, requestedStop, false)
                : annotatedSegment.getFeatures();
    }

    int getStart(){
        return (restricted)
                ? requestedStart
                : annotatedSegment.getStartCoordinate();
    }

    int getStop(){
        return (restricted)
                ? requestedStop
                : annotatedSegment.getStopCoordinate();
    }

    String getSegmentId(){
        return annotatedSegment.getSegmentId();
    }

    String getSegmentLabel(){
        return annotatedSegment.getSegmentLabel();
    }

    String getType(){
        return annotatedSegment.getType();
    }

    String getVersion(){
        return annotatedSegment.getVersion();
    }

}
