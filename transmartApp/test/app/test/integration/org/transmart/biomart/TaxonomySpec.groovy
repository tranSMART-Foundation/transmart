package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class TaxonomySpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_TAXONOMY'
		Table table = assertTable('BIO_TAXONOMY')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_TAXONOMY_ID'
		assertPk table, 'BIO_TAXONOMY_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String label
		// label column: 'TAXON_LABEL'
		assertColumn table, 'TAXON_LABEL', 'varchar(255)'

		// String name
		// name column: 'TAXON_NAME'
		assertColumn table, 'TAXON_NAME', 'varchar(255)'

		// String ncbiTaxId
		assertColumn table, 'NCBI_TAX_ID', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
