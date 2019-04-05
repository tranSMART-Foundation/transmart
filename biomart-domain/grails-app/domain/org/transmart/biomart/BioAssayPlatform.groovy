package org.transmart.biomart

class BioAssayPlatform {
    String accession
    String array
    String description
    String name
    String organism
    String platformTechnology
    String platformType
    String platformVersion
    String vendor

    String uniqueId

    static transients = ['fullName', 'uniqueId']

    static hasMany = [analyses:BioAssayAnalysis]

    static mapping = {
	table 'BIOMART.BIO_ASSAY_PLATFORM'
	id generator: 'sequence', params: [sequence: 'BIOMART.SEQ_BIO_DATA_ID'], column: 'BIO_ASSAY_PLATFORM_ID'
        version false
        cache true

        accession column: 'PLATFORM_ACCESSION'
	analyses joinTable: [name: 'BIOMART.BIO_DATA_PLATFORM', key: 'BIO_ASSAY_PLATFORM_ID']
        array column: 'PLATFORM_ARRAY'
	description column: 'PLATFORM_DESCRIPTION'
	name column: 'PLATFORM_NAME'
	organism column: 'PLATFORM_ORGANISM'
        vendor column: 'PLATFORM_VENDOR'
    }

    static constraints = {
	description nullable: true, maxSize: 2000
	name nullable: true, maxSize: 400
	platformTechnology nullable: true
	platformType nullable: true
	platformVersion nullable: true, maxSize: 400
    }

    /**
     * Find concept code by its uniqueId
     * @return BioAssayPlatform with matching uniqueId or null, if match not found.
     */
    static BioAssayPlatform findByUniqueId(String uniqueId) {
	executeQuery('from BioAssayPlatform where id=(select id from BioData where uniqueId=:uniqueId)',
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

	if (!id) {
	    return
        }

	String bioDataUid = BioData.where { id == this.id }.uniqueId.get()
	if (bioDataUid) {
	    uniqueId = bioDataUid
	    uniqueId
        }
        else {
	    'BAP:' + accession
        }
    }

    String getFullName() {
	platformType + '/' + platformTechnology + '/' + vendor + '/' + name
    }
}
