package org.transmart.dataexport

import com.google.common.io.Files
import com.recomdata.asynchronous.JobResultsService
import com.recomdata.transmart.data.export.ClinicalExportService
import grails.test.mixin.TestMixin
import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.querytool.Item
import org.transmartproject.core.querytool.Panel
import org.transmartproject.core.querytool.QueryDefinition
import org.transmartproject.db.dataquery.clinical.ClinicalTestData
import org.transmartproject.db.i2b2data.I2b2Data
import org.transmartproject.db.i2b2data.ObservationFact
import org.transmartproject.db.i2b2data.PatientDimension
import org.transmartproject.db.i2b2data.PatientTrialCoreDb
import org.transmartproject.db.ontology.ConceptTestData
import org.transmartproject.db.ontology.I2b2
import org.transmartproject.db.querytool.QueriesResourceService
import org.transmartproject.db.test.RuleBasedIntegrationTestMixin

import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.greaterThan
import static org.hamcrest.Matchers.hasProperty
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.transmart.dataexport.FileContentTestUtils.parseSepValTable
import static org.transmartproject.db.TestDataHelper.save

@TestMixin(RuleBasedIntegrationTestMixin)
@WithGMock
class ClinicalExportServiceTests {

    ClinicalExportService clinicalExportService
    QueriesResourceService queriesResourceService

    File tmpDir
    def queryResult

    ConceptTestData conceptData
    I2b2Data i2b2Data
    List<ObservationFact> facts
    I2b2 studyNode
    I2b2 sexNode

    @Before
    void setUp() {

        //Create some test data
        String trialId = 'STUDY_ID_2'

        List<PatientDimension> patients = I2b2Data.createTestPatients(3, -100, trialId)

        conceptData = ConceptTestData.createDefault()
        conceptData.tableAccesses << ConceptTestData
            .createTableAccess(
            level: 0,
            fullName: '\\foo\\',
            name: 'foo',
            tableCode: 'foo',
            tableName: 'i2b2')

        List<I2b2> studyNodes = conceptData.i2b2List.findAll { it.cComment?.endsWith(trialId) }

        studyNode = studyNodes.find { it.name == 'study2' }
        sexNode = studyNodes.find { it.name == 'sex' }
        I2b2 femaleNode = studyNodes.find { it.name == 'female' }
        I2b2 maleNode = studyNodes.find { it.name == 'male' }
        I2b2 charNode = studyNodes.find { it.name == 'with%some$characters_' }
        I2b2 study1SubNode = studyNodes.find { it.name == 'study1' }

        List<PatientTrialCoreDb> patientTrials = I2b2Data.createPatientTrialLinks(patients, trialId)
        i2b2Data = new I2b2Data(trialName: trialId, patients: patients, patientTrials: patientTrials)
        facts = ClinicalTestData.createDiagonalCategoricalFacts(
            3,
            [femaleNode, maleNode],
            i2b2Data.patients)
        facts << ClinicalTestData.createObservationFact(charNode.code, i2b2Data.patients[0], -20000, 'test value')
        facts << ClinicalTestData.createObservationFact(study1SubNode.code, i2b2Data.patients[1], -20001, 'foo')

        conceptData.saveAll()
        i2b2Data.saveAll()
        save facts

        tmpDir = Files.createTempDir()

        clinicalExportService.jobResultsService = new JobResultsService(jobResults: [test: [Status: 'In Progress']])

	QueryDefinition definition = new QueryDefinition([new Panel(items: [new Item(conceptKey: studyNode.key)])])

        queryResult = queriesResourceService.runQuery(definition, 'test')
    }

    @Test
    void testDataWithConceptPathSpecified() {
	List<File> files = clinicalExportService.exportClinicalData(
	    'test', queryResult.id,[sexNode.key], tmpDir, false)

        assertThat files, contains(
	    hasProperty('absolutePath', endsWith(File.separator + 'data_clinical.tsv')))

	File dataFile = files[0]

	List<List<String>> dataTable = parseSepValTable(dataFile)
        assertThat dataTable, contains(
            contains('Subject ID', '\\foo\\study2\\sex\\'),
            contains('SUBJ_ID_3', 'female'),
            contains('SUBJ_ID_2', 'male'),
	    contains('SUBJ_ID_1', 'female'))
    }

