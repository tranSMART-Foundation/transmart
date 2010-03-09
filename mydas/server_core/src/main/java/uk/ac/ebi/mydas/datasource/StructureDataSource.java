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

import java.util.Collection;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.BadReferenceObjectException;
import uk.ac.ebi.mydas.model.structure.DasStructure;


/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 *
 * Interface to implement in the case the datasource have the capability of return 3d sequences
 */
public interface StructureDataSource {

    /**
     * This method returns a DasStructure object, describing the structure
     * of the structureId passed in as argument.
     * @param structureId This is the ID of the reference structure.
     *
     * @return A DasStructure object.  a protein 3D structure, including metadata and coordinates. 
     * @throws BadReferenceObjectException in the event that your server does not include information about this segment.
     * @throws DataSourceException should be thrown if there is any
     * fatal problem with loading this data source. 
     */
    public DasStructure getStructure (String structureId,Collection<String> chainIdCollection,Collection<String> modelIdCollection) throws BadReferenceObjectException, DataSourceException;

}
