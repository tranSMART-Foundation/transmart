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

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class DeVariantSubjectDetailCoreDb implements Serializable {

    String alt
    String chr
    String filter
    String format
    String info
    Long pos
    String quality
    String ref
    String rsId
    String variant

    static belongsTo = [dataset: DeVariantDatasetCoreDb] //TODO: implement constraint on dataset

    static constraints = {
        alt     nullable: true
        filter  nullable: true
        format  nullable: true
        info    nullable: true
        quality nullable: true
        variant nullable: true
    }

    static mapping = {
        table 'deapp.de_variant_subject_detail'
        id composite: ['dataset', 'rsId', 'chr', 'pos']
        version   false

//        alt     column: 'alt'
//	chr     column: 'chr'
//        dataset column: 'dataset_id'
//        filter  column: 'filter'
//        format  column: 'format'
//        info    column: 'info'
//	pos     column: 'pos'
        quality column: 'qual'
//        ref     column: 'ref'
//        rsId    column: 'rs_id'
        variant column: 'variant_value', sqlType: 'clob'
    }
}
