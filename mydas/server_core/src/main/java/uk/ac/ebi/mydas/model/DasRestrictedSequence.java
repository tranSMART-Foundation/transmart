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
 * Created using IntelliJ IDEA.
 * Date: 12-May-2007
 * Time: 11:26:01
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * This class extends DasSequence for use with the
 * {@link uk.ac.ebi.mydas.datasource.RangeHandlingReferenceDataSource}
 * class.  This method overrides the getRestrictedSequenceString method
 * on the assumption that the data source has already restricted
 * the sequence.
 */
public class DasRestrictedSequence extends DasSequence implements Serializable {

    /**
     * Constructor for a DasRestrictedSequence object.
     * @param segmentId being the requested segment ID.
     * @param sequence being the sequence set by the data source.  Note that
     * it is assumed that the data source is restricting this to the sequence
     * between the start and stop coordinates requested.
     * @param startCoordinate being the requested start coordinate.
     * @param version being the sequence version.  May be a date,
     * version number or checksum etc.
     * @param molType being one of:
     * <ul>
     *      <li>
     *           DasSequence.TYPE_DNA
     *      </li>
     *      <li>
     *           DasSequence.TYPE_ssRNA
     *      </li>
     *      <li>
     *           DasSequence.TYPE_dsRNA
     *      </li>
     *      <li>
     *           DasSequence.TYPE_PROTEIN
     *      </li>
     * </ul>
     * @throws DataSourceException to capture any exception thrown by the super constructor.
     */
    public DasRestrictedSequence(String segmentId, String sequence, int startCoordinate, String version, String molType) throws DataSourceException {
        super(segmentId, sequence, startCoordinate, version, molType);
    }

    /**
     * This method overrides the getRestrictedSequenceString method
     * on the assumption that the data source has already restricted
     * the sequence.
     * @param requestedStart requested start coordinate on the segment.
     * @param requestedStop  requested stop coordinate on the segment.
     * @return a String that contains the requested sequence.
     */
    public String getRestrictedSequenceString(int requestedStart, int requestedStop){
        return sequenceString;
    }
}
