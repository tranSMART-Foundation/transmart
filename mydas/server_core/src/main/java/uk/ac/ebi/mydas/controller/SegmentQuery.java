package uk.ac.ebi.mydas.controller;

import java.util.regex.Matcher;

/**
 * Created Using IntelliJ IDEA.
 * Date: 15-May-2007
 * Time: 16:50:03
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class SegmentQuery {

    private String segmentId;

    private Integer startCoordinate;

    private Integer stopCoordinate;

    public SegmentQuery(Matcher segmentRangePatternMatcher){
        segmentId = segmentRangePatternMatcher.group(1);
        String startString = segmentRangePatternMatcher.group(3);
        String stopString = segmentRangePatternMatcher.group(4);
        startCoordinate = (startString == null || startString.length() == 0)
                ? null
                : new Integer (startString);
        stopCoordinate = (stopString == null || stopString.length() == 0)
                ? null
                : new Integer (stopString);
    }


    public String getSegmentId() {
        return segmentId;
    }

    public Integer getStartCoordinate() {
        return startCoordinate;
    }

    public Integer getStopCoordinate() {
        return stopCoordinate;
    }
}
