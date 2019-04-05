package annotation

class AmTagTemplate {
    Boolean activeInd = true
    String tagTemplateName
    String tagTemplateSubtype
    String tagTemplateType

    SortedSet amTagItems
    static hasMany = [amTagItems: AmTagItem]

    static mapping = {
	table 'AMAPP.am_tag_template'
	id generator: 'sequence', params: [sequence: 'AMAPP.SEQ_AMAPP_DATA_ID'], column: 'tag_template_id'
        version false
        cache true
        sort 'tagTemplateName'

        amTagItems lazy: false
    }

    static constraints = {
	tagTemplateName maxSize: 200
	tagTemplateSubtype maxSize: 50
	tagTemplateType maxSize: 50
    }

    String toString() {
	'ID: ' + id + ', Template Name: ' + tagTemplateName + ', Template Type: ' + tagTemplateType
    }
}
