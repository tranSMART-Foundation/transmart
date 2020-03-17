package annotation

import groovy.util.logging.Slf4j

@Slf4j('logger')
class AmTagItemService {

    static transactional = false

    List<AmTagItem> getDisplayItems(Long key) {

	logger.info 'getDisplayItems Searching amTagItems for tag template {}', key

	if (!key) {
            logger.error 'Unable to retrieve an amTagItems with a null key value'
	    return null
        }

	List<AmTagItem> amTagItems = AmTagItem.executeQuery('''
		from AmTagItem ati
		where viewInGrid=1
		and ati.amTagTemplate.id = :amTagTemplateId
		order by displayOrder''', [amTagTemplateId: key])

	logger.debug 'amTagItems = {} for key = {}', amTagItems, key

	amTagItems
    }

    List<AmTagItem> getChildDisplayItems(Long key) {
	logger.info 'getChildDisplayItems Searching child amTagItems for tag template {}', key

	if (!key) {
            logger.error 'Unable to retrieve an child amTagItems with a null key value'
	    return null
        }

	List<AmTagItem> amTagItems = AmTagItem.executeQuery('''
		from AmTagItem ati
		where viewInChildGrid=1
		  and ati.amTagTemplate.id = :amTagTemplateId
		order by displayOrder''', [amTagTemplateId: key])

	logger.debug 'amTagItems = {} for key = {}', amTagItems, key

	amTagItems
    }

    List<AmTagItem> getEditableItems(Long key) {
	logger.info 'getEditableItems Searching amTagItems for tag template {}', key

	if (!key) {
            logger.error 'Unable to retrieve an amTagItems with a null key value'
	    return []
        }

	List<AmTagItem> amTagItems = AmTagItem.executeQuery('''
		from AmTagItem ati
		where ati.amTagTemplate.id = :templateId
		  and ati.editable = 1
		order by ati.displayOrder''', [templateId: key])

	logger.debug 'amTagItems = {} for key = {}', amTagItems, key

	amTagItems
    }

    List<AmTagItem> getRequiredItems(Long key) {

	logger.info 'getRequiredItems Searching amTagItems for tag template {}', key

	if (!key) {
            logger.error 'Unable to retrieve an amTagItems with a null key value'
	    return null
        }

	List<AmTagItem> amTagItems = AmTagItem.executeQuery('''
		from AmTagItem ati
		where required=1
		  and ati.amTagTemplate.id = :amTagTemplateId
		order by displayOrder''', [amTagTemplateId: key])

	logger.debug 'amTagItems = {} for key = {}', amTagItems, key

	amTagItems
    }
}
