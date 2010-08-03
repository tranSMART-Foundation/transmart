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
import uk.ac.ebi.mydas.model.DasEntryPoint;
import uk.ac.ebi.mydas.model.DasSequence;

import java.util.Collection;

/**
 * Created Using IntelliJ IDEA.
 * Date: 04-May-2007
 * Time: 15:09:43
 *
 * @author Phil Jones, EMBL-EBI, pjones@ebi.ac.uk
 */
public interface ReferenceDataSource extends AnnotationDataSource{

    /**
     * Returns a DasSequence object that describes the sequence for the requested segment id. (e.g. accession)
     * @param segmentId being the name / accession of the sequence being requested.
     * @return a DasSequence object, holding the sequenceString, version and start / end coordinates of the sequence.
     * @throws DataSourceException to encapsulate any exceptions thrown by the datasource
     * and allow the MydasServlet to return a decent error header to the client.
     * @throws uk.ac.ebi.mydas.exceptions.BadReferenceObjectException should be thrown if the segment requested does not
     * exist in this data source.
     */
    public DasSequence getSequence (String segmentId) throws BadReferenceObjectException, DataSourceException;

    /**
     * Returns the value to be returned from the entry_points command, specifically
     * the /DASEP/ENTRY_POINTS/@version attribute.
     *
     * This is a <b>mandatory</b> value so you must ensure that this method does not
     * return null or an empty String. (The MydasServlet will return an error to the
     * client if you do).
     * @return a non-null, non-zero length String, being the version number of the
     * entry points / datasource.
     * @throws DataSourceException to encapsulate any exceptions thrown by the datasource
     * and allow the MydasServlet to return a decent error header to the client.
     */
    public String getEntryPointVersion () throws DataSourceException;

    /**
     * Returns an ordered Collection of DasEntryPoint objects to implement the entry_point command.
     * The DasEntryPoint object encapsulates information including the segment id, the
     * start coordinate, end coordinate, type, orientation and description of a segment.
     * Reference servers should always return entry points in the same order,
     * starting on position 1 (rather than 0). Reference servers are responsible to
     * take care of start and stop positions, thus they should only return the collection corresponding to
     * those positions (including both limits). If start is greater that the collection size,
     * an empty collection should be returned, if the stop is greater than the collection size,
     * the returned collection will include only those existing entry points from the specified start position.
     * when the initial request does not specify any positions, DasCommandManager will send 
     * (1, getTotalEntryPoints) as parameters.
     * For some servers it is important to limit the number of
     * entry points actually retrieved; in this case it is recommended to the server to declare the
     * max_entry_points attribute in MydasServerConfig.xml. This max_entry_point should be
     * considered when implementing this method; however, MyDas will double check that.
     * @param start Initial row position on the entry points collection for this server
     * @param stop Final row position ont the entry points collection for this server
     * @return a Collection of DasEntryPoint objects
     * @throws DataSourceException to encapsulate any exceptions thrown by the datasource
     * and allow the MydasServlet to return a decent error header to the client.
     */
    public Collection<DasEntryPoint> getEntryPoints(Integer start, Integer stop) throws DataSourceException;

    /**
     * Returns the value to be returned from the entry_points command, specifically
     * the /DASEP/ENTRY_POINTS/@total attribute.
     *
     * This is a <b>mandatory</b> value so you must ensure that this method is implemented.
     * @return an integer being the total number of entry points on this datasource.
     * @throws DataSourceException to encapsulate any exceptions thrown by the datasource
     * and allow the MydasServlet to return a decent error header to the client.
     */
    public int getTotalEntryPoints () throws DataSourceException;
}
