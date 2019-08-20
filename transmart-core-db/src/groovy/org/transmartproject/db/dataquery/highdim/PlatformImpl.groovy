package org.transmartproject.db.dataquery.highdim

//import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.transmartproject.core.dataquery.highdim.Platform

@Slf4j('logger')
//@CompileStatic
class PlatformImpl implements Platform {

    Date    annotationDate
    String  genomeReleaseId
    String  id
    String  markerType
    String  organism
    String  title

    @Override
    Iterable<?> getTemplate() {
	logger.debug 'PlatformImpl.getTemplate throws exception'
	throw new UnsupportedOperationException()
    }
}
