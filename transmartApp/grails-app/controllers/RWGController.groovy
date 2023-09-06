import fm.FmFile
import fm.FmFolder
import fm.FmFolderService

import grails.converters.JSON

import groovy.util.logging.Slf4j
import groovy.xml.StreamingMarkupBuilder

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.json.JSONArray
import org.json.JSONObject
import org.transmart.SearchKeywordService
import org.transmart.searchapp.SearchTaxonomy
import org.transmartproject.db.log.AccessLogService

import transmartapp.SolrFacetService

@Slf4j('logger')
class RWGController {

    AccessLogService accessLogService
    FmFolderService fmFolderService
    FormLayoutService formLayoutService
    GeneSignatureService geneSignatureService
    SearchKeywordService searchKeywordService
    SolrFacetService solrFacetService
    TrialQueryService trialQueryService

    GrailsApplication grailsApplication

    def index() {

	def rwgSearchFilter = session.rwgSearchFilter
	def rwgSearchOperators = session.rwgSearchOperators

	[rwgSearchFilter   : rwgSearchFilter ? rwgSearchFilter.join(',,,') : '',
	 rwgSearchOperators: rwgSearchOperators ? rwgSearchOperators.join(';') : '',
	 globalOperator    : session.globalOperator,
	 rwgSearchCategory : session.searchCategory,
	 exportCount       : session.export?.size(),
	 debug             : params.debug]
    }

    def ajaxWelcome() {
        //add a unused model to be able to use the template
	render template: 'welcome', model: [page: 'RWG']
    }

    def searchLog() {
	render([log: session.searchLog] as JSON)
    }

    def updateSearchCategory() {
//	logger.debug 'updateSearchCategory params.id {}', params.id
	session.searchCategory = params.id
	render 'OK'
    }

    /**
     * START: Methods for the faceted search filter
     */

    /*
     * Retrieve the SOLR field name equivalent for a term name
     */

    private String getSOLRCategoryName(String field) {
        // set to uppercase and replace spaces with underscores
	field.toUpperCase().replace ' ', '_'
    }

    /**
     * Add a new node to the taxonomy Dynatree (and recursively add children if any exist).
     * parentNode: Node to add to tree
     * json: JSON array containing the 'children' of the jQuery dynaTree
     * isCategory: boolean indicating whether the node being added is a category
     * categoryName: name of the category (i.e. as stored in database and displayed in tree)
     * uniqueTreeId: unique identifier for the node being added. This will be a concatenation of the parent's unique id + the index of this child's index in children list
     *     e.g. category nodes will be 1,2,3; their children will be 1:1, 1:2, 1:3, 2:1, ...; their children 1:1:1, 1:1:2, ...
     * initialFacetCounts: JSONObject containing the initial facet counts for the nodes in the tree
     */
    private void addDynaNode(SearchTaxonomy parentNode, JSONArray json, boolean isCategory,
                             String categoryName, String uniqueTreeId, JSONObject initialFacetCounts) {
        JSONArray children = new JSONArray()

        // create map for attributes of node
	Map parent = [:]

        // create a custom attribute for term name
	parent.termName = parentNode.termName

        // generate the id for use in tree and for link to active terms
        // if there is a link to an active term, use that as id (i.e. search_keyword_id)
        // if not, use the id from the search_taxonomy table prepended with a letter (to ensure that there are no id collisions)
        def id
        if (parentNode.searchKeywordId) {
            id = parentNode.searchKeywordId
        }
        else {
            id = 'X' + parentNode.id
        }
	parent.id = id

        // create the key that matches what we use in javascript to identify search terms
        // assuming for now that the category and the category display are the same (with category being all caps); may
        // need to break this out into separate fields
	parent.key = categoryName + '|' + categoryName.toUpperCase() + ';' + parentNode.termName + ';' + id

        // if category, then display as folder and don't show checkbox; other levels, not a folder and show checkbox
	parent.isFolder = isCategory
	parent.hideCheckbox = isCategory

        // add custom attributes for each node
	parent.isCategory = isCategory
	parent.categoryName = categoryName + '|' + categoryName.toUpperCase()

        // create a uniqueTreeId for each node so we can identify it from it's copies
        //  (id and key are not unique amongst copies)
	parent.uniqueTreeId = uniqueTreeId

        // Create custom attributes for the facet count for this node, and one for the initial facet
        //   count which will be used to save the value when the tree gets cleared so we don't have to requery
        // Set to -1 for category nodes
        if (isCategory) {
	    parent.facetCount = -1
	    parent.initialFacetCount = -1

            //title is same as term name for categories
	    parent.title = parentNode.termName
        }
        else {
	    JSONObject jo = initialFacetCounts.get(getSOLRCategoryName(categoryName))

	    String idString = id

            // retrieve the count for the term id if it exists in the json object, otherwise
            //  none found so it's zero
	    int count = jo.has(idString) ? jo.getInt(idString) : 0
	    parent.facetCount = count
	    parent.initialFacetCount = count

            // if the initial count is zero, don't add to tree
            if (count == 0) {
                return
            }

            // include facet count in title for non-category nodes
	    parent.title = /${parentNode.termName} (${count})/
        }

	int childIndex = 1
        if (parentNode.children) {
            // recursively add each child
            for (childNode in parentNode.children) {
		addDynaNode childNode, children, false, categoryName, uniqueTreeId + ';' + childIndex, initialFacetCounts
                childIndex++
            }
        }

        // don't add categories without children to tree
	if (isCategory && !children) {
            //Removing this for now, we won't have any children in our tree. We are doing browse popups.
            //return
        }

        // add children to parent map
	parent.children = children

        // add parent map to json array
        json.put(parent)
    }

