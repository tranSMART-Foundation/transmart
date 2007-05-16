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

/**
 * Created Using IntelliJ IDEA.
 * Date: 15-May-2007
 * Time: 15:42:10
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public class DasRestrictedAnnotatedSegment extends DasAnnotatedSegment{
    /**
     * Constructor for a DasAnnotatedSegment object that ensures that the object is valid.
     * See the documentation of the various getters to find out where in DAS XML these fields may be used.
     *
     * @param segmentId       <b>Required.</b> This is the identifier for the segment / sequence under query.
     * @param startCoordinate <b>Required.</b> Start position of the feature
     *                        <i>DAS servers are often required to serve non-positional features, such as descriptions (of the entire
     *                        segment) or related citations.  A commonly accepted mechanism is to give non-positional features start
     *                        and end coordinates of 0, however this is not enforced and is not part of the DAS 1.53 specification.</i>
     * @param stopCoordinate  <b>Required.</b> Stop position of the feature.
     * @param version         <b>Required.</b> a String indicating the version of the segment that is annotated.  What this
     *                        version consists of is not defined - may be a date, a checksum, a version number etc.  If you are
     *                        developing an annotation server, you must implement the same mechanism as the 'map master' reference server
     *                        that your server uses as authority.
     * @param segmentLabel    <b>Optional.</b> A human readable label for the segment.  If this is not given (null or
     *                        empty string) the segment ID will be used in its place.
     * @param features        being a Collection of zero or more {@link DasFeature} objects.  Each of these objects describes a single
     *                        feature.
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     *          to allow you to handle problems with the data source, such as SQLExceptions,
     *          parsing errors etc.
     */
    public DasRestrictedAnnotatedSegment(String segmentId, int startCoordinate, int stopCoordinate, String version, String segmentLabel, Collection<DasFeature> features) throws DataSourceException {
        super(segmentId, startCoordinate, stopCoordinate, version, segmentLabel, features);
    }

    /**
     * This method returns features within the specified coordinates as requested.
     *
     * Overrides the method in DasAnnotatedSegment that does the work of checking that the features fall within the
     * coordinates.  This method leaves this entirely to the data source implementor, who has
     * implemented a RangeHandlingAnnotationDataSource or a RangeHandlingReferenceDataSource.
     *
     * The basis of this override is that the DataSource is ensuring that the features added to the features collection
     * include only those that match the requested coordinates.  It is entirely up to the implementor to decide
     * whether these matching features only need to overlap the requested start and stop coordinates, or whether they
     * must be completely enclosed within these coordinates.
     *
     * @param requestedStart being the start coordinate requested by the client.
     * @param requestedStop being the stop coordinate requested by the client.
     * @param strictlyEnclosed a boolean to indicate if matching features must be strictly enclosed within the
     * requestedStart and requestedStop.  if this value is false, then an overlap is sufficient for a match.
     * @return a Collection<DasFeature> of the DasFeature objects that match.
     */
    public Collection<DasFeature> getFeatures(int requestedStart, int requestedStop, boolean strictlyEnclosed){
        return features;
    }
}
