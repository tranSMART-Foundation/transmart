package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class CellLineSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_CELL_LINE'
		Table table = assertTable('BIO_CELL_LINE')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_CELL_LINE_ID'
		assertPk table, 'BIO_CELL_LINE_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String attcNumber
		assertColumn table, 'ATTC_NUMBER', 'varchar(255)'

		// Long bioDiseaseId
		assertColumn table, 'BIO_DISEASE_ID', 'bigint'

		// String cellLineName
		assertColumn table, 'CELL_LINE_NAME', 'varchar(255)'

		// String description
		assertColumn table, 'DESCRIPTION', 'varchar(255)'

		// String disease
		assertColumn table, 'DISEASE', 'varchar(255)'

		// String diseaseStage
		assertColumn table, 'DISEASE_STAGE', 'varchar(255)'

		// String diseaseSubtype
		assertColumn table, 'DISEASE_SUBTYPE', 'varchar(255)'

		// String metastaticSite
		assertColumn table, 'METASTATIC_SITE', 'varchar(255)'

		// String origin
		assertColumn table, 'ORIGIN', 'varchar(255)'

		// String primarySite
		assertColumn table, 'PRIMARY_SITE', 'varchar(255)'

		// String species
		assertColumn table, 'SPECIES', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
