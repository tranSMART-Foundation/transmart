package org.transmart.plugin.custom

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class CmsSection {

    String closure
    String instanceType
    String name

    static mapping = {
	table name: 'CMS_SECTION', schema: 'BIOMART_USER'
	id generator: 'sequence', params: [sequence: 'BIOMART_USER.SEQ_CMS_SECTION_ID']
	cache true
	version false

	closure type: 'text'
    }

    static constraints = {
	name unique: 'instanceType'
    }
}
