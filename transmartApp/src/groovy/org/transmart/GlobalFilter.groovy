package org.transmart

import groovy.transform.CompileStatic
import org.transmart.searchapp.SearchKeyword

/**
 * @author mmcduffie
 */
@CompileStatic
class GlobalFilter {

    public static final String CATEGORY_GENE = 'GENE'
    public static final String CATEGORY_PATHWAY = 'PATHWAY'
    public static final String CATEGORY_COMPOUND = 'COMPOUND'
    public static final String CATEGORY_DISEASE = 'DISEASE'
    public static final String CATEGORY_TRIAL = 'TRIAL'
    public static final String CATEGORY_TEXT = 'TEXT'
    public static final String CATEGORY_STUDY = 'STUDY'
    public static final String CATEGORY_GENE_SIG = 'GENESIG'
    public static final String CATEGORY_GENE_LIST = 'GENELIST'

    Map<String, KeywordSet> categoryFilterMap = [:]

    boolean isEmpty() {
	for (KeywordSet value in categoryFilterMap.values()) {
	    if (value) {
                return false
            }
        }
	true
    }

    boolean isTextOnly() {
	boolean hasText = false
	for (String key in categoryFilterMap.keySet()) {
	    if (categoryFilterMap[key]) {
		if (key == CATEGORY_TEXT) {
                    hasText = true
                }
                else {
                    return false
                }
            }
        }
	hasText
    }

    boolean containsFilter(SearchKeyword filter) {
	categoryFilterMap[filter.dataCategory].contains filter
    }

    KeywordSet getBioMarkerFilters() {
	geneFilters + pathwayFilters + geneSignatureFilters + geneListFilters
    }

    boolean hasAnyListFilters() {
	pathwayFilters || geneSignatureFilters || geneListFilters
    }

    KeywordSet getGenePathwayFilters() {
	geneFilters + pathwayFilters
    }

    KeywordSet getAllListFilters() {
	pathwayFilters + geneSignatureFilters + geneListFilters
    }

    KeywordSet getGeneFilters() {
	findFiltersByCategory CATEGORY_GENE
    }

    KeywordSet getGeneSignatureFilters() {
	findFiltersByCategory CATEGORY_GENE_SIG
    }

    KeywordSet getGeneListFilters() {
	findFiltersByCategory CATEGORY_GENE_LIST
    }

    KeywordSet getGeneSigListFilters() {
	geneSignatureFilters + geneListFilters
    }

    KeywordSet getPathwayFilters() {
	findFiltersByCategory CATEGORY_PATHWAY
    }

    KeywordSet getDiseaseFilters() {
	findFiltersByCategory CATEGORY_DISEASE
    }

    KeywordSet getTrialFilters() {
	findFiltersByCategory CATEGORY_TRIAL
    }

    KeywordSet getStudyFilters() {
	findFiltersByCategory CATEGORY_STUDY
    }

    KeywordSet getCompoundFilters() {
	findFiltersByCategory CATEGORY_COMPOUND
    }

    KeywordSet getTextFilters() {
	findFiltersByCategory CATEGORY_TEXT
    }

    KeywordSet getAllFilters() {
	geneFilters + pathwayFilters + compoundFilters + diseaseFilters + trialFilters +
	    textFilters + studyFilters + geneSignatureFilters + geneListFilters
    }

    /**
     * keywords for given category or an empty instance if not present
     */
    KeywordSet findFiltersByCategory(String category) {
	KeywordSet filters = categoryFilterMap[category]
        if (filters == null) {
            filters = new KeywordSet()
	    categoryFilterMap[category] = filters
        }
	(KeywordSet) filters.clone()
    }

    // Returns list of keywords for keywordset. Useful for building 'in' clauses or search terms.
    String formatKeywordList(KeywordSet set, String separator, String textQualifier, int maxLength) {
        String list = ''
        for (filter in set) {
            String s = ''
	    if (list && separator) {
                s = separator
            }
	    if (textQualifier) {
                s += textQualifier
            }
            s += filter.keyword
	    if (textQualifier) {
                s += textQualifier
            }
	    if (maxLength && list.length() + s.length() > maxLength) {
                break
            }
            list += s
        }
	list
    }

    // Returns list of bioDataIds for specified category. Useful for building 'in' clauses.
    String formatIdList(KeywordSet set, String separator) {
	StringBuilder list = new StringBuilder()
	for (SearchKeyword filter in set) {
	    if (separator && list) {
		list << separator
            }
	    list << filter.bioDataId
        }
	list.toString()
    }

    void addKeywordFilter(SearchKeyword keyword) {
	KeywordSet klist = categoryFilterMap[keyword.dataCategory]
        if (klist == null) {
            // make sure no dup
            klist = new KeywordSet()
	    categoryFilterMap[keyword.dataCategory] = klist
        }
	klist << keyword
    }

    void removeKeywordFilter(SearchKeyword keyword) {
	categoryFilterMap[keyword.dataCategory]?.removeKeyword keyword
    }

    boolean hasPathway() {
	pathwayFilters
    }
}
