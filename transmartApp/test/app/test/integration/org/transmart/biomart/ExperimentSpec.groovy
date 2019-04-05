package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class ExperimentSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_EXPERIMENT'
		Table table = assertTable('BIO_EXPERIMENT')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_EXPERIMENT_ID'
		assertPk table, 'BIO_EXPERIMENT_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String accession
		assertColumn table, 'ACCESSION', 'varchar(255)'

		// String accessType
		// accessType nullable: true
		assertColumn table, 'ACCESS_TYPE', 'varchar(255)', true

		// String bioMarkerType
		// bioMarkerType column: 'BIOMARKER_TYPE'
		// bioMarkerType nullable: true
		assertColumn table, 'BIOMARKER_TYPE', 'varchar(255)', true

		// Date completionDate
		// completionDate nullable: true
		assertColumn table, 'COMPLETION_DATE', 'timestamp', true

		// String country
		// country nullable: true
		assertColumn table, 'COUNTRY', 'varchar(255)', true

		// String description
		// description nullable: true, maxSize: 4000
		assertColumn table, 'DESCRIPTION', 'varchar(4000)', true

		// String design
		// design nullable: true, maxSize: 4000
		assertColumn table, 'DESIGN', 'varchar(4000)', true

		// String institution
		// institution nullable: true
		assertColumn table, 'INSTITUTION', 'varchar(255)', true

		// String overallDesign
		// overallDesign nullable: true, maxSize: 4000
		assertColumn table, 'OVERALL_DESIGN', 'varchar(4000)', true

		// String primaryInvestigator
		// primaryInvestigator nullable: true, maxSize: 800
		assertColumn table, 'PRIMARY_INVESTIGATOR', 'varchar(800)', true

		// Date startDate
		// startDate nullable: true
		assertColumn table, 'START_DATE', 'timestamp', true

		// String status
		// status nullable: true
		assertColumn table, 'STATUS', 'varchar(255)', true

		// String target
		// target nullable: true
		assertColumn table, 'TARGET', 'varchar(255)', true

		// String title
		// title nullable: true, maxSize: 2000
		assertColumn table, 'TITLE', 'varchar(2000)', true

		// String type
		// type column: 'BIO_EXPERIMENT_TYPE'
		// type nullable: true, maxSize: 400
		assertColumn table, 'BIO_EXPERIMENT_TYPE', 'varchar(400)', true

		!table.columns
		!table.foreignKeys
		!table.indexes
	}

	void 'test getCompoundNames()'() {
		when:
		Experiment e = new Experiment()

		then:
		!e.compoundNames

		when:
		e.addToCompounds new Compound(genericName: 'c1')

		then:
		'c1' == e.compoundNames

		when:
		e.addToCompounds new Compound(genericName: 'c2')

		then:
		('c1; c2' == e.compoundNames) || ('c2; c1' == e.compoundNames)
	}

	void 'test getDiseaseNames()'() {
		when:
		Experiment e = new Experiment()

		then:
		!e.diseaseNames

		when:
		e.addToDiseases new Disease(disease: 'd1')

		then:
		'd1' == e.diseaseNames

		when:
		e.addToDiseases new Disease(disease: 'd2')

		then:
		('d1; d2' == e.diseaseNames) || ('d2; d1' == e.diseaseNames)
	}

	void 'test getOrganismNames()'() {
		when:
		Experiment e = new Experiment()

		then:
		!e.organismNames

		when:
		e.addToOrganisms new Taxonomy(name: 't1')

		then:
		't1' == e.organismNames

		when:
		e.addToOrganisms new Taxonomy(name: 't2')

		then:
		('t1; t2' == e.organismNames) || ('t2; t1' == e.organismNames)
	}

	void 'test getUniqueId()'() {
		when:
		Experiment e = new Experiment()

		then:
		!e.uniqueId

		when:
		BioData bd = new BioData()
		e.addToUniqueIds bd

		then:
		bd.is e.uniqueId
	}

	void 'test toString()'() {
		when:
		Experiment e = new Experiment(type: 'the_type', title: 'the_title', description: 'the_description', accession: 'the_accession')
		e.id = 42

		then:
		'id: 42; type: the_type; title: the_title; description: the_description; accesion: the_accession' == e.toString()
	}
}
