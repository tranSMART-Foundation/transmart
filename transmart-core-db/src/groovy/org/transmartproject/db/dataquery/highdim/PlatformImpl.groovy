package org.transmartproject.db.dataquery.highdim

import groovy.transform.CompileStatic
import org.transmartproject.core.dataquery.highdim.Platform

@CompileStatic
class PlatformImpl implements Platform {

    String  id
    String  title
    String  organism
    Date    annotationDate
    String  markerType
    String  genomeReleaseId

    Iterable getTemplate() {
        throw new UnsupportedOperationException()
    }
}
