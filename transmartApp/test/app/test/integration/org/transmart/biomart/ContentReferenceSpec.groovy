package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class ContentReferenceSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_CONTENT_REFERENCE'
		Table table = assertTable('BIO_CONTENT_REFERENCE')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_CONTENT_REFERENCE_ID'
		assertPk table, 'BIO_CONTENT_REFERENCE_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Long bioDataId
		assertColumn table, 'BIO_DATA_ID', 'bigint'

		// Content content
		// content column: 'BIO_CONTENT_ID'
		assertForeignKeyColumn table, 'BIO_CONTENT', 'BIO_CONTENT_ID' // table 'BIO_CONTENT'

		// String type
		// type column: 'CONTENT_REFERENCE_TYPE'
		// type maxSize: 400
		assertColumn table, 'CONTENT_REFERENCE_TYPE', 'varchar(400)'

		// BioAssayAnalysis.files joinTable: [name: 'BIO_CONTENT_REFERENCE', key: 'BIO_DATA_ID', column: 'BIO_CONTENT_REFERENCE_ID']
		assertForeignKey table, 'BIO_ASSAY_ANALYSIS', 'BIO_DATA_ID' // table 'BIO_ASSAY_ANALYSIS'

		// Experiment.files joinTable: [name: 'BIO_CONTENT_REFERENCE', key: 'BIO_DATA_ID', column: 'BIO_CONTENT_REFERENCE_ID']
		assertForeignKey table, 'BIO_EXPERIMENT', 'BIO_DATA_ID' // table 'BIO_EXPERIMENT'

		// Literature.files joinTable: [name: 'BIO_CONTENT_REFERENCE', key: 'BIO_DATA_ID', column: 'BIO_CONTENT_REFERENCE_ID']
		assertForeignKey table, 'BIO_DATA_LITERATURE', 'BIO_DATA_ID' // table 'BIO_DATA_LITERATURE'

		// redundant fk from pk to pk
		assertForeignKey table, 'BIO_CONTENT_REFERENCE', 'BIO_CONTENT_REFERENCE_ID'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
