/*
 * Copyright © 2013-2014 The Hyve B.V.
 *
 * This file is part of transmart-core-db.
 *
 * Transmart-core-db is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * transmart-core-db.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmartproject.db.ontology

import groovy.util.logging.Slf4j
import org.springframework.util.Assert
import org.transmartproject.core.concept.ConceptFullName
import org.transmartproject.db.AbstractTestData
import org.transmartproject.db.i2b2data.ConceptDimension
import org.transmartproject.db.i2b2data.ObservationFact
import org.transmartproject.db.i2b2data.PatientDimension
import org.transmartproject.db.user.AccessLevelTestData

import static org.transmartproject.db.dataquery.clinical.ClinicalTestData.*
import static org.transmartproject.db.i2b2data.I2b2Data.createTestPatients
import static org.transmartproject.db.ontology.ConceptTestData.createConceptDimensions
import static org.transmartproject.db.ontology.ConceptTestData.createI2b2Concept

@Slf4j('logger')
class AcrossTrialsTestData extends AbstractTestData {

	public static final String MODIFIER_MALE = 'SNOMED:F-03CE6'
	public static final String MODIFIER_FEMALE = 'SNOMED:F-03CE5'
	public static final String MODIFIER_AGE_AT_DIAGNOSIS = 'SNOMED:F-08104'

	ConceptTestData conceptTestData
	List<PatientDimension> patients
	List<ObservationFact> facts
	AccessLevelTestData accessLevelTestData
	List<DeXtrialNode> deXtrialNodes
	List<ModifierMetadataCoreDb> modifierMetadatas

	static AcrossTrialsTestData createDefault() {
		List<DimensionAndMetadata> list = []
		list << createModifier('\\Demographics\\', 'CDEMO', 'F')
		list << createModifier('\\Demographics\\Age at Diagnosis\\', MODIFIER_AGE_AT_DIAGNOSIS,
				'L', 'N', 'year')
		list << createModifier('\\Demographics\\Sex\\', 'SNOMED:F-03D86', 'F')
		list << createModifier('\\Demographics\\Sex\\Female\\', MODIFIER_FEMALE, 'L')
		list << createModifier('\\Demographics\\Sex\\Male\\', MODIFIER_MALE, 'L')

		AcrossTrialsTestData result = new AcrossTrialsTestData()
		result.deXtrialNodes = list*.deXtrialNode
		result.modifierMetadatas = list*.metadata

		TableAccess tableAccess = ConceptTestData.createTableAccess(
				level: 0,
				fullName: '\\foo\\',
				name: 'foo',
				tableCode: 'i2b2 main',
				tableName: 'i2b2')

		int c = 1
		List<I2b2> i2b2List = [
				createI2b2Concept(code: c++, level: 1, fullName: '\\foo\\study1\\', name: 'study1',
						cComment: 'trial:STUDY_ID_1', cVisualattributes: 'FA'),
				createI2b2Concept(code: c++, level: 2, fullName: '\\foo\\study1\\age at diagnosis\\',
						name: 'age at diagnosis', cComment: 'trial:STUDY_ID_1', cVisualattributes: 'LA',
						metadataxml: ConceptTestData.numericXml),
				createI2b2Concept(code: c++, level: 2, fullName: '\\foo\\study1\\male\\', name: 'male',
						cComment: 'trial:STUDY_ID_1', cVisualattributes: 'LA'),
				createI2b2Concept(code: c++, level: 2, fullName: '\\foo\\study1\\female\\', name: 'female',
						cComment: 'trial:STUDY_ID_1', cVisualattributes: 'LA'),
				createI2b2Concept(code: c++, level: 1, fullName: '\\foo\\study2\\', name: 'study2',
						cComment: 'trial:STUDY_ID_2', cVisualattributes: 'FA'),
				createI2b2Concept(code: c++, level: 2, fullName: '\\foo\\study2\\age at diagnosis\\',
						name: 'age at diagnosis', cComment: 'trial:STUDY_ID_2', cVisualattributes: 'LA',
						metadataxml: ConceptTestData.numericXml),
				createI2b2Concept(code: c++, level: 2, fullName: '\\foo\\study2\\male\\', name: 'male',
						cComment: 'trial:STUDY_ID_2', cVisualattributes: 'LA'),
				createI2b2Concept(code: c++, level: 2, fullName: '\\foo\\study2\\female\\', name: 'female',
						cComment: 'trial:STUDY_ID_2', cVisualattributes: 'LA')]

		result.conceptTestData = new ConceptTestData(tableAccesses: [tableAccess],
				i2b2List: i2b2List, conceptDimensions: createConceptDimensions(i2b2List))

		List<PatientDimension> patientsStudy1 = createTestPatients(2, -400L, 'STUDY_ID_1')
		List<PatientDimension> patientsStudy2 = createTestPatients(2, -500L, 'STUDY_ID_2')

		List<ObservationFact> observations = []
		observations.addAll createDiagonalCategoricalFacts(2,
				i2b2List.findAll { it.fullName =~ /\\foo\\study1\\(fe)?male/ }, patientsStudy1)
		observations.addAll createDiagonalCategoricalFacts(2,
				i2b2List.findAll { it.fullName =~ /\\foo\\study2\\(fe)?male/ }, patientsStudy2)
		observations.addAll createObservationFact(
				conceptDimensionFor(result.conceptTestData, '\\foo\\study1\\age at diagnosis\\'),
				patientsStudy1[0], DUMMY_ENCOUNTER_ID, 1100)
		observations.addAll createObservationFact(
				conceptDimensionFor(result.conceptTestData, '\\foo\\study1\\age at diagnosis\\'),
				patientsStudy1[1], DUMMY_ENCOUNTER_ID, 2101)
		observations.addAll createObservationFact(
				conceptDimensionFor(result.conceptTestData, '\\foo\\study2\\age at diagnosis\\'),
				patientsStudy2[0], DUMMY_ENCOUNTER_ID, 1200)
		observations.addAll createObservationFact(
				conceptDimensionFor(result.conceptTestData, '\\foo\\study2\\age at diagnosis\\'),
				patientsStudy2[1], DUMMY_ENCOUNTER_ID, 2201)

		observations[0].modifierCd = MODIFIER_MALE
		observations[1].modifierCd = MODIFIER_FEMALE
		observations[2].modifierCd = MODIFIER_MALE
		observations[3].modifierCd = MODIFIER_FEMALE
		observations[4..7]*.modifierCd = MODIFIER_AGE_AT_DIAGNOSIS

		result.patients = patientsStudy1 + patientsStudy2
		result.facts = observations

		result.accessLevelTestData = AccessLevelTestData.createWithAlternativeConceptData(
				result.conceptTestData)

		result
	}

	private static DimensionAndMetadata createModifier(String path, String code, String nodeType,
	                                                   String valueType = 'T', String unit = null) {
		Assert.notNull path, 'path is required'
		Assert.notNull code, 'code is required'
		Assert.notNull nodeType, 'nodeType is required'

		Map props = [path     : path,
		             code     : code,
		             nodeType : nodeType,
		             valueType: valueType,
		             unit     : unit,
		             name     : new ConceptFullName(path)[-1],
		             level    : new ConceptFullName(path).length - 1,
		             visitInd : 'N' as char]

		new DimensionAndMetadata(
				deXtrialNode: new DeXtrialNode(props),
				metadata: new ModifierMetadataCoreDb(props))
	}

	void saveAll() {
		saveAll deXtrialNodes, logger
		saveAll modifierMetadatas, logger
		conceptTestData.saveAll()
		saveAll patients, logger
		saveAll facts, logger
		accessLevelTestData.saveAll()
	}

	private static ConceptDimension conceptDimensionFor(ConceptTestData conceptTestData, String fullName) {
		conceptTestData.conceptDimensions.find { fullName == fullName /* TODO always true */ }
	}

	private static class DimensionAndMetadata {
		DeXtrialNode deXtrialNode
		ModifierMetadataCoreDb metadata
	}
}
