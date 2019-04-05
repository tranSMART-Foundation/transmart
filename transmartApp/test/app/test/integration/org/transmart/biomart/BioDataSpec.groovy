package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioDataSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_DATA_UID'
		Table table = assertTable('BIO_DATA_UID')

		// id column: 'BIO_DATA_ID'
		assertPk table, 'BIO_DATA_ID', AUTOINC

		// String type
		// type column: 'BIO_DATA_TYPE'
		assertColumn table, 'BIO_DATA_TYPE', 'varchar(255)'

		// String uniqueId
		assertColumn table, 'UNIQUE_ID', 'varchar(255)'

		// BioAssayAnalysis.uniqueIds joinTable: [name: 'BIO_DATA_UID', key: 'BIO_DATA_ID']
		assertForeignKey table, 'BIO_ASSAY_ANALYSIS', 'BIO_DATA_ID' // table 'BIO_ASSAY_ANALYSIS'

		// ConceptCode.bioDataUid joinTable: [name: 'BIO_DATA_UID', key: 'BIO_DATA_ID']
		assertForeignKey table, 'BIO_CONCEPT_CODE', 'BIO_DATA_ID' // table 'BIO_CONCEPT_CODE'

		// Experiment.uniqueIds joinTable: [name: 'BIO_DATA_UID', key: 'BIO_DATA_ID']
		assertForeignKey table, 'BIO_EXPERIMENT', 'BIO_DATA_ID' // table 'BIO_EXPERIMENT'

		// redundant fk from pk to pk
		assertForeignKey table, 'BIO_DATA_UID', 'BIO_DATA_ID'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
