package org.transmartproject.export

import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.dataquery.highdim.vcf.GenomicVariantType
import org.transmartproject.core.dataquery.highdim.vcf.VcfCohortInfo
import org.transmartproject.db.dataquery.highdim.vcf.VcfDataRow
import spock.lang.Specification

class VCFExporterSpec extends Specification {

	private MockTabularResultHelper helper = new MockTabularResultHelper()
	private VCFExporter exporter = new VCFExporter()
	private TabularResult tabularResult

	void 'test whether supported datatypes are recognized'() {
		expect:
		// Only VCF datatype is supported
		exporter.isDataTypeSupported 'vcf'
		!exporter.isDataTypeSupported('other')
		!exporter.isDataTypeSupported(null)
	}

	void 'test whether a cancelled export does not produce output'() {
		when:
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

		TabularResult tabularResult = Mock()
		Projection projection = Mock()

		exporter.export tabularResult, projection, { name, ext -> outputStream }, { true }

		then:
		// As the export is cancelled,
		!outputStream.toString()
	}

	void 'test whether a basic tabular result is exported properly'() {
		when:
		tabularResult = createMockVCFTabularResult()

		// Create cohort projection, as that is used for exporting tab separated files
		Projection projection = Mock()

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

		exporter.export tabularResult, projection, { name, ext -> outputStream }

		// Assert we have at least some text, in UTF-8 encoding
		String output = outputStream.toString('UTF-8')

		then:
		output

		when:
		// We expect 4 lines: 1 header line, two lines  for assay 1, and one line for assay 2
		List lines = output.readLines()

		then:
		lines.size() == 8
		expectHeader(lines[0..2])
		expectDataHeader(lines[3])

		// Check different chromosomal positions
		lines[4].startsWith(['1', '100', 'rs0010', 'G', '', '50', 'PASS'].join('\t'))
		lines[4].endsWith(['GT:DP', '0/0:3', '0/0:7'].join('\t'))

		lines[5].startsWith(['2', '190', '.', 'A', 'G', '90', 'q10'].join('\t'))
		// There is no T nucleotide anymore. That's why index was changed from 2 to 1.
		lines[5].endsWith(['GT', '0/1', '1/1'].join('\t'))

		lines[6].startsWith(['X', '190', '.', 'G', 'A', '90', 'PASS'].join('\t'))
		lines[6].endsWith(['GT:DP', '0:3', '1:7'].join('\t'))

		lines[7].startsWith(['6', '190', '.', 'G', 'A,T,C', '90', 'q10'].join('\t'))
		lines[7].endsWith(['DP:GT:QS', '1:0/1:2', '5:2/3:8'].join('\t'))
	}

	void 'test whether a infofields are exported properly'() {
		when:
		tabularResult = createMockVCFTabularResult()

		// Create cohort projection, as that is used for exporting tab separated files
		Projection projection = Mock()

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

		exporter.export tabularResult, projection, { name, ext -> outputStream }

		// Assert we have at least some text, in UTF-8 encoding
		String output = outputStream.toString('UTF-8')

		then:
		output

		when:
		// We expect 4 lines: 1 header line, two lines  for assay 1, and one line for assay 2
		List lines = output.readLines()

		then:
		lines.size() == 8

		when:
		List<String> infoFields = lines[4..6].collect { it.split('\t')[7].tokenize(';') }

		then:
		infoFields[0].contains 'AA=0'
		infoFields[0].contains 'AN=4'
		infoFields[0].contains 'NS=2'

		infoFields[1].contains 'AA=0'
		infoFields[1].contains 'AC=1'
		infoFields[1].contains 'AF=0.25'
		infoFields[1].contains 'AN=4'
		infoFields[1].contains 'NS=2'

		infoFields[2].contains 'H3'
		infoFields[2].contains 'AC=1'
		infoFields[2].contains 'AF=0.5'
		infoFields[2].contains 'AN=2'
		infoFields[2].contains 'NS=2'
	}

