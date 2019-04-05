package org.transmartproject.export

import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.projections.AllDataProjection
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.exceptions.NoSuchResourceException
import spock.lang.Specification

class TabSeparatedExporterSpec extends Specification {

	private MockTabularResultHelper helper = new MockTabularResultHelper()
	private TabSeparatedExporter exporter = new TabSeparatedExporter()
	private TabularResult tabularResult

	void 'test whether supported datatypes are recognized'() {
		when:
		HighDimensionDataTypeResource mrnaResource = Mock()
		mrnaResource.getSupportedProjections() >> ['all_data']

		HighDimensionDataTypeResource mirnaResource = Mock()
		mirnaResource.getSupportedProjections() >> ['default_real_projection', 'some_other_projection', 'all_data']

		HighDimensionDataTypeResource otherResource = Mock()
		otherResource.getSupportedProjections() >> ['default_real_projection', 'some_other_projection']

		HighDimensionResource resourceService = Mock()
		resourceService.getSubResourceForType('mrna') >> mrnaResource
		resourceService.getSubResourceForType('mirna') >> mirnaResource
		resourceService.getSubResourceForType('other') >> otherResource
		resourceService.getSubResourceForType(null) >> { throw new NoSuchResourceException('Unknown data type: null') }
		resourceService.getSubResourceForType('unknownFormat') >> { throw new NoSuchResourceException('Unknown data type: other') }

		exporter.highDimensionResourceService = resourceService

		then:
		// Tab separated export is supported on every datatype
		// with ALL_DATA_PROJECTION
		exporter.isDataTypeSupported('mrna')
		exporter.isDataTypeSupported('mirna')

		!exporter.isDataTypeSupported('other')
		!exporter.isDataTypeSupported(null)
		!exporter.isDataTypeSupported('unknownFormat')
	}

	void 'test whether a cancelled export does not produce output'() {
		when:
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

		TabularResult tabularResult = Mock()
		Projection projection = Mock()

		exporter.export(tabularResult, projection, { name, ext -> outputStream }, { true })

		then:
		// As the export is cancelled,
		!outputStream.toString()
	}

	void 'test whether a basic tabular result is exported properly'() {
		when:
		// Setup tabularResult and projection to test with
		List<AssayColumn> sampleAssays = helper.createSampleAssays(2)
		Map<String, List<Object>> labelToData = [
				row1: [[property1: 4,], [property1: 2, property2: 3]],
				row2: [[property1: 5, property2: 10], null]]
		tabularResult = helper.createMockTabularResult(assays: sampleAssays, data: labelToData)

		// Create all data projection, as that is used for exporting tab separated files
		AllDataProjection projection = Mock()
		projection.getRowProperties() >> [label: String]
		projection.getDataProperties() >> [property1: Integer, property2: Integer]

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

		exporter.export(tabularResult, projection, { name, ext -> outputStream })

		String output = outputStream.toString('UTF-8')

		// Assert we have at least some text, in UTF-8 encoding
		then:
		output

		List<String> lines = output.readLines()

		// We expect 4 lines: 1 header line, two lines  for assay 1, and one line for assay 2
		then:
		lines.size() == 4

		// Check header line
		lines[0] == ['Assay ID', 'PROPERTY1', 'PROPERTY2', 'LABEL'].join(exporter.SEPARATOR)

		// Check the data lines. First line should contain 'null' for property2, as that one was not set
		lines[1] == ['1', '4', 'null', 'row1'].join(exporter.SEPARATOR)

		lines[2] == ['2', '2', '3', 'row1'].join(exporter.SEPARATOR)

		lines[3] == ['1', '5', '10', 'row2'].join(exporter.SEPARATOR)
	}
}
