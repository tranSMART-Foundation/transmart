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

package org.transmartproject.db.dataquery.clinical

import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.clinical.ClinicalVariableColumn

@CompileStatic
@ToString(includes = ['patientId', 'data'])
class PatientIdAnnotatedDataRow implements DataRow<ClinicalVariableColumn, Object> {

    Long patientId

    List data

    Map<ClinicalVariableColumn, Integer> columnToIndex

    String getLabel() {
        throw new UnsupportedOperationException()
    }

    def getAt(int index) {
        data[index]
    }

    def getAt(ClinicalVariableColumn column) {
        data[columnToIndex[column]]
    }

    Iterator iterator() {
        data.iterator()
    }
}
