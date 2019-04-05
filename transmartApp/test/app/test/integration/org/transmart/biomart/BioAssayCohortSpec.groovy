package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssayCohortSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_ASSAY_COHORT'
		Table table = assertTable('BIO_ASSAY_COHORT')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_ASSAY_COHORT_ID'
		assertPk table, 'BIO_ASSAY_COHORT_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String cohortId
		assertColumn table, 'COHORT_ID', 'varchar(255)'

		// String cohortTitle
		assertColumn table, 'COHORT_TITLE', 'varchar(255)'

		// String disease
		assertColumn table, 'DISEASE', 'varchar(255)'

		// String longDesc
		assertColumn table, 'LONG_DESC', 'varchar(255)'

		// String organism
		assertColumn table, 'ORGANISM', 'varchar(255)'

		// String pathology
		assertColumn table, 'PATHOLOGY', 'varchar(255)'

		// String sampleType
		assertColumn table, 'SAMPLE_TYPE', 'varchar(255)'

		// String shortDesc
		assertColumn table, 'SHORT_DESC', 'varchar(255)'

		// String studyId
		assertColumn table, 'STUDY_ID', 'varchar(255)'

		// String treatment
		assertColumn table, 'TREATMENT', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
