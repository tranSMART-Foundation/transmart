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

    private int startCoordinate;

    private int stopCoordinate;


    public DasTarget(String targetId, int startCoordinate, int stopCoordinate) {
        this.targetId = targetId;
        this.startCoordinate = startCoordinate;
        this.stopCoordinate = stopCoordinate;
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
}
