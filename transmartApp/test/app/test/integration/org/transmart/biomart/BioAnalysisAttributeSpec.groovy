package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAnalysisAttributeSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_ANALYSIS_ATTRIBUTE'
		Table table = assertTable('BIO_ANALYSIS_ATTRIBUTE')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_ANALYSIS_ATTRIBUTE_ID'
		assertPk table, 'BIO_ANALYSIS_ATTRIBUTE_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Long bioAssayAnalysisID
		// bioAssayAnalysisID column: 'BIO_ASSAY_ANALYSIS_ID'
		assertColumn table, 'BIO_ASSAY_ANALYSIS_ID', 'bigint'

		// String sourceCode
		// sourceCode nullable: true
		// sourceCode column: 'SOURCE_CD'
		assertColumn table, 'SOURCE_CD', 'varchar(255)', true

		// String studyID
		// studyID nullable: true
		// studyID column: 'STUDY_ID'
		assertColumn table, 'STUDY_ID', 'varchar(255)', true

		// Long termID
		// termID nullable: true
		// termID column: 'TERM_ID'
		assertColumn table, 'TERM_ID', 'bigint', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
