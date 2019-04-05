package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class LiteratureProteinEffectDataSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_LIT_PE_DATA'
		Table table = assertTable('BIO_LIT_PE_DATA')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_LIT_PE_DATA_ID'
		assertPk table, 'BIO_LIT_PE_DATA_ID'
		assertSequence 'SEQ_BIO_DATA_ID'
		assertForeignKey table, 'BIO_DATA_LITERATURE', 'BIO_LIT_PE_DATA_ID' // extends Literature, table 'BIO_DATA_LITERATURE'

		// all properties are nullable because LiteratureAlterationData extends Literature

		// String description
		assertColumn table, 'DESCRIPTION', 'varchar(255)', true

		// String etlId
		assertColumn table, 'ETL_ID', 'varchar(255)', true

		// LiteratureModelData inVitroModel
		assertForeignKeyColumn table, 'BIO_LIT_MODEL_DATA', 'IN_VITRO_MODEL_ID',
				'bigint', true // table 'BIO_LIT_MODEL_DATA'

		// LiteratureModelData inVivoModel
		assertForeignKeyColumn table, 'BIO_LIT_MODEL_DATA', 'IN_VIVO_MODEL_ID',
				'bigint', true // table 'BIO_LIT_MODEL_DATA'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
