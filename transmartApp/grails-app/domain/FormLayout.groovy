class FormLayout {

    String column
    String dataType
    Boolean display = true
    String displayName
    String key
    Integer sequence

    static mapping = {
	table 'SEARCHAPP.SEARCH_FORM_LAYOUT'
	id generator: 'sequence', params: [sequence: 'SEARCHAPP.SEQ_SEARCH_FORM_LAYOUT_ID'], column: 'FORM_LAYOUT_ID'
        version false

        column column: 'FORM_COLUMN'
	key column: 'FORM_KEY'
    }

    static constraints = {
	dataType nullable: true
	displayName nullable: true
	sequence nullable: true
    }
}
