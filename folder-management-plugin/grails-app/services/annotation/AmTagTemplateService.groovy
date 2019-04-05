package annotation

import groovy.util.logging.Slf4j

@Slf4j('logger')
class AmTagTemplateService {

    static transactional = false

    AmTagTemplate getTemplate(String key) {

	logger.info 'Searching amTagTemplateAssociation for {}', key

	AmTagTemplateAssociation amTagTemplateAssociation

        if (key) {
            amTagTemplateAssociation = AmTagTemplateAssociation.findByObjectUid(key)
	    logger.debug 'amTagTemplateAssociation = {} for key = {}', amTagTemplateAssociation, key
        }
        else {
            logger.error 'Unable to retrieve an AmTagTemplateAssociation with a null key value'
        }

	AmTagTemplate amTagTemplate
        if (amTagTemplateAssociation) {
	    logger.debug 'Searching amTagTemplate'
            amTagTemplate = AmTagTemplate.get(amTagTemplateAssociation.tagTemplateId)
	    logger.debug 'amTagTemplate = {}', amTagTemplate
	    logger.debug 'amTagTemplate.tagItems = {}', amTagTemplate.amTagItems
        }
        else {
	    logger.debug 'AmTagTemplate is null for tag template association = {}', key
        }

	amTagTemplate
    }
}