    // Create the JSON string used as the 'children' of the taxonomy DynaTree
    def getDynatree() {
	render 'Not implemented'
    }

    //Just clear the search filter and render non-null back
    def clearSearchFilter() {
	session.rwgSearchFilter = [:]
	session.rwgSearchOperators = [:]
	render 'OK'
    }

    def getFacetResults(String searchTerms, String searchOperators, String globalOperator, String page) {

	session.folderSearchList = [[], []] //Clear the folder search list

        /*
         * Record this as the latest search and store it in the session
         */
        //Search string is saved in session (for passing between RWG and Dataset Explorer pages)

	String[] searchTermParts = searchTerms?.split(',,,')
	if (searchTermParts && searchTermParts[0] == '') {
	    searchTermParts = null
        }

	String[] searchOperatorParts = searchOperators?.split(';')
	if (searchOperatorParts && searchOperatorParts[0] == '') {
	    searchOperatorParts = null
        }

	session.rwgSearchFilter = searchTermParts
	session.rwgSearchOperators = searchOperatorParts
	session.globalOperator = globalOperator
	session.geneFilter = []

	List<String> searchLog = ['Starting a new search']

	// Pre-processing

        //Convert gene categories into unified category. Our operator will be the last one processed.
	List<List> geneGroups = []
	List<String> categorizedSearchTerms = request.getParameterValues('q') as List
	List<String> processedSearchTerms = []

        // Store search terms for use in folder details
	session.rwgCategorizedSearchTerms = categorizedSearchTerms

        //Separate gene-related search terms into gene groups.
        //Always set geneOperator if this is a gene search term - the last one is the one we want to use.
	String geneOperator = 'or'
	for (String categoryLine in categorizedSearchTerms) {
	    String operator = categoryLine.split('::')[1].toUpperCase()
	    String category = categoryLine.split('::')[0]

	    String categoryName = category.split(':', 2)[0]
	    String[] termList = category.split(':', 2)[1].split('\\|')

	    if (categoryName == 'GENE') {
                //Easy - get each term and add them as gene groups of 1
                for (term in termList) {
		    geneGroups << [term]
                }
                geneOperator = operator
            }
	    else if (categoryName == 'GENELIST' || categoryName == 'GENESIG') {
                for (t in termList) {
		    List expandedList = geneSignatureService.expandGeneList(t)
                    if (expandedList) {
			geneGroups << [expandedList]
                    }
                }
                geneOperator = operator
            }
	    else if (categoryName == 'PATHWAY') {
                for (t in termList) {
		    List expandedList = geneSignatureService.expandPathway(t)
                    if (expandedList) {
			geneGroups << [expandedList + t] //Retain pathways
                    }
                }
                geneOperator = operator

            }
            else {
		processedSearchTerms << categoryLine
            }
        }

        //Now create a new GENE category with the computed groups and the latest operator.
        if (geneGroups) {
	    List geneGroupStrings = []
            for (group in geneGroups) {
		geneGroupStrings << group.join('/')
            }
	    String newGeneCategory = 'GENE:' + geneGroupStrings.join('|') + '::' + geneOperator
	    processedSearchTerms << newGeneCategory
	    session.geneFilter = newGeneCategory
        }

        //If we have no search terms and this is for RWG, just return the top level
	if (!processedSearchTerms && page == 'RWG') {
	    searchLog << 'No search terms found - returning all programs'
	    session.searchLog = searchLog
            //retrieve folders id to expand as opened nodes
	    render template: '/fmFolder/folders', plugin: 'folderManagement', model: [
		folderContentsAccessLevelMap: fmFolderService.getFolderContentsWithAccessLevelInfo(null),
		nodesToExpand               : sessionRwgOpenedNodes()]
            return
        }

	accessLogService.report 'Browse-Search', ''

//	logger.info 'getFacetResults processedSearchTerms {} page {} globalOperator {} searchLog {}',
//	    processedSearchTerms, page, globalOperator, searchLog

	// Run the search!
	Map combinedResult = solrFacetService.getCombinedResults(processedSearchTerms, page, globalOperator, searchLog)
	session.searchLog = combinedResult.searchLog

        /**
         * Organize and display
         */
	if (page == 'RWG') {
	    Map numbers = [PROGRAM: 0, STUDY: 0, ASSAY: 0, ANALYSIS: 0, FOLDER: 0]

//	    logger.info 'getFacetResults RWG combinedResult {}', combinedResult

            if (combinedResult.paths) {
		List<List> pathLists = finalizePathLists(combinedResult.paths)
		session.folderSearchList = pathLists
		String folderSearchString = pathLists[0].join(',') + ',' //Extra , - used to identify search results
		String uniqueLeavesString = pathLists[1].join(',') + ','
		session.searchLog << 'Final folder string: ' + folderSearchString

                //if no accession in search list, calculate number of each folder type:
                def numbersJSON
		if (!searchTerms.contains('|ACCESSION;')) {
                    for (folderName in pathLists[0]) {
			FmFolder folder = FmFolder.findByFolderFullName folderName
                        if (!folder) {
			    logger.info 'No folder with full name {}', folderName
                            continue
                        }
			int c = numbers[folder.folderType] ?: 0
                        numbers[folder.folderType] = c + 1
                    }
                    numbersJSON = new JSONObject(numbers)
                }

                //retrieve folders id to expand as opened nodes
		List<String> nodesToExpand = sessionRwgOpenedNodes()
		List<String> nodesToClose = sessionRwgClosedNodes()

//		logger.info 'getFacetResults RWG nodesToExpand {}', nodesToExpand

		Map<FmFolder, String> folderContentsAccessLevelMap =
		    fmFolderService.getFolderContentsWithAccessLevelInfo(null)
		render template: '/fmFolder/folders', plugin: 'folderManagement', model: [
		    folderContentsAccessLevelMap: folderContentsAccessLevelMap,
		    folderSearchString          : folderSearchString,
		    uniqueLeavesString          : uniqueLeavesString,
		    auto                        : true,
		    resultNumber                : numbersJSON,
		    nodesToExpand               : nodesToExpand,
		    nodesToClose                : nodesToClose]
	    }
	    else {
		session.folderSearchList = [[], []]
		render template: '/fmFolder/noResults', plugin: 'folderManagement',
		    model: [resultNumber: new JSONObject(numbers)]
            }
        }
        else {
            def pathLists = finalizePathLists(combinedResult.paths)
//	    logger.info 'getFacetResults nonRWG pathLists.size {} pathLists[0] {} pathLists[1] {}',
//		pathLists.size(), pathLists[0], pathLists[1]

	    render([searchResults: pathLists[0], uniqueLeaves: pathLists[1]] as JSON)
        }
    }

