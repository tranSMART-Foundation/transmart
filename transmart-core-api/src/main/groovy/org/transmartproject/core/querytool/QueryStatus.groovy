package org.transmartproject.core.querytool

import groovy.transform.CompileStatic

/**
 * An enumeration for query statuses. This is a static version of a subset of
 * i2b2's qt_query_status_type table.
 */
@CompileStatic
enum QueryStatus {
    PROCESSING  (2),
    FINISHED    (3),
    ERROR       (4),
    COMPLETED   (6)

    final int id

    private QueryStatus(int id) {
        this.id = id
    }

    static QueryStatus forId(int id) {
        values().find { QueryStatus it -> it.id == id }
    }
}
