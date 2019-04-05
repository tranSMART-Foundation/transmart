package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class LiteratureInteractionModelMVSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_LIT_INT_MODEL_MV'
		Table table = assertTable('BIO_LIT_INT_MODEL_MV')

		// id column: 'BIO_LIT_INT_DATA_ID'
		assertPk table, 'BIO_LIT_INT_DATA_ID', AUTOINC

		// String experimentalModel
		assertColumn table, 'EXPERIMENTAL_MODEL', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
