package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssayAnalysisDataTeaSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_ASSAY_ANALYSIS_DATA_TEA'
		Table table = assertTable('BIO_ASSAY_ANALYSIS_DATA_TEA')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_ASY_ANALYSIS_DATA_ID'
		assertPk table, 'BIO_ASY_ANALYSIS_DATA_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// Double adjustedPvalue
		assertColumn table, 'ADJUSTED_PVALUE', 'double'

		// String adjustedPValueCode
		// adjustedPValueCode column: 'ADJUSTED_P_VALUE_CODE'
		assertColumn table, 'ADJUSTED_P_VALUE_CODE', 'varchar(255)'

		// BioAssayAnalysis analysis
		// analysis column: 'BIO_ASSAY_ANALYSIS_ID'
		assertForeignKeyColumn table, 'BIO_ASSAY_ANALYSIS', 'BIO_ASSAY_ANALYSIS_ID' // table 'BIO_ASSAY_ANALYSIS'

		// BioAssayPlatform assayPlatform
		// assayPlatform column: 'BIO_ASSAY_PLATFORM_ID'
		assertForeignKeyColumn table, 'BIO_ASSAY_PLATFORM', 'BIO_ASSAY_PLATFORM_ID' // table 'BIO_ASSAY_PLATFORM'

		// Double cutValue
		assertColumn table, 'CUT_VALUE', 'double'

		// Experiment experiment
		// experiment column: 'BIO_EXPERIMENT_ID'
		assertForeignKeyColumn table, 'BIO_EXPERIMENT', 'BIO_EXPERIMENT_ID' // table 'BIO_EXPERIMENT'

		// String experimentType
		// experimentType column: 'BIO_EXPERIMENT_TYPE'
		assertColumn table, 'BIO_EXPERIMENT_TYPE', 'varchar(255)'

		// BioAssayFeatureGroup featureGroup
		// featureGroup column: 'BIO_ASSAY_FEATURE_GROUP_ID'
		assertForeignKeyColumn table, 'BIO_ASSAY_FEATURE_GROUP', 'BIO_ASSAY_FEATURE_GROUP_ID' // table 'BIO_ASSAY_FEATURE_GROUP'

		// String featureGroupName
		assertColumn table, 'FEATURE_GROUP_NAME', 'varchar(255)'

		// Double foldChangeRatio
		assertColumn table, 'FOLD_CHANGE_RATIO', 'double'

		// Double numericValue
		assertColumn table, 'NUMERIC_VALUE', 'double'

		// String numericValueCode
		assertColumn table, 'NUMERIC_VALUE_CODE', 'varchar(255)'

		// Double preferredPvalue
		assertColumn table, 'PREFERRED_PVALUE', 'double'

		// Double rawPvalue
		assertColumn table, 'RAW_PVALUE', 'double'

		// String resultsValue
		assertColumn table, 'RESULTS_VALUE', 'varchar(255)'

		// Double rhoValue
		assertColumn table, 'RHO_VALUE', 'double'

		// Double rValue
		assertColumn table, 'R_VALUE', 'double'

		// Double teaNormalizedPValue
		// teaNormalizedPValue column: 'TEA_NORMALIZED_PVALUE'
		assertColumn table, 'TEA_NORMALIZED_PVALUE', 'double'

		// Long teaRank
		assertColumn table, 'TEA_RANK', 'bigint'

		!table.columns
		!table.foreignKeys
		!table.indexes
	}

	void 'test getTopAnalysisDataForAnalysis()'() {
		when:
		BioAssayAnalysis analysis1 = save(new BioAssayAnalysis(analysisMethodCode: 'x', assayDataType: 'x',
				dataCount: 0, etlId: 'x', teaDataCount: 0))
		BioAssayAnalysis analysis2 = save(new BioAssayAnalysis(analysisMethodCode: 'x', assayDataType: 'x',
				dataCount: 0, etlId: 'x', teaDataCount: 0))

		BioMarker bm1a = new BioMarker(bioMarkerType: 't1a')
		BioMarker bm1b = new BioMarker(bioMarkerType: 't1b')
		BioMarker bm2a = new BioMarker(bioMarkerType: 't2a')
		BioMarker bm2b = new BioMarker(bioMarkerType: 't2b')
		BioMarker bm3a = new BioMarker(bioMarkerType: 't3a')
		BioMarker bm4a = new BioMarker(bioMarkerType: 't4a')
		BioMarker bm4b = new BioMarker(bioMarkerType: 't4b')
		BioMarker bm4c = new BioMarker(bioMarkerType: 't4c')

		BioAssayFeatureGroup bafg1 = save(
				new BioAssayFeatureGroup(name: 'x1', type: 'x')
						.addToMarkers(bm1a)
						.addToMarkers(bm1b))

		BioAssayFeatureGroup bafg2 = save(
				new BioAssayFeatureGroup(name: 'x2', type: 'x')
						.addToMarkers(bm2a)
						.addToMarkers(bm2b))

		BioAssayFeatureGroup bafg3 = save(new BioAssayFeatureGroup(name: 'x3', type: 'x').addToMarkers(bm3a))

		BioAssayFeatureGroup bafg4 = save(
				new BioAssayFeatureGroup(name: 'x4', type: 'x')
						.addToMarkers(bm4a)
						.addToMarkers(bm4b)
						.addToMarkers(bm4c))

		BioAssayAnalysisDataTea baadt1 = createBioAssayAnalysisDataTea(analysis1, bafg1, 30)
		BioAssayAnalysisDataTea baadt2 = createBioAssayAnalysisDataTea(analysis1, bafg2, 40)
		BioAssayAnalysisDataTea baadt3 = createBioAssayAnalysisDataTea(analysis1, bafg3, 55)
		BioAssayAnalysisDataTea baadt4 = createBioAssayAnalysisDataTea(analysis2, bafg4, 40)

		List<Object[]> rows1 = BioAssayAnalysisDataTea.getTop50AnalysisDataForAnalysis(analysis1.id)
		List<Object[]> rows2 = BioAssayAnalysisDataTea.getTop50AnalysisDataForAnalysis(analysis2.id)

		then:
		4 == rows1.size()
		baadt2.is rows1[0][0]
		baadt2.is rows1[1][0]
		baadt1.is rows1[2][0]
		baadt1.is rows1[3][0]

		3 == rows2.size()
		baadt4.is rows2[0][0]
		baadt4.is rows2[1][0]
		baadt4.is rows2[2][0]

		when:
		Set<BioMarker> markers1 = [rows1[0][1], rows1[1][1]]
		Set<BioMarker> markers2 = [rows1[2][1], rows1[3][1]]
		Set<BioMarker> markers3 = [rows2[0][1], rows2[1][1], rows2[2][1]]

		then:
		bm2a in markers1
		bm2b in markers1

		bm1a in markers2
		bm1b in markers2

		bm4a in markers3
		bm4b in markers3
		bm4b in markers3
	}

	private BioAssayAnalysisDataTea createBioAssayAnalysisDataTea(BioAssayAnalysis analysis, BioAssayFeatureGroup bafg, long teaRank) {
		BioAssayPlatform bap = BioAssayPlatform.list()[0]
		if (!bap) {
			bap = save(new BioAssayPlatform(accession: 'x', array: 'x', organism: 'x', vendor: 'x'))
		}

		Experiment e = Experiment.list()[0]
		if (!e) {
			e = save(new Experiment(accession: 'x'))
		}

		save new BioAssayAnalysisDataTea(adjustedPvalue: 0, adjustedPValueCode: 'x', analysis: analysis, assayPlatform: bap,
				cutValue: 0, experiment: e, experimentType: 'x', featureGroup: bafg, featureGroupName: 'x',
				foldChangeRatio: 0, numericValue: 0, numericValueCode: 'x', preferredPvalue: 0, rawPvalue: 0,
				resultsValue: 'x', rhoValue: 0, rValue: 0, teaNormalizedPValue: 0, teaRank: teaRank)
	}
}
