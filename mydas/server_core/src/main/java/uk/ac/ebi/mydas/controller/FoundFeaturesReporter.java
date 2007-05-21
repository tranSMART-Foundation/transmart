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
public class FoundFeaturesReporter implements FeaturesReporter {

    private boolean restricted;

    private Integer requestedStart;

    private Integer requestedStop;

    private DasAnnotatedSegment annotatedSegment;

    FoundFeaturesReporter(DasAnnotatedSegment annotatedSegment, SegmentQuery segmentQuery){
        this.restricted = segmentQuery.getStartCoordinate() != null;
        this.requestedStart = segmentQuery.getStartCoordinate();
        this.requestedStop = segmentQuery.getStopCoordinate();
        this.annotatedSegment = annotatedSegment;
    }

    FoundFeaturesReporter(DasAnnotatedSegment annotatedSegment){
        this.restricted = false;
        this.annotatedSegment = annotatedSegment;
    }

    Collection<DasFeature> getFeatures(boolean strictlyEnclosed) {
        return (restricted)
                ? annotatedSegment.getFeatures(requestedStart, requestedStop, strictlyEnclosed)
                : annotatedSegment.getFeatures();
    }

    public int getStart(){
        return (restricted)
                ? requestedStart
                : annotatedSegment.getStartCoordinate();
    }

    public int getStop(){
        return (restricted)
                ? requestedStop
                : annotatedSegment.getStopCoordinate();
    }

    public String getSegmentId(){
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
