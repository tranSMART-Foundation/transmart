package org.transmart.biomart

class BioAssayCohort {
    String cohortId
    String cohortTitle
    String disease
    String longDesc
    String organism
    String pathology
    String sampleType
    String shortDesc
    String studyId
    String treatment

    static mapping = {
	table 'BIOMART.BIO_ASSAY_COHORT'
	id generator: 'sequence', params: [sequence: 'BIOMART.SEQ_BIO_DATA_ID'], column: 'BIO_ASSAY_COHORT_ID'
	version false
	cache usage:'read-only'
    }
}
