package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioDataAttributeSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_DATA_ATTRIBUTE'
		Table table = assertTable('BIO_DATA_ATTRIBUTE')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_DATA_ATTRIBUTE_ID'
		assertPk table, 'BIO_DATA_ATTRIBUTE_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Long bioDataId
		assertColumn table, 'BIO_DATA_ID', 'bigint'

		// String propertyCode
		assertColumn table, 'PROPERTY_CODE', 'varchar(255)'

		// String propertyUnit
		assertColumn table, 'PROPERTY_UNIT', 'varchar(255)'

		// String propertyValue
		assertColumn table, 'PROPERTY_VALUE', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
