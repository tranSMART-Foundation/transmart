package org.transmart.biomart

class ConceptCode {
    String bioConceptCode
    String codeDescription
    String codeName
    String codeTypeName

    String uniqueId

    static transients = ['uniqueId']

    static hasMany = [bioDataUid: BioData]

    static mapping = {
	table 'BIOMART.BIO_CONCEPT_CODE'
	id generator: 'sequence', params: [sequence: 'BIOMART.SEQ_BIO_DATA_ID'], column: 'BIO_CONCEPT_CODE_ID'
        version false
	cache true

	bioDataUid joinTable: [name: 'BIOMART.BIO_DATA_UID', key: 'BIO_DATA_ID']
    }

    static constraints = {
	bioConceptCode nullable: true, maxSize: 400
	codeDescription nullable: true, maxSize: 2000
	codeTypeName nullable: true, maxSize: 400
    }

    /**
     * Find concept code by its uniqueId
     * @return concept code with matching uniqueId or null, if match not found.
     */
    static ConceptCode findByUniqueId(String uniqueId) {
	executeQuery('from ConceptCode where id=(select id from BioData where uniqueId=:uniqueId)',
		     [uniqueId: uniqueId])[0]
    }

    /**
     * Use transient property to support unique ID for tagValue.
     * @return tagValue's uniqueId
     */
    String getUniqueId() {
	if (uniqueId) {
            return uniqueId
        }

	String bioDataUid = BioData.where { id == this.id }.uniqueId.get()
	if (bioDataUid) {
	    uniqueId = bioDataUid
	    return uniqueId
        }
    }
}
