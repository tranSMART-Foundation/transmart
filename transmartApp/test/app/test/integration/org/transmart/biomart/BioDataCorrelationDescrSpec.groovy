package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioDataCorrelationDescrSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_DATA_CORREL_DESCR'
		Table table = assertTable('BIO_DATA_CORREL_DESCR')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_DATA_CORREL_DESCR_ID'
		assertPk table, 'BIO_DATA_CORREL_DESCR_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String correlation
		assertColumn table, 'CORRELATION', 'varchar(255)'

		// String description
		assertColumn table, 'DESCRIPTION', 'varchar(255)'

		// String source
		assertColumn table, 'SOURCE', 'varchar(255)'

		// String sourceCode
		assertColumn table, 'SOURCE_CODE', 'varchar(255)'

		// String status
		assertColumn table, 'STATUS', 'varchar(255)'

		// String typeName
		assertColumn table, 'TYPE_NAME', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
