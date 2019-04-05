package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class PatientEventAttributeSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_PATIENT_EVENT_ATTR'
		Table table = assertTable('BIO_PATIENT_EVENT_ATTR')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID']
		assertPk table, 'id'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String attributeNumericValue
		// attributeNumericValue nullable: true, maxSize: 400
		assertColumn table, 'ATTRIBUTE_NUMERIC_VALUE', 'varchar(400)', true

		// String attributeTextValue
		// attributeTextValue nullable: true, maxSize: 400
		assertColumn table, 'ATTRIBUTE_TEXT_VALUE', 'varchar(400)', true

		// Long bioClinicTrialAttributeId
		// bioClinicTrialAttributeId column: 'BIO_CLINIC_TRIAL_ATTR_ID'
		assertColumn table, 'BIO_CLINIC_TRIAL_ATTR_ID', 'bigint'

		// String bioPatientAttrCode
		// bioPatientAttrCode maxSize: 400
		assertColumn table, 'BIO_PATIENT_ATTR_CODE', 'varchar(400)'

		// Long bioPatientAttributeId
		assertColumn table, 'BIO_PATIENT_ATTRIBUTE_ID', 'bigint'

		// Long bioPatientEventId
		assertColumn table, 'BIO_PATIENT_EVENT_ID', 'bigint'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
