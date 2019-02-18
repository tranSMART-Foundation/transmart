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
import org.transmartproject.db.AbstractTestData
import org.transmartproject.db.TestDataHelper
import org.transmartproject.db.i2b2data.ConceptDimension

@Slf4j('logger')
class ConceptTestData extends AbstractTestData {

	private static final Map<String, ?> i2b2xBase = [
			factTableColumn   : '',
			dimensionTableName: '',
			columnName        : '',
			columnDataType    : '',
			operator          : '',
			dimensionCode     : '',
			mAppliedPath      : '',
			updateDate        : new Date()].asImmutable()

	// Common default values for creating a concept.
	private static final Map<String, String> conceptDefaultValues = [
			factTableColumn   : 'CONCEPT_CD',
			dimensionTableName: 'CONCEPT_DIMENSION',
			columnName        : 'CONCEPT_PATH',
			operator          : 'LIKE',
			columnDataType    : 'T'].asImmutable()

	private static final Set<String> ontologyQueryRequiredFields
	static {
		Set<String> fields = ['dimensionCode'] as Set
		fields.addAll conceptDefaultValues.keySet()
		ontologyQueryRequiredFields = fields.asImmutable()
	}

	static final String numericXml = '''\
<ValueMetadata>
  <Oktousevalues>Y</Oktousevalues>
</ValueMetadata>
'''

	List<TableAccess> tableAccesses
	List<I2b2> i2b2List
	List<I2b2Tag> i2b2TagsList
	List<ConceptDimension> conceptDimensions

	static ConceptTestData createDefault() {

		List<TableAccess> tableAccesses = []
		tableAccesses << createTableAccess(level: 0, fullName: '\\foo\\', name: 'foo', tableCode: 'i2b2 main', tableName: 'i2b2')

		List<I2b2> i2b2List = []
		i2b2List << createI2b2Concept(code: -1, level: 0, fullName: '\\foo\\',
				name: 'foo', cVisualattributes: 'CA')
		i2b2List << createI2b2Concept(code: 1, level: 1, fullName: '\\foo\\study1\\',
				name: 'study1', cComment: 'trial:STUDY_ID_1', cVisualattributes: 'FA')
		i2b2List << createI2b2Concept(code: 2, level: 2, fullName: '\\foo\\study1\\bar\\',
				name: 'bar', cComment: 'trial:STUDY_ID_1', cVisualattributes: 'LAH', metadataxml: numericXml)
		i2b2List << createI2b2Concept(code: 3, level: 1, fullName: '\\foo\\study2\\',
				name: 'study2', cComment: 'trial:STUDY_ID_2', cVisualattributes: 'FA')
		i2b2List << createI2b2Concept(code: 4, level: 2, fullName: '\\foo\\study2\\study1\\',
				name: 'study1', cComment: 'trial:STUDY_ID_2', cVisualattributes: 'LAH', metadataxml: numericXml)
		// used only in AccessLevelTestData
		i2b2List << createI2b2Concept(code: 5, level: 1, fullName: '\\foo\\study3\\',
				name: 'study3', cComment: 'trial:STUDY_ID_3', cVisualattributes: 'FA')
		// useful to test rest-api
		i2b2List << createI2b2Concept(code: 6, level: 2, fullName: '\\foo\\study2\\long path\\',
				name: 'long path', cComment: 'trial:STUDY_ID_2', cVisualattributes: 'FA')
		i2b2List << createI2b2Concept(code: 7, level: 3, fullName: '\\foo\\study2\\long path\\with%some$characters_\\',
				name: 'with%some$characters_', cComment: 'trial:STUDY_ID_2', cVisualattributes: 'LA',
				metadataxml: numericXml)
		//categorical node
		i2b2List << createI2b2Concept(code: 8, level: 2, fullName: '\\foo\\study2\\sex\\',
				name: 'sex', cComment: 'trial:STUDY_ID_2', cVisualattributes: 'FA')
		i2b2List << createI2b2Concept(code: 9, level: 3, fullName: '\\foo\\study2\\sex\\male\\',
				name: 'male', cComment: 'trial:STUDY_ID_2', cVisualattributes: 'LA')
		i2b2List << createI2b2Concept(code: 10, level: 3, fullName: '\\foo\\study2\\sex\\female\\',
				name: 'female', cComment: 'trial:STUDY_ID_2', cVisualattributes: 'LA')

		List<ConceptDimension> conceptDimensions = createConceptDimensions(i2b2List)

		List<I2b2Tag> i2b2Tags = createI2b2Tags(i2b2List)

		new ConceptTestData(
				tableAccesses: tableAccesses,
				i2b2List: i2b2List,
				conceptDimensions: conceptDimensions,
				i2b2TagsList: i2b2Tags)
	}

