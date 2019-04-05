package annotation

class AmTagAssociation implements Serializable {
    private static final long serialVersionUID = 1

    String objectType
    String objectUid
    String subjectUid
    Long tagItemId

    static mapping = {
	table 'AMAPP.am_tag_association'
	id composite: ['objectUid', 'subjectUid']
        version false
        cache true
        sort 'tagTemplateName'
    }

    static AmTagAssociation get(String objectUid, Long tagItemId, String subjectUid) {
	findByObjectUidAndTagItemIdAndSubjectUid objectUid, tagItemId, subjectUid
    }

    static boolean remove(String objectUid, Long tagItemId, String subjectUid, boolean flush = false) {
	AmTagAssociation instance = get(objectUid, tagItemId, subjectUid)
        instance ? instance.delete(flush: flush) : false
    }

    String toString() {
	'objectType: ' + objectType + ', subjectUid: ' + subjectUid +
	    ', objectUid: ' + objectUid + ', tagItemId: ' + tagItemId
    }
}
