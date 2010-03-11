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
import uk.ac.ebi.mydas.model.alignment.DasAlignment;


/**
 * @author Gustavo Salazar, EMBL-EBI, gsalazar@ebi.ac.uk
 *
 * Interface to implement in the case the datasource have the capability of return alignments
 */
public interface AlignmentDataSource {

    /**
     * This method returns a DasAlignment object. It represents one or more pairwise or multiple alignments. Such an alignment may be primary data, or simply a mapping between coordinate systems. Provision is made for both sequence and 3D alignments. 
     * @param alignmentId Selects the alignment to be returned, either by the alignment ID (for primary data alignments) or the ID of a row (for mapping alignments).
     * @param subjects Filters the rows to be returned in the alignment by the ID of one of the rows (e.g. a sequence within a sequence alignment). Also allows for specifying the number of other rows around the subject to be included in the alignment. Thus the alignment can be "sliced" horizontally.
     * @param subjectcoordsys The URI of the coordinate system the subject being queried belongs to. Used where the DAS source supports alignments containing different reference objects, such as mappings.
     * @param rowStart The start of an alternative way to restrict the rows included in the returned alignment, by providing an absolute range of rows. Useful for alignments with many rows, such as protein family alignments.
     * @param rowEnd The end of an alternative way to restrict the rows included in the returned alignment, by providing an absolute range of rows. Useful for alignments with many rows, such as protein family alignments.
     * @param colStart Specifies the start of a vertical slice of the alignment by restricting the columns to be included in the returned alignment. Useful for alignments with many columns, such as genomic DNA sequence alignments.
     * @param colEnd Specifies the end of a vertical slice of the alignment by restricting the columns to be included in the returned alignment. Useful for alignments with many columns, such as genomic DNA sequence alignments.
     * @return A DasAlignment object describing the alignment of the query passed in as argument.
     * @throws BadReferenceObjectException in the event that your server does not include information about this segment.
     * @throws DataSourceException should be thrown if there is any fatal problem with loading this data source. 
     */
    public DasAlignment getAlignment (String alignmentId,Collection<String> subjects,String subjectcoordsys,Integer rowStart, Integer rowEnd, Integer colStart, Integer colEnd) throws BadReferenceObjectException, DataSourceException;

}
