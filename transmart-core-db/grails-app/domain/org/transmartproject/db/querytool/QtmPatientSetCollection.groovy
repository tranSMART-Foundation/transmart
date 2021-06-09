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

import org.transmartproject.db.i2b2data.PatientDimension

class QtmPatientSetCollection {

    Long            setIndex

    static belongsTo = [patient: PatientDimension,
			resultInstance: QtmQueryResultInstance]

    static mapping = {
        table          schema:   'I2B2DEMODATA'
        id             column:   'patient_set_coll_id', generator: 'identity'
	version false
        sort           setIndex: 'asc'

        patient        column:   'patient_num'
	resultInstance column:   'result_instance_id'
    }

    static constraints = {
	patient          nullable: true
        resultInstance   nullable: true
	setIndex         nullable: true
    }
}
