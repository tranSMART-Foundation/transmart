/*
 * Copyright 2007 Philip Jones, EMBL-European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * For further details of the mydas project, including source code,
 * downloads and documentation, please see:
 *
 * http://code.google.com/p/mydas/
 *
 */

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

    public String toString(){
        StringBuffer buf = new StringBuffer(segmentId);
        if (startCoordinate != null){
            // The regex pattern used to construct this object
            // guarantees that ((startCoordinate == null) == (stopCoordinate == null))
            buf.append ('_').append(startCoordinate);
            buf.append ('_').append(stopCoordinate);
        }
        return buf.toString();
    }
}
