package annotation

import groovy.util.logging.Slf4j

@Slf4j('logger')
class AmTagTemplateService {

    boolean transactional = true

    def serviceMethod() {

    }

    def getTemplate(String key) {

        logger.info "Searching amTagTemplateAssociation for " + key

        def amTagTemplateAssociation
        def amTagTemplate

        if (key) {
            amTagTemplateAssociation = AmTagTemplateAssociation.findByObjectUid(key)
            logger.info "amTagTemplateAssociation = " + amTagTemplateAssociation + " for key = " + key
        } else {
            logger.error "Unable to retrieve an AmTagTemplateAssociation with a null key value"
        }

        if (amTagTemplateAssociation) {
            logger.info "Searching amTagTemplate"
            amTagTemplate = AmTagTemplate.get(amTagTemplateAssociation.tagTemplateId)
            logger.info "amTagTemplate = " + amTagTemplate.toString()
            logger.info "amTagTemplate.tagItems = " + amTagTemplate.amTagItems

        } else {
            logger.error "AmTagTemplate is null for tag template association = " + key
        }

        return amTagTemplate
    }

}
