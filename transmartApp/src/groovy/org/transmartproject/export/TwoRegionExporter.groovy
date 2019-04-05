package org.transmartproject.export

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.tworegion.DeTwoRegionEventGene
import org.transmartproject.db.dataquery.highdim.tworegion.DeTwoRegionJunction
import org.transmartproject.db.dataquery.highdim.tworegion.DeTwoRegionJunctionEvent
import org.transmartproject.db.dataquery.highdim.tworegion.JunctionRow

import javax.annotation.PostConstruct

@Slf4j('logger')
class TwoRegionExporter implements HighDimExporter {
    @Autowired private HighDimExporterRegistry highDimExporterRegistry

    @PostConstruct
    void init() {
	highDimExporterRegistry.registerHighDimensionExporter format, this
    }

    boolean isDataTypeSupported(String dataType) {
        dataType == 'two_region'
    }

    String getProjection() {
        Projection.ALL_DATA_PROJECTION
    }

    String getFormat() {
        'two region'
    }

    String getDescription() {
        'Exports two region data in junction and events file'
    }

    void export(TabularResult data, Projection projection, Closure<OutputStream> newOutputStream,
	        Closure<Boolean> isCancelled = null) {
	logger.info 'started exporting to {}', format
	long startTime = System.currentTimeMillis()

        if (isCancelled && isCancelled()) {
            return
        }

        //exports two files per sample - junctions and events which link to junctions via ids
        List<AssayColumn> assayList = data.indicesList
        Map<String, Writer> junctionStreamPerSample = [:]
        Map<String, Writer> eventStreamPerSample = [:]

        try {
	    for (JunctionRow datarow in data) {
                if (isCancelled && isCancelled()) {
                    return
                }

		for (AssayColumn assay in assayList) {
                    if (isCancelled && isCancelled()) {
                        return
                    }
                    DeTwoRegionJunction junction = datarow[assay]
		    if (junction == null) {
			//not all assays have all junctions
			continue
		    }

		    String assaytag = junction.assay.patientInTrialId + "_" + junction.assay.id

		    Writer junctionStream = junctionStreamPerSample[assaytag]
                    if (junctionStream == null) {
                        junctionStream = new BufferedWriter(new OutputStreamWriter(
			    newOutputStream(assaytag + "_junctions", 'tsv'), 'UTF-8'))
			junctionStream << "id\tup_chr\tup_pos\tup_strand\tup_end\tdown_chr\tdown_pos\tdown_strand\tdown_end\tis_in_frame\n"
                        junctionStreamPerSample[assaytag] = junctionStream
                    }

		    Writer eventStream = eventStreamPerSample[assaytag]
                    if (eventStream == null) {
                        eventStream = new BufferedWriter(new OutputStreamWriter(
			    newOutputStream(assaytag + "_events", 'tsv'), 'UTF-8'))
			eventStream << "reads_span\treads_junction\tpairs_span\tpairs_junction\tpairs_end" +
			    "\treads_counter\tbase_freq\tjunction_id\tcga_type\tsoap_class\tgene_ids\tgene_effect\n"
                        eventStreamPerSample[assaytag] = eventStream
                    }

                    junction.with {
			junctionStream << "$id\t$upChromosome\t$upPos\t$upStrand\t$upEnd\t$downChromosome\t$downPos\t$downStrand\t$downEnd\t$isInFrame\n"
                    }

		    for (DeTwoRegionJunctionEvent junctionEvent in junction.junctionEvents) {
                        junctionEvent.with {
			    eventStream << "$readsSpan\t$readsJunction\t$pairsSpan\t$pairsJunction\t$pairsEnd\t$pairsCounter\t$baseFreq\t${junction.id}"
                        }
                        junctionEvent.event.with {
			    eventStream << "\t$cgaType\t$soapClass\t"
                        }
			StringBuilder sbGenes = new StringBuilder()
			StringBuilder sbEffects = new StringBuilder()
			for (DeTwoRegionEventGene gene in junctionEvent.event.eventGenes) {
			    sbGenes << gene.geneId << ';'
			    sbEffects << gene.effect << ';'
                        }
			if (sbGenes) {
                            eventStream << sbGenes << '\t' << sbEffects
			}
			else {
                            eventStream << 'null\tnull'
			}
                        eventStream << '\n'
                    }
                }
            }
        }
        finally {
	    for (it in junctionStreamPerSample.values()) {
                it.close()
            }
	    for (it in eventStreamPerSample.values()) {
                it.close()
            }
        }
	logger.info 'Exporting data took {} ms', System.currentTimeMillis() - startTime
    }
}
