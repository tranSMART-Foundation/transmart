package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class LiteratureModelDataSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_LIT_MODEL_DATA'
		Table table = assertTable('BIO_LIT_MODEL_DATA')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_LIT_MODEL_DATA_ID'
		assertPk table, 'BIO_LIT_MODEL_DATA_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String animalWildType
		assertColumn table, 'ANIMAL_WILD_TYPE', 'varchar(255)'

		// String bodySubstance
		assertColumn table, 'BODY_SUBSTANCE', 'varchar(255)'

		// String cellLine
		assertColumn table, 'CELL_LINE', 'varchar(255)'

		// String cellType
		assertColumn table, 'CELL_TYPE', 'varchar(255)'

		// String challenge
		assertColumn table, 'CHALLENGE', 'varchar(255)'

		// String component
		assertColumn table, 'COMPONENT', 'varchar(255)'

		// String controlChallenge
		assertColumn table, 'CONTROL_CHALLENGE', 'varchar(255)'

		// String description
		assertColumn table, 'DESCRIPTION', 'varchar(255)'

		// String etlId
		assertColumn table, 'ETL_ID', 'varchar(255)'

		// String experimentalModel
		assertColumn table, 'EXPERIMENTAL_MODEL', 'varchar(255)'

		// String geneId
		assertColumn table, 'GENE_ID', 'varchar(255)'

		// String modelType
		assertColumn table, 'MODEL_TYPE', 'varchar(255)'

		// String sentization
		assertColumn table, 'SENTIZATION', 'varchar(255)'

		// String stimulation
		assertColumn table, 'STIMULATION', 'varchar(255)'

		// String tissue
		assertColumn table, 'TISSUE', 'varchar(255)'

		// String zygosity
		assertColumn table, 'ZYGOSITY', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
