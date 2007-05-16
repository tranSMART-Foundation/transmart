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
     * Extends the ReferenceDataSource inteface to allow the creation of an Annotation
     * data source.  The only significant difference is that a Reference data source can also
     * serve the sequenceString of the requested segment.
     * @param segmentReference being the name of the sequenceString being requested.
     * @return a DasSequence object, holding the sequenceString and start / end coordinates of the sequenceString
     * or null if the reference is not found.
     * @throws DataSourceException to encapsulate any exceptions thrown by the datasource
     * and allow the MydasServlet to return a decent error header to the client.
     */
    public DasSequence getSequence (String segmentReference) throws BadReferenceObjectException, DataSourceException;

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
     * Returns a Collection of DasEntryPoint objects to implement the entry_point command.
     * @return a Collection of DasEntryPoint objects
     * @throws DataSourceException to encapsulate any exceptions thrown by the datasource
     * and allow the MydasServlet to return a decent error header to the client.
     */
    public Collection<DasEntryPoint> getEntryPoints() throws DataSourceException;
}
