package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class LiteratureInteractionDataSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_LIT_INT_DATA'
		Table table = assertTable('BIO_LIT_INT_DATA')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_LIT_INT_DATA_ID'
		assertPk table, 'BIO_LIT_INT_DATA_ID'
		assertSequence 'SEQ_BIO_DATA_ID'
		assertForeignKey table, 'BIO_DATA_LITERATURE', 'BIO_LIT_INT_DATA_ID' // extends Literature, table 'BIO_DATA_LITERATURE'

		// all properties are nullable because LiteratureAlterationData extends Literature

		// String effect
		assertColumn table, 'EFFECT', 'varchar(255)', true

		// String etlId
		assertColumn table, 'ETL_ID', 'varchar(255)', true

		// String interactionMode
		assertColumn table, 'INTERACTION_MODE', 'varchar(255)', true

		// LiteratureModelData inVitroModel
		assertForeignKeyColumn table, 'BIO_LIT_MODEL_DATA', 'IN_VITRO_MODEL_ID',
				'bigint', true // table 'BIO_LIT_MODEL_DATA'

		// LiteratureModelData inVivoModel
		assertForeignKeyColumn table, 'BIO_LIT_MODEL_DATA', 'IN_VIVO_MODEL_ID',
				'bigint', true // table 'BIO_LIT_MODEL_DATA'

		// String localization
		assertColumn table, 'LOCALIZATION', 'varchar(255)', true

		// String mechanism
		assertColumn table, 'MECHANISM', 'varchar(255)', true

		// String region
		assertColumn table, 'REGION', 'varchar(255)', true

		// String regulation
		assertColumn table, 'REGULATION', 'varchar(255)', true

		// String sourceComponent
		assertColumn table, 'SOURCE_COMPONENT', 'varchar(255)', true

		// String sourceGeneId
		assertColumn table, 'SOURCE_GENE_ID', 'varchar(255)', true

		// String targetComponent
		assertColumn table, 'TARGET_COMPONENT', 'varchar(255)', true

		// String targetGeneId
		assertColumn table, 'TARGET_GENE_ID', 'varchar(255)', true

		// String techniques
		assertColumn table, 'TECHNIQUES', 'varchar(255)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
