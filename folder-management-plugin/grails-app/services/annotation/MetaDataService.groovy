package annotation

import groovy.util.logging.Slf4j
import org.transmart.biomart.BioData
import org.transmart.biomart.ConceptCode

@Slf4j('logger')
class MetaDataService {

    static transactional = false

    List<ConceptCode> getViewValues(String fieldValue) {
	logger.debug 'MetaDataService.getViewValues called'

	List<Long> list = []
	for (uniqueId in fieldValue.split('\\|')) {
	    Long bioDataId = BioData.findByUniqueId(uniqueId)?.id
            if (bioDataId) {
		list << bioDataId
            }
        }

	ConceptCode.getAll list
    }
}
