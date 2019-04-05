package org.transmart.searchapp

class SearchTaxonomyRels {
    SearchTaxonomy child
    SearchTaxonomy parent
	
    static mapping = {
	table 'SEARCHAPP.SEARCH_TAXONOMY_RELS'
	id generator: 'sequence', params: [sequence: 'SEARCHAPP.SEQ_SEARCH_TAXONOMY_RELS_ID'], column: 'SEARCH_TAXONOMY_RELS_ID'
	version false
    }
	
    static constraints = {
	parent nullable: true
    }
}
