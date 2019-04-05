package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssayAnalysisDataIdxSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table name: 'BIO_ASY_ANALYSIS_DATA_IDX', schema: 'BIOMART'
		Table table = assertTable('BIO_ASY_ANALYSIS_DATA_IDX', 'BIOMART')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_ASY_ANALYSIS_DATA_IDX_ID'
		assertPk table, 'BIO_ASY_ANALYSIS_DATA_IDX_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Integer display_idx
		assertColumn table, 'DISPLAY_IDX', 'integer'

		// String ext_type
		assertColumn table, 'EXT_TYPE', 'varchar(255)'

		// Integer field_idx
		assertColumn table, 'FIELD_IDX', 'integer'

		// String field_name
		assertColumn table, 'FIELD_NAME', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
