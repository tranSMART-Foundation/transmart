package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class ConceptCodeSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_CONCEPT_CODE'
		Table table = assertTable('BIO_CONCEPT_CODE')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_CONCEPT_CODE_ID'
		assertPk table, 'BIO_CONCEPT_CODE_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String bioConceptCode
		// bioConceptCode nullable: true, maxSize: 400
		assertColumn table, 'BIO_CONCEPT_CODE', 'varchar(400)', true

		// String codeDescription
		// codeDescription nullable: true, maxSize: 2000
		assertColumn table, 'CODE_DESCRIPTION', 'varchar(2000)', true

		// String codeName
		assertColumn table, 'CODE_NAME', 'varchar(255)'

		// String codeTypeName
		// codeTypeName nullable: true, maxSize: 400
		assertColumn table, 'CODE_TYPE_NAME', 'varchar(400)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}

	void 'test getUniqueId()'() {
		when:
		String uniqueId = 'uniqueId'
		ConceptCode cc = new ConceptCode(codeName: 'codeName')
		cc.uniqueId = uniqueId

		then: 'if uniqueId is set, use that'
		uniqueId == cc.uniqueId

		when:
		cc.uniqueId = null

		then: 'if uniqueId is not set but id is null (not persistent) return null'
		!cc.uniqueId

		when:
		cc.id = SHARED_ID
		cc = save(cc)

		then: 'if uniqueId is not set and id is not null but there is no associated BioData, return null'
		!cc.hasErrors()
		!cc.uniqueId

		when:
		cc.uniqueId = null
		String bioDataUniqueId = 'uniqueId_from_BioData'
		BioData bd = new BioData(type: 'type', uniqueId: bioDataUniqueId)
		bd.id = SHARED_ID

		then: 'if uniqueId is not set and there is an associated BioData, return its uniqueId'
		save bd
		bioDataUniqueId == cc.uniqueId
	}

	void 'test findByUniqueId()'() {
		given:
		String uniqueId = 'uniqueId_from_BioData'

		expect: 'if there is no associated BioData, return null'
		!ConceptCode.findByUniqueId(uniqueId)

		when:
		ConceptCode cc = new ConceptCode(codeName: 'codeName')
		cc.id = SHARED_ID
		save cc

		BioData bd = new BioData(type: 'type', uniqueId: uniqueId)
		bd.id = SHARED_ID
		save bd

		flushAndClear()

		cc = ConceptCode.findByUniqueId(uniqueId)

		then: 'if there is an associated BioData, return the ConceptCode with the same id'
		cc
		uniqueId == cc.uniqueId
	}
}
