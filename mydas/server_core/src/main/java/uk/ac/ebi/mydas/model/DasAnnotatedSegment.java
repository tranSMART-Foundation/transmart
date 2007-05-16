package uk.ac.ebi.mydas.model;

import uk.ac.ebi.mydas.exceptions.DataSourceException;

import java.util.Collection;
import java.util.ArrayList;

/**
 * Created Using IntelliJ IDEA.
 * Date: 14-May-2007
 * Time: 15:26:17
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * The DasAnnotatedSegment is used as a holder for {@link DasFeature}, as well as describing the
 * segment that is annotated with these features.
 *
 * A Data Source is required to be able to return a {@link Collection<DasAnnotatedSegment>} of these objects.
 */
public class DasAnnotatedSegment extends DasSegment{

    /**
     * A collection of {@link DasFeature} objects, being the features annotated on this segment.
     * Holds the complete contents of a /DASGFF/GFF/SEGMENT/FEATURE element (for the features request)
     * and is used to derive the contents of the
     * /DASTYPES/GFF/SEGMENT/TYPE element (for the types request).
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

    /**
     * Constructor for a DasAnnotatedSegment object that ensures that the object is valid.
     * See the documentation of the various getters to find out where in DAS XML these fields may be used.
     * @param segmentId <b>Required.</b> This is the identifier for the segment / sequence under query.
     * @param startCoordinate <b>Required.</b> Start coordinate of the segment.
     * @param stopCoordinate <b>Required.</b> Stop coordinate of the segment.
     * @param version <b>Required.</b> a String indicating the version of the segment that is annotated.  What this
     * version consists of is not defined - may be a date, a checksum, a version number etc.  If you are
     * developing an annotation server, you must implement the same mechanism as the 'map master' reference server
     * that your server uses as authority.
     * @param segmentLabel <b>Optional.</b> A human readable label for the segment.  If this is not given (null or
     * empty string) the segment ID will be used in its place.
     * @param features being a Collection of zero or more {@link DasFeature} objects.  Each of these objects describes a single
     * feature.
     * @throws DataSourceException to allow you to handle problems with the data source, such as SQLExceptions,
     * parsing errors etc.
     */
    public DasAnnotatedSegment(String segmentId, int startCoordinate, int stopCoordinate, String version, String segmentLabel, Collection<DasFeature> features)
            throws DataSourceException {
        super(startCoordinate, stopCoordinate, segmentId, version);
        this.features = features;
        this.segmentLabel = segmentLabel;
    }

    /**
     * Returns a collection of {@link DasFeature} objects, being the features annotated on this segment.
     * Holds the complete contents of a /DASGFF/GFF/SEGMENT/FEATURE element (for the features request)
     * and is used to derive the contents of the
     * /DASTYPES/GFF/SEGMENT/TYPE element (for the types request).
     * @return a collection of {@link DasFeature} objects, being the features annotated on this segment.
     */
    public Collection<DasFeature> getFeatures() {
        return features;
    }

    /**
     * This method returns features within the specified coordinates as requested.  Configurable - the
     *
     * @param requestedStart being the start coordinate requested by the client.
     * @param requestedStop being the stop coordinate requested by the client.
     * @param strictlyEnclosed a boolean to indicate if matching features must be strictly enclosed within the
     * requestedStart and requestedStop.  if this value is false, then an overlap is sufficient for a match.
     * @return a Collection<DasFeature> of the DasFeature objects that match.
     */
    public Collection<DasFeature> getFeatures(int requestedStart, int requestedStop, boolean strictlyEnclosed){
        Collection<DasFeature> restrictedFeatures = new ArrayList<DasFeature>(this.features.size());
        if (features != null){
            for (DasFeature feature : features){
                if (strictlyEnclosed && requestedStart <= feature.getStartCoordinate() && requestedStop >= feature.getStopCoordinate()){
                    restrictedFeatures.add (feature);
                }
                else if (! strictlyEnclosed && (requestedStop >= feature.getStartCoordinate() && requestedStop <= feature.getStopCoordinate())
                        || (requestedStart >= feature.getStartCoordinate() && requestedStart <= feature.getStopCoordinate())){
                    restrictedFeatures.add (feature);
                }
            }
        }
        return restrictedFeatures;
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
