import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.GlobalFilter
import org.transmart.KeywordSet
import org.transmart.SearchFilter
import org.transmart.SearchKeywordService
import org.transmart.SearchResult
import org.transmart.biomart.BioDataExternalCode
import org.transmart.plugin.shared.SecurityService
import org.transmart.searchapp.CustomFilter
import org.transmart.searchapp.CustomFilterItem
import org.transmart.searchapp.SearchKeyword
import org.transmart.searchapp.SearchKeywordTerm
import org.transmartproject.db.log.AccessLogService

@Slf4j('logger')
class SearchController {

    private static final String SEARCH_DELIMITER = 'SEARCHDELIMITER'

    @Autowired private AccessLogService accessLogService
    @Autowired private SearchService searchService
    @Autowired private SearchKeywordService searchKeywordService
    @Autowired private SecurityService securityService

    def index() {
	session.searchFilter = new SearchFilter()
    }

    def list() {
	if (!params.max) {
	    params.max = 20
	}
	[geneExprAnalysisList: GeneExprAnalysis.list(params),
	 total               : GeneExprAnalysis.count(),
	 page                : true]
    }

    def loadSearchAnalysis() {
	params.query = 'gene' + SEARCH_DELIMITER +
	    'pathway' + SEARCH_DELIMITER +
	    'genelist' + SEARCH_DELIMITER +
	    'genesig:' + params.query
        loadSearch()
    }

    /**
     * load list of keyword terms to following data category:
     * - MIRNA
     * - GENE
     * - PATHWAY
     * - GENELIST
     * - GENESIG
     */
    def loadSearchPathways() {
        params.query = 'metabolite_superpathway' + SEARCH_DELIMITER +
            'metabolite_subpathway' + SEARCH_DELIMITER +
            'metabolite' + SEARCH_DELIMITER +
            'protein' + SEARCH_DELIMITER +
            'mirna' + SEARCH_DELIMITER +
            'gene' + SEARCH_DELIMITER +
            'pathway' + SEARCH_DELIMITER +
            'genelist' + SEARCH_DELIMITER +
            'genesig:' + params.query
        loadSearch()
    }

    /**
     * find top 20 biomarkers that exist in the keyword table  with case insensitive LIKE match
     */
    private loadSearch() {
	String query = params.query
	String category
	String values
	if (query.contains(':')) {
	    category = query.substring(0, query.indexOf(':')).toUpperCase().replace('-', ' ')
	    values = query.substring(query.indexOf(':') + 1).toUpperCase()
        }
        else {
            category = 'ALL'
	    values = query.toUpperCase()
        }

	Set<SearchKeyword> keywords = []
        // don't execute query if category is All and the term is empty
	if ('ALL' != category || !values) {
	    String queryStr = '''
					SELECT distinct t.searchKeyword, t.keywordTerm, t.rank, t.termLength
					FROM org.transmart.searchapp.SearchKeywordTerm t
					WHERE t.keywordTerm LIKE :term || '%' '''
	    Map queryParams = [term: values]
            // filter by category if specified
	    if ('ALL' != category) {
                queryStr += ' AND t.searchKeyword.dataCategory IN (:category) '
		queryParams.category = category.split(SEARCH_DELIMITER)
            }
            // permission to view search keyword (Admin gets all)
	    if (!securityService.principal().isAdmin()) {
                queryStr += ' AND (t.ownerAuthUserId = :uid OR t.ownerAuthUserId IS NULL)'
		queryParams.uid = securityService.currentUserId()
            }
            // order by rank/term, if no term specified. otherwise, order by rank/length/term so short terms are matched first.
	    if (values) {
		queryStr += ' ORDER BY t.rank ASC, t.termLength ASC, t.keywordTerm'
            }
            else {
		queryStr += ' ORDER BY t.rank ASC, t.keywordTerm'
            }

	    for (k in SearchKeywordTerm.executeQuery(queryStr, queryParams, [max: 20])) {
		keywords << k[0]
            }
        }

	renderSearchKeywords keywords
    }

