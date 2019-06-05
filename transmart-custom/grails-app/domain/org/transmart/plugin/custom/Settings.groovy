package org.transmart.plugin.custom

class Settings {

    String fieldname
    String fieldvalue
    Date lastUpdated
    Long userid

    static mapping = {
	table 'BIOMART_USER.APPLICATION_SETTINGS'
	id generator: 'sequence', params: [sequence: 'BIOMART_USER.APPLICATION_SETTINGS_ID_SEQ']
	cache true
    }

    static constraints = {
	fieldvalue maxSize: 2000
    }
}
