package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class ClinicalTrialTimePointSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_CLINC_TRIAL_TIME_PT'
		Table table = assertTable('BIO_CLINC_TRIAL_TIME_PT')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_CLINC_TRIAL_TM_PT_ID'
		assertPk table, 'BIO_CLINC_TRIAL_TM_PT_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// ClinicalTrial clinicalTrial
		// clinicalTrial column: 'BIO_EXPERIMENT_ID'
		assertForeignKeyColumn table, 'BIO_CLINICAL_TRIAL', 'BIO_EXPERIMENT_ID' // table 'BIO_CLINICAL_TRIAL'

		// Date endDate
		assertColumn table, 'END_DATE', 'timestamp'

		// Date startDate
		assertColumn table, 'START_DATE', 'timestamp'

		// String timePoint
		assertColumn table, 'TIME_POINT', 'varchar(255)'

		// String timePointCode
		assertColumn table, 'TIME_POINT_CODE', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
