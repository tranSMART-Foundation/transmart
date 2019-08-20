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
import org.spockframework.util.Assert
import org.transmartproject.db.AbstractTestData
import org.transmartproject.db.biomarker.BioDataCorrelDescr
import org.transmartproject.db.biomarker.BioDataCorrelationCoreDb
import org.transmartproject.db.biomarker.BioMarkerCoreDb
import org.transmartproject.db.dataquery.highdim.correlations.CorrelationType
import org.transmartproject.db.dataquery.highdim.correlations.CorrelationTypesRegistry
import org.transmartproject.db.i2b2data.I2b2Data
import org.transmartproject.db.i2b2data.PatientDimension
import org.transmartproject.db.ontology.ConceptTestData
import org.transmartproject.db.search.SearchGeneSignature
import org.transmartproject.db.search.SearchKeywordCoreDb
import org.transmartproject.db.TestDataHelper

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

@Slf4j('logger')
class HighDimTestData extends AbstractTestData {

	static final long BIO_DATA_CORREL_DESCR_SEQ = -9100

	static final CorrelationTypesRegistry CORRELATION_TYPES_REGISTRY
	static {
		CORRELATION_TYPES_REGISTRY = new CorrelationTypesRegistry()
		CORRELATION_TYPES_REGISTRY.init()
	}

	static List<DeSubjectSampleMapping> createTestAssays(List<PatientDimension> patients,
	                                                     long baseId, DeGplInfo platform,
	                                                     String trialName = 'SAMP_TRIAL',
	                                                     String conceptCode = 'concept code #1', // bogus
	                                                     String sampleCodePrefix = 'SAMPLE_FOR_') {

		patients.collect { PatientDimension p ->
			DeSubjectSampleMapping s = new DeSubjectSampleMapping(
					patient: p,
					patientInTrialId: p.sourcesystemCd.split(':')[1],

					// common
					siteId: 'site id #1',
					conceptCode: conceptCode,
					trialName: trialName,
					timepointName: 'timepoint name #1',
					timepointCd: 'timepoint code',
					sampleTypeName: 'sample name #1',
					sampleTypeCd: 'sample code',
					tissueTypeName: 'tissue name #1',
					tissueTypeCd: 'tissue code',
					sampleCode: sampleCodePrefix + p.id,
					platform: platform)
			s.id = --baseId
			s
		}
	}

	static ConceptTestData createConcept(String root = 'PUBLIC', String concept_code = 'concept code #1',
	                                     String trialName = 'SAMP_TRIAL', String name = 'SAMP_CONCEPT',
	                                     String tableCode = 'i2b2 main') {
		ConceptTestData result = new ConceptTestData()
		result.addLeafConcept root, trialName, name, concept_code, tableCode
		result
	}

	//to be removed (unnecessary indirection)
	static List<PatientDimension> createTestPatients(int n, long baseId, String trialName = 'SAMP_TRIAL') {
		I2b2Data.createTestPatients n, baseId, trialName
	}

	// returns list with two elements: the biomarkers, and the search keywords
	static List<BioMarkerCoreDb> createBioMarkers(long baseId, List<Map<String, String>> attributes,
	                                              String type = 'GENE', String organism = 'HOMO SAPIENS',
	                                              String sourceCode = 'Entrez') {
		(0..attributes.size() - 1).collect { int i ->
			assertThat([attributes[i].name,
			            attributes[i].externalId], everyItem(is(notNullValue())))
			BioMarkerCoreDb bm = new BioMarkerCoreDb(
					type: type,
					organism: organism,
					sourceCode: sourceCode,
					*: attributes[i])
			bm.id = baseId - 1 - i
			bm
		}
	}

	static List<SearchKeywordCoreDb> createSearchKeywordsForBioMarkers(
			List<BioMarkerCoreDb> biomarkers, long baseId) {
		biomarkers.collect { BioMarkerCoreDb it ->
			SearchKeywordCoreDb res = new SearchKeywordCoreDb(
					keyword: it.name,
					bioDataId: it.id,
					uniqueId: it.type + ':' + it.externalId,
					dataCategory: it.type)
			res.id = --baseId
			res
		}
	}

	static List<SearchKeywordCoreDb> createSearchKeywordsForGeneSignatures(
			List<SearchGeneSignature> geneSignatures, long baseId) {
		geneSignatures.collect { SearchGeneSignature sig ->
			SearchKeywordCoreDb res = new SearchKeywordCoreDb(
					keyword: sig.name,
					bioDataId: sig.id,
					uniqueId: 'GENESIG:' + sig.id,
					dataCategory: 'GENESIG')
			res.id = --baseId
			res
		}
	}

	static List<BioDataCorrelationCoreDb> createCorrelationPairs(
			long baseId, List<BioMarkerCoreDb> from, List<BioMarkerCoreDb> to) {

		List<BioDataCorrelationCoreDb> pairs = []
		for (int i = 0; i < from.size(); i++) {
			pairs << createCorrelation(baseId - 1 - i, from[i], to[i])
		}
		pairs
	}

	private static BioDataCorrelationCoreDb createCorrelation(long id, BioMarkerCoreDb left, BioMarkerCoreDb right) {

		// registryTable's rows are the target, hence the order of the arg
		CorrelationType correlationType = CORRELATION_TYPES_REGISTRY.registryTable.get(right.type, left.type)
		Assert.notNull correlationType, 'Did not know I could associate ' + left.type + ' with ' + right.type

		BioDataCorrelDescr descr = BioDataCorrelDescr.findWhere(correlation: correlationType.name)
		if (!descr) {
			descr = new BioDataCorrelDescr(correlation: correlationType.name) // the rest doesn't really matter
			descr.id = --BIO_DATA_CORREL_DESCR_SEQ
		}

		BioDataCorrelationCoreDb res = new BioDataCorrelationCoreDb(
				description: descr,
				leftBioMarker: left,
				rightBioMarker: right)
		res.id = id
		res
	}

    //to be removed (unnecessary indirection)
    static void save(List objects) {
        TestDataHelper.save(objects)
    }

    void saveAll() {}
}
