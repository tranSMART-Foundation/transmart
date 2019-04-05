package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class CurationDatasetSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_CURATION_DATASET'
		Table table = assertTable('BIO_CURATION_DATASET')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_CURATION_DATASET_ID'
		assertPk table, 'BIO_CURATION_DATASET_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Long bioAnalysisPlatformId
		// bioAnalysisPlatformId column: 'BIO_ASY_ANALYSIS_PLATFORM_ID'
		assertColumn table, 'BIO_ASY_ANALYSIS_PLATFORM_ID', 'bigint'

		// Date createDate
		// createDate nullable: true
		assertColumn table, 'CREATE_DATE', 'timestamp', true

		// Long creator
		// creator nullable: true
		assertColumn table, 'CREATOR', 'bigint', true

		// String curationType
		// curationType column: 'BIO_CURATION_TYPE'
		// curationType maxSize: 400
		assertColumn table, 'BIO_CURATION_TYPE', 'varchar(400)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