    private List<List> finalizePathLists(pathList) {
	List uniquePaths = []
	List uniqueLeaves = []
        for (path in pathList) {
            if (!uniquePaths.contains(path)) {
		uniquePaths << path
            }

            if (!uniqueLeaves.contains(path)) {
                //If no other path in the path list starts with this path, it's uniquely a leaf
		boolean childPathFound = false
                for (otherPath in pathList) {
		    if (otherPath.startsWith(path) && otherPath != path) {
                        childPathFound = true
                        break
                    }
                }
                if (!childPathFound) {
		    uniqueLeaves << path
		}
            }
        }

	[uniquePaths, uniqueLeaves]
    }

    /**
     * START: Methods for the keyword search
     */
    // Sets the search filter for the new search.
    def newSearch() {
	session.solrSearchFilter = []
	render ''
    }

    // Return search categories for the drop down
    def getSearchCategories() {
        render searchKeywordService.findSearchCategories() as JSON
    }

    // Return search categories configuration for the drop down
    def getSearchCategoryConfig() {

        List result = []

//	logger.info 'getSearchCategoryConfig start'

	def properties = grailsApplication.config.com.recomdata.category.hide.toProperties().sort()

	properties.eachWithIndex{it, i ->
//	    logger.info 'getSearchCategoryConfig vategory.hide [{}] {} : {}', i, it.key, it.value
	    if(it.value == 'true') {
		result.add([category:it.key, value:'hide'])
	    } else {
		result.add([category:it.key, value:'show'])
	    }
	}

//	logger.info 'getSearchCategoryConfig found {} hide values', result.size()

//	result.eachWithIndex{it,i ->
//	    logger.info 'result[{}] {}', i, it
//	}
	render result as JSON
    }

