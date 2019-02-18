package jobs.steps

import com.google.common.collect.Lists
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.rnaseq.RnaSeqValues

class RNASeqDumpDataStep extends AbstractDumpHighDimensionalDataStep {

    private static final Map PER_ASSAY_COLUMNS = [readcount: { RnaSeqValues v -> v.getReadcount() }]
            //normalizedreadcount:    { RnaSeqValues v -> v.getNormalizedReadcount() },
            //lognormalizedreadcount: { RnaSeqValues v -> v.getLogNormalizedReadcount() },
            //zscore:                 { RnaSeqValues v -> v.getZscore() },

    int rowNumber = 1

    RNASeqDumpDataStep() {
        callPerColumn = false
    }

    @Override
    protected computeCsvRow(String subsetName, String seriesName, DataRow genericRow, AssayColumn column /* null */, cell /* null */) {
        RegionRow<RnaSeqValues> row = genericRow
        // +1 because the first column has no header
        List<String> line = Lists.newArrayListWithCapacity(csvHeader.size() + 1)
        line << rowNumber++ as String
        line << row.name as String
        line << row.chromosome as String
        line << row.start as String
        line << row.end as String
        line << row.numberOfProbes as String
        line << row.cytoband as String
        line << row.bioMarker as String

        PER_ASSAY_COLUMNS.each {k, Closure<RnaSeqValues> value ->
            for (AssayColumn assay in assays) {
                line << value(row.getAt(assay)) as String
            }
        }

        line
    }

    @Lazy
    List<String> csvHeader = {
        List<String> r = ['regionname', 'chromosome', 'start', 'end', 'num.probes', 'cytoband', 'genesymbol']

        PER_ASSAY_COLUMNS.keySet().each {String head ->
            for (AssayColumn assay in assays) {
                r << '' + head + '.' + assay.patientInTrialId.toString()
            }
        }

        r
    }()

    @Lazy
    List<AssayColumn> assays = {
        results.values().iterator().next().indicesList
    }()

}
