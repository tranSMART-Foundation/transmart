package org.transmart.biomart

class AdHocProperty {
    String key
    Long objectId
    String value
	
    static mapping = {
	table 'BIOMART.BIO_AD_HOC_PROPERTY'
	id generator: 'sequence', params: [sequence: 'BIOMART.SEQ_BIO_DATA_ID'], column: 'AD_HOC_PROPERTY_ID'
	version false
		
	key column: 'PROPERTY_KEY'
	objectId column: 'BIO_DATA_ID'
	value column: 'PROPERTY_VALUE'
    }
	
    String toString() { value }
}
