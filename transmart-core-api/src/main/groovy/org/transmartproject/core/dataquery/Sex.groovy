package org.transmartproject.core.dataquery

import groovy.transform.CompileStatic

@CompileStatic
enum Sex {

    MALE,
    FEMALE,
    UNKOWN

    String toString() {
        name().toLowerCase Locale.ENGLISH
    }

    static Sex fromString(String name) {
        values().find {
            it.toString() == name?.toLowerCase(Locale.ENGLISH)
        } ?: UNKOWN
    }
}
