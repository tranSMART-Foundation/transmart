package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class ContentSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_CONTENT'
		Table table = assertTable('BIO_CONTENT')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_FILE_CONTENT_ID'
		assertPk table, 'BIO_FILE_CONTENT_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String contentAbstract
		// contentAbstract column: 'ABSTRACT'
		// contentAbstract nullable: true, maxSize: 4000
		assertColumn table, 'ABSTRACT', 'varchar(4000)', true

		// String location
		// location nullable: true, maxSize: 800
		assertColumn table, 'LOCATION', 'varchar(800)', true

		// String name
		// name column: 'FILE_NAME'
		// name nullable: true, maxSize: 2000
		assertColumn table, 'FILE_NAME', 'varchar(2000)', true

		// ContentRepository repository
		assertForeignKeyColumn table, 'BIO_CONTENT_REPOSITORY', 'REPOSITORY_ID' // table 'BIO_CONTENT_REPOSITORY'

		// String title
		// title nullable: true, maxSize: 2000
		assertColumn table, 'TITLE', 'varchar(2000)', true

		// String type
		// type column: 'FILE_TYPE'
		// type maxSize: 400
		assertColumn table, 'FILE_TYPE', 'varchar(400)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}

	void 'test getAbsolutePath'() {
		when:
		Content c = new Content(repository: new ContentRepository())

		then:
		'//' == c.absolutePath

		when:
		c.repository.location = 'reploc'

		then:
		'reploc//' == c.absolutePath

		when:
		c.location = 'loc'

		then:
		'reploc/loc/' == c.absolutePath

		when:
		c.name = 'name'

		then:
		'reploc/loc/name' == c.absolutePath
	}
}
