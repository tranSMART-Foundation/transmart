package org.transmart

import groovy.transform.CompileStatic
import org.transmart.searchapp.SearchKeyword

/**
 * @author mmcduffie
 */
@CompileStatic
class SearchFilter {

    // TODO BB
    SearchKeywordService searchKeywordService = new SearchKeywordService()

    String searchText
    String datasource
    GeneExprFilter geFilter = new GeneExprFilter()
    LiteratureFilter litFilter = new LiteratureFilter()
    TrialFilter trialFilter = new TrialFilter()
    DocumentFilter documentFilter = new DocumentFilter()
    GlobalFilter globalFilter = new GlobalFilter()
    HeatmapFilter heatmapFilter = new HeatmapFilter()
    ExperimentAnalysisFilter expAnalysisFilter = new ExperimentAnalysisFilter()
    ExpressionProfileFilter exprProfileFilter = new ExpressionProfileFilter()
    String summaryWithLinks
    String pictorTerms

    int acttab() {
	switch (datasource) {
	    case 'trial': return 0
	    case 'experiment': return 1
	    case 'profile': return 2
	    case 'document': return 4
	    default: datasource?.startsWith('literature') ? 3 : 5
	}
    }

    String acttabname() {
	switch (datasource) {
	    case 'trial': return 'trial'
	    case 'experiment': return 'pretrial'
	    case 'profile': return 'profile'
	    case 'document': return 'doc'
	    default: datasource?.startsWith('literature') ? 'jubilant' : 'datasource'
	}
    }

    void createPictorTerms() {

	KeywordSet geneFilters = globalFilter.geneFilters
        // Get all pathway ids from globalFilter
	String pathwayIds = globalFilter.formatIdList(globalFilter.getAllListFilters(), ',')
        // If there are pathways, then get all genes in pathways and add them to the geneFilters (hash set)
	if (pathwayIds) {
	    geneFilters.addAll searchKeywordService.expandAllListToGenes(pathwayIds)
        }

        // Format the gene filter keywords into comma separated strings
	if (geneFilters) {
            pictorTerms = globalFilter.formatKeywordList(geneFilters, ',', '', 1900)
        }
        else {
            pictorTerms = null
	}
    }

    String marshal() {
        // todo -- add filter stuff in
	'<SearchFilter.searchText:' + searchText + '>'
    }

    /** For the ResNet and the GeneGo tabs */
    String getExternalTerms() {

	KeywordSet geneFilters = globalFilter.geneFilters
	String pathwayIds = globalFilter.formatIdList(globalFilter.getAllListFilters(), ',')
	if (pathwayIds) {
	    geneFilters.addAll searchKeywordService.expandAllListToGenes(pathwayIds)
        }

	StringBuilder s = new StringBuilder()
	appendKeywordSet geneFilters, s
	appendKeywordSet globalFilter.textFilters, s
	appendKeywordSet globalFilter.diseaseFilters, s
	appendKeywordSet globalFilter.compoundFilters, s
	appendKeywordSet globalFilter.trialFilters, s

	if (s) {
	    s
        }
	else {
	    searchText
        }
    }

    private void appendKeywordSet(KeywordSet ks, StringBuilder s) {
	if (ks) {
	    if (s) {
		s << ' AND '
            }
	    s << globalFilter.formatKeywordList(ks, ' OR ', '', 1900)
        }
    }
}