    def loadCategories() {

	List<Object[]> categories = SearchKeyword.executeQuery('''
				select distinct k.dataCategory as value, k.displayDataCategory as label
				from org.transmart.searchapp.SearchKeyword k
				order by lower(k.dataCategory)''')
	List<Map> rows = [[value: 'all', label: 'all']]
	for (Object[] category in categories) {
	    Map<String, String> row = [:]
	    String dataCategory = category[0]
	    String displayDataCategory = category[1]
	    if (dataCategory.equalsIgnoreCase('study')) {
                row.value = 'study'
                row.label = 'geo/ebi'
            }
            else {
		row.value = dataCategory.toLowerCase().replace(' ', '-')
		if (displayDataCategory) {
		    row.label = displayDataCategory.toLowerCase()
                }
            }
	    rows << row
        }

	render params.callback + '(' + ([rows: rows] as JSON) + ')'
    }

    // Used by EditFiltersWindow to load records for list of global filters.
    def loadCurrentFilters() {
	KeywordSet filters = sessionSearchFilter().globalFilter.allFilters
	logger.info 'SearchController.loadCurrentFilters() count = {}', filters?.size()
	renderSearchKeywords filters
    }

    // Used by EditFiltersWindow to split a pathway.
    def loadPathwayFilters(String id) {
	List<SearchKeyword> genes = []
	if (id) {
	    SearchKeyword keyword = getSearchKeyword(id)
            genes = searchKeywordService.expandPathwayToGenes(keyword.bioDataId.toString())
        }
	renderSearchKeywords genes
    }

    /**
     * load pathway and gene for heatmap
     */
    def loadHeatMapFilterAJAX() {

	String values = params.query
	if (values != null) {
            values = values.replace('-', '').toUpperCase()
	}

	List<Object[]> keywordResults = SearchKeywordTerm.executeQuery('''
				SELECT distinct t.searchKeyword, t.keywordTerm, t.rank, t.termLength
				FROM org.transmart.searchapp.SearchKeywordTerm t
				WHERE t.searchKeyword.dataCategory IN ('GENE', 'PATHWAY', 'GENESIG','GENELIST')
				AND t.keywordTerm LIKE :values
				AND (t.ownerAuthUserId=:ownerAuthUserId OR t.ownerAuthUserId IS NULL)
				ORDER BY t.rank ASC, t.termLength ASC, t.keywordTerm''',
		[values: values + '%', ownerAuthUserId: securityService.currentUserId()], [max: 20])

	Set<SearchKeyword> keywords = []
	for (Object[] result in keywordResults) {
	    keywords << result[0]
	}
	renderSearchKeywords keywords
    }

    /**
     * render keywords json object
     */
    private renderSearchKeywords(Collection<SearchKeyword> keywords) {

	List<Long> dataIds = []
	for (SearchKeyword keyword in keywords) {
            if (keyword.dataCategory != 'TEXT') {
		dataIds << keyword.bioDataId
            }
        }

	List<BioDataExternalCode> allSynonyms
	if (dataIds) {
	    allSynonyms = BioDataExternalCode.executeQuery('''
					SELECT DISTINCT bdec
					FROM org.transmart.biomart.BioDataExternalCode bdec
					WHERE bdec.bioDataId IN(:ids)
					AND bdec.codeType='SYNONYM' ''',
							   [ids: dataIds])
        }

	Map<Long, List<BioDataExternalCode>> synMap = [:]
	for (BioDataExternalCode syn in allSynonyms) {
	    List<BioDataExternalCode> synList = synMap[syn.bioDataId]
            if (synList == null) {
                synList = []
		synMap[syn.bioDataId] = synList
            }
	    synList << syn
        }

	List<Map> itemlist = []
	for (SearchKeyword keyword in keywords) {
            if (keyword.dataCategory != 'TEXT') {
		List<BioDataExternalCode> synonyms = synMap[keyword.bioDataId]
		String syntext = formatSynonyms(synonyms)
		String category = keyword.dataCategory
		String display = keyword.displayDataCategory
		String ssource = keyword.dataSource
		if (ssource) {
                    ssource = ssource + '>'
                }
                else {
                    ssource = ''
                }
		itemlist << [id: keyword.id, source: ssource, keyword: keyword.keyword, synonyms: syntext, category: category, display: display]
            }
            else {
		itemlist << [id: keyword.id, source: '', keyword: keyword.keyword, synonyms: '', category: 'TEXT', display: 'Text']
            }
        }

	render params.callback + '(' + ([rows: itemlist] as JSON) + ')'
    }

