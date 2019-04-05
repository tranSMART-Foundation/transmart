package org.transmartproject.export

import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.Platform
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.protein.ProteinDataRow
import spock.lang.Specification

import static org.transmartproject.export.ProteomicsBedExporter.DEFAULT_RGB
import static org.transmartproject.export.ProteomicsBedExporter.HIGH_VALUE_RGB
import static org.transmartproject.export.ProteomicsBedExporter.HIGH_ZSCORE_THRESHOLD
import static org.transmartproject.export.ProteomicsBedExporter.LOW_VALUE_RGB
import static org.transmartproject.export.ProteomicsBedExporter.LOW_ZSCORE_THRESHOLD

class ProteomicsBedExporterSpec extends Specification {

	private MockTabularResultHelper helper = new MockTabularResultHelper()
	private ProteomicsBedExporter exporter = new ProteomicsBedExporter()
	private TabularResult tabularResult
	private Projection projection = Mock()
	private BigDecimal lowZscoreValue = LOW_ZSCORE_THRESHOLD - 0.1
	private BigDecimal highZscoreValue = HIGH_ZSCORE_THRESHOLD + 0.1

	void 'test whether Proteomics data is exported properly'() {
		when:
		tabularResult = createMockProteomicsTabularResult()

		List<ByteArrayOutputStream> outputStreams = []

		exporter.export(tabularResult, projection, { name, ext ->
			outputStreams << new ByteArrayOutputStream()
			outputStreams[-1]
		})

		then:
		outputStreams.size() == 2

		when:
		String assay1Output = outputStreams[0].toString("UTF-8")
		String assay2Output = outputStreams[1].toString("UTF-8")

		List<String> assayLines = assay1Output.readLines()

		then:
		//NOTE: bioMarker is used as label if it's present
		assayLines.size() == 4
		assayLines[0] == 'track name="sample_code_1" itemRgb="On" genome_build="hg19"'
		assayLines[1] == 'chrX\t1234\t1300\ttest-region1\t' + lowZscoreValue + '\t.\t1234\t1300\t' + LOW_VALUE_RGB
		assayLines[2] == 'CHRY\t1301\t1400\ttest-bio-marker\t' + 0 + '\t.\t1301\t1400\t' + DEFAULT_RGB
		assayLines[3] == 'chr9\t1000\t2000\ttest-region3\t' + lowZscoreValue + '\t.\t1000\t2000\t' + LOW_VALUE_RGB

		when:
		assayLines = assay2Output.readLines()

		then:
		assayLines.size() == 4
		assayLines[0] == 'track name="sample_code_2" itemRgb="On" genome_build="hg19"'
		assayLines[1] == 'chrX\t1234\t1300\ttest-region1\t' + highZscoreValue + '\t.\t1234\t1300\t' + HIGH_VALUE_RGB
		assayLines[2] == 'CHRY\t1301\t1400\ttest-bio-marker\t' + highZscoreValue + '\t.\t1301\t1400\t' + HIGH_VALUE_RGB
		assayLines[3] == 'chr9\t1000\t2000\ttest-region3\t' + 0 + '\t.\t1000\t2000\t' + DEFAULT_RGB
	}

	private TabularResult createMockProteomicsTabularResult() {
		// Setup tabularResult and projection to test with
		List<AssayColumn> sampleAssays = helper.createSampleAssays(2)
		Map<String, List<Object>> dataRows = [
				row1: [[intensity: 110, zscore: lowZscoreValue],
				       [intensity: 220, zscore: highZscoreValue]],
				row2: [[intensity: 330, zscore: 0],
				       [intensity: 440, zscore: highZscoreValue]],
				row3: [[intensity: 550, zscore: lowZscoreValue],
				       [intensity: 660, zscore: 0]]]

		Map<String, Map> proteomicsRowProperties = [
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
			createProteomicsRowForAssays(sampleAssays, data, proteomicsRowProperties[rowName])
		}.iterator()

		TabularResult highDimResult = Mock()
		highDimResult.getIndicesList() >> sampleAssays
		highDimResult.getRows() >> iterator
		highDimResult.iterator() >> iterator
		highDimResult
	}

	private DataRow createProteomicsRowForAssays(List<AssayColumn> assays, List data,
	                                             Map<String, Object> properties) {

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

		new ProteinDataRow(
				chromosome: properties.chromosome,
				start: properties.start,
				end: properties.end,
				peptide: properties.name,
				uniprotName: properties.bioMarker,
				data: values.values() as List,
				platform: platform,
				assayIndexMap: assayIndexMap)
	}
}
