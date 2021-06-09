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

package org.transmartproject.db.dataquery.highdim.mrna

import org.transmartproject.db.biomarker.BioMarkerCoreDb
import org.transmartproject.db.dataquery.highdim.DeGplInfo

class DeMrnaAnnotationCoreDb {

    Long       geneId     // aka primary external id
    String     geneSymbol
    String     gplId
    String     organism
    String     probeId    // a string user probe name

    static transients = [ 'bioMarkerGene' ]

    static hasMany = [dataRows: DeSubjectMicroarrayDataCoreDb]

    static belongsTo = [ platform: DeGplInfo ]

    static mappedBy = [dataRows: 'probe']

    static mapping = {
        table 'deapp.de_mrna_annotation'
        id          column: 'probeset_id',       generator: 'assigned'
        version     false
        sort        id: 'asc'

        gplId       insertable: false, updateable: false
        platform    column: 'gpl_id'
    }

    static constraints = {
        geneId     nullable: true
        geneSymbol nullable: true, maxSize: 100
        gplId      nullable: true, maxSize: 50
        organism   nullable: true, maxSize: 100
        platform   nullable: true
        probeId    nullable: true, maxSize: 100
    }

    BioMarkerCoreDb getBioMarkerGene() {
        BioMarkerCoreDb.findByExternalId(geneId as String)
    }
}
