package org.transmartproject.export

import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.tworegion.DeTwoRegionEvent
import org.transmartproject.db.dataquery.highdim.tworegion.DeTwoRegionEventGene
import org.transmartproject.db.dataquery.highdim.tworegion.DeTwoRegionJunction
import org.transmartproject.db.dataquery.highdim.tworegion.DeTwoRegionJunctionEvent
import org.transmartproject.db.dataquery.highdim.tworegion.JunctionRow
import spock.lang.Specification

/**
 * @author j.hudecek
 */
class TwoRegionExporterSpec extends Specification {

	private MockTabularResultHelper helper = new MockTabularResultHelper()
	private TwoRegionExporter exporter = new TwoRegionExporter()
	private TabularResult tabularResult
	private Projection projection = Mock()

	void setup() {
		exporter.highDimExporterRegistry = [registerHighDimensionExporter: { format, exporter -> }] as HighDimExporterRegistry
		exporter.init()
	}

	void 'test whether two region data is exported properly'() {
		when:
		tabularResult = createMockTwoRegionTabularResult()
		List<ByteArrayOutputStream> outputStreams = []

		exporter.export(tabularResult, projection, { name, ext ->
			outputStreams << new ByteArrayOutputStream()
			outputStreams[-1]
		})

		then:
		//3 assays, have both events and junction files=6 files in total
		outputStreams.size() == 6

		when:
		String junctionOutput = outputStreams[0].toString('UTF-8')
		String eventOutput = outputStreams[1].toString('UTF-8')

		List<String> junctions = junctionOutput.readLines()

		then:
		junctions[0] == 'id\tup_chr\tup_pos\tup_strand\tup_end\tdown_chr\tdown_pos\tdown_strand\tdown_end\tis_in_frame'
		junctions[1] == '1\t3\t12\t-\t18\t1\t2\t+\t10\ttrue'
		junctions[2] == '2\t13\t12\t-\t18\t10\t2\t+\t10\ttrue'

		when:
		List<String> events = eventOutput.readLines()

		then:
		events[0] == 'reads_span\treads_junction\tpairs_span\tpairs_junction\tpairs_end\treads_counter\tbase_freq\tjunction_id\tcga_type\tsoap_class\tgene_ids\tgene_effect'
		events[1] == 'null\tnull\t10\tnull\tnull\tnull\tnull\t1\tdeletion\tnull\tnull\tnull'
		events[2] == 'null\tnull\t10\tnull\tnull\tnull\tnull\t2\tdeletion\tnull\tnull\tnull'

		when:
		junctionOutput = outputStreams[2].toString('UTF-8')
		eventOutput = outputStreams[3].toString('UTF-8')

		junctions = junctionOutput.readLines()

		then:
		junctions[0] == 'id\tup_chr\tup_pos\tup_strand\tup_end\tdown_chr\tdown_pos\tdown_strand\tdown_end\tis_in_frame'
		junctions[1] == '3\t3\t12\t-\t18\tX\t2\t+\t10\ttrue'
		junctions[2] == '4\t3\t12\t-\t18\tY\t2\t+\t10\ttrue'

		when:
		events = eventOutput.readLines()

		then:
		events[0] == 'reads_span\treads_junction\tpairs_span\tpairs_junction\tpairs_end\treads_counter\tbase_freq\tjunction_id\tcga_type\tsoap_class\tgene_ids\tgene_effect'
		events[1] == 'null\tnull\t10\tnull\tnull\tnull\tnull\t3\tnull\ttranslocation\tTP53;\tfusion;'
	}

	private TabularResult createMockTwoRegionTabularResult() {
		// Setup tabularResult and projection to test with
		List<AssayColumn> sampleAssays = helper.createSampleAssays(3)
		List<DeSubjectSampleMapping> assays = sampleAssays.collect({
			new DeSubjectSampleMapping(id: it.id, patientInTrialId: it.patientInTrialId)
		})

		List junctions = []
		//1st event: deletion assay0, chr1 2-10 - chr3 12-18 + assay0, chr10 2-10 - chr13 12-18 + assay0, chrX 2-10 - chr3 12-18
		//2nd event: deletion assay1, chrX 2-10 - chr3 12-18
		//junction without event assay1, chrY 2-10 - chr3 12-18
		DeTwoRegionEvent event = new DeTwoRegionEvent(cgaType: 'deletion')
		DeTwoRegionJunction junction = new DeTwoRegionJunction(
				downChromosome: '1',
				downPos: 2,
				downEnd: 10,
				downStrand: '+' as char,
				upChromosome: '3',
				upPos: 12,
				upEnd: 18,
				upStrand: '-' as char,
				isInFrame: true,
				assay: assays[0])
		junction.junctionEvents = [new DeTwoRegionJunctionEvent(
				event: event,
				junction: junction,
				pairsSpan: 10)]
		junction.id = 1
		junctions << junction

		junction = new DeTwoRegionJunction(
				downChromosome: '10',
				downPos: 2,
				downEnd: 10,
				downStrand: '+' as char,
				upChromosome: '13',
				upPos: 12,
				upEnd: 18,
				upStrand: '-' as char,
				isInFrame: true,
				assay: assays[0])
		junction.junctionEvents = [new DeTwoRegionJunctionEvent(
				event: event,
				junction: junction,
				pairsSpan: 10)]
		junction.id = 2
		junctions << junction

		DeTwoRegionEventGene gene = new DeTwoRegionEventGene(geneId: 'TP53', effect: 'fusion')

		event = new DeTwoRegionEvent(soapClass: 'translocation', eventGenes: [gene])
		gene.event = event

		DeTwoRegionJunction junction2 = new DeTwoRegionJunction(
				downChromosome: 'X',
				downPos: 2,
				downEnd: 10,
				downStrand: '+' as char,
				upChromosome: '3',
				upPos: 12,
				upEnd: 18,
				upStrand: '-' as char,
				isInFrame: true,
				assay: assays[1])
		junction2.junctionEvents = [new DeTwoRegionJunctionEvent(
				event: event,
				junction: junction2,
				pairsSpan: 10)]
		junction2.id = 3
		junctions << junction2

		junction = new DeTwoRegionJunction(
				downChromosome: 'Y',
				downPos: 2,
				downEnd: 10,
				downStrand: '+' as char,
				upChromosome: '3',
				upPos: 12,
				upEnd: 18,
				upStrand: '-' as char,
				isInFrame: true,
				assay: assays[1])
		junction.id = 4
		junctions << junction

		junction = new DeTwoRegionJunction(
				downChromosome: 'Y',
				downPos: 2,
				downEnd: 10,
				downStrand: '+' as char,
				upChromosome: '3',
				upPos: 12,
				upEnd: 18,
				upStrand: '-' as char,
				isInFrame: true,
				assay: assays[2])
		junction.id = 5
		junctions << junction

		List<JunctionRow> jrs = junctions.collect { DeTwoRegionJunction dejunction ->
			int assayIndex = assays.findIndexOf { DeSubjectSampleMapping ssm -> dejunction.assay.id == ssm.id }
			new JunctionRow(sampleAssays[assayIndex], assayIndex, 3, dejunction)
		}

		new TabularResult() {
			List getIndicesList() { sampleAssays }
			Iterator getRows() { jrs.iterator() }
			Iterator iterator() { jrs.iterator() }
			String getColumnsDimensionLabel() {}
			String getRowsDimensionLabel() {}
			void close() {}
		}
	}
}