    def getFilterCategories() {
        render searchKeywordService.findFilterCategories() as JSON
    }

    // Return search keywords
    def searchAutoComplete(String category, String term) {
//	logger.debug 'searchAutoComplete category {} term {}'. category, term 
	render searchKeywordService.findSearchKeywords(
	    category ?: 'ALL', term,
	    params.int('max', 15)) as JSON
    }

    // Load the trial analysis for the given trial
    def getTrialAnalysis(String trialNumber) {
	accessLogService.report 'Loading trial analysis', trialNumber

	render template: '/RWG/analysis', model: [
	    aList: trialQueryService.querySOLRTrialAnalysis(trialNumber, sessionSolrSearchFilter())]
    }

    def getFileDetails(FmFile file) {
	List<FormLayout> layout = formLayoutService.getLayout('file')
	render template: '/fmFolder/fileMetadata', plugin: 'folderManagement', model: [
	    layout: layout,
	    file  : file]
    }

    def solrQuery() {}

    //Execute arbitrary SOLR query, because SOLR's web interface doesn't work
    def executeSolrQuery(String q) {
        // submit request
        def solrRequestUrl = createSOLRQueryPath()
	URLConnection solrConnection = new URL(solrRequestUrl).openConnection()
        solrConnection.requestMethod = 'POST'
        solrConnection.doOutput = true

        // add params to request
	Writer dataWriter = new OutputStreamWriter(solrConnection.outputStream)
	dataWriter.write q
        dataWriter.flush()
        dataWriter.close()

	XmlSlurper slurper = new XmlSlurper()

        // process response
        if (solrConnection.responseCode == solrConnection.HTTP_OK) {
	    def xml = solrConnection.inputStream.withStream { slurper.parse it }

	    render contentType: 'application/xml',
		text: new StreamingMarkupBuilder().bind { mkp.yield xml }
        }
        else {
	    render 'SOLR Request failed! Request url:' + solrRequestUrl +
		'  Response code:' + solrConnection.responseCode +
		'  Response message:' + solrConnection.responseMessage
        }

        solrConnection.disconnect()
    }

