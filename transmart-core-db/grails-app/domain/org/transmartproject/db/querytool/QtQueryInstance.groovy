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

class QtQueryInstance {

    String          batchMode
    String          deleteFlag = 'N'
    Date            endDate
    String          groupId
    String          message
    QtQueryMaster   queryMaster
    Date            startDate
    Integer         statusTypeId
    String          userId

    static hasMany = [queryResults: QtQueryResultInstance]

    static belongsTo = QtQueryMaster

    static mapping = {
        table       schema: 'I2B2DEMODATA'
        /* use sequence instead of identity because our Oracle schema doesn't
         * have a trigger that fills the column in this case */
	id          column: 'query_instance_id', generator: 'sequence',
            params: [sequence: 'qt_sq_qi_qiid', schema: 'i2b2demodata']
	version false

        queryMaster column: 'query_master_id'
    }

    static constraints = {
        batchMode      nullable:   true,   maxSize:   50
        deleteFlag     nullable:   true,   maxSize:   3
        endDate        nullable:   true
        groupId        maxSize:    50
        message        nullable:   true
        statusTypeId   nullable:   true
        userId         maxSize:    50
    }
}
