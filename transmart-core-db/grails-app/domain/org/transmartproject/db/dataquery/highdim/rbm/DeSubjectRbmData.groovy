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

package org.transmartproject.db.dataquery.highdim.rbm

import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.i2b2data.PatientDimension

class DeSubjectRbmData {

    BigDecimal logIntensity
    String unit
    BigDecimal value
    BigDecimal zscore

    static hasMany = [annotations: DeRbmAnnotation]

    static belongsTo = [annotations: DeRbmAnnotation,
			assay: DeSubjectSampleMapping,
			patient: PatientDimension]

    static mapping = {
        table 'deapp.de_subject_rbm_data'
        id generator: 'sequence', params: [sequence: 'deapp.de_subject_rbm_data_seq']
        version false

        annotations joinTable: [
//        assay column: 'assay_id'
//        patient column: 'patient_id'
            column: 'annotation_id',
            key: 'data_id',
            name: 'deapp.de_rbm_data_annotation_join']
    }

    static constraints = {
        assay nullable: true
        logIntensity nullable: true, scale: 17
        unit nullable: true, maxSize: 150
        value nullable: true, scale: 17
        zscore nullable: true, scale: 17
    }
}
