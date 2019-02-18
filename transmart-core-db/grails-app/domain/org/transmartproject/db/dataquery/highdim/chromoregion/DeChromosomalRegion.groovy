/*
 * Copyright © 2013-2014 The Hyve B.V.
 *
 * This file is part of transmart-core-db.
 *
 * Transmart-core-db is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * transmart-core-db.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmartproject.db.dataquery.highdim.chromoregion

import org.transmartproject.core.dataquery.highdim.chromoregion.Region
import org.transmartproject.db.dataquery.highdim.DeGplInfo
import org.transmartproject.db.dataquery.highdim.acgh.DeSubjectAcghData
import org.transmartproject.db.dataquery.highdim.rnaseq.DeSubjectRnaseqData

class DeChromosomalRegion implements Region {

    String  chromosome
    String  cytoband
    Long    end
    Long    geneId
    String  geneSymbol
    String  gplId
    String  name
    Integer numberOfProbes
    Long    start

    String organism // unused

    static hasMany = [dataRowsRnaSeq: DeSubjectRnaseqData,
                      dataRowsAcgh: DeSubjectAcghData]

    static belongsTo = [platform: DeGplInfo]

    static mappedBy = [dataRowsRnaSeq: 'region',
                       dataRowsAcgh: 'region']

    static mapping = {
        table          schema: 'deapp'
        id             column:  'region_id',  generator: 'assigned'
        version false

        end            column: 'end_bp'
        gplId          insertable: false, updateable: false
        name           column: 'region_name'
        numberOfProbes column: 'num_probes'
        platform       column: 'gpl_id'
        start          column: 'start_bp'
    }
    
    static constraints = {
        chromosome     nullable: true, maxSize: 2
        cytoband       nullable: true, maxSize: 100
        end            nullable: true
        geneId         nullable: true
        geneSymbol     nullable: true, maxSize: 100
        name           nullable: true, maxSize: 100
        numberOfProbes nullable: true
        organism       nullable: true, maxSize: 200
        platform       nullable: true
        start          nullable: true
    }

}
