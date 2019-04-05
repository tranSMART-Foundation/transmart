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
class ProteomicsBedExporter extends AbstractChromosomalRegionBedExporter {

    static final BigDecimal HIGH_ZSCORE_THRESHOLD = new BigDecimal(1.5d)
    static final BigDecimal LOW_ZSCORE_THRESHOLD = HIGH_ZSCORE_THRESHOLD.negate()

    static final String LOW_VALUE_RGB = '0,0,205'
    static final String HIGH_VALUE_RGB = '205,0,0'
    static final String DEFAULT_RGB = '196,196,196'

    @Autowired HighDimExporterRegistry highDimExporterRegistry

    @PostConstruct
    void init() {
	highDimExporterRegistry.registerHighDimensionExporter format, this
    }

    boolean isDataTypeSupported(String dataType) {
        dataType == 'protein'
    }

    String getProjection() {
        Projection.ALL_DATA_PROJECTION
    }

    @CompileDynamic
    protected List calculateRow(RegionRow datarow, AssayColumn assay) {
	BigDecimal zscore = datarow[assay]['zscore']

	[datarow.chromosome,
         datarow.start,
         datarow.end,
	 datarow instanceof BioMarkerDataRow ? datarow.bioMarker ?: datarow.name : datarow.name, //Name of the BED line
	 zscore, //Score
	 '.', //Strand. We do not use strand information
	 datarow.start, //Thick start
	 datarow.end, //Thick end
	 getColor(zscore)] //Item RGB
    }

    private static String getColor(BigDecimal zscore) {
        if (zscore < LOW_ZSCORE_THRESHOLD) {
            LOW_VALUE_RGB
	}
	else if (zscore > HIGH_ZSCORE_THRESHOLD) {
            HIGH_VALUE_RGB
	}
	else {
            DEFAULT_RGB
        }
    }
}
