package matchers

import com.google.common.collect.Table
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeDiagnosingMatcher

import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.is

class TableMatcher<R, C, V> extends TypeSafeDiagnosingMatcher<Table<R, C, V>> {

    public static <R, C, V> TableMatcher<R, C, V> table(List<R> rowHeaders,
                                                        List<C> columnHeaders,
                                                        List<List<V>> values) {
        if (!values || !columnHeaders || !rowHeaders || !values[0]) {
            throw new IllegalArgumentException('Empty data')
        }
        if (values.size() != rowHeaders.size()) {
            throw new IllegalArgumentException(
                    "Mismatch in number of data rows and row headers size: " +
                            "${values.size()} != ${rowHeaders.size()}")
        }
        if ((values*.size() as Set).size() > 1) {
            throw new IllegalArgumentException("All the rows should have the " +
                    "same size. Got sizes: ${values*.size()}")
        }
        if (values[0].size() != columnHeaders.size()) {
            throw new IllegalArgumentException(
                    "Mismatch in number of data columns and column headers size: " +
                            "${values[0].size()} != ${columnHeaders.size()}")
        }

        new TableMatcher<R, C, V>(rowHeaders:    rowHeaders,
                columnHeaders: columnHeaders,
                values:        values)
    }

    // matches the order as well!
    List<R> rowHeaders
    List<C> columnHeaders
    List<List<V>> values

    @Override
    protected boolean matchesSafely(Table<R, C, V> rcvTable,
                                    Description mismatchDescription) {
        def matches = true
        def rowHeadersMatcher = contains(rowHeaders.collect { is it })
        def columnHeadersMatcher = contains(columnHeaders.collect { is it })

        if (!rowHeadersMatcher.matches(rcvTable.rowKeySet())) {
            reportMismatch('row headers', rowHeadersMatcher,
                    rcvTable.rowKeySet(), mismatchDescription, matches)
            matches = false
        }
        if (!columnHeadersMatcher.matches(rcvTable.columnKeySet())) {
            reportMismatch('column headers', columnHeadersMatcher,
                    rcvTable.columnKeySet(), mismatchDescription, matches)
            matches = false
        }

        rowHeaders.eachWithIndex { rowName, rowIndex ->
            columnHeaders.eachWithIndex { columnName, columnIndex ->
                def matcher = is(values[rowIndex][columnIndex])
                def recvDataPoint = rcvTable.get rowName, columnName

                if (!matcher.matches(recvDataPoint)) {
                    reportMismatch(
                            "cell ($rowIndex=$rowName, $columnIndex=$columnName)",
                            matcher, recvDataPoint, mismatchDescription, matches)
                    matches = false
                }
            }
        }

        matches
    }

    static void reportMismatch(String name,
                               Matcher<?> matcher,
                               Object item,
                               Description mismatchDescription,
                               boolean firstMismatch) {
        if (!firstMismatch) {
            mismatchDescription.appendText(", ");
        }
        mismatchDescription.appendText(name).appendText(" ");
        matcher.describeMismatch(item, mismatchDescription);
    }

    @Override
    void describeTo(Description description) {
        description.appendText('table with row headers=').
                appendValue(rowHeaders).
                appendText(', column headers=').
                appendValue(columnHeaders).
                appendText(' and data=').
                appendValue(values)
    }
}
