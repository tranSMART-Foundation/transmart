package org.transmart.plugin.custom

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class CmsFile {

	byte[] bytes
	String contentType
	String instanceType
	Date lastUpdated
	String name

	static mapping = {
		table name: 'CMS_FILE', schema: 'BIOMART_USER'
		id generator: 'sequence', params: [sequence: 'BIOMART_USER.SEQ_CMS_FILE_ID']
		cache true
	}

	static constraints = {
		bytes maxSize: 1024 * 1024 * 10 // 10 MB
		name unique: 'instanceType'
	}
}
