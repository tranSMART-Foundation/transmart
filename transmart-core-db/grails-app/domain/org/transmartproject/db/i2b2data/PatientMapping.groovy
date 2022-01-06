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

package org.transmartproject.db.i2b2data

import org.transmartproject.core.dataquery.PatientMapping
import org.transmartproject.core.dataquery.Sex
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping

class PatientMapping implements PatientMapping {

    // private
    String sourcesystemCd

    // unused
    String patientIde
    String patientIdeSource
    String patientIdeStatus
    String projectId
    Date   updateDate
    Date   uploadDate
    Date   downloadDate
    Date   importDate
    Long   uploadId

    static mapping = {
        table 'i2b2demodata.patient_mapping'
        id         generator: 'assigned', column: 'patient_num'
        version false
    }

    static constraints = {
        patientIde       nullable: false, maxSize: 200
        patientIdeSource nullable: false, maxSize: 50
        patientIdeStatus nullable: true,  maxSize: 50
        projectId        nullable: false, maxSize: 50
        sourcesystemCd   nullable: true,  maxSize: 50
        downloadDate     nullable: true
        importDate       nullable: true
        updateDate       nullable: true
        uploadDate       nullable: true
        uploadId         nullable: true
    }

}
