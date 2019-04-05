package org.transmart.biomart

class BioAnalysisAttribute {
    Long bioAssayAnalysisID
    String sourceCode
    String studyID
    Long termID
	
    static mapping = {
	table 'BIOMART.BIO_ANALYSIS_ATTRIBUTE'
	id generator: 'sequence', params: [sequence: 'BIOMART.SEQ_BIO_DATA_ID'], column: 'BIO_ANALYSIS_ATTRIBUTE_ID'
	version false

	bioAssayAnalysisID column:'BIO_ASSAY_ANALYSIS_ID'
	sourceCode column:'SOURCE_CD'
	studyID column: 'STUDY_ID'
	termID column: 'TERM_ID'
    }
	
    static constraints = {
	sourceCode nullable: true
	studyID nullable: true
	termID nullable: true
    }
}
