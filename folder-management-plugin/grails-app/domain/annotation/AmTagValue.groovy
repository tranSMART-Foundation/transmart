package annotation

class AmTagValue {

    String value

    String uniqueId

    static transients = ['uniqueId']

    static mapping = {
	table 'AMAPP.am_tag_value'
	id generator: 'sequence', params: [sequence: 'AMAPP.SEQ_AMAPP_DATA_ID'], column: 'tag_value_id'
	version false
	cache true
	sort 'value'
    }

    static constraints = {
	value maxSize: 2000
    }

    static AmTagValue findByUniqueId(String uniqueId) {
	// TODO BB
	get AmData.findByUniqueId(uniqueId)?.id
    }

    /**
     * Use transient property to support unique ID for tagValue.
     * @return tagValue's uniqueId
     */
    String getUniqueId() {
	if (uniqueId) {
	    return uniqueId
	}

	AmData data = AmData.get(id)
	if (data) {
	    uniqueId = data.uniqueId
	    return uniqueId
	}
    }
}
