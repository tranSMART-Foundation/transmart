package bio

import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.biomart.BioData

@Slf4j('logger')
class BioDataService {

    static transactional = false

    @Autowired private GrailsApplication grailsApplication

    def getBioDataObject(String uid) {
        def bioDataObject
	BioData bioData = BioData.findByUniqueId(uid)
	logger.debug 'bioData = {}', bioData
	if (bioData) {
	    Class clazz = grailsApplication.getDomainClass().clazz // TODO this will fail, getDomainClass() takes a single String arg
	    logger.debug 'clazz = {}', clazz
            bioDataObject = clazz.findByObjectUid(folder.getUniqueId())
	    logger.debug 'bioDataObject = {}', bioDataObject
        }

	bioDataObject
    }
}
