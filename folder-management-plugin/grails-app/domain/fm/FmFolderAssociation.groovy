package fm

import groovy.util.logging.Slf4j
import org.transmart.biomart.BioData

@Slf4j('logger')
class FmFolderAssociation implements Serializable {
    private static final long serialVersionUID = 1

    FmFolder fmFolder
    String objectType
    String objectUid

    static transients = ['bioObject']

    static mapping = {
	table 'FMAPP.FM_FOLDER_ASSOCIATION'
	id composite: ['objectUid', 'fmFolder']
        version false
        cache true
        sort 'objectUid'

	fmFolder column: 'folder_id'
    }

    static FmFolderAssociation get(String objectUid, long fmFolderId) {
	findByObjectUidAndFmFolder objectUid, FmFolder.load(fmFolderId)
    }

    static boolean remove(String objectUid, FmFolder fmFolder, boolean flush = false) {
	FmFolderAssociation instance = findByObjectUidAndFmFolder(objectUid, fmFolder)
        instance ? instance.delete(flush: flush) : false
    }

    // TODO BB move this and lookupDomainClass() to a service and call that directly
    def getBioObject() {
	logger.info 'ObjectUID={}', objectUid
	BioData bioData = BioData.findByUniqueId(objectUid)
	if (bioData) {
	    lookupDomainClass()?.get(bioData.id)
        }
    }

    // TODO BB cache
    protected Class lookupDomainClass() {
        // This probably should come from the config file
	String domainClassName = objectType //conf.rememberMe.persistentToken.domainClassName ?: ''
	if (domainClassName == 'bio.Experiment') {
	    domainClassName = 'org.transmart.biomart.Experiment'
        }

	Class clazz = grails.util.Holders.grailsApplication.getClassForName(domainClassName)
	if (clazz) {
	    clazz
	}
	else {
	    logger.error 'Class not found: "{}"', domainClassName
	}
    }

    String toString() {
	'objectUid: ' + objectUid + ', objectType: ' + objectType + ', Folder: ' + fmFolder.id
    }
}