    def saveFacetedSearch(String searchTerms, String searchOperators, String globalOperator) {
	session.folderSearchList = [[], []] //Clear the folder search list

        //Search string is saved in session (for passing between RWG and Dataset Explorer pages)

	String[] searchTermParts = searchTerms?.split(',,,')
	if (searchTermParts && searchTermParts[0] == '') {
	    searchTermParts = null
        }

	String[] searchOperatorParts = searchOperators?.split(';')
	if (searchOperatorParts && searchOperatorParts[0] == '') {
	    searchOperatorParts = null
        }

	session.rwgSearchFilter = searchTermParts
	session.rwgSearchOperators = searchOperatorParts
	session.globalOperator = globalOperator
	session.geneFilter = []
	render 'OK'
    }

    def addOpenedNodeRWG(String node) {
	List<String> openedNodes = sessionRwgOpenedNodes()
	List<String> closedNodes = sessionRwgClosedNodes()
	if (closedNodes.grep(node)) {
	    closedNodes.remove node
        }
	else if (!openedNodes.grep(node)) {
	    openedNodes << node
        }
	render 'OK'
    }

    def removeOpenedNodeRWG(String node) {
	List<String> openedNodes = sessionRwgOpenedNodes()
	List<String> closedNodes = sessionRwgClosedNodes()
	if (openedNodes.grep(node)) {
	    openedNodes.remove node
	}
	else {
	    if (!closedNodes.grep(node)) {
		closedNodes << node
            }
        }
	render 'OK'
    }

    def resetOpenedNodes() {//used for RWG and DSE
	session.rwgOpenedNodes = []
	session.dseOpenedNodes = []
	session.rwgClosedNodes = []
	session.dseClosedNodes = []
	render 'OK'
    }

    def addOpenedNodeDSE(String node) {
	List<String> openedNodes = sessionDseOpenedNodes()
	List<String> closedNodes = sessionDseClosedNodes()
	if (closedNodes.grep(node.replace('\\', '\\\\'))) {
	    closedNodes.remove node.replace('\\', '\\\\')
        }
	else if (!openedNodes.grep(node.replace('\\', '\\\\')) && node != 'treeRoot') {
	    openedNodes << node.replace('\\', '\\\\')
	}
	render 'OK'
    }

    def removeOpenedNodeDSE(String node) {
	List<String> openedNodes = sessionDseOpenedNodes()
	List<String> closedNodes = sessionDseClosedNodes()
	if (openedNodes.grep(node.replace('\\', '\\\\'))) {
	    openedNodes.remove node.replace('\\', '\\\\')
        }
	else {
	    if (!closedNodes.grep(node.replace('\\', '\\\\'))) {
		closedNodes << node.replace('\\', '\\\\')
            }
        }
	render 'OK'
    }

    private List<String> sessionSolrSearchFilter() {
	session.solrSearchFilter
    }

    private List<String> sessionDseOpenedNodes() {
	sessionList 'dseOpenedNodes'
    }

    private List<String> sessionDseClosedNodes() {
	sessionList 'dseClosedNodes'
    }

    private List<String> sessionRwgOpenedNodes() {
	sessionList 'rwgOpenedNodes'
    }

    private List<String> sessionRwgClosedNodes() {
	sessionList 'rwgClosedNodes'
    }

    private List<String> sessionList(String attributeName) {
	List<String> list = session[attributeName]
	if (list == null) {
	    list = []
	    session[attributeName] = list
        }
	list
    }
}
