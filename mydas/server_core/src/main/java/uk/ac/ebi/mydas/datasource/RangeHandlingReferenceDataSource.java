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

package uk.ac.ebi.mydas.datasource;

import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.exceptions.CoordinateErrorException;
import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.model.DasRestrictedAnnotatedSegment;
import uk.ac.ebi.mydas.model.DasRestrictedSequence;

import java.util.Collection;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 15:12:06
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 *
 * If your datasource implements only the {@link uk.ac.ebi.mydas.datasource.ReferenceDataSource} interface,
 * the MydasServlet will handle restricting the sequenceString returned to
 * the start / stop coordinates in the request and you will only need to
 * implement the <code>getSequenceString (String segmentReference) : String</code>
 * method and the <code>getFeatures (String segmentReference) : Collection&gt;DasFeature&lt;</code> method.
 *
 * Implementing this interface however will allow you to take control
 * of filtering by start / stop coordinates in your ReferenceDataSource for both the sequence
 * and the features.
 *
 * This is seful if your DAS source includes massive segments, otherwise don't bother - just implement a
 * {@link ReferenceDataSource}.
 */
public interface RangeHandlingReferenceDataSource extends ReferenceDataSource{

    /**
     * TODO Detailed documentation required here.
     * If you also implement this interface, this will allow you to take control
     * of filtering the sequenceString returned by start / stop coordinates in your ReferenceDataSource.
     * @param segmentReference
     * @param start
     * @param stop
     * @return a DasSequence object for the reference, or null if it is not found.
     * @throws uk.ac.ebi.mydas.exceptions.BadReferenceObjectException
     * @throws uk.ac.ebi.mydas.exceptions.DataSourceException
     * @throws uk.ac.ebi.mydas.exceptions.CoordinateErrorException
     */
    public DasRestrictedSequence getSequence (String segmentReference, int start, int stop) throws CoordinateErrorException, BadReferenceObjectException, DataSourceException;

    /**
     * Implement this method to allow you to take control
     * of filtering by start / stop coordinates in your AnnotationDataSource.
     *
     * Note that this returns a Collection of DasRestrictedAnnotatedSegment objects.  When the DAS features command
     * is called, including restrictions on the start and stop coordinates of the matching features, the
     * DasRestrictedAnnotatedSegment will just return all the features that the data source had added to it, so it
     * is the responsibility of the data source to filter these features by coordinate.
     *
     * The DAS 1.53 specification is ambiguous about how a match by coordinate is defined.  It is up to you whether matching
     * features should fall strictly within the start and stop coordinates, or whether they may merely overlap with the
     * specified region.
     *
     * Useful if your DAS source includes massive segments, otherwise don't bother - just implement a
     * ReferenceDataSource.
     * @param segmentReference
     * @param start
     * @param stop
     * @return
     * @throws BadReferenceObjectException
     * @throws DataSourceException
     */
    public DasRestrictedAnnotatedSegment getFeatures(String segmentReference, int start, int stop) throws CoordinateErrorException, BadReferenceObjectException, DataSourceException;
}
