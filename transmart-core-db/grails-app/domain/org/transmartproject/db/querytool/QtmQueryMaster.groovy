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

class QtmQueryMaster {

    Date createDate
    Date deleteDate
    String deleteFlag = 'N'/* 'N'/'Y' */
    String generatedSql
    String groupId
    String i2b2RequestXml
    String masterTypeCd
    String name
    Long pluginId
    String requestXml
    String userId

    static hasMany = [queryInstances: QtmQueryInstance]

    static mapping = {
        table          schema: 'I2B2DEMODATA'
        /* use sequence instead of identity because our Oracle schema doesn't
         * have a trigger that fills the column in this case */
        id             column: 'query_master_id', generator: 'sequence', params: [sequence: 'i2b2demodata.qtm_sq_qm_qmid']
        version false

        generatedSql   type:   'text'
        requestXml     type:   'text'
        i2b2RequestXml column: 'I2B2_REQUEST_XML', type: 'text'
    }

    static constraints = {
        deleteDate     nullable: true
        deleteFlag     nullable: true, maxSize: 3
        generatedSql   nullable: true
        groupId        maxSize:  50
        i2b2RequestXml nullable: true
        masterTypeCd   nullable: true, maxSize: 2000
        name           maxSize:  250
        pluginId       nullable: true
        requestXml     nullable: true
        userId         maxSize:  50
    }

    String toString() {
        getClass().canonicalName + "[${attached?'attached':'not attached'}" +
                "] [ id=$id, name=$name, createDate=$createDate ]"
    }
}
