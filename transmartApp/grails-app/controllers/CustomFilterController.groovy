import org.springframework.beans.factory.annotation.Autowired
import org.transmart.GlobalFilter
import org.transmart.KeywordSet
import org.transmart.SearchFilter
import org.transmart.plugin.shared.SecurityService
import org.transmart.searchapp.CustomFilter
import org.transmart.searchapp.CustomFilterItem
import org.transmart.searchapp.SearchKeyword

/**
 * @author mmcduffie
 */
class CustomFilterController {

    static allowedMethods = [save: 'POST', update: 'POST']

    @Autowired private SecurityService securityService

    def list() {
	if (!params.max) {
	    params.max = 10
        }

	List<CustomFilter> customFilters = CustomFilter.findAllBySearchUserId(securityService.currentUserId())
	for (CustomFilter customFilter in customFilters) {
	    customFilter.summary = createSummaryWithLinks(createKeywordMap(customFilter))
	}
	[customFilters: customFilters]
    }

    def delete(CustomFilter customFilter) {
	if (!customFilter) {
	    flash.message = "CustomFilter not found with id ${params.id}"
	    redirect action: 'list'
	    return
	}

	if (!canUpdate(customFilter)) {
	    flash.message = "You are not authorized to delete the custom filter with ID ${params.id}."
	    redirect action: 'list'
	    return
	}

	customFilter.delete()
	flash.message = "CustomFilter ${params.id} deleted"
	redirect action: 'list', params: [ts: System.currentTimeMillis()]
    }

    def edit(CustomFilter customFilter) {
	if (!customFilter) {
	    flash.message = "CustomFilter not found with id ${params.id}"
	    redirect action: 'list'
	    return
        }

	if (!canUpdate(customFilter)) {
	    flash.message = "You are not authorized to edit the custom filter with ID ${params.id}."
	    redirect action: 'list'
	    return
        }

	customFilter.summary = createSummaryWithLinks(createKeywordMap(customFilter))
	[customFilter: customFilter]
    }

    def update(CustomFilter customFilter) {
	params.privateFlag = params.privateFlag == 'on' ? 'Y' : 'N'
	if (customFilter) {
	    customFilter.properties = params
	    if (!customFilter.hasErrors() && customFilter.save()) {
		flash.message = "CustomFilter ${params.id} updated"
		redirect action: 'list', params: [ts: System.currentTimeMillis(), lastFilterID: params.id]
            }
            else {
		render view: 'edit', model: [customFilter: customFilter]
            }
        }
        else {
	    flash.message = "CustomFilter not found with id ${params.id}"
	    redirect action: 'edit', id: params.id
        }
    }

    def create() {
	Map<String, Collection<SearchKeyword>> map = createKeywordMap(sessionSearchFilter().globalFilter)
	[customFilter: new CustomFilter(
	    searchUserId: securityService.currentUserId(),
	    privateFlag: 'N',
	    summary: createSummaryWithLinks(map))]
    }

    def save() {
	params.privateFlag = params.privateFlag == 'on' ? 'Y' : 'N'
	CustomFilter filter = new CustomFilter(params)
	Map<String, Collection<SearchKeyword>> map = createKeywordMap(sessionSearchFilter().globalFilter)
	for (String key in map.keySet()) {
	    Collection<SearchKeyword> keywords = map[key]
	    for (SearchKeyword keyword in keywords) {
		filter.addToItems new CustomFilterItem(uniqueId: keyword.uniqueId, bioDataType: keyword.dataCategory)
            }
        }
        if (!filter.hasErrors() && filter.save()) {
	    flash.message = "CustomFilter ${filter.id} created"
	    redirect action: 'list', params: [ts: System.currentTimeMillis(), lastFilterID: filter.id]
        }
        else {
	    render view: 'create', model: [customFilter: filter]
        }
    }

    private boolean canUpdate(CustomFilter customFilter) {
	customFilter?.searchUserId == securityService.currentUserId()
    }

    private Map<String, Collection<SearchKeyword>> createKeywordMap(GlobalFilter gfilter) {
	Map<String, Collection<SearchKeyword>> map = [:]
	addToMap gfilter.geneFilters, map, 'GENE'
	addToMap gfilter.pathwayFilters, map, 'PATHWAY'
	addToMap gfilter.compoundFilters, map, 'COMPOUND'
	addToMap gfilter.diseaseFilters, map, 'DISEASE'
	addToMap gfilter.trialFilters, map, 'TRIAL'
	addToMap gfilter.studyFilters, map, 'STUDY'
	addToMap gfilter.geneSignatureFilters, map, 'GENESIG'
	addToMap gfilter.textFilters, map, 'TEXT'
	map
    }

