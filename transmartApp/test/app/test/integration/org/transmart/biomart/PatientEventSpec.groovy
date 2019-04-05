package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class PatientEventSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_PATIENT_EVENT'
		Table table = assertTable('BIO_PATIENT_EVENT')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_PATIENT_EVENT_ID'
		assertPk table, 'BIO_PATIENT_EVENT_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Long bioClinicTrialTimepointId
		assertColumn table, 'BIO_CLINIC_TRIAL_TIMEPOINT_ID', 'bigint'

		// Long bioPatientId
		assertColumn table, 'BIO_PATIENT_ID', 'bigint'

		// String eventCode
		// eventCode nullable: true, maxSize: 400
		assertColumn table, 'EVENT_CODE', 'varchar(400)', true

		// Date eventDate
		// eventDate nullable: true
		assertColumn table, 'EVENT_DATE', 'timestamp', true

		// String eventTypeCode
		// eventTypeCode nullable: true, maxSize: 400
		assertColumn table, 'EVENT_TYPE_CODE', 'varchar(400)', true

		// String site
		// site nullable: true, maxSize: 800
		assertColumn table, 'SITE', 'varchar(800)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
