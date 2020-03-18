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

package org.transmartproject.db.dataquery.highdim

import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.assay.SampleType
import org.transmartproject.core.dataquery.assay.Timepoint
import org.transmartproject.core.dataquery.assay.TissueType
import org.transmartproject.db.i2b2data.PatientDimension

class DeSubjectSampleMapping implements Assay {

    String assayUid
    String categoryCd
    String conceptCode
    String dataUid
    Long omicPatientId
    String omicSourceStudy
    String patientInTrialId
    String patientUid
    DeGplInfo platform
    String platformType
    String platformTypeCd
    String rbmPanel
    String sampleCode
    Long sampleId
    String sampleTypeCd
    String sampleTypeName
    String siteId
    String sourceCd
    String subjectType
    String timepointCd
    String timepointName
    String tissueTypeCd
    String tissueTypeName
    String trialName

    static transients = ['timepoint', 'sampleType', 'tissueType']

    static belongsTo = [patient: PatientDimension]

    static mapping = {
        table            schema: 'deapp'
        id               column: 'assay_id',    generator: 'assigned'
	version false

        patient          column: 'patient_id',  cascade: 'save-update'
        patientInTrialId column: 'subject_id'
        platform         column: 'gpl_id',      cascade: 'save-update'
        platformType     column: 'platform'
        platformTypeCd   column: 'platform_cd'
        sampleCode       column: 'sample_cd'
        sampleTypeName   column: 'sample_type'
        timepointName    column: 'timepoint'
        tissueTypeName   column: 'tissue_type'

        sort           id:     'asc'
    }

    static constraints = {
        assayUid         nullable: true, maxSize: 100
        categoryCd       nullable: true, maxSize: 1000
        conceptCode      nullable: true, maxSize: 1000
        dataUid          nullable: true, maxSize: 100
        omicPatientId    nullable: true
        omicSourceStudy  nullable: true, maxSize: 200
        patient          nullable: true
        patientInTrialId nullable: true, maxSize: 100
        patientUid       nullable: true, maxSize: 50
        platform         nullable: true
        platformType     nullable: true, maxSize: 50
        platformTypeCd   nullable: true, maxSize: 50
        rbmPanel         nullable: true, maxSize: 50
        sampleCode       nullable: true, maxSize: 200
        sampleId         nullable: true
        sampleTypeCd     nullable: true, maxSize: 50
        sampleTypeName   nullable: true, maxSize: 100
        siteId           nullable: true, maxSize: 100
        sourceCd         nullable: true, maxSize: 50
        subjectType      nullable: true, maxSize: 100
        timepointCd      nullable: true, maxSize: 50
        timepointName    nullable: true, maxSize: 100
        tissueTypeCd     nullable: true, maxSize: 50
        tissueTypeName   nullable: true, maxSize: 100
        trialName        nullable: true, maxSize: 100
	}

    //  region Properties with values generated on demand
    Timepoint getTimepoint() {
        new Timepoint(code: timepointCd, label: timepointName)
    }

    SampleType getSampleType() {
        new SampleType(code: sampleTypeCd, label: sampleTypeName)
    }

    TissueType getTissueType() {
        new TissueType(code: tissueTypeCd, label: tissueTypeName)
    }
    //  endregion
}
