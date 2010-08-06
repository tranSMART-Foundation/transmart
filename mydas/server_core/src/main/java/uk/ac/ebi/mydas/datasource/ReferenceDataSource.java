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
}
