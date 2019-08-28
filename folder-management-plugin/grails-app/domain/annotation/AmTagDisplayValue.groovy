package annotation

import groovy.util.logging.Slf4j

@Slf4j('logger')
class AmTagDisplayValue implements Serializable {
    private static final long serialVersionUID = 1

    AmTagItem amTagItem
    String codeName
    String displayValue
    Long objectId
    String objectType
    String objectUid
    String subjectUid
    String uniqueId

    static transients = ['codeName', 'uniqueId']

    static mapping = {
	table 'am_tag_display_vw' // TODO BB .. this is a view
	id composite: ['subjectUid', 'objectUid', 'amTagItem']
	version false
	cache true
	sort 'value'

	amTagItem column: 'tag_item_id'
    }

    static AmTagDisplayValue get(String subjectUid, long objectId) {
	findBySubjectUidAndObjectId subjectUid, objectId
    }

    static Collection<AmTagDisplayValue> findAllDisplayValue(String subjectUid, long amTagItemId) {
	findAllBySubjectUidAndAmTagItem subjectUid, AmTagItem.load(amTagItemId)
    }

    /**
     * Use transient property to support unique ID for tagValue.
     * @return tagValue's uniqueId
     */
    String getUniqueId() {
        if (uniqueId == null) {
            uniqueId = objectUid
        }
	uniqueId
    }

    String getCodeName() {
        if (codeName == null) {
            codeName = displayValue
        }
	codeName
    }

    String toString() {
	'Subject UID: ' + subjectUid + ', ' + 'Object UID: ' + objectUid + ', ' + 'Display Value: ' + displayValue
    }
}
