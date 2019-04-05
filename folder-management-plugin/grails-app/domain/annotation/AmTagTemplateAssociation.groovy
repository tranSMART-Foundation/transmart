package annotation

class AmTagTemplateAssociation {

    String objectUid
    Long tagTemplateId

    static mapping = {
	table 'AMAPP.am_tag_template_association'
	id generator: 'sequence', params: [sequence: 'AMAPP.SEQ_AMAPP_DATA_ID']
        version false
        cache true
        sort 'value'
    }

    static constraints = {
	objectUid maxSize: 200
    }
}
