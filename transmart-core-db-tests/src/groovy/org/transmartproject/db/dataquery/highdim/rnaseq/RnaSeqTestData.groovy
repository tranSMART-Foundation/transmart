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

package org.transmartproject.db.dataquery.highdim.rnaseq

import groovy.util.logging.Slf4j
import org.transmartproject.db.AbstractTestData
import org.transmartproject.db.dataquery.highdim.DeGplInfo
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.HighDimTestData
import org.transmartproject.db.dataquery.highdim.SampleBioMarkerTestData
import org.transmartproject.db.dataquery.highdim.chromoregion.DeChromosomalRegion
import org.transmartproject.db.i2b2data.PatientDimension
import org.transmartproject.db.ontology.ConceptTestData
import org.transmartproject.db.querytool.QtQueryMaster
import org.transmartproject.db.search.SearchKeywordCoreDb

import static org.transmartproject.db.dataquery.highdim.HighDimTestData.createTestAssays
import static org.transmartproject.db.dataquery.highdim.HighDimTestData.createTestPatients
import static org.transmartproject.db.querytool.QueryResultData.createQueryResult

@Slf4j('logger')
class RnaSeqTestData extends AbstractTestData {

	static final String REGION_PLATFORM_MARKER_TYPE = 'RNASEQ_RCNT'
	static final String TRIAL_NAME = 'REGION_SAMP_TRIAL_RNASEQ'

	private String conceptCode

	SampleBioMarkerTestData bioMarkerTestData
	ConceptTestData concept
	List<SearchKeywordCoreDb> searchKeywords
	DeGplInfo regionPlatform
	DeGplInfo bogusTypePlatform
	List<DeChromosomalRegion> regions
	List<PatientDimension> patients
	QtQueryMaster allPatientsQueryResult
	List<DeSubjectSampleMapping> assays
	List<DeSubjectRnaseqData> rnaseqData

	RnaSeqTestData(String conceptCode = 'concept code #1',
	               SampleBioMarkerTestData bioMarkerTestData = null) {
		this.conceptCode = conceptCode
		this.bioMarkerTestData = bioMarkerTestData ?: new SampleBioMarkerTestData()
		createTestData()
	}

	DeSubjectRnaseqData createRNASEQData(DeChromosomalRegion region, DeSubjectSampleMapping assay,
	                                     int readcount = 0, double normalizedreadcount = 0.0) {
		new DeSubjectRnaseqData(
				region: region,
				jRegion: region,
				assay: assay,
				patient: assay.patient,
				readcount: readcount,
				normalizedReadcount: normalizedreadcount,
				logNormalizedReadcount: Math.log(normalizedreadcount) / Math.log(2.0),
				zscore: ((Math.log(normalizedreadcount) / Math.log(2.0)) - 0.5) / 1.5)
	}

	void saveAll() {
		bioMarkerTestData.saveGeneData()

		save regionPlatform, logger
		save bogusTypePlatform, logger
		saveAll regions, logger
		saveAll patients, logger
		save allPatientsQueryResult, logger
		saveAll assays, logger
		saveAll rnaseqData, logger

		concept.saveAll()
	}

	private void createTestData() {
		concept = HighDimTestData.createConcept('RNASEQPUBLIC', 'concept code #1', TRIAL_NAME, 'RNASEQ_CONCEPT')

		searchKeywords = bioMarkerTestData.geneSearchKeywords +
				bioMarkerTestData.proteinSearchKeywords +
				bioMarkerTestData.geneSignatureSearchKeywords

		regionPlatform = new DeGplInfo(
				title: 'Test Region Platform',
				organism: 'Homo Sapiens',
				annotationDate: Date.parse('yyyy-MM-dd', '2013-05-03'),
				markerType: REGION_PLATFORM_MARKER_TYPE,
				genomeReleaseId: 'hg18')
		regionPlatform.id = 'test-region-platform_rnaseq'

		bogusTypePlatform = new DeGplInfo(markerTypeId: 'bogus marker type')
		bogusTypePlatform.id = 'bogus-marker-platform_rnaseq'

		regions = [
				new DeChromosomalRegion(
						platform: regionPlatform,
						chromosome: '1',
						start: 33,
						end: 9999,
						numberOfProbes: 42,
						cytoband: '1p12.1',
						name: 'region 1:33-9999',
						geneSymbol: 'ADIRF',
						geneId: -130753,
						gplId: 'gplId'),
				new DeChromosomalRegion(
						platform: regionPlatform,
						chromosome: '2',
						start: 66,
						end: 99,
						numberOfProbes: 2,
						cytoband: '2q7.2',
						name: 'region 2:66-99',
						geneSymbol: 'AURKA',
						geneId: -130751,
						gplId: 'gplId'),
		]
		regions[0].id = -1011L
		regions[1].id = -1012L

		patients = createTestPatients(2, -2010, 'REGION_SAMP_TRIAL_RNASEQ')

		allPatientsQueryResult = createQueryResult(patients)

		assays = createTestAssays(patients, -3010L, regionPlatform, TRIAL_NAME)

		rnaseqData = [
				createRNASEQData(regions[0], assays[0], 1, 1.0),
				createRNASEQData(regions[0], assays[1], 10, 4.0),
				createRNASEQData(regions[1], assays[0], 2, 0.5),
				createRNASEQData(regions[1], assays[1], 2, 2.0)]
	}
}
