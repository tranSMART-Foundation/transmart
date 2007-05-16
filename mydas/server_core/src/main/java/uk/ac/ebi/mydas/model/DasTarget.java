package uk.ac.ebi.mydas.model;

/**
 * Created Using IntelliJ IDEA.
 * Date: 14-May-2007
 * Time: 17:03:30
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class DasTarget {

    private String targetId;

    private String targetName;

    private int startCoordinate;

    private int stopCoordinate;

    /**
     * Constructor for a DasTarget, serialized out as the response to a feature request.
     * @param targetId <b>Mandatory</b>
     * @param startCoordinate <b>Mandatory</b>
     * @param stopCoordinate <b>Mandatory</b>
     * @param targetName <b>Optional</b>
     */
    public DasTarget(String targetId, int startCoordinate, int stopCoordinate, String targetName) {
        this.targetId = targetId;
        this.startCoordinate = startCoordinate;
        this.stopCoordinate = stopCoordinate;
        this.targetName = targetName;
    }


    public String getTargetId() {
        return targetId;
    }

    public int getStartCoordinate() {
        return startCoordinate;
    }

    public int getStopCoordinate() {
        return stopCoordinate;
    }

    public String getTargetName() {
        return targetName;
    }
}
