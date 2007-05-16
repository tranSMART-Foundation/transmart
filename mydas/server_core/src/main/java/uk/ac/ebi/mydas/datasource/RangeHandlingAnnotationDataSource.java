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

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.model.DasAnnotatedSegment;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 14:34:22
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 *
 * If your datasource implements only the {@link uk.ac.ebi.mydas.datasource.AnnotationDataSource} interface,
 * the MydasServlet will handle restricting the features returned to
 * the start / stop coordinates in the request and you will only need to
 * implement the <code>getFeatures (String segmentReference) : List&lt;DasFeature&gt;</code>
 * method.
 *
 * If you also implement this interface, this will allow you to take control
 * of filtering by start / stop coordinates in your AnnotationDataSource.
 */
public interface RangeHandlingAnnotationDataSource extends AnnotationDataSource{

    /**
     * This method returns a List of DasAnnotatedSegment objects, describing the annotated segment and the features
     * of the segmentId passed in as argument.
     * @param segmentId being the reference of the segment requested in the DAS request (not including
     * start and stop coordinates)
     *
     * If your datasource implements only this interface,
     * the MydasServlet will handle restricting the features returned to
     * the start / stop coordinates in the request and you will only need to
     * implement this method to return Features.  If on the other hand, your data source
     * includes massive segments, you may wish to implement the {@link uk.ac.ebi.mydas.datasource.RangeHandlingAnnotationDataSource}
     * interface.  It will then be the responsibility of your AnnotationDataSource plugin to
     * restrict the features returned for the requested range.
     * @param start
     * @param stop
     *
     * @return A DasAnnotatedSegment object.  This describes the segment that is annotated, limited
     * to the information required for the /DASGFF/GFF/SEGMENT element.  References a Collection of
     * DasFeature objects.   Note that this is a basic Collection - this gives you complete control over the details
     * of the Collection type - so you can create your own comparators etc.
     * @throws BadReferenceObjectException in the event that your server does not include information about this segment.
     * @throws DataSourceException should be thrown if there is any
     * fatal problem with loading this data source.  <bold>It is highly desirable for the implementation to test itself in this init method and throw
     * a DataSourceException if it fails, e.g. to attempt to get a Connection to a database
     * and read a record.</bold>
     */
    public DasAnnotatedSegment getFeatures(String segmentId, int start, int stop) throws BadReferenceObjectException, DataSourceException;

}
