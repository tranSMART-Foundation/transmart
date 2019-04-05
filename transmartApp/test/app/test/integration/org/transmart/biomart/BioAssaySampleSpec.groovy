package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssaySampleSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_ASSAY_SAMPLE'
		Table table = assertTable('BIO_ASSAY_SAMPLE')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID']
		assertPk table, 'id'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Long bioAssayId
		assertColumn table, 'BIO_ASSAY_ID', 'bigint'

		// Long bioClinicTrialTimepointId
		assertColumn table, 'BIO_CLINIC_TRIAL_TIMEPOINT_ID', 'bigint'

		// Long bioSampleId
		assertColumn table, 'BIO_SAMPLE_ID', 'bigint'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
