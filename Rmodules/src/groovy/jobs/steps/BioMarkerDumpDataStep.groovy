package jobs.steps

import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.BioMarkerDataRow

class BioMarkerDumpDataStep extends AbstractDumpHighDimensionalDataStep {

    final List<String> csvHeader = [ 'PATIENT.ID', 'VALUE', 'PROBE.ID', 'GENE_SYMBOL', 'SUBSET' ]

    @Override
    protected computeCsvRow(String subsetName, String seriesName, DataRow row, AssayColumn column, cell) {

        assert row instanceof BioMarkerDataRow

        [getRowKey(subsetName, seriesName, column.patientInTrialId),
                row[column],
                row.label,
                row.bioMarker,
                subsetName]
    }

}
