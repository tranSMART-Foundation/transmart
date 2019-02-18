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

package org.transmartproject.db.dataquery.highdim.metabolite

import groovy.util.logging.Slf4j
import org.transmartproject.db.AbstractTestData
import org.transmartproject.db.dataquery.highdim.DeGplInfo
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.HighDimTestData
import org.transmartproject.db.dataquery.highdim.SampleBioMarkerTestData
import org.transmartproject.db.i2b2data.PatientDimension
import org.transmartproject.db.ontology.ConceptTestData
import org.transmartproject.db.search.SearchKeywordCoreDb

@Slf4j('logger')
class MetaboliteTestData extends AbstractTestData {

	public static final String TRIAL_NAME = 'METABOLITE_EXAMPLE_TRIAL'

	private long deMetaboliteSubPathwayId = -700
	private long searchKeywordCoreDbId = -800
	private long otherSearchKeywordCoreDbId = -900

	SampleBioMarkerTestData biomarkerTestData = new SampleBioMarkerTestData()
	ConceptTestData concept
	DeGplInfo platform
	List<PatientDimension> patients
	List<DeSubjectSampleMapping> assays
	List<DeMetaboliteSuperPathway> superPathways
	List<DeMetaboliteSubPathway> subPathways
	List<SearchKeywordCoreDb> searchKeywordsForSubPathways
	List<SearchKeywordCoreDb> searchKeywordsForSuperPathways
	List<DeMetaboliteAnnotation> annotations
	List<DeSubjectMetabolomicsData> data

	MetaboliteTestData() {
		createTestData()
	}

	void saveAll() {
		biomarkerTestData.saveMetabolomicsData()

		save platform, logger
		saveAll patients, logger
		saveAll assays, logger
		saveAll superPathways, logger
		saveAll searchKeywordsForSuperPathways, logger
		saveAll subPathways, logger
		saveAll searchKeywordsForSubPathways, logger
		saveAll annotations, logger
		saveAll data, logger
		concept.saveAll()
	}

	private void createTestData() {
		concept = HighDimTestData.createConcept('METABOLITEPUBLIC', 'concept code #1',
				TRIAL_NAME, 'METABOLITE_CONCEPT')

		platform = new DeGplInfo(
				title: 'Bogus metabolite platform',
				organism: 'Homo Sapiens',
				markerType: 'METABOLOMICS')
		platform.id = 'BOGUS_METABOLITE_PLATFORM'

		patients = HighDimTestData.createTestPatients(2, -300, TRIAL_NAME)

		assays = HighDimTestData.createTestAssays(patients, -400, platform, TRIAL_NAME)

		// keep in sync with SampleBioMarkerTestData
		superPathways = [
				new DeMetaboliteSuperPathway(name: 'Carboxylic Acid', gplId: platform),
				new DeMetaboliteSuperPathway(name: 'Phosphoric Acid', gplId: platform)]
		superPathways[0].id = -601L
		superPathways[1].id = -602L

		// keep in sync with SampleBioMarkerTestData
		subPathways = [
				createSubPathway('No superpathway subpathway', null),
				createSubPathway('Cholesterol biosynthesis', superPathways[0]),
				createSubPathway('Squalene synthesis', superPathways[0]),
				createSubPathway('Pentose Metabolism', superPathways[1])]


		searchKeywordsForSubPathways = subPathways.collect { DeMetaboliteSubPathway it ->
			SearchKeywordCoreDb res = new SearchKeywordCoreDb(
					keyword: it.name,
					bioDataId: it.id,
					uniqueId: 'METABOLITE_SUBPATHWAY' + it.id, // no actual external pk
					dataCategory: 'METABOLITE_SUBPATHWAY')
			res.id = --searchKeywordCoreDbId
			res
		}

		searchKeywordsForSuperPathways = superPathways.collect { DeMetaboliteSuperPathway it ->
			SearchKeywordCoreDb res = new SearchKeywordCoreDb(
					keyword: it.name,
					bioDataId: it.id,
					uniqueId: 'METABOLITE_SUPERPATHWAY' + it.id, // no actual external pk
					dataCategory: 'METABOLITE_SUPERPATHWAY')
			res.id = --otherSearchKeywordCoreDbId
			res
		}

		// keep in sync with SampleBioMarkerTestData
		annotations = [
				createAnnotation(-501, 'Cryptoxanthin epoxide', 'HMDB30538', []),
				createAnnotation(-502, 'Cryptoxanthin 5,6:5\',8\'-diepoxide', 'HMDB30537', subPathways[0..1]),
				createAnnotation(-503, 'Majoroside F4', 'HMDB30536', subPathways[1..3])]

		double intensity = 0
		data = []
		for (DeMetaboliteAnnotation annotation in annotations) {
			for (DeSubjectSampleMapping assay in assays) {
				data << createDataEntry(assay, annotation, intensity += 0.1)
			}
		}
	}

	private DeMetaboliteSubPathway createSubPathway(String name, DeMetaboliteSuperPathway superPathway) {
		DeMetaboliteSubPathway ret = new DeMetaboliteSubPathway(
				name: name,
				superPathway: superPathway,
				gplId: platform)
		ret.id = --deMetaboliteSubPathwayId
		ret
	}

	private DeMetaboliteAnnotation createAnnotation(long id, String metaboliteName, String metabolite,
	                                                List<DeMetaboliteSubPathway> subpathways) {
		DeMetaboliteAnnotation res = new DeMetaboliteAnnotation(
				biochemicalName: metaboliteName,
				hmdbId: metabolite,
				platform: platform)
		res.id = id
		for (DeMetaboliteSubPathway it in subpathways) {
			it.addToAnnotations res
		}
		res
	}

	private DeSubjectMetabolomicsData createDataEntry(DeSubjectSampleMapping assay,
	                                                  DeMetaboliteAnnotation annotation,
	                                                  double intensity) {
		new DeSubjectMetabolomicsData(
				assay: assay,
				patient: assay.patient,
				annotation: annotation,
				jAnnotation: annotation,
				rawIntensity: intensity,
				logIntensity: Math.log(intensity),
				zscore: (intensity - 0.35) / 0.1871
		)
	}
}
