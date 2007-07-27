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

package uk.ac.ebi.mydas.model;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

import java.io.Serializable;

/**
 * Created Using IntelliJ IDEA.
 * Date: 14-May-2007
 * Time: 15:20:03
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * Abstract parent class of all classes that describe a segment, such as the
 * DasSequence class that includes details of the sequence of the segment and the
 * DasFeature class that holds all of the features of a segment.
 */
public abstract class DasSegment implements Serializable {

    /**
     * <b>Mandatory</b> id of the segment.
     */
    protected String segmentId;

    /**
     * <b>Mandatory</b> start coordinate of the segment.
     */
    protected int startCoordinate;

    /**
     * <b>Mandatory</b> stop coordinate of the segment.
     */
    protected int stopCoordinate;

    /**
     * <b>Mandatory</b> version of the segment. Typically this will be
     * a version number, date or checksum for the sequence.
     */
    protected String version;

    public DasSegment(int startCoordinate, int stopCoordinate, String segmentId, String version) 
        throws DataSourceException{
        // Check that it has an ID.
        if (segmentId == null || segmentId.length() == 0){
            throw new DataSourceException("An attempt has been made to instantiate a DasSegment object that has no segmentId");
        }
        // Check that it has a version.
        if (version == null  || version.length() == 0){
            throw new DataSourceException("An attempt has been made to instantiate a DasSegment object that has no version");
        }
        this.startCoordinate = startCoordinate;
        this.stopCoordinate = stopCoordinate;
        this.segmentId = segmentId;
        this.version = version;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public int getStartCoordinate() {
        return startCoordinate;
    }

    public int getStopCoordinate() {
        return stopCoordinate;
    }

    public String getVersion() {
        return version;
    }

}
