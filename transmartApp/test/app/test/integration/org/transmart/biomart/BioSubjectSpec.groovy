package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioSubjectSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_SUBJECT'
		Table table = assertTable('BIO_SUBJECT')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_SUBJECT_ID'
		assertPk table, 'BIO_SUBJECT_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String organism
		// organism nullable: true, maxSize: 400
		assertColumn table, 'ORGANISM', 'varchar(400)', true

		// Long siteSubjectId
		// siteSubjectId nullable: true
		assertColumn table, 'SITE_SUBJECT_ID', 'bigint', true

		// String source
		// source nullable: true, maxSize: 400
		assertColumn table, 'SOURCE', 'varchar(400)', true

		// String sourceCode
		// sourceCode nullable: true, maxSize: 400
		assertColumn table, 'SOURCE_CODE', 'varchar(400)', true

		// String status
		// status nullable: true, maxSize: 400
		assertColumn table, 'STATUS', 'varchar(400)', true

		// String type
		// type column: 'BIO_SUBJECT_TYPE'
		// type maxSize: 400
		assertColumn table, 'BIO_SUBJECT_TYPE', 'varchar(400)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
