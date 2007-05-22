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

/**
 * Created Using IntelliJ IDEA.
 * Date: 21-May-2007
 * Time: 18:05:35
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class UnknownSegmentReporter implements SegmentReporter {

    SegmentQuery query;

    public UnknownSegmentReporter(SegmentQuery query){
        this.query = query;
    }
    public Integer getStart() {
        return query.getStartCoordinate();
    }

    public Integer getStop() {
        return query.getStopCoordinate();
    }

    public String getSegmentId() {
        return query.getSegmentId();
    }
}
