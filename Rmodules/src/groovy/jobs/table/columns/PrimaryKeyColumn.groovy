package jobs.table.columns

import com.google.common.collect.ImmutableMap
import groovy.transform.CompileStatic
import jobs.table.BackingMap

@CompileStatic
class PrimaryKeyColumn extends AbstractColumn {

    @Override
    void onReadRow(String dataSourceName, row) {
        // don't care
    }

    @Override
    Map<String, Object> consumeResultingTableRows() {
        ImmutableMap.of()
    }

    @Override
    void onAllDataSourcesDepleted(int columnNumber, BackingMap backingMap) {
        for (pk in backingMap.primaryKeys) {
            backingMap.putCell pk, columnNumber, pk
        }
    }
}
