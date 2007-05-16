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
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;

import java.util.List;
import java.util.ArrayList;

/**
 * Created Using IntelliJ IDEA.
 * Date: 10-May-2007
 * Time: 10:31:11
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 * TODO Document this class in detail.
 */
public class DasSequence extends DasSegment{

    protected String sequenceString;
    protected String molType;
    public static final String TYPE_DNA = "DNA";
    public static final String TYPE_ssRNA = "ssRNA";
    public static final String TYPE_dsRNA = "dsRNA";
    public static final String TYPE_PROTEIN = "Protein";
    protected static final List<String> PERMITTED_TYPES = new ArrayList<String>(4);

    static{
        PERMITTED_TYPES.add(TYPE_DNA);
        PERMITTED_TYPES.add(TYPE_ssRNA);
        PERMITTED_TYPES.add(TYPE_dsRNA);
        PERMITTED_TYPES.add(TYPE_PROTEIN);
    }

    /**
     * @param sequence
     * @param startCoordinate
     * @param version
     * @param molType
     * @param segmentName
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     */
    public DasSequence(String segmentName, String sequence, int startCoordinate, String version, String molType)
            throws DataSourceException {

        super(startCoordinate,
                (sequence == null)
                        ? startCoordinate
                        : startCoordinate + sequence.length() - 1,
                segmentName,
                version);
        // Check there is a sequenceString
        if (sequence == null || sequence.length() == 0){
            throw new DataSourceException ("An attempt has been made to instantiate a DasSequence object that has no sequenceString");
        }
         // Check the type is one of the permitted types.
        if (molType == null || ! PERMITTED_TYPES.contains(molType)){
            throw new DataSourceException ("An attempt has been made to instantiate a DasSequence object that is not one of the permitted types (provided as public static String members of the DasSequence object).");
        }

        this.molType = molType;
        this.sequenceString = sequence;
    }


    public String getSequenceString() {
        return sequenceString;
    }

    public String getRestrictedSequenceString(int requestedStart, int requestedStop)
            throws CoordinateErrorException {
        return sequenceString.substring(requestedStart - startCoordinate, requestedStop - startCoordinate + 1);
    }

    public String getMolType() {
        return molType;
    }
}
