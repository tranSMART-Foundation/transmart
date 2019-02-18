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

package org.transmartproject.db.dataquery.highdim.protein

import groovy.util.logging.Slf4j
import org.transmartproject.db.AbstractTestData
import org.transmartproject.db.biomarker.BioMarkerCoreDb
import org.transmartproject.db.dataquery.highdim.DeGplInfo
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.HighDimTestData
import org.transmartproject.db.dataquery.highdim.SampleBioMarkerTestData
import org.transmartproject.db.i2b2data.PatientDimension
import org.transmartproject.db.ontology.ConceptTestData

@Slf4j('logger')
class ProteinTestData extends AbstractTestData {

	public static final String TRIAL_NAME = 'PROTEIN_SAMP_TRIAL'

	SampleBioMarkerTestData biomarkerTestData = new SampleBioMarkerTestData()
	DeGplInfo platform
	ConceptTestData concept
	List<PatientDimension> patients
	List<DeSubjectSampleMapping> assays
	List<DeProteinAnnotation> annotations
	List<DeSubjectProteinData> data

	ProteinTestData() {
		createTestData()
	}

	List<BioMarkerCoreDb> getProteins() {
		biomarkerTestData.proteinBioMarkers
	}

	void saveAll() {
		biomarkerTestData.saveProteinData()

		save platform, logger
		saveAll patients, logger
		saveAll assays, logger
		saveAll annotations, logger
		saveAll data, logger

		concept.saveAll()
	}

	private void createTestData() {
		platform = new DeGplInfo(
				title: 'Bogus protein platform',
				organism: 'Homo Sapiens',
				markerType: 'PROTEOMICS')
		platform.id = 'BOGUS_PROTEIN_PLATFORM' // ?? what should be here

		concept = HighDimTestData.createConcept('PROTEINPUBLIC', 'concept code #1',
				TRIAL_NAME, 'PROTEIN_CONCEPT')

		patients = HighDimTestData.createTestPatients(2, -300, TRIAL_NAME)

		assays = HighDimTestData.createTestAssays(patients, -400, platform, TRIAL_NAME)

		annotations = [
				// not the actual full sequences here...
				createAnnotation(-501, 'Adipogenesis regulatory factor', 'PVR_HUMAN1', 'MASKGLQDLK'),
				createAnnotation(-502, 'Adiponectin', 'PVR_HUMAN2', 'MLLLGAVLLL'),
				createAnnotation(-503, 'Urea transporter 2', 'PVR_HUMAN3', 'MSDPHSSPLL')]

		double intensity = 0
		data = []
		for (DeProteinAnnotation annotation in annotations) {
			for (DeSubjectSampleMapping assay in assays) {
				data << createDataEntry(assay, annotation, intensity += 0.1)
			}
		}
	}

	private DeProteinAnnotation createAnnotation(long id, String proteinName, String uniprotName, String peptide) {
		DeProteinAnnotation res = new DeProteinAnnotation(
				peptide: peptide,
				uniprotId: biomarkerTestData.proteinBioMarkers.find { it.name == proteinName }.externalId,
				uniprotName: uniprotName,
				platform: platform,
				gplId: 'gplId')
		res.id = id
		res
	}

	private DeSubjectProteinData createDataEntry(DeSubjectSampleMapping assay, DeProteinAnnotation annotation,
	                                             double intensity) {
		new DeSubjectProteinData(
				assay: assay,
				patient: assay.patient,
				annotation: annotation,
				jAnnotation: annotation,
				intensity: intensity,
				logIntensity: Math.log(intensity),
				zscore: (intensity - 0.35) / 0.1871)
	}
}