    def doSearch() {

	SearchFilter filter = sessionSearchFilter()
	SearchResult sResult = new SearchResult()
	searchService.doResultCount sResult, filter
        filter.summaryWithLinks = createSummaryWithLinks(filter)
        filter.createPictorTerms()
        boolean defaultSet = false

        if (sResult.trialCount > 0) {
	    filter.datasource = 'trial'
            defaultSet = true
        }
        else if (!defaultSet && sResult.experimentCount > 0) {
	    filter.datasource = 'experiment'
            defaultSet = true
        }
        else if (!defaultSet && sResult.profileCount > 0) {
	    filter.datasource = 'profile'
            defaultSet = true
        }
        else if (!defaultSet && sResult.literatureCount() > 0) {
	    filter.datasource = 'literature'
            defaultSet = true
        }
        else if (!defaultSet && sResult.documentCount > 0) {
	    filter.datasource = 'document'
            defaultSet = true
        }
        else {
	    filter.datasource = 'document'
        }

	accessLogService.report 'Search', filter.marshal()

	render view: 'list', model: [searchresult: sResult, page: false]
    }

    /**
     * conduct a search, params expected or keywork id or keyword text
     */
    def search(String id) {
	logger.info 'search: {}', params

	SearchKeyword keyword
	if (id) {
	    keyword = getSearchKeyword(id)
        }

	if (keyword) {
	    SearchFilter searchFilter = new SearchFilter()
	    session.searchFilter = searchFilter
	    createUpdateSessionFilter keyword
	    searchFilter.searchText = keyword.keyword
	    redirect action: 'doSearch'
        }
        else {
	    redirect action: 'index'
	}
    }

    def newSearch() {
        session.searchFilter = new SearchFilter()
	redirect action: 'search', params: params
    }

    def searchCustomFilter(CustomFilter customFilter) {

	SearchFilter sfilter = new SearchFilter()
	GlobalFilter gfilter = sfilter.globalFilter

	if (customFilter) {
	    if (customFilter.privateFlag != 'Y' || customFilter.searchUserId == securityService.currentUserId()) {
		List<String> uniqueIds = []
		for (CustomFilterItem item in customFilter.items) {
		    String id = item.uniqueId
                    if (item.bioDataType == 'TEXT') {
			gfilter.addKeywordFilter new SearchKeyword(
			    keyword: id.substring(id.indexOf(':') + 1),
			    uniqueId: id,
			    displayDataCategory: 'Text',
			    dataCategory: 'TEXT')
                    }
                    else {
			uniqueIds << item.uniqueId
                    }
                }

		for (SearchKeyword keyword in SearchKeyword.findAllByUniqueIdInList(uniqueIds)) {
		    gfilter.addKeywordFilter keyword
                }
            }
            else {
		flash.message = "You are not authorized to view the custom filter with ID ${params.id}."
		redirect action: 'index'
            }
        }
        else {
	    flash.message = "The custom filter with ID ${params.id} no longer exists."
	    redirect action: 'index'
        }

        sfilter.searchText = ''
        session.searchFilter = sfilter

	redirect action: 'doSearch', params: [ts: System.currentTimeMillis()]
    }

    /**
     * Updates global filters after editing with advanced filter window.
     */
    def searchEdit() {
	SearchFilter sfilter = new SearchFilter()
	GlobalFilter gfilter = sfilter.globalFilter
	String[] ids = (params.ids ?: '').split(',')
	String[] texts = (params.texts ?: '').split(',')
	if (ids || texts) {
	    for (String id in ids) {
		SearchKeyword keyword = getSearchKeyword(id)
		if (keyword) {
		    gfilter.addKeywordFilter keyword
                }
            }
	    for (String text in texts) {
		SearchKeyword keyword = getSearchKeyword(text)
		if (keyword) {
		    gfilter.addKeywordFilter keyword
                }
            }
        }

        if (!gfilter.isEmpty()) {
            session.searchFilter = sfilter
	    redirect action: 'doSearch'
        }
        else {
	    redirect action: 'index'
	}
    }

    /**
     * Removes a global filter
     */
    def remove(String id) {
	GlobalFilter gfilter = sessionSearchFilter().globalFilter
	if (id) {
	    SearchKeyword keyword = getSearchKeyword(id)
	    if (keyword) {
		gfilter.removeKeywordFilter keyword
            }
        }
        if (gfilter.isEmpty()) {
            session.searchFilter = new SearchFilter()
	    redirect action: 'index'
        }
        else {
	    redirect action: 'doSearch'
        }
    }

    def searchHeaderSearch() {
        params.sourcepage = 'search'
	redirect action: 'search', params: params
    }

    def showDefaultFilter() {
	render template: 'defaultFilter'
    }

