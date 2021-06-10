package jobs.table.columns

import com.google.common.base.MoreObjects
import groovy.transform.CompileStatic
import jobs.table.BackingMap
import jobs.table.Column
import jobs.table.MissingValueAction
import org.transmartproject.core.dataquery.clinical.ClinicalVariableColumn
import org.transmartproject.core.exceptions.InvalidArgumentsException

@CompileStatic
abstract class AbstractColumn implements Column {

    String header

    MissingValueAction missingValueAction = new MissingValueAction.DropRowMissingValueAction()

    void onDataSourceDepleted(String dataSourceName, Iterable dataSource) {
        // override to do something here
    }

    void beforeDataSourceIteration(String dataSourceName, Iterable dataSource) {
        // override to do something here
    }

    void onAllDataSourcesDepleted(int columnNumber, BackingMap backingMap) {
        // override to do something here
    }

    Closure getValueTransformer() {}

    String toString() {
        MoreObjects.toStringHelper(this).add('header', header).toString()
    }

    protected Number validateNumber(ClinicalVariableColumn col, value) {
	if (value instanceof Number) {
	    return (Number) value
	}

	if (value instanceof CharSequence) {
	    String s = value.toString().trim()
	    if (s.isNumber()) {
		return s.toBigDecimal()
	    }
	}

	throw new InvalidArgumentsException("Got non-numerical value for column $col; value was $value")
    }
}
