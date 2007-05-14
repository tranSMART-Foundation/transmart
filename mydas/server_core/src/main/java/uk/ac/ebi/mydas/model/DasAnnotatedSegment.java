package uk.ac.ebi.mydas.model;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

import java.util.Collection;

/**
 * Created Using IntelliJ IDEA.
 * Date: 14-May-2007
 * Time: 15:26:17
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class DasAnnotatedSegment extends DasSegment{

    /**
     * A collection of DasFeature objects, being the features annotated on this segment.
     */
    Collection<DasFeature> features;

    /**
     * The label for this segment.  Used for the features command for attribute
     * /DASGFF/GFF/SEGMENT/@label
     *
     * or the types command for attribute
     * /DASTYPES/GFF/SEGMENT/@label
     */
    String segmentLabel;

    /**
     * The type of the segment.  Used to describe the type of the segment, NOT the features (a confusing aspect of
     * the DAS 1.53 specification.) The specification indicates that this is to allow future annotation from
     * ontologies to be included.
     *
     * Used for the feature command for attribute
     * /DASGFF/GFF/SEGMENT/@type
     *
     * or the types command for attribute
     * /DASTYPES/GFF/SEGMENT/@type
     */
    String type;


    public DasAnnotatedSegment(String segmentId, int startCoordinate, int stopCoordinate, String version, String segmentLabel, Collection<DasFeature> features)
            throws DataSourceException {
        super(startCoordinate, stopCoordinate, segmentId, version);
        this.features = features;
        this.segmentLabel = segmentLabel;
    }

    /**
     * Returns a collection of DasFeature objects, being the features annotated on this segment.
     * @return a collection of DasFeature objects, being the features annotated on this segment.
     */
    public Collection<DasFeature> getFeatures() {
        return features;
    }

    /**
     * Returns the label for this segment.  Used for the features command for attribute
     * /DASGFF/GFF/SEGMENT/@label
     *
     * or the types command for attribute
     * /DASTYPES/GFF/SEGMENT/@label
     * @return  the label for this segment.
     */
    public String getSegmentLabel() {
        return segmentLabel;
    }

    /**
     * Returns the type of the segment.  Used to describe the type of the segment, NOT the features (a confusing aspect of
     * the DAS 1.53 specification.) The specification indicates that this is to allow future annotation from
     * ontologies to be included.
     *
     * Used for the feature command for attribute
     * /DASGFF/GFF/SEGMENT/@type
     *
     * or the types command for attribute
     * /DASTYPES/GFF/SEGMENT/@type
     * @return the type of the segment.
     */
    public String getType() {
        return type;
    }
}
