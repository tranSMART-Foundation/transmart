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

import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * Created Using IntelliJ IDEA.
 * Date: 10-May-2007
 * Time: 10:31:11
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 * This class is returned by the getSequence method of the implemented
 * datasource interface to represent a sequence and provide the necessary
 * information to construct a
 * /DASSEQUENCE/SEQUENCE element in response to the sequence command, or the
 * /DASDNA/SEQUENCE element in response to the dna command.
 */
@SuppressWarnings("serial")
public class DasSequence extends DasSegment implements Serializable {

    /**
     *  <b>Mandatory</b> sequence String used to populate the
     * /DASSEQUENCE/SEQUENCE element (sequence command) or the
     * /DASDNA/SEQUENCE/DNA element (dna command).
     */
    protected String sequenceString;


    /**
     * @deprecated DAS1.6 does not include a moltype attribute
     * <b>Mandatory</b> value indicating the type of molecule.
     *
     * Used to populate the
     * /DASSEQUENCE/SEQUENCE/@moltype attribute in response to the
     * sequence command.
     *
     * Must be one of:
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
     */
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
     * The label attribute (optional) supplies a human readable label for display purposes. If omitted, it is assumed the ID is suitable for display.
     */
    private String label;
    
    /**
     * @param sequence <b>Mandatory</b> sequence String used to populate the
     * /DASSEQUENCE/SEQUENCE element (sequence command) or the
     * /DASDNA/SEQUENCE/DNA element (dna command).
     * @param startCoordinate <b>Mandatory</b> start coordinate of the sequence.
     * @param version  <b>Mandatory</b> version of the sequence.  Typically may be
     * a date, version number or checksum.  Used to populate the
     * /DASSEQUENCE/SEQUENCE/@version attribute or the /DASDNA/SEQUENCE/@version
     * attribute.
     * @param label The label attribute (optional) supplies a human readable 
     * label for display purposes. If omitted, it is assumed the ID is 
     * suitable for display.
     * @param molType <b>DEPRECATED</b> value indicating the type of molecule.
     * Used to populate the
     * /DASSEQUENCE/SEQUENCE/@moltype attribute in response to the
     * sequence command.
     * Must be one of:
     * <ul><li>DasSequence.TYPE_DNA</li>
     *      <li>DasSequence.TYPE_ssRNA</li>
     *      <li>DasSequence.TYPE_dsRNA</li>
     *      <li>DasSequence.TYPE_PROTEIN</li></ul>
     * @param segmentId <b>Mandatory</b> id of the segment.
     * Representing the /DASSEQUENCE/SEQUENCE/@id attribute or the /DASDNA/SEQUENCE/@id
     * attribute.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException in the event that
     * there is a problem with the information used to instantiate this object.
     */
    public DasSequence(String segmentId, String sequence, int startCoordinate, String version, String label)
            throws DataSourceException {

        super(startCoordinate,
                (sequence == null)
                        ? startCoordinate
                        : startCoordinate + sequence.length() - 1,
                segmentId,
                version);
        // Check there is a sequenceString
        if (sequence == null || sequence.length() == 0){
            throw new DataSourceException ("An attempt has been made to instantiate a DasSequence object that has no sequenceString");
        }

        this.sequenceString = sequence;
        this.label=label;
    }


    public String getSequenceString() {
        return sequenceString;
    }

    public String getRestrictedSequenceString(int requestedStart, int requestedStop)
            throws CoordinateErrorException {
        return sequenceString.substring(requestedStart - startCoordinate, requestedStop - startCoordinate + 1);
    }

    /**
     * @deprecated
     */
    public String getMolType() {
        return molType;
    }

    public String getLabel(){
    	return label;
    }
}
