package org.transmartproject.export

import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.Platform
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.chromoregion.RegionRowImpl
import spock.lang.Specification

class RnaSeqBedExporterSpec extends Specification {

	private MockTabularResultHelper helper = new MockTabularResultHelper()
	private RnaSeqBedExporter exporter = new RnaSeqBedExporter()
	private TabularResult tabularResult
	private Projection projection = Mock()

	void 'test whether RNASeq data is exported properly'() {
		when:
		tabularResult = createMockRnaSeqTabularResult()

		List<ByteArrayOutputStream> outputStreams = []

		exporter.export(tabularResult, projection, { name, ext ->
			outputStreams << new ByteArrayOutputStream()
			outputStreams[-1]
		})

		then:
		outputStreams.size() == 2

		when:
		String assay1Output = outputStreams[0].toString('UTF-8')
		String assay2Output = outputStreams[1].toString('UTF-8')

		List<String> assayLines = assay1Output.readLines()

		then:
		//NOTE: bioMarker is used as label if it's present
		assayLines.size() == 4
		assayLines[0] == 'track name="sample_code_1" useScore="1" genome_build="hg19"'
		assayLines[1] == 'chrX\t1234\t1300\ttest-region1\t100'
		assayLines[2] == 'CHRY\t1301\t1400\ttest-bio-marker\t300'
		assayLines[3] == 'chr9\t1000\t2000\ttest-region3\t500'

		when:
		assayLines = assay2Output.readLines()

		then:
		assayLines.size() == 4
		assayLines[0] == 'track name="sample_code_2" useScore="1" genome_build="hg19"'
		assayLines[1] == 'chrX\t1234\t1300\ttest-region1\t200'
		assayLines[2] == 'CHRY\t1301\t1400\ttest-bio-marker\t400'
		assayLines[3] == 'chr9\t1000\t2000\ttest-region3\t600'
	}

	private TabularResult createMockRnaSeqTabularResult() {
		// Setup tabularResult and projection to test with
		List<AssayColumn> sampleAssays = helper.createSampleAssays(2)
		Map<String, List<Object>> dataRows = [
				row1: [[readcount: 100], [readcount: 200]],
				row2: [[readcount: 300], [readcount: 400]],
				row3: [[readcount: 500], [readcount: 600]]]

		Map<String, Map> rnaSeqRowProperties = [
				row1: [chromosome: 'chrX',
				       start     : 1234,
				       end       : 1300,
				       name      : 'test-region1',
				       platform  : [genomeReleaseId: 'hg19']],
				row2: [chromosome: 'CHRY',
				       start     : 1301,
				       end       : 1400,
				       name      : 'test-region2',
				       bioMarker : 'test-bio-marker',
				       platform  : [genomeReleaseId: 'hg19']],
				row3: [chromosome: '9',
				       start     : 1000,
				       end       : 2000,
				       name      : 'test-region3',
				       platform  : [genomeReleaseId: 'hg19']]]

		Iterator<DataRow<AssayColumn, Object>> iterator = dataRows.collect { String rowName, List data ->
			createRnaSeqRowForAssays(sampleAssays, data, rnaSeqRowProperties[rowName])
		}.iterator()

		TabularResult highDimResult = Mock()
		highDimResult.getIndicesList() >> sampleAssays
		highDimResult.getRows() >> iterator
		highDimResult.iterator() >> iterator

		highDimResult
	}

	private DataRow createRnaSeqRowForAssays(List<AssayColumn> assays, List data, Map<String, Object> properties) {

		Platform platform = null
		if (properties.platform) {
			platform = Mock()
			platform.getGenomeReleaseId() >> properties.platform.genomeReleaseId
		}

		Map<AssayColumn, Object> values = helper.dot(assays, data)

		Map<AssayColumn, Integer> assayIndexMap = [:]
		values.eachWithIndex { Map.Entry<AssayColumn, Object> entry, int i ->
			assayIndexMap[entry.key] = i
		}

		new RegionRowImpl(
				chromosome: properties.chromosome,
				start: properties.start,
				end: properties.end,
				name: properties.name,
				bioMarker: properties.bioMarker,
				data: values.values() as List,
				platform: platform,
				assayIndexMap: assayIndexMap)
	}
}
