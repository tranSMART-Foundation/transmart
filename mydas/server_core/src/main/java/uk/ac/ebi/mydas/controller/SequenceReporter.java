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

import uk.ac.ebi.mydas.model.DasSequence;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;

/**
 * Created using IntelliJ IDEA.
 * Date: 12-May-2007
 * Time: 11:49:24
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
class SequenceReporter{

    private boolean restricted;

    private int requestedStart;

    private int requestedStop;

    private DasSequence sequence;


    SequenceReporter(DasSequence sequence, SegmentQuery segmentQuery) throws CoordinateErrorException {

        this.restricted = true;
        this.requestedStart = segmentQuery.getStartCoordinate();
        this.requestedStop = segmentQuery.getStopCoordinate(); 
        this.sequence = sequence;

        if (requestedStart < sequence.getStartCoordinate() || requestedStop > sequence.getStopCoordinate()){
            throw new CoordinateErrorException(sequence.getSegmentId(), requestedStart,  requestedStop);
        }
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
        return sequence.getSegmentId();
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
