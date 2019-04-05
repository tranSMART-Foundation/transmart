package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class ContentRepositorySpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_CONTENT_REPOSITORY'
		Table table = assertTable('BIO_CONTENT_REPOSITORY')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_CONTENT_REPO_ID'
		assertPk table, 'BIO_CONTENT_REPO_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String activeYN
		// activeYN column: 'ACTIVE_Y_N'
		// activeYN nullable: true, maxSize: 1
		assertColumn table, 'ACTIVE_Y_N', 'varchar(1)', true

		// String location
		// location nullable: true, maxSize: 1020
		assertColumn table, 'LOCATION', 'varchar(1020)', true

		// String locationType
		// locationType nullable: true, maxSize: 400
		assertColumn table, 'LOCATION_TYPE', 'varchar(400)', true

		// String repositoryType
		// repositoryType maxSize: 400
		assertColumn table, 'REPOSITORY_TYPE', 'varchar(400)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}
}
