package annotation

import fm.FmFolder
import groovy.util.logging.Slf4j

@Slf4j('logger')
class AmTagItemService {

    boolean transactional = true

    def serviceMethod() {

    }

    def getDisplayItems(Long key) {

        logger.info "getDisplayItems Searching amTagItems for tag template " + key

        def amTagItems

        if (key) {
            Map<String, Object> paramMap = new HashMap<Long, Object>();

            StringBuffer sb = new StringBuffer();
            sb.append("from AmTagItem ati where viewInGrid=1 ");
            sb.append(" and ati.amTagTemplate.id = :amTagTemplateId order by displayOrder");
            paramMap.put("amTagTemplateId", key);

            amTagItems = AmTagItem.findAll(sb.toString(), paramMap);

            logger.info "amTagItems = " + amTagItems + " for key = " + key
        } else {
            logger.error "Unable to retrieve an amTagItems with a null key value"
        }


        return amTagItems
    }

    def getChildDisplayItems(Long key) {
        logger.info "getChildDisplayItems Searching child amTagItems for tag template " + key

        def amTagItems

        if (key) {
            Map<String, Object> paramMap = new HashMap<Long, Object>();

            StringBuffer sb = new StringBuffer();
            sb.append("from AmTagItem ati where viewInChildGrid=1 ");
            sb.append(" and ati.amTagTemplate.id = :amTagTemplateId order by displayOrder");
            paramMap.put("amTagTemplateId", key);

            amTagItems = AmTagItem.findAll(sb.toString(), paramMap);

            logger.info "amTagItems = " + amTagItems + " for key = " + key
        } else {
            logger.error "Unable to retrieve an child amTagItems with a null key value"
        }

        return amTagItems

    }

    def getEditableItems(Long key) {
        logger.info "getEditableItems Searching amTagItems for tag template " + key

        def amTagItems = []

        if (key) {
            amTagItems = AmTagItem.findAll(
                "from AmTagItem ati where ati.amTagTemplate.id = :templateId and ati.editable = '1' order by ati.displayOrder",
                [templateId: key])

            logger.info "amTagItems = ${amTagItems} for key = ${key}"
        } else {
            logger.error "Unable to retrieve an amTagItems with a null key value"
        }

        amTagItems
    }

    def getRequiredItems(Long key) {

        logger.info "getRequiredItems Searching amTagItems for tag template " + key

        def amTagItems

        if (key) {
            Map<String, Object> paramMap = new HashMap<Long, Object>();

            StringBuffer sb = new StringBuffer();
            sb.append("from AmTagItem ati where required=1 ");
            sb.append(" and ati.amTagTemplate.id = :amTagTemplateId order by displayOrder");
            paramMap.put("amTagTemplateId", key);

            amTagItems = AmTagItem.findAll(sb.toString(), paramMap);

            logger.info "amTagItems = " + amTagItems + " for key = " + key
        } else {
            logger.error "Unable to retrieve an amTagItems with a null key value"
        }


        return amTagItems
    }

    def beforeValidate(FmFolder folder, params) {
        if (folder.folderName) {
            def amTagTemplate = AmTagTemplate.findByTagTemplateType(folder.folderName)
            def metaDataTagItems = amTagItemService.getRequiredItems(amTagTemplate.id)
            metaDataTagItems.each
                    {
                        if (it.tagItemType != 'FIXED') {
                            if (null != params."amTagItem_${it.id}" && "" != params."amTagItem_${it.id}") {
                                folder.errors.addError(it.displayName, it.displayName + " is required")
                            }
                        }
                    }
        } else {
            folder.errors.addError("folderName", "Folder name must have a value")
        }

    }

}
