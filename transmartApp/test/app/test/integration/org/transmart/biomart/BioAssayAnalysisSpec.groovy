package org.transmart.biomart

import test.Table

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class BioAssayAnalysisSpec extends AbstractDomainSpec {

	void 'verify mapping -> DDL'() {
		when:
		exportSchema()

		then:
		// table 'BIO_ASSAY_ANALYSIS'
		Table table = assertTable('BIO_ASSAY_ANALYSIS')

		// id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID'], column: 'BIO_ASSAY_ANALYSIS_ID'
		assertPk table, 'BIO_ASSAY_ANALYSIS_ID'
		assertSequence 'SEQ_BIO_DATA_ID'

		// String analysisMethodCode
		// analysisMethodCode column: 'ANALYSIS_METHOD_CD'
		assertColumn table, 'ANALYSIS_METHOD_CD', 'varchar(255)'

		// BioAssayAnalysisPlatform analysisPlatform
		// analysisPlatform column: 'BIO_ASY_ANALYSIS_PLTFM_ID'
		// analysisPlatform nullable: true
		assertForeignKeyColumn table, 'BIO_ASY_ANALYSIS_PLTFM', 'BIO_ASY_ANALYSIS_PLTFM_ID',
				'bigint', true // table 'BIO_ASY_ANALYSIS_PLTFM'

		// String analystId
		// analystId nullable: true, maxSize: 1020
		assertColumn table, 'ANALYST_ID', 'varchar(1020)', true

		// String assayDataType
		// assayDataType column: 'BIO_ASSAY_DATA_TYPE'
		assertColumn table, 'BIO_ASSAY_DATA_TYPE', 'varchar(255)'

		// Date createDate
		// createDate column: 'ANALYSIS_CREATE_DATE'
		// createDate nullable: true
		assertColumn table, 'ANALYSIS_CREATE_DATE', 'timestamp', true

		// Long dataCount
		assertColumn table, 'DATA_COUNT', 'bigint'

		// String etlId
		assertColumn table, 'ETL_ID', 'varchar(255)'

		// Double foldChangeCutoff
		// foldChangeCutoff nullable: true
		assertColumn table, 'FOLD_CHANGE_CUTOFF', 'double', true

		// String longDescription
		// longDescription nullable: true, maxSize: 4000
		assertColumn table, 'LONG_DESCRIPTION', 'varchar(4000)', true

		// String name
		// name column: 'ANALYSIS_NAME'
		// name nullable: true, maxSize: 1000
		assertColumn table, 'ANALYSIS_NAME', 'varchar(1000)', true

		// Double pValueCutoff
		// pValueCutoff column: 'PVALUE_CUTOFF'
		// pValueCutoff nullable: true
		assertColumn table, 'PVALUE_CUTOFF', 'double', true

		// String qaCriteria
		// qaCriteria nullable: true, maxSize: 4000
		assertColumn table, 'QA_CRITERIA', 'varchar(4000)', true

		// Double rValueCutoff
		// rValueCutoff column: 'RVALUE_CUTOFF'
		// rValueCutoff nullable: true
		assertColumn table, 'RVALUE_CUTOFF', 'double', true

		// String shortDescription
		// shortDescription nullable: true, maxSize: 1020
		assertColumn table, 'SHORT_DESCRIPTION', 'varchar(1020)', true

		// Long teaDataCount
		assertColumn table, 'TEA_DATA_COUNT', 'bigint'

		// String type
		// type column: 'ANALYSIS_TYPE'
		// type nullable: true, maxSize: 400
		assertColumn table, 'ANALYSIS_TYPE', 'varchar(400)', true

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

		BioAssayAnalysisData baadt1 = createBioAssayAnalysisData(analysis1, bafg1, 30)
		BioAssayAnalysisData baadt2 = createBioAssayAnalysisData(analysis1, bafg2, 40)
		BioAssayAnalysisData baadt3 = createBioAssayAnalysisData(analysis1, bafg3, 55)
		BioAssayAnalysisData baadt4 = createBioAssayAnalysisData(analysis2, bafg4, 40)

		List<Object[]> rows1 = BioAssayAnalysis.getTopAnalysisDataForAnalysis(analysis1.id, 50)
		List<Object[]> rows2 = BioAssayAnalysis.getTopAnalysisDataForAnalysis(analysis2.id, 50)

		then:
		5 == rows1.size()
		3 == rows2.size()

		when:
		def findTuplesFor = { List<Object[]> rows, BioAssayAnalysisData baadt ->
			rows.findAll { Object[] row -> baadt.is row[0] } as Set<Object[]>
		}
		Set<Object[]> subRows1 = findTuplesFor(rows1, baadt1)
		Set<Object[]> subRows2 = findTuplesFor(rows1, baadt2)
		Set<Object[]> subRows3 = findTuplesFor(rows1, baadt3)
		Set<Object[]> subRows4 = findTuplesFor(rows2, baadt4)

		def extractBioMarkers = { Set<Object[]> subRows -> subRows.collect { it[1] } }
		Collection<BioMarker> markers1 = extractBioMarkers(subRows1)
		Collection<BioMarker> markers2 = extractBioMarkers(subRows2)
		Collection<BioMarker> markers3 = extractBioMarkers(subRows3)
		Collection<BioMarker> markers4 = extractBioMarkers(subRows4)

		then:
		2 == markers1.size()
		bm1a in markers1
		bm1b in markers1

		2 == markers2.size()
		bm2a in markers2
		bm2b in markers2

		1 == markers3.size()
		bm3a in markers3

		3 == markers4.size()
		bm4a in markers4
		bm4b in markers4
		bm4b in markers4
	}

	void 'test getUniqueId()'() {
		when:
		BioAssayAnalysis baa = new BioAssayAnalysis()

		then:
		!baa.uniqueId

		when:
		BioData bd = new BioData()
		baa.addToUniqueIds bd

		then:
		bd.is baa.uniqueId
	}

	private BioAssayAnalysisData createBioAssayAnalysisData(BioAssayAnalysis analysis, BioAssayFeatureGroup bafg, double foldChangeRatio) {
		BioAssayPlatform bap = BioAssayPlatform.list()[0]
		if (!bap) {
			bap = save(new BioAssayPlatform(accession: 'x', array: 'x', organism: 'x', vendor: 'x'))
		}

		Experiment e = Experiment.list()[0]
		if (!e) {
			e = save(new Experiment(accession: 'x'))
		}

		save new BioAssayAnalysisData(adjustedPvalue: 0, adjustedPValueCode: 'x', analysis: analysis, assayPlatform: bap,
				cutValue: 0, experiment: e, featureGroup: bafg, featureGroupName: 'x', foldChangeRatio: foldChangeRatio,
				numericValue: 0, numericValueCode: 'x', preferredPvalue: 0, rawPvalue: 0, resultsValue: 'x', rhoValue: 0,
				rValue: 0, teaNormalizedPValue: 0)
	}
}