	private boolean expectHeader(List<String> headerLines) {
		assert headerLines[0] == '##fileformat=VCFv4.2'
		assert headerLines[1] == '##fileDate=' + new Date().format('yyyyMMdd')
		assert headerLines[2].startsWith('##source=transmart')
		true
	}

	private boolean expectDataHeader(String dataHeaderLine) {
		assert dataHeaderLine == '#' + [
				'CHROM', 'POS', 'ID', 'REF', 'ALT', 'QUAL', 'FILTER', 'INFO', 'FORMAT',
				'assay_1', 'assay_2'].join('\t')
		true
	}

	private TabularResult createMockVCFTabularResult() {
		// Setup tabularResult and projection to test with
		List<AssayColumn> sampleAssays = helper.createSampleAssays(2)
		List<String> sampleCodes = sampleAssays*.sampleCode
		Map<String, List<Object>> labelToData = [
				row1: [[allele1: 0, allele2: 0, subjectId: sampleCodes[0], subjectPosition: 1],
				       [allele1: 0, allele2: 0, subjectId: sampleCodes[1], subjectPosition: 2]],
				row2: [[allele1: 0, allele2: 2, subjectId: sampleCodes[0], subjectPosition: 1],
				       [allele1: 2, allele2: 2, subjectId: sampleCodes[1], subjectPosition: 2]],
				row3: [[allele1: 0, subjectId: sampleCodes[0], subjectPosition: 1], // X chromosome
				       [allele1: 1, subjectId: sampleCodes[1], subjectPosition: 2]],
				row4: [[allele1: 0, allele2: 1, subjectId: sampleCodes[0], subjectPosition: 1],
				       [allele1: 2, allele2: 3, subjectId: sampleCodes[1], subjectPosition: 2]]]

		Map<String, Map> vcfProperties = [
				row1: [chromosome        : 1,
				       position          : 100,
				       rsId              : 'rs0010',
				       referenceAllele   : 'G',
				       alternativeAlleles: [],
				       quality           : 50,
				       filter            : 'PASS',
				       infoFields        : [AA: 0, XYZ: 100],
				       format            : 'GT:DP',
				       variants          : '0/0:3\t0/0:7',
				       cohortInfo        : [referenceAllele        : 'G',
				                            alternativeAlleles     : [],
				                            alleles                : ['G'],
				                            alleleCount            : [4],
				                            alleleFrequency        : [1.0],
				                            totalAlleleCount       : 4,
				                            numberOfSamplesWithData: 2]],
				row2: [chromosome        : 2,
				       position          : 190,
				       rsId              : '.',
				       referenceAllele   : 'A',
				       alternativeAlleles: ['T', 'G'],
				       quality           : 90,
				       filter            : 'q10',
				       infoFields        : [AA: 0, NS: 100],
				       format            : 'GT',
				       variants          : '0/2\t2/2',
				       cohortInfo        : [referenceAllele        : 'A',
				                            alternativeAlleles     : ['G'],
				                            alleles                : ['A', 'G'],
				                            alleleCount            : [3, 1],
				                            alleleFrequency        : [0.75, 0.25],
				                            totalAlleleCount       : 4,
				                            numberOfSamplesWithData: 2]],
				row3: [chromosome        : 'X',
				       position          : 190,
				       rsId              : '.',
				       referenceAllele   : 'G',
				       alternativeAlleles: ['A'],
				       quality           : 90,
				       filter            : 'PASS',
				       infoFields        : [AN: 1, TEST: 3, H3: true],
				       format            : 'GT:DP',
				       variants          : '0:3\t1:7',
				       cohortInfo        : [referenceAllele        : 'G',
				                            alternativeAlleles     : ['A'],
				                            alleles                : ['G', 'A'],
				                            alleleCount            : [1, 1],
				                            alleleFrequency        : [0.5, 0.5],
				                            totalAlleleCount       : 2,
				                            numberOfSamplesWithData: 2]],
				row4: [chromosome        : 6,
				       position          : 190,
				       rsId              : '.',
				       referenceAllele   : 'G',
				       alternativeAlleles: ['A', 'T', 'C'],
				       quality           : 90,
				       filter            : 'q10',
				       infoFields        : [AA: 0, NS: 100],
				       format            : 'DP:GT:QS',
				       variants          : '1:0/1:2\t5:2/3:8',
				       cohortInfo        : [referenceAllele        : 'G',
				                            alternativeAlleles     : ['A', 'T', 'C'],
				                            alleles                : ['G', 'A', 'T', 'C'],
				                            alleleCount            : [1, 1, 1, 1],
				                            alleleFrequency        : [0.25, 0.25, 0.25, 0.25],
				                            totalAlleleCount       : 4,
				                            numberOfSamplesWithData: 2]]]

		Iterator<DataRow<AssayColumn, Object>> iterator = labelToData.collect { String label, List<Object> data ->
			createVCFRowForAssays sampleAssays, data, vcfProperties[label], label
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

	private DataRow createVCFRowForAssays(List<AssayColumn> assays, List data,
	                                      Map<String, Object> vcfProperties, String label) {

		Map<AssayColumn, Object> values = helper.dot(assays, data)

		Map<AssayColumn, Integer> assayIndexMap = [:]
		values.eachWithIndex { Map.Entry<AssayColumn, Object> entry, int i ->
			assayIndexMap[entry.key] = i
		}

		// Add values for each assay
		List<String> variants = vcfProperties.variants ? vcfProperties.variants.tokenize('\t') : []
		Map<Assay, String> variantForAssay = [:]
		for (int i = 0; i < variants.size(); i++) {
			variantForAssay[values.keySet()[i]] = variants[i]
		}

		VcfDataRow row = new VcfDataRow() {
			String getLabel() { label }
			List<String> getAlternativeAlleles() { vcfProperties.alternativeAlleles }
			Map<String, String> getInfoFields() { vcfProperties.infoFields }
			VcfCohortInfo getCohortInfo() {
				new VcfCohortInfoWithNumberOfSamplesWithData(vcfProperties.cohortInfo.numberOfSamplesWithData) {
					String getReferenceAllele() { vcfProperties.cohortInfo.referenceAllele }
					List<String> getAlternativeAlleles() { vcfProperties.cohortInfo.alternativeAlleles }
					List<String> getAlleles() { vcfProperties.cohortInfo.alleles }
					List<Integer> getAlleleCount() { vcfProperties.cohortInfo.alleleCount }
					List<Double> getAlleleFrequency() { vcfProperties.cohortInfo.alleleFrequency }
					int getTotalAlleleCount() { vcfProperties.cohortInfo.totalAlleleCount }
					List<GenomicVariantType> getGenomicVariantTypes() {}
					String getMajorAllele() {}
					String getMinorAllele() {}
					Double getMinorAlleleFrequency() {}
				}
			}
			String getOriginalSubjectData(Assay assay) { variantForAssay[assay] }
		}

		row.chromosome = vcfProperties.chromosome
		row.position = vcfProperties.position
		row.rsId = vcfProperties.rsId
		row.referenceAllele = vcfProperties.referenceAllele
		row.quality = vcfProperties.quality
		row.filter = vcfProperties.filter
		row.format = vcfProperties.format
		row.variants = vcfProperties.variants

		row.data = values.values() as List
		row.assayIndexMap = assayIndexMap

		row
	}
}

abstract class VcfCohortInfoWithNumberOfSamplesWithData implements VcfCohortInfo {
	final int numberOfSamplesWithData
	VcfCohortInfoWithNumberOfSamplesWithData(int n) {
		numberOfSamplesWithData = n
	}
}
