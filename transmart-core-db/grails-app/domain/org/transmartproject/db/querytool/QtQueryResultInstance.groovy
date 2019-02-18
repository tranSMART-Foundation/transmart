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

package org.transmartproject.db.querytool

import org.transmartproject.core.dataquery.Patient
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.core.querytool.QueryStatus
import org.transmartproject.db.dataquery.clinical.PatientQuery
import org.transmartproject.db.dataquery.clinical.patientconstraints.PatientSetsConstraint
import org.transmartproject.db.i2b2data.PatientDimension

class QtQueryResultInstance implements QueryResult {

    String deleteFlag = 'N'
    String description
    Date endDate
    String errorMessage
    String obfuscMethod
    QtQueryInstance queryInstance
    Long realSetSize
    Short resultTypeId = 1
    Long setSize
    Date startDate
    Short statusTypeId

    static transients = ['username']

    static hasMany = [patientSet: QtPatientSetCollection,
                      patientsA:  PatientDimension]

    static belongsTo = QtQueryInstance

    static mapping = {
        table          schema: 'I2B2DEMODATA'
        /* use sequence instead of identity because our Oracle schema doesn't
         * have a trigger that fills the column in this case */
        id             column: 'result_instance_id', generator: 'sequence', params: [sequence: 'i2b2demodata.qt_sq_qri_qriid']
	version false

        errorMessage   column: 'message'
        patientsA      joinTable: [name:   'qt_patient_set_collection',
                                   key:    'result_instance_id',
                                   column: 'patient_num']
        queryInstance  column: 'query_instance_id', fetch: 'join'
    }

    static constraints = {
        deleteFlag     nullable:   true,   maxSize:   3
        description    nullable:   true,   maxSize:   200
        endDate        nullable:   true
        errorMessage   nullable:   true
        obfuscMethod   nullable:   true,   maxSize:   500
        realSetSize    nullable:   true
        setSize        nullable:   true
    }

    @Override
    QueryStatus getStatus() {
        QueryStatus.forId(statusTypeId)
    }

    @Override
    Set<Patient> getPatients() {
        new PatientQuery([new PatientSetsConstraint([this])]).list()
    }

    @Override
    String getUsername() {
        queryInstance.userId
    }
}
