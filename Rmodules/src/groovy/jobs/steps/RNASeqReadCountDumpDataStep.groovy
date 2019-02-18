package jobs.steps

import com.google.common.collect.Lists
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.rnaseq.RnaSeqValues

class RNASeqReadCountDumpDataStep extends AbstractDumpHighDimensionalDataStep {

    RNASeqReadCountDumpDataStep() {
        callPerColumn = false
    }

    @Override
    protected computeCsvRow(String subsetName, String seriesName, DataRow genericRow, AssayColumn column /*null*/, cell /*null*/) {

        RegionRow<RnaSeqValues> row = genericRow
        List<String> line = Lists.newArrayListWithCapacity(csvHeader.size())
        line <<  row.bioMarker ?: row.name as String

        for (AssayColumn assay in assays) {
            line << row.getAt(assay).readcount as String
        }

        line
    }

    @Lazy
    List<String> csvHeader = {
        List<String> r = ['regionname']

        for (AssayColumn assay in assays) {
            r << assay.patientInTrialId
        }

        r
    }()

    @Lazy def assays = {
        results.values().iterator().next().indicesList
    }()
}
