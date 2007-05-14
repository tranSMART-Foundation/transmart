package uk.ac.ebi.mydas.model;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

/**
 * Created Using IntelliJ IDEA.
 * Date: 14-May-2007
 * Time: 15:20:03
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public abstract class DasSegment {
    
    protected String segmentId;
    protected int startCoordinate;
    protected int stopCoordinate;
    protected String version;

    public DasSegment(int startCoordinate, int stopCoordinate, String segmentId, String version) 
        throws DataSourceException{
        // Check that it has an ID.
        if (segmentId == null || segmentId.length() == 0){
            throw new DataSourceException("An attempt has been made to instantiate a DasSegment object that has no segmentId");
        }
        // And a version.
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
