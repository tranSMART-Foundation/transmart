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
public class DasSequence {

    private String segmentName;

    protected String sequenceString;

    private int startCoordinate;

    private int stopCoordinate;

    private String version;

    private String molType;

    public static final String TYPE_DNA = "DNA";

    public static final String TYPE_ssRNA = "ssRNA";

    public static final String TYPE_dsRNA = "dsRNA";

    public static final String TYPE_PROTEIN = "Protein";

    private static final List<String> PERMITTED_TYPES = new ArrayList<String>(4);

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
     */
    public DasSequence(String segmentName, String sequence, int startCoordinate, String version, String molType)
            throws DataSourceException {
        // Check there is a sequenceString
        if (sequence == null || sequence.length() == 0){
            throw new DataSourceException ("An attempt has been made to instantiate a DasSequence object that has no sequenceString");
        }
        // And check that it has a name.
        if (segmentName == null || segmentName.length() == 0){
            throw new DataSourceException ("An attempt has been made to instantiate a DasSequence object that has no segmentName");
        }
        // Check the type is one of the permitted types.
        if (molType == null || ! PERMITTED_TYPES.contains(molType)){
            throw new DataSourceException ("An attempt has been made to instantiate a DasSequence object that is not one of the permitted types (provided as public static String members of the DasSequence object).");
        }

        this.sequenceString = sequence;
        this.startCoordinate = startCoordinate;
        this.stopCoordinate = sequence.length() - startCoordinate + 1;
        this.version = version;
        this.molType = molType;
        this.segmentName = segmentName;
    }


    public String getSegmentName() {
        return segmentName;
    }

    public String getSequenceString() {
        return sequenceString;
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

    public String getMolType() {
        return molType;
    }

    public String getRestrictedSequenceString(int requestedStart, int requestedStop)
            throws CoordinateErrorException {
        return sequenceString.substring(requestedStart - startCoordinate, requestedStop - startCoordinate + 1);
    }
}
