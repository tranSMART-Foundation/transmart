package org.transmartproject.db.util

import groovy.transform.CompileStatic
import org.hibernate.ScrollableResults
import org.transmartproject.core.IterableResult

@CompileStatic
class ScrollableResultsWrappingIterable<T> extends AbstractOneTimeCallIterable<T> implements IterableResult<T> {

    protected final ScrollableResultsIterator scrollableResultsIterator

    ScrollableResultsWrappingIterable(ScrollableResults scrollableResults) {
        scrollableResultsIterator = new ScrollableResultsIterator(scrollableResults)
    }

    protected Iterator getIterator() {
        scrollableResultsIterator
    }

    void close() throws IOException {
        scrollableResultsIterator.close()
    }
}
