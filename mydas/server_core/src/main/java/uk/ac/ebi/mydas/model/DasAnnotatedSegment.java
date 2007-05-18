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

import java.util.Collection;
import java.util.ArrayList;

import org.apache.log4j.Logger;

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
     * Define a static logger variable so that it references the
     * Logger instance named "XMLUnmarshaller".
     */
    private static final Logger logger = Logger.getLogger(DasAnnotatedSegment.class);

    /**
     * A collection of {@link DasFeature} objects, being the features annotated on this segment.
     * Holds the complete contents of a /DASGFF/GFF/SEGMENT/FEATURE element (for the features request)
     * and is used to derive the contents of the
     * /DASTYPES/GFF/SEGMENT/TYPE element (for the types request).
     */
    Collection<DasFeature> features;

    DasComponentFeature selfComponent;

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
     *
     * If the das source has added DasComponentFeatures to the DasAnnotatedSegment, they will all be added
     * to the Collection as well.
     * @return a collection of {@link DasFeature} objects, being the features annotated on this segment.
     */
    public Collection<DasFeature> getFeatures() {
        if (selfComponent == null){
            return features;
        }
        else {
            Collection<DasFeature> allFeatures = new ArrayList<DasFeature>(features);
            allFeatures.add(selfComponent);
            allFeatures.addAll(selfComponent.getReportableSubComponents());
            allFeatures.addAll(selfComponent.getReportableSuperComponents());
            return allFeatures;
        }
    }

    /**
     * This method returns features within the specified coordinates as requested.
     *
     * @param requestedStart being the start coordinate requested by the client.
     * @param requestedStop being the stop coordinate requested by the client.
     * @param strictlyEnclosed a boolean to indicate if matching features must be strictly enclosed within the
     * requestedStart and requestedStop.  if this value is false, then an overlap is sufficient for a match.
     * @return a Collection<DasFeature> of the DasFeature objects that match.
     */
    public Collection<DasFeature> getFeatures(int requestedStart, int requestedStop, boolean strictlyEnclosed){
        if (logger.isDebugEnabled()){
            logger.debug("DasAnnotatedSegment.getFeatures (start, stop, strictlyEnclosed) called.  StrictlyEnclosed = " + strictlyEnclosed);
        }
        Collection<DasFeature> allFeatures = this.getFeatures();
        Collection<DasFeature> restrictedFeatures = new ArrayList<DasFeature>(allFeatures.size());
        if (features != null){
            for (DasFeature feature : allFeatures){
                if (strictlyEnclosed && requestedStart <= feature.getStartCoordinate() && requestedStop >= feature.getStopCoordinate()){

                    if (logger.isDebugEnabled()){
                        logger.debug("Strictly enclosed.  Feature passed: Requested start: " + requestedStart + ". Requested stop: " +
                        requestedStop + ". Feature start: " + feature.getStartCoordinate() + ". Feature stop: " + feature.getStopCoordinate());
                    }
                    
                    restrictedFeatures.add (feature);
                }
                else if ((! strictlyEnclosed) && ((requestedStop >= feature.getStartCoordinate() && requestedStop <= feature.getStopCoordinate())
                        || (requestedStart >= feature.getStartCoordinate() && requestedStart <= feature.getStopCoordinate()))){

                    if (logger.isDebugEnabled()){
                        logger.debug("Overlap only.  Feature passed: Requested start: " + requestedStart + ". Requested stop: " + 
                        requestedStop + ". Feature start: " + feature.getStartCoordinate() + ". Feature stop: " + feature.getStopCoordinate());
                    }

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

    /**
     * TODO Create some detailed documentation of how to build an assembly using this mechanism.
     * This method creates and returns the component feature that represents this
     * annotated segment.  To add to the assembly, use this method to retrieve the
     * 'self' component and then add subparts or superparts to this.  (This can
     * be done recursively, to create a map of components to any level.)
     * @return a DasComponentFeature object that is the segment itself represented
     * as a component.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     */
    public DasComponentFeature getSelfComponentFeature() throws DataSourceException{
        if (selfComponent == null){
            selfComponent = new DasComponentFeature(this);
        }
        return selfComponent;
    }
}
