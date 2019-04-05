package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssaySpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_ASSAY'
		Table table = assertTable('BIO_ASSAY')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_ASSAY_ID'
		assertPk table, 'BIO_ASSAY_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Long assayPlatformId
		// assayPlatformId column: 'BIO_ASY_PLATFORM_ID'
		// assertColumn table, 'BIO_ASY_PLATFORM_ID', 'bigint' // TODO BB
		assertColumn table, 'ASSAY_PLATFORM_ID', 'bigint'

		// String description
		// description nullable: true, maxSize: 4000
		assertColumn table, 'DESCRIPTION', 'varchar(4000)', true

		// Experiment experiment
		assertForeignKeyColumn table, 'BIO_EXPERIMENT', 'EXPERIMENT_ID' // table 'BIO_EXPERIMENT'

		// String protocol
		// protocol nullable: true, maxSize: 400
		assertColumn table, 'PROTOCOL', 'varchar(400)', true

		// String requestor
		// requestor nullable: true, maxSize: 400
		assertColumn table, 'REQUESTOR', 'varchar(400)', true

		// Date sampleReceiveDate
		// sampleReceiveDate nullable: true
		assertColumn table, 'SAMPLE_RECEIVE_DATE', 'timestamp', true

		// String sampleType
		// sampleType nullable: true, maxSize: 400
		assertColumn table, 'SAMPLE_TYPE', 'varchar(400)', true

		// String study
		// study nullable: true, maxSize: 400
		assertColumn table, 'STUDY', 'varchar(400)', true

		// Date testDate
		// testDate nullable: true
		assertColumn table, 'TEST_DATE', 'timestamp', true

		// String type
		// type column: 'BIO_ASSAY_TYPE'
		// type maxSize: 400
		assertColumn table, 'BIO_ASSAY_TYPE', 'varchar(400)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
