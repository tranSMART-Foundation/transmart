package org.transmart.searchapp

class Feedback {
    String appVersion
    Date createDate
    String feedbackText
    Long searchUserId

    static mapping = {
	table 'SEARCHAPP.SEARCH_USER_FEEDBACK'
	id generator: 'sequence', params: [sequence: 'SEARCHAPP.SEQ_SEARCH_DATA_ID'], column: 'SEARCH_USER_FEEDBACK_ID'
        version false
    }

    static constraints = {
	searchUserId nullable: true
    }
}
