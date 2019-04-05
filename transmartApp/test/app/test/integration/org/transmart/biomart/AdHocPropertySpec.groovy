package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class AdHocPropertySpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_AD_HOC_PROPERTY'
		Table table = assertTable('BIO_AD_HOC_PROPERTY')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'AD_HOC_PROPERTY_ID'
		assertPk table, 'AD_HOC_PROPERTY_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String key
		// key column: 'PROPERTY_KEY'
		assertColumn table, 'PROPERTY_KEY', 'varchar(255)'

		// Long objectId
		// objectId column: 'BIO_DATA_ID'
		assertColumn table, 'BIO_DATA_ID', 'bigint'

		// String value
		// value column: 'PROPERTY_VALUE'
		assertColumn table, 'PROPERTY_VALUE', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
