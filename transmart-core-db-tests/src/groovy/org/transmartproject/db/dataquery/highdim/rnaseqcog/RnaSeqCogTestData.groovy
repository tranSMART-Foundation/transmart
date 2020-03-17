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

package org.transmartproject.db.dataquery.highdim.rnaseqcog

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
class RnaSeqCogTestData extends AbstractTestData {

	public static final String TRIAL_NAME = 'RNASEQ_COG_SAMP_TRIAL'

	SampleBioMarkerTestData biomarkerTestData = new SampleBioMarkerTestData()
	ConceptTestData concept
	DeGplInfo platform
	List<PatientDimension> patients
	List<DeSubjectSampleMapping> assays
	List<DeRnaAnnotation> annotations
	List<DeSubjectRnaData> data

	RnaSeqCogTestData() {
		createTestData()
	}

	List<BioMarkerCoreDb> getGenes() {
		biomarkerTestData.geneBioMarkers
	}

	void saveAll() {
		biomarkerTestData.saveGeneData()

		save platform, logger
		saveAll patients, logger
		saveAll assays, logger
		saveAll annotations, logger
		saveAll data, logger

		concept.saveAll()
	}

	private void createTestData() {
		concept = HighDimTestData.createConcept('RNASEQCOGPUBLIC', 'concept code #1',
				TRIAL_NAME, 'RNASEQCOG_CONCEPT')

		platform = new DeGplInfo(
				title: 'Bogus RNA-Seq platform',
				organism: 'Homo Sapiens',
				markerType: 'RNASEQCOG')
		platform.id = 'BOGUS_RNA-SEQ_PLATFORM' // ?? what should be here


		patients = HighDimTestData.createTestPatients(2, -300, TRIAL_NAME)

		assays = HighDimTestData.createTestAssays(patients, -400, platform, TRIAL_NAME)

		long id = -500
		annotations = biomarkerTestData.geneBioMarkers[0..2].collect {
			createAnnotation(--id as String, it)
		}

		double intensity = 0
		data = []
		annotations.each { annotation ->
			for (DeSubjectSampleMapping assay in assays) {
				data << createDataEntry(assay, annotation, intensity += 0.1)
			}
		}
	}

	private DeRnaAnnotation createAnnotation(id, BioMarkerCoreDb gene) {
		DeRnaAnnotation res = new DeRnaAnnotation(
				geneSymbol: gene.name,
				geneId: gene.externalId,
				platform: platform)
		res.id = id
		res
	}

	private DeSubjectRnaData createDataEntry(DeSubjectSampleMapping assay,
	                                         DeRnaAnnotation annotation,
	                                         double intensity) {
		new DeSubjectRnaData(
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
