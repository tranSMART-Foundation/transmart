package org.transmart.searchapp

import org.transmart.biomart.BioAnalysisAttribute

class BioAnalysisAttributeLineage {
    SearchTaxonomy ancestorTerm
    BioAnalysisAttribute bioAnalysisAttribute
	
    static mapping = {
	table 'BIOMART.BIO_ANALYSIS_ATTRIBUTE_LINEAGE'
	id column:'BIO_ANALYSIS_ATT_LINEAGE_ID'
	version false
    }
}
