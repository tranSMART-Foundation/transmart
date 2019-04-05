package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssayPlatformSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_ASSAY_PLATFORM'
		Table table = assertTable('BIO_ASSAY_PLATFORM')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_ASSAY_PLATFORM_ID'
		assertPk table, 'BIO_ASSAY_PLATFORM_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String accession
		// accession column: 'PLATFORM_ACCESSION'
		assertColumn table, 'PLATFORM_ACCESSION', 'varchar(255)'

		// String array
		// array column: 'PLATFORM_ARRAY'
		assertColumn table, 'PLATFORM_ARRAY', 'varchar(255)'

		// String description
		// description column: 'PLATFORM_DESCRIPTION'
		// description nullable: true, maxSize: 2000
		assertColumn table, 'PLATFORM_DESCRIPTION', 'varchar(2000)', true

		// String name
		// name column: 'PLATFORM_NAME'
		// name nullable: true, maxSize: 400
		assertColumn table, 'PLATFORM_NAME', 'varchar(400)', true

		// String organism
		// organism column: 'PLATFORM_ORGANISM'
		assertColumn table, 'PLATFORM_ORGANISM', 'varchar(255)'

		// String platformTechnology
		// platformTechnology nullable: true
		assertColumn table, 'PLATFORM_TECHNOLOGY', 'varchar(255)', true

		// String platformType
		// platformType nullable: true
		assertColumn table, 'PLATFORM_TYPE', 'varchar(255)', true

		// String platformVersion
		// platformVersion nullable: true, maxSize: 400
		assertColumn table, 'PLATFORM_VERSION', 'varchar(400)', true

		// String vendor
		// vendor column: 'PLATFORM_VENDOR'
		assertColumn table, 'PLATFORM_VENDOR', 'varchar(255)'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}

	void 'test getUniqueId()'() {
		when:
		String uniqueId = 'uniqueId'
		BioAssayPlatform bap = new BioAssayPlatform(accession: 'accession', array: 'array', organism: 'organism', vendor: 'vendor')
		bap.uniqueId = uniqueId

		then: 'if uniqueId is set, use that'
		uniqueId == bap.uniqueId

		when:
		bap.uniqueId = null

		then: 'if uniqueId is not set but id is null (not persistent) return null'
		!bap.uniqueId

		when:
		bap.id = SHARED_ID
		bap = save(bap)

		then: 'if uniqueId is not set and id is not null but there is no associated BioData, return "BAP:" + accession'
		!bap.hasErrors()
		'BAP:accession' == bap.uniqueId

		when:
		bap.uniqueId = null
		String bioDataUniqueId = 'uniqueId_from_BioData'
		BioData bd = new BioData(type: 'type', uniqueId: bioDataUniqueId)
		bd.id = SHARED_ID

		then: 'if uniqueId is not set and there is an associated BioData, return its uniqueId'
		save bd
		bioDataUniqueId == bap.uniqueId
	}

	void 'test findByUniqueId()'() {
		given:
		String uniqueId = 'uniqueId_from_BioData'

		expect: 'if there is no associated BioData, return null'
		!BioAssayPlatform.findByUniqueId(uniqueId)

		when:
		BioAssayPlatform bap = new BioAssayPlatform(accession: 'accession', array: 'array', organism: 'organism', vendor: 'vendor')
		bap.id = SHARED_ID
		save bap

		BioData bd = new BioData(type: 'type', uniqueId: uniqueId)
		bd.id = SHARED_ID
		save bd

		flushAndClear()

		bap = BioAssayPlatform.findByUniqueId(uniqueId)

		then: 'if there is an associated BioData, return the BioAssayPlatform with the same id'
		bap
		uniqueId == bap.uniqueId
	}
}
