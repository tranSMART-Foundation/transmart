package org.transmart.dataexport

import org.transmartproject.export.HighDimExporter
import org.transmartproject.export.HighDimExporterRegistry
import spock.lang.Specification

class HighDimExporterRegistryUnitSpec extends Specification {

	private HighDimExporterRegistry testee = new HighDimExporterRegistry()

	private HighDimExporter fooExporter = [
			getFormat          : { -> 'foo' },
			isDataTypeSupported: { it == 'foo' }] as HighDimExporter
	private HighDimExporter barExporter = [
			getFormat          : { -> 'bar' },
			isDataTypeSupported: { it == 'bar' }] as HighDimExporter

	void 'test multiple exporters for the same file format'() {
		when:
		String fileFormat = 'TSV'

		testee.registerHighDimensionExporter(fileFormat, fooExporter)
		testee.registerHighDimensionExporter(fileFormat, barExporter)

		Set<HighDimExporter> exporters = testee.findExporters(fileFormat, null)

		then:
		exporters.contains fooExporter
		exporters.contains barExporter
	}

	void 'test several file formats for data type'() {
		when:
		HighDimExporter fooExporter2 = [
				getFormat          : { -> 'foo' },
				isDataTypeSupported: { dt -> dt == 'foo' }] as HighDimExporter

		testee.registerHighDimensionExporter('TSV', fooExporter)
		testee.registerHighDimensionExporter('CSV', barExporter)
		testee.registerHighDimensionExporter('TXT', fooExporter2)

		Set<HighDimExporter> exporters = testee.findExporters(null, 'foo')

		then:
		exporters.contains fooExporter
		exporters.contains fooExporter2
	}

	void 'test all on no constraints'() {
		when:
		testee.registerHighDimensionExporter('TSV', fooExporter)
		testee.registerHighDimensionExporter('CSV', barExporter)

		Set<HighDimExporter> exporters = testee.findExporters(null, null)

		then:
		exporters.contains fooExporter
		exporters.contains barExporter
	}

	void 'test both constraints'() {
		when:
		testee.registerHighDimensionExporter('TSV', fooExporter)
		testee.registerHighDimensionExporter('CSV', barExporter)

		Set<HighDimExporter> exporters = testee.findExporters('TSV', 'foo')

		then:
		exporters.contains fooExporter
	}

	void 'test does not match'() {
		when:
		testee.registerHighDimensionExporter('TSV', fooExporter)
		testee.registerHighDimensionExporter('CSV', barExporter)

		Set<HighDimExporter> exporters = testee.findExporters('CSV', 'foo')

		then:
		exporters.isEmpty()
	}
}
