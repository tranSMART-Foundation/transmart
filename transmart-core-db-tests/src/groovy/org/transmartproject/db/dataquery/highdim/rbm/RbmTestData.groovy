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

package org.transmartproject.db.dataquery.highdim.rbm

import groovy.util.logging.Slf4j
import org.transmartproject.db.AbstractTestData
import org.transmartproject.db.biomarker.BioMarkerCoreDb
import org.transmartproject.db.dataquery.highdim.DeGplInfo
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.HighDimTestData
import org.transmartproject.db.dataquery.highdim.SampleBioMarkerTestData
import org.transmartproject.db.i2b2data.PatientDimension
import org.transmartproject.db.ontology.ConceptTestData
import org.transmartproject.db.search.SearchKeywordCoreDb

@Slf4j('logger')
class RbmTestData extends AbstractTestData {

	public static final String TRIAL_NAME = 'RBM_SAMP_TRIAL'

	SampleBioMarkerTestData bioMarkerTestData = new SampleBioMarkerTestData()
	DeGplInfo platform
	ConceptTestData concept
	List<PatientDimension> patients
	List<DeSubjectSampleMapping> assays
	List<DeRbmAnnotation> annotations
	List<DeSubjectRbmData> data
	List<SearchKeywordCoreDb> searchKeywords

	RbmTestData() {
		createTestData()
	}

	List<BioMarkerCoreDb> getBioMarkers() {
		bioMarkerTestData.geneBioMarkers
	}

	void saveAll() {
		bioMarkerTestData.saveProteinData()

		save platform, logger
		saveAll patients, logger
		saveAll assays, logger
		saveAll annotations, logger
		saveAll data, logger

		concept.saveAll()
	}

	private void createTestData() {
		platform = new DeGplInfo(
				title: 'RBM platform',
				organism: 'Homo Sapiens',
				markerType: 'RBM')
		platform.id = 'BOGUSRBMplatform'

		concept = HighDimTestData.createConcept('RBMPUBLIC', 'concept code #1', TRIAL_NAME, 'RBM_CONCEPT')

		patients = HighDimTestData.createTestPatients(2, -300, TRIAL_NAME)

		assays = HighDimTestData.createTestAssays(patients, -400, platform, TRIAL_NAME)

		annotations = [
				//Adiponectin
				createAnnotation(-501, 'Antigene1', 'Q15848', 'PVR_HUMAN1', 'AURKA', -601),
				//Urea transporter 2
				createAnnotation(-502, 'Antigene2', 'Q15849', 'PVR_HUMAN2', 'SLC14A2', -602),
				//Adipogenesis regulatory factor
				createAnnotation(-503, 'Antigene3', 'Q15847', 'PVR_HUMAN3', 'ADIRF', -603),
				createAnnotation(-504, 'Antigene3', 'Q15850', 'PVR_HUMAN4', 'EMBL', -604)]

		double value = 0
		data = []
		annotations.groupBy { DeRbmAnnotation it -> it.antigenName }
				.eachWithIndex { Map.Entry<String, List<DeRbmAnnotation>> annotationsEntry, int index ->
			for (DeSubjectSampleMapping assay in assays) {
				data << createRbmEntry(assay, annotationsEntry.value, (value += 0.1), ('A'..'Z')[index])
			}
				}

		searchKeywords = bioMarkerTestData.geneSearchKeywords +
				bioMarkerTestData.proteinSearchKeywords +
				bioMarkerTestData.geneSignatureSearchKeywords
	}

	private DeRbmAnnotation createAnnotation(long id, String antigene, String uniprotId, String uniprotName, String geneSymbol, long geneId) {
		DeRbmAnnotation res = new DeRbmAnnotation(
				gplId: platform.id,
				antigenName: antigene,
				uniprotId: uniprotId,
				uniprotName: uniprotName,
				geneSymbol: geneSymbol,
				geneId: geneId,
				platform: platform)
		res.id = id
		res
	}

	private DeSubjectRbmData createRbmEntry(DeSubjectSampleMapping assay,
	                                        List<DeRbmAnnotation> annotations,
	                                        double value,
	                                        String unit = null) {
		new DeSubjectRbmData(
				annotations: annotations as Set,
				assay: assay,
				patient: assay.patient,
				value: value,
				logIntensity: Math.log(value),
				unit: unit ? '(' + unit + ')' : null,
				zscore: (value - 0.35) / 0.1871)
	}
}
