package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioSpeciesOrganismSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_SPECIES_ORGANISM'
		Table table = assertTable('BIO_SPECIES_ORGANISM')

		// id column: 'ID'
		assertPk table, 'ID', AUTOINC

		// String organism
		// organism nullable: true, maxSize: 200
		assertColumn table, 'ORGANISM', 'varchar(200)', true

		// String species
		// species nullable: true, maxSize: 200
		assertColumn table, 'SPECIES', 'varchar(200)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
