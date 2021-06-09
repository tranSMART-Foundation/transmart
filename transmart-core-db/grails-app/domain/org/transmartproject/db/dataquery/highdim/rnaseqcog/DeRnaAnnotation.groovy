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

package org.transmartproject.db.dataquery.highdim.rnaseqcog

import org.transmartproject.db.dataquery.highdim.DeGplInfo

class DeRnaAnnotation implements Serializable {

    String geneId
    String geneSymbol
    String gplId
    Long   probesetId
    String transcriptId	              // the Entrez accession; "primary external id"

    // irrelevant
    String organism

    static transients = ['id']

    static hasMany = ['dataRows': DeSubjectRnaData]

    static belongsTo = [ platform: DeGplInfo ]

    static mappedBy = ['dataRows': 'annotation']

    static mapping = {
        table    schema: 'deapp'
        id       name: 'transcriptId', generator: 'assigned'
        version  false

        gplId       insertable: false, updateable: false
        platform column: 'gpl_id'
    }

    static constraints = {
        geneId       nullable: true, maxSize: 100
        geneSymbol   nullable: true, maxSize: 100
        transcriptId maxSize: 50
        platform     nullable: true
        gplId        nullable: true, maxSize: 50
        organism     nullable: true, maxSize: 100
        probesetId   nullable: true
    }

    void setId(String id) {
        transcriptId = id
    }

    String getId() {
        transcriptId
    }
}
