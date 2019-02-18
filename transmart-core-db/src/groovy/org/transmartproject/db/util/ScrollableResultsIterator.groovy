package org.transmartproject.db.util

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.hibernate.ScrollableResults

@CompileStatic
@Slf4j('logger')
class ScrollableResultsIterator<T> implements Iterator<T>, Closeable {

    private ScrollableResults scrollableResults
    private Boolean hasNext = null
    private boolean closed

    ScrollableResultsIterator(ScrollableResults results) {
        scrollableResults = results
    }

    boolean hasNext() {
        if (hasNext == null) {
            hasNext = scrollableResults.next()
        }
	else {
            hasNext
        }
    }

    T next() {
        if (hasNext()) {
            hasNext = null
            (T) scrollableResults.get(0)
        }
	else {
            throw new NoSuchElementException()
        }
    }

    void remove() {
        throw new UnsupportedOperationException()
    }

    void close() throws IOException {
        scrollableResults.close()
        closed = true
    }

    protected void finalize() throws Throwable {
        super.finalize()
        if (!closed) {
            logger.error 'Failed to call close before the object was scheduled to be garbage collected'
            close()
        }
    }
}
