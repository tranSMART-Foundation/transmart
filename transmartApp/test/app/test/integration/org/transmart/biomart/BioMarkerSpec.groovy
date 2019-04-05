package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioMarkerSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_MARKER'
		Table table = assertTable('BIO_MARKER')

		// id column: 'BIO_MARKER_ID'
		assertPk table, 'BIO_MARKER_ID', AUTOINC

		// String bioMarkerType
		// bioMarkerType maxSize: 400
		assertColumn table, 'BIO_MARKER_TYPE', 'varchar(400)'

		// String description
		// description column: 'BIO_MARKER_DESCRIPTION'
		// description nullable: true, maxSize: 2000
		assertColumn table, 'BIO_MARKER_DESCRIPTION', 'varchar(2000)', true

		// String name
		// name column: 'BIO_MARKER_NAME'
		// name nullable: true, maxSize: 400
		assertColumn table, 'BIO_MARKER_NAME', 'varchar(400)', true

		// String organism
		// organism nullable: true, maxSize: 400
		assertColumn table, 'ORGANISM', 'varchar(400)', true

		// String primaryExternalId
		// primaryExternalId nullable: true, maxSize: 400
		assertColumn table, 'PRIMARY_EXTERNAL_ID', 'varchar(400)', true

		// String primarySourceCode
		// primarySourceCode nullable: true, maxSize: 400
		assertColumn table, 'PRIMARY_SOURCE_CODE', 'varchar(400)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}

	void 'test getUniqueId()'() {
		when:
		String uniqueId = 'uniqueId'
		BioMarker bm = new BioMarker(bioMarkerType: 'bioMarkerType')
		bm.uniqueId = uniqueId

		then: 'if uniqueId is set, use that'
		uniqueId == bm.uniqueId

		when:
		bm.uniqueId = null

		then: 'if uniqueId is not set but id is null (not persistent) return null'
		!bm.uniqueId

		when:
		bm.id = SHARED_ID
		bm = save(bm)

		then: 'if uniqueId is not set and id is not null but there is no associated BioData, return null'
		!bm.hasErrors()
		!bm.uniqueId

		when:
		bm.uniqueId = null
		String bioDataUniqueId = 'uniqueId_from_BioData'
		BioData bd = new BioData(type: 'type', uniqueId: bioDataUniqueId)
		bd.id = SHARED_ID

		then: 'if uniqueId is not set and there is an associated BioData, return its uniqueId'
		save bd
		bioDataUniqueId == bm.uniqueId
	}

	void 'test findByUniqueId()'() {
		given:
		String uniqueId = 'uniqueId_from_BioData'

		expect: 'if there is no associated BioData, return null'
		!BioMarker.findByUniqueId(uniqueId)

		when:
		BioMarker bm = new BioMarker(bioMarkerType: 'bioMarkerType')
		bm.id = SHARED_ID
		save bm

		BioData bd = new BioData(type: 'type', uniqueId: uniqueId)
		bd.id = SHARED_ID
		save bd

		flushAndClear()

		bm = BioMarker.findByUniqueId(uniqueId)

		then: 'if there is an associated BioData, return the BioMarker with the same id'
		bm
		uniqueId == bm.uniqueId
	}
}