	void saveAll() {
		saveAll tableAccesses, logger
		saveAll i2b2List, logger
		saveAll i2b2TagsList, logger
		saveAll conceptDimensions, logger
	}

	static I2b2 createI2b2(Map properties) {
		I2b2 i2b2 = new I2b2(i2b2xBase)
		i2b2.properties = properties
		i2b2
	}

	static I2b2Secure createI2b2Secure(Map properties) {
		I2b2Secure i2b2Secure = new I2b2Secure(i2b2xBase)
		i2b2Secure.properties = properties
		i2b2Secure
	}

	/**
	 * Creates an I2b2 concept with extra fields set so some observation/patient related queries can work
	 */
	static I2b2 createI2b2Concept(Map properties) {

		assert properties.fullName
		assert properties.code

		Map extraProps = [ dimensionCode: properties.get('fullName')] // needed for ontology queries

		//field values are set in layers:
		//1 - conceptDefaultValues: typical values for concepts
		//2 - extraProps: derived or unique per object
		//3 - properties: values given by the client (these will override all others)
		//4 - completes any remaining mandatory fields with a dummy value
		I2b2 o = new I2b2(conceptDefaultValues)
		o.properties = extraProps
		o.properties = properties
		TestDataHelper.completeObject o
		//we need to make sure the i2b2 instance is valid for the ontology queries
		checkValidForOntologyQueries o
		o
	}

	static addI2b2(Map properties) {
		save createI2b2(properties), logger
	}

	static TableAccess createTableAccess(Map properties) {
		TableAccess tableAccess = new TableAccess(
				level             : 0,
				factTableColumn   : '',
				dimensionTableName: '',
				columnName        : '',
				columnDataType    : '',
				operator          : '',
				dimensionCode     : '')
		tableAccess.properties = properties
		tableAccess
	}

	static addTableAccess(Map properties) {
		save createTableAccess(properties), logger
	}

	static List<I2b2> createMultipleI2B2(int count, String basePath = '\\test',
	                                     String codePrefix = 'test', int level = 1) {
		(1..count).collect { int i ->
			String name = 'concept' + i
			String fullName = basePath + '\\' + name + '\\'
			I2b2 o = new I2b2(conceptDefaultValues)
			o.properties = [
					name         : name,
					fullName     : fullName,
					code         : codePrefix + i,
					level        : level,
					dimensionCode: fullName]
			TestDataHelper.completeObject o //completes the object with any missing values for mandatory fields
			o
		}
	}

	/**
	 * @return ConceptDimension list for the given I2b2 list
	 */
	static List<ConceptDimension> createConceptDimensions(List<I2b2> list) {
		list.collect {
			assert it.code != null
			new ConceptDimension(conceptPath: it.fullName, conceptCode: it.code)
		}
	}

	private static List<I2b2Tag> createI2b2Tags(List<I2b2> list) {
		int number = 2
		list.collectMany { I2b2 i2b2 ->
			(1..number).collect { int iteration ->
				new I2b2Tag(
						ontologyTermFullName: i2b2.fullName,
						name: i2b2.code + ' name ' + iteration,
						description: i2b2.code + ' description ' + iteration,
						//for reverse order
						position: number - iteration + 1)
			}
		}
	}

	private static void checkValidForOntologyQueries(I2b2 input) {
		List<String> missing = TestDataHelper.getMissingValueFields(input, ontologyQueryRequiredFields)
		if (missing) {
			throw new IllegalArgumentException(
					'Some I2b2 instances miss fields required for ontology queries: ' + missing)
		}
	}

	/**
	 * Adds a leaf concept to this data, along with its folder and root concepts.
	 *
	 * @param root name of the root concept to be created
	 * @param study name of the folder concept to be created
	 * @param concept name of the concep to be created
	 * @param code concept code
	 * @param tableCode table code
	 * @return new concept
	 */
	I2b2 addLeafConcept(String root = 'base',
	                    String study = 'folder',
	                    String concept = 'leaf',
	                    String code = 'mycode',
	                    String tableCode = 'i2b2 main') {

		initListsIfNull()

		tableAccesses << createTableAccess(fullName: '\\' + root + '\\', name: root, tableCode: tableCode, tableName: 'i2b2')
		i2b2List << createI2b2Concept(level: 1, fullName: '\\' + root + '\\' + study + '\\',
				name: study, code: study, cVisualattributes: 'FA')
		I2b2 result = createI2b2Concept(level: 2, fullName: '\\' + root + '\\' + study + '\\' + concept + '\\',
				name: concept, code: code, cVisualattributes: 'LA')

		i2b2List << result

		conceptDimensions.addAll createConceptDimensions([result])

		result
	}

	private void initListsIfNull() {
		tableAccesses = tableAccesses ?: [] as List
		i2b2List = i2b2List ?: [] as List
		conceptDimensions = conceptDimensions ?: [] as List
	}
}
