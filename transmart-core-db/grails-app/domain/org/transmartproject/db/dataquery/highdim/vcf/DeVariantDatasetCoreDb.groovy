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

package org.transmartproject.db.dataquery.highdim.vcf

class DeVariantDatasetCoreDb {

    String datasourceId
    Date etlDate
    String etlId
    String genome
    String id
    String metadataComment
    String variantDatasetType

    static hasMany = [details: DeVariantSubjectDetailCoreDb,
		      summaries: DeVariantSubjectSummaryCoreDb]

    static constraints = {
        datasourceId nullable: true
        etlDate nullable: true
        etlId nullable: true
        metadataComment nullable: true
        variantDatasetType nullable: true
    }

    static mapping = {
        table 'deapp.de_variant_dataset'
        id column:'dataset_id', generator: 'assigned'
        version false
    }
}
