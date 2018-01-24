package org.transmart.plugin.auth0

class Settings {

	String fieldname
	String fieldvalue
	Date lastUpdated
	Long userid

	static mapping = {
		table 'SEARCH_USER_FEEDBACK'
		id generator: 'sequence', params: [sequence: 'SEQ_SEARCH_DATA_ID'], column: 'SEARCH_USER_FEEDBACK_ID'
		version false
		cache true

		fieldname column: 'APP_VERSION'
		fieldvalue column: 'FEEDBACK_TEXT'
		lastUpdated column: 'CREATE_DATE'
		userid column: 'SEARCH_USER_ID'
	}

	static constraints = {
		userid nullable: true
	}
}
