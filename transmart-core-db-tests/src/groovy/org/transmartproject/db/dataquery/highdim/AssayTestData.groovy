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

package org.transmartproject.db.dataquery.highdim

import groovy.util.logging.Slf4j
import org.transmartproject.db.AbstractTestData
import org.transmartproject.db.i2b2data.ConceptDimension
import org.transmartproject.db.i2b2data.PatientDimension
import org.transmartproject.db.ontology.I2b2
import org.transmartproject.db.ontology.TableAccess

import static org.transmartproject.db.ontology.ConceptTestData.createI2b2
import static org.transmartproject.db.ontology.ConceptTestData.createTableAccess

@Slf4j('logger')
class AssayTestData extends AbstractTestData {

	DeGplInfo platform
	DeGplInfo platform2
	List<PatientDimension> patients
	List<TableAccess> i2b2TopConcepts
	List<I2b2> i2b2GenericConcepts
	List<ConceptDimension> dimensionConcepts
	List<DeSubjectSampleMapping> assays

	AssayTestData() {
		createTestData()
	}

	void saveAll() {
		saveAll patients, logger
		saveAll i2b2TopConcepts, logger
		saveAll i2b2GenericConcepts, logger
		saveAll dimensionConcepts, logger
		saveAll assays, logger
	}

	private void createTestData() {
		platform = new DeGplInfo(
				title: 'Affymetrix Human Genome U133A 2.0 Array',
				organism: 'Homo Sapiens',
				markerTypeId: 'Gene Expression')
		platform.id = 'BOGUSGPL570'

		platform2 = new DeGplInfo(
				title: 'Another platform',
				organism: 'Homo Sapiens',
				markerTypeId: 'Gene Expression')
		platform2.id = 'BOGUSANNOTH'

		patients = HighDimTestData.createTestPatients(3, -100)

		i2b2TopConcepts = [createTableAccess(level: 0, fullName: '\\foo\\', name: 'foo',
				tableCode: 'i2b2 main', tableName: 'i2b2')]

		i2b2GenericConcepts = [
				createI2b2(level: 1, fullName: '\\foo\\bar\\', name: 'bar'),
				createI2b2(level: 1, fullName: '\\foo\\xpto\\', name: 'xpto'),
				createI2b2(level: 1, fullName: '\\foo\\xpto2\\', name: 'xpto2'),
		]

		dimensionConcepts = [
				new ConceptDimension(conceptPath: '\\foo\\bar\\', conceptCode: 'CODE-BAR'),
				new ConceptDimension(conceptPath: '\\foo\\xpto\\', conceptCode: 'CODE-XPTO'),
				new ConceptDimension(conceptPath: '\\foo\\xpto2\\', conceptCode: 'CODE-XPTO2')]

		//save is cascaded to the platform
		assays = HighDimTestData.createTestAssays(patients, -200, platform,
				'SAMPLE_TRIAL_1', dimensionConcepts[0].conceptCode) +
				HighDimTestData.createTestAssays(patients, -300, platform,
						'SAMPLE_TRIAL_1', dimensionConcepts[1].conceptCode) +
				HighDimTestData.createTestAssays(patients, -400, platform,
						'SAMPLE_TRIAL_2', dimensionConcepts[1].conceptCode) +
				HighDimTestData.createTestAssays(patients, -500, platform2,
						'SAMPLE_TRIAL_1', dimensionConcepts[2].conceptCode)
	}
}
