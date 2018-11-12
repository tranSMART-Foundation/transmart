package annotation

import groovy.util.logging.Slf4j
import org.transmart.biomart.BioData
import org.transmart.biomart.ConceptCode

@Slf4j('logger')
class MetaDataService {

    boolean transactional = true

    def serviceMethod() {

    }

    def getViewValues(fieldValue) {
        logger.info 'MetaDataService.getViewValues called'

        def terms = fieldValue.split('\\|')
        def list = []
        terms.each {
                    def bioDataId = BioData.find('from BioData where uniqueId=?', it)?.id
                    if (bioDataId) {
                        list.add(bioDataId)
                    }
                }

        logger.info 'list = ' + list

        def tagValues = ''
        if (list.size > 0) {
            tagValues = ConceptCode.executeQuery('from ConceptCode as cc where id in(:list)', [list: list])
        }

        logger.info 'tagValues = ' + tagValues

        return tagValues
    }


}