    private void addToMap(KeywordSet keywordSet, Map<String, Collection<SearchKeyword>> map, String key) {
	if (keywordSet) {
	    map[key] = keywordSet
        }
    }

    private Map<String, Collection<SearchKeyword>> createKeywordMap(CustomFilter filter) {

	Map<String, Collection<SearchKeyword>> map = [:]
	List<String> uniqueIds = []
	for (CustomFilterItem item in filter.items) {
	    String id = item.uniqueId
            if (item.bioDataType == 'TEXT') {
		Collection<SearchKeyword> list
                if (map.containsKey('TEXT')) {
		    list = map.TEXT
                }
                else {
                    list = []
		    map.TEXT = list
                }

		list << new SearchKeyword(
		    keyword: id.substring(id.indexOf(':') + 1),
		    uniqueId: id,
		    dataCategory: 'TEXT')
            }
            else {
		uniqueIds << item.uniqueId
            }
        }

	if (uniqueIds) {
	    Collection<SearchKeyword> keywords = SearchKeyword.findAllByUniqueIdInList(uniqueIds)
	    for (SearchKeyword keyword in keywords) {
		Collection<SearchKeyword> list
                if (map.containsKey(keyword.dataCategory)) {
		    list = map[keyword.dataCategory]
                }
                else {
                    list = []
		    map[keyword.dataCategory] = list
                }
		list << keyword
            }
        }

	map
    }

    /**
     * Creates link to detatils for specified filter keyword.
     */
    private String createSummaryFilter(SearchKeyword keyword) {

	String type = keyword.dataCategory.toLowerCase()

	StringBuilder link = new StringBuilder('<nobr>')
        if (type == 'text') {
	    link << createFilterDetailsLink(id: keyword.keyword, label: keyword.keyword, type: type)
        }
        else {
	    String label = keyword.keyword
	    if (type == 'pathway' && keyword.dataSource) {
                label = keyword.dataSource + '-' + label
            }
	    link << createFilterDetailsLink(id: keyword.bioDataId, label: label, type: type)
        }
	link << '</nobr>'
	link
    }

    /**
     * Creates section in summary for given categories filters.
     */
    private String createSummarySection(String category, Map<String, Collection<SearchKeyword>> keywordMap) {

	StringBuilder section = new StringBuilder()
	Collection<SearchKeyword> filters = keywordMap[(category)]
	for (SearchKeyword filter in filters) {
	    if (section) {
		section << ' OR '
            }
	    section << createSummaryFilter(filter)
        }

	if (!section) {
            return ''
        }

	StringBuilder span = new StringBuilder()
	span << '<span class="filter-item filter-item-'
	span << category.toLowerCase()
	span << '">'
	span << formatCategory(category)
        if (filters.size() > 1) {
	    span << 's'
        }
	span << '&gt;&nbsp;</span>'
	span << section

	span
    }

    /**
     * Creates summary of filters with links to details for filters.
     */
    private String createSummaryWithLinks(Map<String, Collection<SearchKeyword>> keywordMap) {
	StringBuilder summary = new StringBuilder()
	appendSummarySection 'GENE', keywordMap, summary, true
	appendSummarySection 'PATHWAY', keywordMap, summary, true
	appendSummarySection 'GENESIG', keywordMap, summary, true
	appendSummarySection 'COMPOUND', keywordMap, summary, false
	appendSummarySection 'DISEASE', keywordMap, summary, false
	appendSummarySection 'TRIAL', keywordMap, summary, false
	appendSummarySection 'STUDY', keywordMap, summary, false
	appendSummarySection 'TEXT', keywordMap, summary, false
	summary
    }

    private void appendSummarySection(String category, Map<String, Collection<SearchKeyword>> keywordMap,
	                              StringBuilder summary, boolean or) {
	String section = createSummarySection(category, keywordMap)
	if (summary && section) {
	    summary << or ? ' OR ' : ' AND '
	}
	summary << section
    }

    private String formatCategory(String category) {
	category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase()
    }

    private SearchFilter sessionSearchFilter() {
	session.searchFilter
    }
}
