package org.transmartproject.db.dataquery.highdim

import groovy.transform.CompileStatic
import org.transmartproject.core.dataquery.highdim.Platform

@CompileStatic
class PlatformImpl implements Platform {

    Date    annotationDate
    String  genomeReleaseId
    String  id
    String  markerType
    String  organism
    String  title

    Iterable getTemplate() {
        throw new UnsupportedOperationException()
    }
}
