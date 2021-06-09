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

package org.transmartproject.db.dataquery.highdim.acgh

import groovy.util.logging.Slf4j
import org.transmartproject.db.AbstractTestData
import org.transmartproject.db.dataquery.highdim.DeGplInfo
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.HighDimTestData
import org.transmartproject.db.dataquery.highdim.SampleBioMarkerTestData
import org.transmartproject.db.dataquery.highdim.chromoregion.DeChromosomalRegion
import org.transmartproject.db.i2b2data.PatientDimension
import org.transmartproject.db.ontology.ConceptTestData
import org.transmartproject.db.querytool.QtmQueryMaster
import org.transmartproject.db.search.SearchKeywordCoreDb

import static org.transmartproject.db.dataquery.highdim.HighDimTestData.createTestAssays
import static org.transmartproject.db.dataquery.highdim.HighDimTestData.createTestPatients
import static org.transmartproject.db.querytool.QueryResultData.createQueryResult

@Slf4j('logger')
class AcghTestData extends AbstractTestData {

	static final String ACGH_PLATFORM_MARKER_TYPE = 'Chromosomal'
	static final String TRIAL_NAME = 'REGION_SAMP_TRIAL'

	private String conceptCode

	SampleBioMarkerTestData bioMarkerTestData
	ConceptTestData concept
	DeGplInfo regionPlatform
	DeGplInfo bogusTypePlatform
	List<DeChromosomalRegion> regions
	List<PatientDimension> patients
	QtmQueryMaster allPatientsQueryResult
	List<DeSubjectSampleMapping> assays
	List<DeSubjectAcghData> acghData
	List<SearchKeywordCoreDb> searchKeywords

	AcghTestData(String code = 'concept code #1', SampleBioMarkerTestData testData = null) {
		conceptCode = code
		bioMarkerTestData = testData ?: new SampleBioMarkerTestData()
		createTestData()
	}

	void saveAll() {
		bioMarkerTestData.saveGeneData()

		save regionPlatform, logger
		save bogusTypePlatform, logger
		saveAll regions, logger
		saveAll patients, logger
		save allPatientsQueryResult, logger
		saveAll assays, logger
		saveAll acghData, logger
		concept.saveAll()
	}

	private DeSubjectAcghData createACGHData(DeChromosomalRegion region,
	                                         DeSubjectSampleMapping assay, int flag) {
		new DeSubjectAcghData(
				region: region,
				jRegion: region,
				assay: assay,
				patient: assay.patient,
				chipCopyNumberValue: 0.11d,
				segmentCopyNumberValue: 0.12d,
				flag: flag,
				probabilityOfLoss: 0.11d + (flag == -1 ? 0.08d : 0),
				probabilityOfNormal: 0.13d + (flag == 0 ? 0.08d : 0),
				probabilityOfGain: 0.14d + (flag == 1 ? 0.08d : 0),
				probabilityOfAmplification: 0.15d + (flag == 2 ? 0.08d : 0),
		)
	}

	private void createTestData() {
		concept = HighDimTestData.createConcept('ACGHPUBLIC', 'concept code #1',
				TRIAL_NAME, 'REGION_CONCEPT', 'acgh i2b2 main')

		regionPlatform = new DeGplInfo(
				title: 'Test Region Platform',
				organism: 'Homo Sapiens',
				annotationDate: Date.parse('yyyy-MM-dd', '2013-05-03'),
				markerType: ACGH_PLATFORM_MARKER_TYPE,
				genomeReleaseId: 'hg18')
		regionPlatform.id = 'test-region-platform'

		bogusTypePlatform = new DeGplInfo(markerTypeId: 'bogus marker type')
		bogusTypePlatform.id = 'bogus-marker-platform'


		regions = [
				new DeChromosomalRegion(
						platform: regionPlatform,
						chromosome: '1',
						start: 33,
						end: 9999,
						numberOfProbes: 42,
						name: 'region 1:33-9999',
						cytoband: 'cytoband1',
						geneSymbol: 'ADIRF',
						geneId: -130753,
						gplId: 'gplId'),
				new DeChromosomalRegion(
						platform: regionPlatform,
						chromosome: '2',
						start: 66,
						end: 99,
						numberOfProbes: 2,
						name: 'region 2:66-99',
						cytoband: 'cytoband2',
						geneSymbol: 'AURKA',
						geneId: -130751,
						gplId: 'gplId')]
		regions[0].id = -1001L
		regions[1].id = -1002L

		patients = createTestPatients(2, -2000, 'REGION_SAMP_TRIAL')

		allPatientsQueryResult = createQueryResult(patients)

		assays = createTestAssays(patients, -3000L, regionPlatform, TRIAL_NAME, conceptCode)

		acghData = [
				createACGHData(regions[0], assays[0], -1),
				createACGHData(regions[0], assays[1], 0),
				createACGHData(regions[1], assays[0], 1),
				createACGHData(regions[1], assays[1], 2)]

		searchKeywords = bioMarkerTestData.geneSearchKeywords +
				bioMarkerTestData.proteinSearchKeywords +
				bioMarkerTestData.geneSignatureSearchKeywords
	}
}
