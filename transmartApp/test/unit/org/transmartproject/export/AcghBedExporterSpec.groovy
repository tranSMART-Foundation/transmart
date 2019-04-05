package org.transmartproject.export

import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.Platform
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.chromoregion.RegionRowImpl
import spock.lang.Specification

class AcghBedExporterSpec extends Specification {

	private MockTabularResultHelper helper = new MockTabularResultHelper()
	private AcghBedExporter exporter = new AcghBedExporter()
	private TabularResult tabularResult
	private Projection projection = Mock()

	void setup() {
		exporter.highDimExporterRegistry = [
				registerHighDimensionExporter: { format, exporter -> }
		] as HighDimExporterRegistry
		exporter.init()
	}

	void 'test whether aCGH data is exported properly'() {
		when:
		tabularResult = createMockAcghTabularResult()

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

		List<String> assay1Lines = assay1Output.readLines()
		//NOTE: bioMarker is used as label if it's present

		then:
		assay1Lines[0] == 'track name="sample_code_1" itemRgb="On" genome_build="hg19"'
		assay1Lines[1] == 'chrX\t1234\t1300\ttest-region1\t-1\t.\t1234\t1300\t0,0,205'
		assay1Lines[2] == 'CHRY\t1301\t1400\ttest-bio-marker\t1\t.\t1301\t1400\t205,0,0'
		assay1Lines[3] == 'chr9\t1000\t2000\ttest-region3\t11\t.\t1000\t2000\t255,255,255'

		when:
		List<String> assay2Lines = assay2Output.readLines()

		then:
		assay2Lines[0] == 'track name="sample_code_2" itemRgb="On" genome_build="hg19"'
		assay2Lines[1] == 'chrX\t1234\t1300\ttest-region1\t0\t.\t1234\t1300\t169,169,169'
		assay2Lines[2] == 'CHRY\t1301\t1400\ttest-bio-marker\t2\t.\t1301\t1400\t88,0,0'
		assay2Lines[3] == 'chr9\t1000\t2000\ttest-region3\t-1\t.\t1000\t2000\t0,0,205'
	}

	void 'test custom color scheme'() {
		when:
		exporter.acghBedExporterRgbColorScheme = [
				invalid      : [250, 250, 250], //white
				loss         : [205, 0, 0], // red
				normal       : [10, 10, 10], // dark
				gain         : [0, 255, 0], // green
				amplification: [0, 100, 0]] // dark green

		exporter.init()

		tabularResult = createMockAcghTabularResult()

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

		List<String> assay1Lines = assay1Output.readLines()

		then:
		//NOTE: bioMarker is used as label if it's present
		assay1Lines[0] == 'track name="sample_code_1" itemRgb="On" genome_build="hg19"'
		assay1Lines[1] == 'chrX\t1234\t1300\ttest-region1\t-1\t.\t1234\t1300\t205,0,0'
		assay1Lines[2] == 'CHRY\t1301\t1400\ttest-bio-marker\t1\t.\t1301\t1400\t0,255,0'
		assay1Lines[3] == 'chr9\t1000\t2000\ttest-region3\t11\t.\t1000\t2000\t250,250,250'

		when:
		List<String> assay2Lines = assay2Output.readLines()

		then:
		assay2Lines[0] == 'track name="sample_code_2" itemRgb="On" genome_build="hg19"'
		assay2Lines[1] == 'chrX\t1234\t1300\ttest-region1\t0\t.\t1234\t1300\t10,10,10'
		assay2Lines[2] == 'CHRY\t1301\t1400\ttest-bio-marker\t2\t.\t1301\t1400\t0,100,0'
		assay2Lines[3] == 'chr9\t1000\t2000\ttest-region3\t-1\t.\t1000\t2000\t205,0,0'
	}

	private TabularResult createMockAcghTabularResult() {
		// Setup tabularResult and projection to test with
		List<AssayColumn> sampleAssays = helper.createSampleAssays(2)
		Map<String, List<Object>> dataRows = [
				row1: [[flag: -1], [flag: 0]],
				row2: [[flag: 1], [flag: 2]],
				row3: [[flag: 11], [flag: -1]]]

		Map<String, Map> acghRowProperties = [
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
				       platform  : [genomeReleaseId: 'hg19']]
		]


		Iterator<DataRow<AssayColumn, Object>> iterator = dataRows.collect { String rowName, List data ->
			createAcghRowForAssays(sampleAssays, data, acghRowProperties[rowName])
		}.iterator()

		new TabularResult() {
			List getIndicesList() { sampleAssays }
			Iterator getRows() { iterator }
			Iterator iterator() { iterator }
			String getColumnsDimensionLabel() {}
			String getRowsDimensionLabel() {}
			void close() {}
		}
	}

	private DataRow createAcghRowForAssays(List<AssayColumn> assays, List data,
	                                       Map<String, Object> acghProperties) {

		Map<AssayColumn, Object> values = helper.dot(assays, data)

		Platform platform = null
		if (acghProperties.platform) {
			platform = Mock()
			platform.getGenomeReleaseId() >> acghProperties.platform.genomeReleaseId
		}

		Map<AssayColumn, Integer> assayIndexMap = [:]
		values.eachWithIndex { Map.Entry<AssayColumn, Object> entry, int i ->
			assayIndexMap[entry.key] = i
		}

		new RegionRowImpl(
				chromosome: acghProperties.chromosome,
				start: acghProperties.start,
				end: acghProperties.end,
				name: acghProperties.name,
				bioMarker: acghProperties.bioMarker,
				data: values.values() as List,
				platform: platform,
				assayIndexMap: assayIndexMap)
	}
}
