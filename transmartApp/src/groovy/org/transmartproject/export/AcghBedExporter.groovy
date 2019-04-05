package org.transmartproject.export

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.BioMarkerDataRow
import org.transmartproject.core.dataquery.highdim.acgh.CopyNumberState
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.projections.Projection

import javax.annotation.PostConstruct
import javax.annotation.Resource

@CompileStatic
@Slf4j('logger')
class AcghBedExporter extends AbstractChromosomalRegionBedExporter {

    @Autowired HighDimExporterRegistry highDimExporterRegistry
    @Resource Map acghBedExporterRgbColorScheme

    Map<Integer, String> flagToRgbStringMap

    private static final Map<CopyNumberState, List<Integer>> FLAG_TO_RGB_DEFAULT_MAP = [
	(CopyNumberState.INVALID)            : [255, 255, 255], // white
	(CopyNumberState.HOMOZYGOUS_DELETION): [0, 0, 255], // light blue
	(CopyNumberState.LOSS)               : [0, 0, 205], // blue
	(CopyNumberState.NORMAL)             : [169, 169, 169], // gray
	(CopyNumberState.GAIN)               : [205, 0, 0], // red
	(CopyNumberState.AMPLIFICATION)      : [88, 0, 0], // dark red
    ]

    private Map<Integer, String> prepareFlagToRgbStringMap() {
        Map<Integer, String> result = [:]
	for (CopyNumberState cpNState in CopyNumberState.values()) {
            List<Integer> rgbValues = FLAG_TO_RGB_DEFAULT_MAP[cpNState]
            if (acghBedExporterRgbColorScheme && acghBedExporterRgbColorScheme[cpNState.name().toLowerCase()]) {
		rgbValues = (List<Integer>) acghBedExporterRgbColorScheme[cpNState.name().toLowerCase()]
            }
            result[cpNState.intValue] = rgbValues.join(',')
        }
        result
    }

    @PostConstruct
    void init() {
	flagToRgbStringMap = prepareFlagToRgbStringMap()
	highDimExporterRegistry.registerHighDimensionExporter format, this
    }

    boolean isDataTypeSupported(String dataType) {
        dataType == 'acgh'
    }

    String getProjection() {
        Projection.ALL_DATA_PROJECTION
    }

    @CompileDynamic
    protected List calculateRow(RegionRow datarow, AssayColumn assay) {
	int flag = datarow[assay]['flag']

	[datarow.chromosome,
         datarow.start,
         datarow.end,
	 datarow instanceof BioMarkerDataRow ? datarow.bioMarker ?: datarow.name : datarow.name, // Name of the BED line
	 flag, // Score
	 '.', // Strand. We do not use strand information
	 datarow.start, // Thick start
	 datarow.end, // Thick end
	 flagToRgbStringMap[flag]] // Item RGB
    }
}
