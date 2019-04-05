package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssayDatasetSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_ASSAY_DATASET'
		Table table = assertTable('BIO_ASSAY_DATASET')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_ASSAY_DATASET_ID'
		assertPk table, 'BIO_ASSAY_DATASET_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// BioAssay bioAssay
		assertForeignKeyColumn table, 'BIO_ASSAY', 'BIO_ASSAY_ID' // table 'BIO_ASSAY'

		// Date createDate
		// createDate nullable: true
		assertColumn table, 'CREATE_DATE', 'timestamp', true

		// String criteria
		// criteria column: 'DATASET_CRITERIA'
		// criteria nullable: true, maxSize: 2000
		assertColumn table, 'DATASET_CRITERIA', 'varchar(2000)', true

		// String description
		// description column: 'DATASET_DESCRIPTION'
		// description nullable: true, maxSize: 2000
		assertColumn table, 'DATASET_DESCRIPTION', 'varchar(2000)', true

		// Experiment experiment
		// experiment column: 'BIO_EXPERIMENT_ID'
		assertForeignKeyColumn table, 'BIO_EXPERIMENT', 'BIO_EXPERIMENT_ID' // table 'BIO_EXPERIMENT'

		// String name
		// name column: 'DATASET_NAME'
		// name nullable: true, maxSize: 800
		assertColumn table, 'DATASET_NAME', 'varchar(800)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
