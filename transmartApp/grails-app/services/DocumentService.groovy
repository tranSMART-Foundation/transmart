import com.recomdata.search.DocumentHit
import com.recomdata.search.DocumentQuery
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.transmart.GlobalFilter
import org.transmart.KeywordSet
import org.transmart.SearchFilter
import org.transmart.SearchKeywordService
import org.transmart.searchapp.SearchKeyword

/**
 * @author mmcduffie
 */
class DocumentService implements InitializingBean {

    static transactional = false

    @Autowired private SearchKeywordService searchKeywordService
    @Autowired private GlobalFilterService globalFilterService

    @Value('${com.recomdata.searchengine.index:}')
    private String index

    private DocumentQuery documentQuery

    int documentCount(SearchFilter sfilter) {
	Map<String, List<String>> terms = documentTerms(sfilter)
	Map<String, List<String>> filters = sfilter.documentFilter.filters
	documentQuery.searchCount terms, filters
    }

    List<DocumentHit> documentData(SearchFilter sfilter, GrailsParameterMap params) {
	Map pagingParams = globalFilterService.createPagingParamMap(params)
	Map<String, List<String>> terms = documentTerms(sfilter)
	Map<String, List<String>> filters = sfilter.documentFilter.filters
	DocumentHit[] documents = documentQuery.search(terms, filters, pagingParams.max, pagingParams.offset)
	documents ? documents as List : []
    }

    Map<String, List<String>> documentTerms(SearchFilter sfilter) {

	GlobalFilter gfilter = sfilter.globalFilter
	KeywordSet geneFilters = gfilter.geneFilters
	String pathwayIds = gfilter.formatIdList(gfilter.allListFilters, ',')
        // If there are pathways, then get all genes in pathways and add them to the geneFilters (hash set)
	if (pathwayIds) {
	    geneFilters.addAll searchKeywordService.expandAllListToGenes(pathwayIds)
        }

	Map<String, List<String>> terms = [:]
        int termCount = 0
	processKeywordSet geneFilters, terms, GlobalFilter.CATEGORY_GENE, termCount
	processKeywordSet gfilter.compoundFilters, terms, GlobalFilter.CATEGORY_COMPOUND, termCount
	processKeywordSet gfilter.diseaseFilters, terms, GlobalFilter.CATEGORY_DISEASE, termCount
	processKeywordSet gfilter.trialFilters, terms, GlobalFilter.CATEGORY_TRIAL, termCount
	processKeywordSet gfilter.textFilters, terms, GlobalFilter.CATEGORY_TEXT, termCount

	terms
    }

    private List<String> getTermList(KeywordSet keywords) {
	List<String> terms = []
	for (SearchKeyword keyword in keywords) {
            if (terms.size() < DocumentQuery.MAX_CLAUSE_COUNT - 1) {
		terms << keyword.keyword
            }
            else {
                break
            }
        }

	terms
    }

    // Encode string value for display on HMTL page and encode out-of-band characters.
    String encodeHTML(String value) {
	if (!value) {
            return ''
        }

	value = value.replace('<span class="search-term">', '???HIT_OPEN???')
        value = value.replace('</span>', '???HIT_CLOSE???')
        value = value.encodeAsHTML()
	value = value.replace('???HIT_OPEN???', '<span class="search-term">')
        value = value.replace('???HIT_CLOSE???', '</span>')

	StringBuilder result = new StringBuilder()

	if (value) {
	    int len = value.length() - 1
            for (i in 0..len) {
		int ch = value.charAt(i)
                if (ch < 32) {
		    result << ' '
                }
                else if (ch >= 128) {
		    result << '&#' << ch
                }
                else {
		    result << (char) ch
                }
            }
        }

	result
    }

    private int processKeywordSet(KeywordSet keywordSet, Map<String, List<String>> terms,
	                          String key, int termCount) {
	if (keywordSet) {
	    List<String> list = getTermList(keywordSet)
	    termCount += list.size()
	    if (termCount < DocumentQuery.MAX_CLAUSE_COUNT) {
		terms[key] = list
	    }
	}

	termCount
    }

    void afterPropertiesSet() {
	documentQuery = new DocumentQuery(index)
    }
}
