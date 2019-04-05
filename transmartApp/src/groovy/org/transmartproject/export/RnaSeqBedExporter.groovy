package org.transmartproject.export

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.BioMarkerDataRow
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.projections.Projection

import javax.annotation.PostConstruct

@CompileStatic
@Slf4j('logger')
class RnaSeqBedExporter extends AbstractChromosomalRegionBedExporter {

    @Autowired
    HighDimExporterRegistry highDimExporterRegistry

    @PostConstruct
    void init() {
	highDimExporterRegistry.registerHighDimensionExporter(format, this)
    }

    boolean isDataTypeSupported(String dataType) {
        dataType == 'rnaseq'
    }

    String getProjection() {
        Projection.ALL_DATA_PROJECTION
    }

    @CompileDynamic
    protected List calculateRow(RegionRow datarow, AssayColumn assay) {
        int readcount = datarow[assay]['readcount']
	[datarow.chromosome,
         datarow.start,
         datarow.end,
	 datarow instanceof BioMarkerDataRow ? datarow.bioMarker ?: datarow.name : datarow.name, //Name of the BED line
	 readcount] // Score
    }
}