    /**
     * Parses the searchText and populates the global search filter
     */
    private void createUpdateSessionFilter(SearchKeyword keyword) {
	SearchFilter filter = sessionSearchFilter() ?: new SearchFilter()
	updateSearchFilter keyword, filter
	session.searchFilter = filter
    }

    /**
     * update existing search Filter
     */
    private void updateSearchFilter(SearchKeyword keyword, SearchFilter filter) {
        filter.searchText = keyword.keyword
	filter.globalFilter.addKeywordFilter keyword
    }

    /**
     * Creates link to detatils for specified filter keyword.
     */
    private String createSummaryFilter(SearchKeyword keyword) {

	String type = keyword.dataCategory.toLowerCase()

	StringBuilder link = new StringBuilder('<nobr>')
        if (type == 'text') {
	    link << createFilterDetailsLink(id: keyword.keyword, label: keyword.keyword, type: type)
	    link << createRemoveFilterLink(id: keyword.keyword)
        }
        else if (type == 'pathway') {
	    String label = keyword.keyword
	    if (keyword.dataSource) {
                label = keyword.dataSource + '-' + label
            }
	    link << createFilterDetailsLink(id: keyword.bioDataId, label: label, type: type)
	    link << createRemoveFilterLink(id: keyword.id)
        }
        else {
	    link << createFilterDetailsLink(id: keyword.bioDataId, label: keyword.keyword, type: type)
	    link << createRemoveFilterLink(id: keyword.id)
        }
	link << '</nobr>'

	link
    }

    /**
     * Creates section in summary for given categories filters.
     */
    private String createSummarySection(String category, GlobalFilter gfilter) {

	StringBuilder section = new StringBuilder()
	KeywordSet filters = gfilter.findFiltersByCategory(category)
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
	span << '&gt; </span>'
	span << section

	span
    }

    /**
     * Creates summary of filters with links to details for filters.
     */
    private String createSummaryWithLinks(SearchFilter filter) {
        GlobalFilter gfilter = filter.globalFilter
	StringBuilder summary = new StringBuilder()
	appendSummarySection gfilter.CATEGORY_GENE, gfilter, summary, true
	appendSummarySection gfilter.CATEGORY_PATHWAY, gfilter, summary, true
	appendSummarySection gfilter.CATEGORY_GENE_SIG, gfilter, summary, true
	appendSummarySection gfilter.CATEGORY_GENE_LIST, gfilter, summary, true
	appendSummarySection gfilter.CATEGORY_COMPOUND, gfilter, summary, false
	appendSummarySection gfilter.CATEGORY_DISEASE, gfilter, summary, false
	appendSummarySection gfilter.CATEGORY_TRIAL, gfilter, summary, false
	appendSummarySection gfilter.CATEGORY_STUDY, gfilter, summary, false
	appendSummarySection gfilter.CATEGORY_TEXT, gfilter, summary, false
	summary
    }

    private void appendSummarySection(String category, GlobalFilter gfilter,
	                              StringBuilder summary, boolean or) {
	String section = createSummarySection(category, gfilter)
	if (summary && section) {
	    summary << or ? ' OR ' : ' AND '
        }
	summary << section
    }

    private String formatSynonyms(List<BioDataExternalCode> synonyms) {
	if (!synonyms) {
	    return ''
	}

	StringBuilder syntext = new StringBuilder()
	boolean first = true
	for (BioDataExternalCode syn in synonyms) {
            if (first) {
                first = false
            }
            else {
		syntext << ', '
            }
	    syntext << syn.code
        }

	String stext = syntext ? '(' + syntext + ')' : ''
	if (stext.length() > 60) {
	    stext = stext.substring(0, 59) + '...'
        }

	stext
    }

    private String formatCategory(String category) {
	category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase()
    }

    /**
     * Gets a SearchKeyword record by id or creates a SearchKeyword for free text.
     */
    private SearchKeyword getSearchKeyword(String id) {
        SearchKeyword keyword
	if (id) {
            // If no matching keywords found, then assume field is 'free text'
	    keyword = SearchKeyword.get(id) ?: new SearchKeyword(
		keyword: id,
		bioDataId: -1,
		uniqueId: 'TEXT:' + id,
		displayDataCategory: 'Text',
		dataCategory: 'TEXT')
        }
	keyword
    }

    def noResult() {
	render view: 'noresult'
    }

    private SearchFilter sessionSearchFilter() {
	session.searchFilter
    }
}
