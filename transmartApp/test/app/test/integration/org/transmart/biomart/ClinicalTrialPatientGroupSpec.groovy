package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class ClinicalTrialPatientGroupSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_CLINC_TRIAL_PT_GROUP'
		Table table = assertTable('BIO_CLINC_TRIAL_PT_GROUP')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_CLINICAL_TRIAL_P_GROUP_ID'
		assertPk table, 'BIO_CLINICAL_TRIAL_P_GROUP_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// ClinicalTrial clinicalTrial
		// clinicalTrial column: 'BIO_EXPERIMENT_ID'
		assertForeignKeyColumn table, 'BIO_CLINICAL_TRIAL', 'BIO_EXPERIMENT_ID' // table 'BIO_CLINICAL_TRIAL'

		// String description
		// description nullable: true, maxSize: 2000
		assertColumn table, 'DESCRIPTION', 'varchar(2000)', true

		// String name
		// name nullable: true, maxSize: 1020
		assertColumn table, 'NAME', 'varchar(1020)', true

		// Long numberOfPatients
		// numberOfPatients nullable: true
		assertColumn table, 'NUMBER_OF_PATIENTS', 'bigint', true

		// String patientGroupTypeCode
		// patientGroupTypeCode nullable: true, maxSize: 400
		assertColumn table, 'PATIENT_GROUP_TYPE_CODE', 'varchar(400)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
