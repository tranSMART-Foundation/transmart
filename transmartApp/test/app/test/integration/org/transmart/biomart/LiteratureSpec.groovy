package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class LiteratureSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_DATA_LITERATURE'
		Table table = assertTable('BIO_DATA_LITERATURE')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_DATA_ID'
		assertPk table, 'BIO_DATA_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Long bioCurationDatasetId
		assertColumn table, 'BIO_CURATION_DATASET_ID', 'bigint'

		// String dataType
		assertColumn table, 'DATA_TYPE', 'varchar(255)'

		// LiteratureReferenceData reference
		// reference column: 'BIO_LIT_REF_DATA_ID'
		assertForeignKeyColumn table, 'BIO_LIT_REF_DATA', 'BIO_LIT_REF_DATA_ID' // table 'BIO_LIT_REF_DATA'

		// String statement
		assertColumn table, 'STATEMENT', 'varchar(255)'

		// String statementStatus
		assertColumn table, 'STATEMENT_STATUS', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
