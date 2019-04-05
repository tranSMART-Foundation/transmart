package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioDataExternalCodeSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_DATA_EXT_CODE'
		Table table = assertTable('BIO_DATA_EXT_CODE')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_DATA_EXT_CODE_ID'
		assertPk table, 'BIO_DATA_EXT_CODE_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Long bioDataId
		assertColumn table, 'BIO_DATA_ID', 'bigint'

		// String bioDataType
		// bioDataType nullable: true, maxSize: 100
		assertColumn table, 'BIO_DATA_TYPE', 'varchar(100)', true

		// String code
		// code maxSize: 500
		assertColumn table, 'CODE', 'varchar(500)'

		// String codeSource
		// codeSource nullable: true, maxSize: 400
		assertColumn table, 'CODE_SOURCE', 'varchar(400)', true

		// String codeType
		// codeType nullable: true, maxSize: 400
		assertColumn table, 'CODE_TYPE', 'varchar(400)', true

		// BioData.externalCodes joinTable: [name: 'BIO_DATA_EXT_CODE', key: 'BIO_DATA_ID', column: 'BIO_DATA_EXT_CODE_ID']
		assertForeignKey table, 'BIO_DATA_UID', 'BIO_DATA_ID' // table 'BIO_DATA_UID'

		// redundant fk from pk to pk
		assertForeignKey table, 'BIO_DATA_EXT_CODE', 'BIO_DATA_EXT_CODE_ID'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
