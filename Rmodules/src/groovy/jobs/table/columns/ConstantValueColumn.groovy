package jobs.table.columns

import com.google.common.collect.ImmutableMap
import groovy.transform.CompileStatic
import jobs.table.MissingValueAction

@CompileStatic
class ConstantValueColumn extends AbstractColumn {

    void setValue(value) {
        missingValueAction = new MissingValueAction.ConstantReplacementMissingValueAction(replacement: value)
    }

    @Override
    void onReadRow(String dataSourceName, row) {
        // purposefully left empty
    }

    @Override
    Map<String, Object> consumeResultingTableRows() {
        ImmutableMap.of()
    }
}