    @Test
    void testMetaWithConceptPathSpecified() {
	List<File> files = clinicalExportService.exportClinicalData(
	    'test', queryResult.id, [sexNode.key], tmpDir, true)

        assertThat files, containsInAnyOrder(
            hasProperty('absolutePath', endsWith(File.separator + 'data_clinical.tsv')),
	    hasProperty('absolutePath', endsWith(File.separator + 'meta.tsv')))

	for (file in files) {
	    assertTrue file.exists()
            assertThat file.length(), greaterThan(0l)
        }

	File metaFile = files.find { it.absolutePath.endsWith File.separator + 'meta.tsv' }

	List<List<String>> metaTable = parseSepValTable(metaFile)
        assertThat metaTable, contains(
            contains('Variable', 'Attribute', 'Description'),
            contains('\\foo\\study2\\sex\\', '8 name 2', '8 description 2'),
            contains('\\foo\\study2\\sex\\', '8 name 1', '8 description 1'),
            contains('\\foo\\study2\\sex\\female\\', '10 name 2', '10 description 2'),
            contains('\\foo\\study2\\sex\\female\\', '10 name 1', '10 description 1'),
            contains('\\foo\\study2\\sex\\male\\', '9 name 2', '9 description 2'),
	    contains('\\foo\\study2\\sex\\male\\', '9 name 1', '9 description 1'))
    }

    @Test
    void testDataWithoutConceptPathSpecified() {
	List<File> files = clinicalExportService.exportClinicalData(
	    'test', queryResult.id, null, tmpDir, false)

        assertThat files, contains(
	    hasProperty('absolutePath', endsWith(File.separator + 'data_clinical.tsv')))

	File dataFile = files.find { it.absolutePath.endsWith File.separator + 'data_clinical.tsv' }
	List<List<String>> table = parseSepValTable(dataFile)
        assertThat table, contains(
            contains('Subject ID', '\\foo\\study2\\long path\\with%some$characters_\\',
                     '\\foo\\study2\\sex\\', '\\foo\\study2\\study1\\'),
            contains('SUBJ_ID_3', '', 'female', ''),
            contains('SUBJ_ID_2', '', 'male', 'foo'),
	    contains('SUBJ_ID_1', 'test value', 'female', ''))
    }

    @Test
    void testMetaWithoutConceptPathSpecified() {
	List<File> files = clinicalExportService.exportClinicalData(
	    'test', queryResult.id, null, tmpDir, true)

        assertThat files, containsInAnyOrder(
            hasProperty('absolutePath', endsWith(File.separator + 'data_clinical.tsv')),
	    hasProperty('absolutePath', endsWith(File.separator + 'meta.tsv')))

	for (file in files) {
	    assertTrue file.exists()
            assertThat file.length(), greaterThan(0l)
        }

	File metaFile = files.find { it.absolutePath.endsWith File.separator + 'meta.tsv' }

	List<List<String>> metaTable = parseSepValTable(metaFile)
        assertThat metaTable, contains(
            contains('Variable', 'Attribute', 'Description'),
            contains('\\foo\\study2\\', '3 name 2', '3 description 2'),
            contains('\\foo\\study2\\', '3 name 1', '3 description 1'),
            contains('\\foo\\study2\\long path\\', '6 name 2', '6 description 2'),
            contains('\\foo\\study2\\long path\\', '6 name 1', '6 description 1'),
            contains('\\foo\\study2\\long path\\with%some$characters_\\', '7 name 2', '7 description 2'),
            contains('\\foo\\study2\\long path\\with%some$characters_\\', '7 name 1', '7 description 1'),
            contains('\\foo\\study2\\sex\\', '8 name 2', '8 description 2'),
            contains('\\foo\\study2\\sex\\', '8 name 1', '8 description 1'),
            contains('\\foo\\study2\\sex\\female\\', '10 name 2', '10 description 2'),
            contains('\\foo\\study2\\sex\\female\\', '10 name 1', '10 description 1'),
            contains('\\foo\\study2\\sex\\male\\', '9 name 2', '9 description 2'),
            contains('\\foo\\study2\\sex\\male\\', '9 name 1', '9 description 1'),
            contains('\\foo\\study2\\study1\\', '4 name 2', '4 description 2'),
	    contains('\\foo\\study2\\study1\\', '4 name 1', '4 description 1'))
    }
}
