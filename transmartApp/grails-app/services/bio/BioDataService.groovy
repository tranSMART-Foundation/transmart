package bio

import groovy.util.logging.Slf4j
import org.transmart.biomart.BioData

@Slf4j('logger')
class BioDataService {

    boolean transactional = true

    def getBioDataObject(String uid) {
        def bioDataObject
        def bioData = BioData.findByUniqueId(uid)
        logger.info 'bioData = ' + bioData
        if (bioData != null) {
            Class clazz = grailsApplication.getDomainClass().clazz
            logger.info 'clazz = ' + clazz
            bioDataObject = clazz.findByObjectUid(folder.getUniqueId())
            logger.info 'bioDataObject = ' + bioDataObject
        }

        return bioDataObject
    }
}
