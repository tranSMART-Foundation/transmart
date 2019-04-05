class RequiredUploadField {

    String field
    String type

    static mapping = {
	table 'SEARCHAPP.SEARCH_REQUIRED_UPLOAD_FIELD'
        id column: 'REQUIRED_UPLOAD_FIELD_ID'
	version false
    }
}
