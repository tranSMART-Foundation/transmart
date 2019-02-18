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

package org.transmartproject.db.dataquery.highdim.mrna

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
class MrnaTestData extends AbstractTestData {

	public static final String TRIAL_NAME = 'MRNA_SAMP_TRIAL'

	private String conceptCode

	SampleBioMarkerTestData bioMarkerTestData
	ConceptTestData concept
	DeGplInfo platform
	List<PatientDimension> patients
	List<SearchKeywordCoreDb> searchKeywords
	List<DeMrnaAnnotationCoreDb> annotations
	List<DeSubjectSampleMapping> assays
	List<DeSubjectMicroarrayDataCoreDb> microarrayData

	MrnaTestData(String conceptCode = 'concept code #1',
	             SampleBioMarkerTestData bioMarkerTestData = null) {
		this.conceptCode = conceptCode
		this.bioMarkerTestData = bioMarkerTestData ?: new SampleBioMarkerTestData()
		createTestData()
	}

	List<BioMarkerCoreDb> getBioMarkers() {
		bioMarkerTestData.geneBioMarkers
	}

	void saveAll(boolean skipBioMarkerData = false) {
		if (!skipBioMarkerData) {
			bioMarkerTestData.saveGeneData()
		}

		save platform, logger
		saveAll annotations, logger
		saveAll patients, logger
		saveAll assays, logger
		saveAll microarrayData, logger

		concept.saveAll()
	}

	void updateDoubleScaledValues() {
		//making sure BigDecimals use the scale specified in the db (otherwise toString() will yield different results)
		flush()

		for (DeSubjectMicroarrayDataCoreDb it in microarrayData) {
			it.refresh()
		}
	}

	private void createTestData() {
		concept = HighDimTestData.createConcept('MRNAPUBLIC', conceptCode, TRIAL_NAME,
				'MRNA_CONCEPT', 'mrna i2b2 main')

		platform = new DeGplInfo(
				title: 'Affymetrix Human Genome U133A 2.0 Array',
				organism: 'Homo Sapiens',
				markerType: 'Gene Expression',
				genomeReleaseId: 'hg19')
		platform.id = 'BOGUSGPL570'

		patients = HighDimTestData.createTestPatients(2, -300, TRIAL_NAME)

		searchKeywords = bioMarkerTestData.geneSearchKeywords +
				bioMarkerTestData.proteinSearchKeywords +
				bioMarkerTestData.geneSignatureSearchKeywords

		annotations = [
				createAnnotation(-201, '1553506_at', bioMarkers[0]),
				createAnnotation(-202, '1553510_s_at', bioMarkers[1]),
				createAnnotation(-203, '1553513_at', bioMarkers[2])]

		assays = HighDimTestData.createTestAssays(patients, -400, platform, TRIAL_NAME, conceptCode)

		// doubles lose some precision when adding 0.1, so use BigDecimals instead
		BigDecimal intensity = BigDecimal.ZERO
		microarrayData = []
		for (DeMrnaAnnotationCoreDb probe in annotations) {
			for (DeSubjectSampleMapping assay in assays) {
				intensity += 0.1
				microarrayData << createMicroarrayEntry(assay, probe, intensity)
			}
		}
	}

	private DeMrnaAnnotationCoreDb createAnnotation(long probesetId, String probeId, BioMarkerCoreDb bioMarker) {
		DeMrnaAnnotationCoreDb res = new DeMrnaAnnotationCoreDb(
				gplId: platform.id,
				probeId: probeId,
				geneSymbol: bioMarker.name,
				geneId: bioMarker.externalId as Long,
				organism: 'Homo sapiens',
				platform: platform)
		res.id = probesetId
		res
	}

	private DeSubjectMicroarrayDataCoreDb createMicroarrayEntry(DeSubjectSampleMapping assay,
	                                                            DeMrnaAnnotationCoreDb probe,
	                                                            double intensity) {
		new DeSubjectMicroarrayDataCoreDb(
				probe: probe,
				jProbe: probe,
				assay: assay,
				patient: assay.patient,
				rawIntensity: intensity,
				logIntensity: Math.log(intensity) / Math.log(2),
				zscore: intensity * 2, // non-sensical value
				trialName: TRIAL_NAME)
	}
}
