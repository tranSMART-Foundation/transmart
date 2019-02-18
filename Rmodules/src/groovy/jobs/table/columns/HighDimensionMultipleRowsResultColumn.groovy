package jobs.table.columns

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import groovy.transform.CompileStatic
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.exceptions.UnexpectedResultException

@CompileStatic
class HighDimensionMultipleRowsResultColumn extends AbstractColumn {

    public static final String SEPARATOR = '|'

    // null in order not to filter the patients
    Set<String> patientsToConsider

    private Map<String, Map<String, Object>> nextResult

    // data source name, label
    private Multimap<String, String> seenRowLabels = HashMultimap.create()

    private Map<String, List<AssayColumn>> assaysMap = [:]

    @Override
    void beforeDataSourceIteration(String dataSourceName, Iterable dataSource) {
        assert dataSource instanceof TabularResult

        assaysMap[dataSourceName] = ((TabularResult<AssayColumn, ?>)dataSource).indicesList.findAll { AssayColumn assay ->
            !patientsToConsider || patientsToConsider.contains(assay.patientInTrialId)
        } as List
    }

    @Override
    void onReadRow(String dataSourceName, row) {
        // calls to onReadRow() and consumeResultingTableRows should be interleaved
        assert row instanceof DataRow
        assert nextResult == null

        String rowLabel = row.label
        if (!seenRowLabels.put(dataSourceName, rowLabel)) {
            throw new UnexpectedResultException("Got more than one row with label $rowLabel")
        }

        // empty cells are dropped; see HighDimensionalDataRowMapAdapter

        nextResult = new HighDimensionalDataRowMapAdapter(
            assaysMap[dataSourceName], row,
            multiDataSourceMode ? (dataSourceName + SEPARATOR) : '')
    }

    boolean isMultiDataSourceMode() {
        /* there may be only one data source, but if patientsToConsider
         * is set, we assume we have to include the data source name
         * in the context */
        patientsToConsider != null
    }

    @Override
    Map<String, Object> consumeResultingTableRows() {
        assert nextResult != null

	Map<String, Object> map = nextResult as Map
        nextResult = null
        map
    }
}
