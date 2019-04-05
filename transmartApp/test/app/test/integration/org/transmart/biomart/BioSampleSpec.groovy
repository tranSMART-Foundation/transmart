package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioSampleSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_SAMPLE'
		Table table = assertTable('BIO_SAMPLE')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_SAMPLE_ID'
		assertPk table, 'BIO_SAMPLE_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Long bioBankId
		// bioBankId nullable: true
		assertColumn table, 'BIO_BANK_ID', 'bigint', true

		// Long bioPatientEventId
		// bioPatientEventId nullable: true
		assertColumn table, 'BIO_PATIENT_EVENT_ID', 'bigint', true

		// BioSubject bioSubject
		assertForeignKeyColumn table, 'BIO_SUBJECT', 'BIO_SUBJECT_ID' // table 'BIO_SUBJECT'

		// CellLine cellLine
		// cellLine column: 'BIO_CELL_LINE_ID'
		assertForeignKeyColumn table, 'BIO_CELL_LINE', 'BIO_CELL_LINE_ID' // table 'BIO_CELL_LINE'

		// String characteristics
		// characteristics nullable: true, maxSize: 2000
		assertColumn table, 'CHARACTERISTICS', 'varchar(2000)', true

		// Experiment experiment
		assertForeignKeyColumn table, 'BIO_EXPERIMENT', 'EXPERIMENT_ID' // table 'BIO_EXPERIMENT'

		// String name
		// name column: 'BIO_SAMPLE_NAME'
		assertColumn table, 'BIO_SAMPLE_NAME', 'varchar(255)'

		// String source
		// source nullable: true, maxSize: 400
		assertColumn table, 'SOURCE', 'varchar(400)', true

		// String sourceCode
		// sourceCode nullable: true, maxSize: 400
		assertColumn table, 'SOURCE_CODE', 'varchar(400)', true

		// String type
		// type column: 'BIO_SAMPLE_TYPE'
		// type maxSize: 400
		assertColumn table, 'BIO_SAMPLE_TYPE', 'varchar(400)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
