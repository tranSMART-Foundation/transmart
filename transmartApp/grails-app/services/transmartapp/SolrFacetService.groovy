package transmartapp

import fm.FmFolder
import fm.FmFolderAssociation
import fm.FmFolderService
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult
import groovy.util.slurpersupport.NoChildren
import groovy.util.slurpersupport.NodeChild
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.transmart.biomart.BioAssayAnalysis
import org.transmart.biomart.BioMarker
import org.transmart.biomart.BioMarkerExpAnalysisMV

@Slf4j('logger')
class SolrFacetService {

    static transactional = false

    @Autowired private FmFolderService fmFolderService
    @Autowired private OntologyService ontologyService

    @Value('${com.rwg.solr.browse.path:}')
    private String solrBrowsePath

    @Value('${com.rwg.solr.host:}')
    private String solrHost

    @Value('${com.rwg.solr.scheme:}')
    private String solrScheme

    @Value('${com.rwg.solr.update.path:}')
    private String solrUpdatePath

    private List searchLog = [] //Search log for debug only! Will be shared across all sessions

    Map getCombinedResults(List<String> categoryList, String page, String globalOperator, List<String> passedInSearchLog) {

        String solrRequestUrl = createSOLRQueryPath()

	List<String> searchResultIds = []
        searchLog = passedInSearchLog

	// whether it is the first category. It is used for the search with AND operator:
	boolean firstCategory = true

        //For each category (except Datanode), construct a SOLR query
	for (String category in categoryList) {
	    searchLog << ' - - - Examining category: ' + category
            //Split off the operator with ::
	    String operator = category.split('::')[1].toUpperCase()
            category = category.split('::')[0]

	    String categoryName = category.split(':', 2)[0]
	    String[] termList = category.split(':', 2)[1].split('\\|')

            //If in Browse, we're gathering folder paths. If in Analyze, we want i2b2 paths
            //CategoryResultIds is used to gather IDs returned by this category - always add to this list, don't intersect (producing OR).
	    List<String> categoryResultIds = []

	    // Start HERE if we're looking for metadata (anything other than text)
	    if (categoryName != 'text') {
                //Make this metadata field into a SOLR query
		searchLog << 'Searching for metadata: ' + termList.join(',')

		String categoryQuery = createCategoryQueryString(categoryName, termList, operator)
		String solrQuery = 'q=' + createSOLRQueryString(URLEncoder.encode(categoryQuery), '', '')
		searchLog << solrQuery
		GPathResult xml = executeSOLRFacetedQuery(solrRequestUrl, solrQuery)

                //If browse, convert this to a folder path list - if analyze, a node path list
		if (page == 'RWG') {
		    categoryResultIds.addAll getFolderList(xml)
		    if (categoryName == 'GENE') {
			searchLog << 'Getting browse folders for gene categories...'
			categoryResultIds.addAll getAnalysesForGenes(termList, operator, false)
                    }
                }
                else {
		    categoryResultIds.addAll getNodesByAccession(getAccessions(xml))
		    if (categoryName == 'GENE') {
			searchLog << 'Getting analyses for gene categories...'
			categoryResultIds.addAll getAnalysesForGenes(termList, operator, true)
                    }
                }
            }
	    // Start HERE if we're looking for text - and include datanodes (as OR)
            else {
                //Content: Get the list of terms, and search in text field OR the data nodes
		searchLog << 'Searching for freetext: ' + termList.join(',')

		String categoryQuery = createCategoryQueryString('text', termList, operator)
		String solrQuery = 'q=' + createSOLRQueryString(URLEncoder.encode(categoryQuery), '', '')
		searchLog << solrQuery

		GPathResult xml = executeSOLRFacetedQuery(solrRequestUrl, solrQuery)

                //If browse, convert this to a folder path list - if analyze, a node path list
		if (page == 'RWG') {
		    categoryResultIds.addAll getFolderList(xml)
                }
                else {
		    categoryResultIds.addAll getNodesByAccession(getAccessions(xml))
                }

                //If browse, get the studies from SOLR that correspond to the returned accessions - if analyze, just add the paths.
		if (page == 'RWG') {
		    searchLog << 'Getting accessions for datanode search: ' + termList
                    def ontologyAccessions = ontologyService.searchOntology(null, termList, 'ALL', 'accession', null, operator)

                    if (ontologyAccessions) {
			StringBuilder sb = new StringBuilder()

                        for (accession in ontologyAccessions) {
			    searchLog << 'Got accession from i2b2 search: ' + accession
			    if (sb) {
				sb << ' OR '
                            }
			    sb << 'ACCESSION:"' << accession << '"'
                        }

			String solrQueryString = 'q=(' + sb + ')&facet=false&rows=1000'

			searchLog << 'Searching SOLR for studies with accessions: ' + solrQueryString
			xml = executeSOLRFacetedQuery(solrRequestUrl, solrQueryString)
			categoryResultIds.addAll getFolderList(xml)
                    }
                    else {
			searchLog << 'No accessions found.'
                    }
                }
                else {
		    searchLog << 'Getting paths for datanode search: ' + termList
		    categoryResultIds.addAll ontologyService.searchOntology(
			null, termList, 'ALL', 'path', null, operator)
                }
            }

	    searchLog << 'Category result IDs: ' + categoryResultIds

            //If the master searchResultsIds list is empty, copy this in - otherwise intersect.
            //If we have nothing for a search category during an AND search, return nothing immediately!
	    if (!categoryResultIds && globalOperator == 'AND') {
		searchLog << 'No results for this category during an AND search - stopping.'
                return [paths: [], searchLog: searchLog]
            }

            if (!searchResultIds) {
                if (firstCategory) {
		    searchLog << 'Starting search results list with the above IDs.'
                    searchResultIds = categoryResultIds
                    firstCategory = false
                }
                else {
		    searchLog << 'Starting search results list with empty list.'
                    searchResultIds = []
                }
            }
            else {
		searchLog << 'Search results so far are these IDs: ' + searchResultIds
		if (globalOperator == 'AND') {
		    searchLog << 'Doing hierarchical intersect for AND search.'
                    searchResultIds = hierarchicalIntersect(searchResultIds, categoryResultIds)
                }
                else {
		    searchLog << 'Combining for OR search.'
		    searchResultIds.addAll categoryResultIds
                }
		searchLog << 'Search results after combining: ' + searchResultIds
	    }
        }

//	logger.info 'getCombinedResults paths {} searchLog {}', searchResultIds, searchLog

        //return the complete list of folder/i2b2 paths!
	[paths: searchResultIds, searchLog: searchLog]
    }

    private List<String> getAnalysesForGenes(String[] termList, String operator, boolean convertToNodes) {
        //We get a list of genes here - slash-delimited for OR.
        //For each set of terms, create a list then check against materialized view.
	List<BioAssayAnalysis> analysisResults = []

	for (String geneList in termList) {
	    searchLog << 'Getting analyses for genes: ' + geneList
	    List<BioMarker> bioMarkers = []
	    for (String uid in geneList.split('/')) {
		bioMarkers << BioMarker.findByUniqueId(uid)
	    }
	    List<BioMarkerExpAnalysisMV> result = BioMarkerExpAnalysisMV.createCriteria().list {
		'in'('marker', bioMarkers)
            }

	    searchLog << 'Found ' + result.size() + ' analysis matches'

            //Union or intersection, as needed by AND/OR
	    if (operator == 'OR') {
		analysisResults.addAll result*.analysis
            }
            else {
                if (!result) {
                    return []
                }

                if (!analysisResults) {
                    analysisResults = result*.analysis
                }
                else {
		    List<BioAssayAnalysis> newAnalyses = result*.analysis
                    //Manually intersect
		    List<BioAssayAnalysis> newResults = []
		    for (BioAssayAnalysis aResult in analysisResults) {
			for (BioAssayAnalysis newResult in newAnalyses) {
                            if (aResult.id == newResult.id) {
				newResults << aResult
                            }
                        }
                    }
                    analysisResults = newResults
                    if (!analysisResults) {
                        return []
                    }
                }
            }
        }

	searchLog << 'Final analysis ID list: ' + (analysisResults*.id).join(', ')

        //Convert to folder UIDs, then convert to nodes if needed
	List<FmFolder> folders = []
	for (BioAssayAnalysis result in analysisResults) {
	    FmFolderAssociation folderAssoc = FmFolderAssociation.findByObjectUid(result.uniqueId.uniqueId)
            if (folderAssoc) {
		FmFolder folder = folderAssoc.fmFolder
		if (folder.activeInd) {
		    folders << folder
		}
            }
        }

	if (!convertToNodes) {
	    return folders*.folderFullName
        }

	List<String> accessions = []
	for (FmFolder fmFolder in folders) {
	    searchLog << 'Finding associated accession for folder: ' + fmFolder.folderFullName
	    String accession = fmFolderService.getAssociatedAccession(fmFolder)
	    if (accession) {
		searchLog << 'Got accession: ' + accession
		accessions << accession
            }
            else {
		searchLog << 'No accession found'
            }
        }

	getNodesByAccession accessions
    }

    private List<String> hierarchicalIntersect(List<String> searchResults, List<String> categoryResults) {

	List<String> newSearchResults = []

        //Add both sets of results to a map - folder/annotation.
	Map<String, List<String>> oldMap = [:]
	Map<String, List<String>> newMap = [:]
	for (String s in searchResults) {
	    oldMap[s] = []
        }
	for (String c in categoryResults) {
	    newMap[c] = []
        }

	// Iterate over both maps, annotating items according to whether old result is a superset,
	// new result is a superset/match, or a conflict is created.

	for (String nk in newMap.keySet()) {
	    for (String ok in oldMap.keySet()) {
                if (nk.startsWith(ok)) {
		    newMap[nk] << 'N'
		    oldMap[ok] << 'N'
		}
		else if (ok.startsWith(nk)) { //Equal will have been handled above
		    newMap[nk] << 'O'
		    oldMap[ok] << 'O'
                }
            }
        }

        //Take Os from old keys, Ns from new keys
	for (nk in newMap.keySet()) {
            if (newMap[nk].contains('N') && !newSearchResults.contains(nk)) {
		newSearchResults << nk
            }
        }
	for (ok in oldMap.keySet()) {
            if (oldMap[ok].contains('O') && !newSearchResults.contains(ok)) {
		newSearchResults << ok
            }
        }

	newSearchResults
    }

    private List<String> getNodesByAccession(List<String> accessions) {
        if (!accessions) {
            return []
        }

        //If we have any accessions, return the node paths from i2b2 (on the study level)
	searchLog << 'Finding study paths in i2b2 with these accessions: ' + accessions
	List<String> results = ontologyService.searchOntology(
	    null, null, 'ALL', 'path', accessions, '')
	searchLog << 'Got paths: ' + results
	results
    }

    private List<String> getAccessions(xml) {
	searchLog << 'Getting accessions from SOLR search results'

	List<String> accessions = []

	for (node in xml.result.doc) {

	    String folderId
            //Use 'folder' if this is a file result, 'id' otherwise
            def folderNode = node.str.findAll { it.@name == 'folder' }
            if (folderNode.size() > 0) {
                folderId = folderNode.text()
		searchLog << 'Got folder ID from SOLR file result: ' + folderId
            }
            else {
                def idNode = node.str.findAll { it.@name == 'id' }
                if (idNode.size() > 0) {
                    folderId = idNode.text()
		    searchLog << 'Got folder ID from SOLR folder result: ' + folderId
                }
                else {
                    logger.error 'SolrFacetService.getAccessions: result node does not contain an id or folder'
                }
            }

	    FmFolder fmFolder = FmFolder.findByUniqueId(folderId)
	    searchLog << 'Finding associated accession for folder: ' + folderId
	    String accession = fmFolderService.getAssociatedAccession(fmFolder)
            if (accession) {
		searchLog << 'Got accession: ' + accession
		accessions << accession
            }
            else {
		searchLog << 'No accession found'
            }
        }

	accessions
    }

    private List<String> getFolderList(GPathResult xml) {

        //retrieve all folderUIDs from the returned data

	List<String> folderSearchList = []
	for (node in xml.result.doc) {

	    String folderId
            //Use 'folder' if this is a file result, 'id' otherwise
            def folderNode = node.str.findAll { it.@name == 'folder' }
            if (folderNode.size() > 0) {
                folderId = folderNode.text()
		searchLog << 'Got folder ID from SOLR file result: ' + folderId
            }
            else {
                def idNode = node.str.findAll { it.@name == 'id' }
                if (idNode.size() > 0) {
                    folderId = idNode.text()
		    searchLog << 'Got folder ID from SOLR folder result: ' + folderId
                }
                else {
                    logger.error 'SolrFacetService.getFolderList: result node does not contain an id or folder'
                }
            }

	    FmFolder fmFolder = FmFolder.findByUniqueId(folderId)
	    if (fmFolder) {
		folderSearchList << fmFolder?.folderFullName
//		logger.info 'getFolderList folderId {} name {}', folderId, fmFolder?.folderFullName
            }
            else {
		logger.error 'No folder found for unique ID: {}', folderId
            }
        }

//	logger.info 'getFolderList result {} searchLog {}', folderSearchList, searchLog

	folderSearchList
    }

    /**
     * Create a query string for the category in the form of (<cat1>:'term1' OR <cat1>:'term2')
     */
    private String createCategoryQueryString(String category, String[] termList, String operator) {

        // create a query for the category in the form of (<cat1>:'term1' OR <cat1>:'term2')
	StringBuilder categoryQuery = new StringBuilder()
	for (String t in termList) {
            t = cleanForSOLR(t)

	    String pathwayInGeneSearch = null

            //If searching on text and we have no spaces (not a phrase search), add wildcards instead of quote marks
	    if (category == 'text') {
		if (t.contains(' ')) {
		    t = '"' + t.toLowerCase() + '"'
                }
                else {
		    t = '*' + t.toLowerCase() + '*'
                }
            }
	    else if (category == 'GENE') {
                //GENE may have individual genes separated by slashes. OR these, and quote each individual one
                //If this is a pathway, flag it
		List<String> geneList = []
                for (g in t.split('/')) {
                    if (g.startsWith('PATHWAY')) {
                        pathwayInGeneSearch = g
                    }
                    else {
			geneList << '"' + g + '"'
                    }
                }
                t = '(' + geneList.join(' OR ') + ')'
            }
            else {
		t = '"' + t + '"'
            }

	    String queryTerm

            //Special case for pathways in a gene search
	    if (category == 'GENE' && pathwayInGeneSearch) {
		queryTerm = '(PATHWAY:("' + pathwayInGeneSearch + '") OR GENE:' + t + ')'
            }
            else {
		queryTerm = category + ':' + t
            }

	    if (categoryQuery) {
		categoryQuery << ' ' << operator << ' ' << queryTerm
            }
            else {
		categoryQuery << queryTerm
            }
        }

        // enclose query clause in parens
	'(' + categoryQuery + ')'
    }

    String cleanForSOLR(String t) {
	t.replace('&', '%26').replace('(', '\\(').replace(')', '\\)')
    }

    /**
     * Create the base URL for the SOLR request
     * @return string containing the base URL for the SOLR query
     */
    private String createSOLRQueryPath() {
	new URI(solrScheme, solrHost, solrBrowsePath, '', '').toURL()
    }

    private String createSOLRUpdatePath() {
	new URI(solrScheme, solrHost, solrUpdatePath, '', '').toURL()
    }

    /**
     * Create the SOLR query string for the faceted query
     * @param nonfacetedQueryString - the portion of the URL containing the non faceted query string
     * @param facetedQueryString - the portion of the URL containing the faceted query string
     * @param facetedFieldsString - the portion of the URL containing the faceted fields string
     * @param maxRows - max number of result rows to return (default to 0
     * @return the SOLR query
     */
    private String createSOLRQueryString(nonfacetedQueryString, facetedQueryString, facetedFieldsString,
	                                 int maxRows = 1000, boolean facetFlag = false) {
	String solrQuery = nonfacetedQueryString + '&facet=' + facetFlag + '&rows=' + maxRows

	if (facetedQueryString) {
	    solrQuery += '&' + facetedQueryString
        }

	if (facetedFieldsString) {
	    solrQuery += '&' + facetedFieldsString
        }

	solrQuery
    }

    /**
     * Execute the SOLR faceted query
     * @param solrRequestUrl - the base URL for the SOLR request
     * @param solrQueryParams - the query string for the faceted search, to be passed into the data for the POST request
     * @return JSONObject containing the facet counts
     */
    private GPathResult executeSOLRFacetedQuery(String solrRequestUrl, String solrQueryParams) {

//	logger.debug solrQueryParams

        // submit request
	URLConnection solrConnection = new URL(solrRequestUrl).openConnection()
        solrConnection.requestMethod = 'POST'
        solrConnection.doOutput = true

        // add params to request
	Writer dataWriter = new OutputStreamWriter(solrConnection.outputStream)
	dataWriter.write solrQueryParams
	dataWriter.write '&fl=id,folder'
        dataWriter.flush()
        dataWriter.close()

        // process response
        if (solrConnection.responseCode == solrConnection.HTTP_OK) {
	    def xml = solrConnection.inputStream.withStream { InputStream it -> new XmlSlurper().parse(it) }
            solrConnection.disconnect()
            return xml
        }

	throw new Exception('SOLR Request failed! Request url:' + solrRequestUrl +
			    '  Response code:' + solrConnection.responseCode +
			    '  Response message:' + solrConnection.responseMessage)
    }

    void reindexFolder(folderUid, String folderType = '') {

	String solrRequestUrl = createSOLRUpdatePath()
	String solrUpdateParams = 'command=full-import&commit=true&clean=false&uid=' + folderUid
        if (folderType) {
            solrUpdateParams += '&entity=' + folderType
        }

        // submit request
	URLConnection solrConnection = new URL(solrRequestUrl).openConnection()
        solrConnection.requestMethod = 'POST'
        solrConnection.doOutput = true

        // add params to request
	Writer dataWriter = new OutputStreamWriter(solrConnection.outputStream)
	dataWriter.write solrUpdateParams
        dataWriter.flush()
        dataWriter.close()

        //If HTTP OK, return success
        if (solrConnection.responseCode == solrConnection.HTTP_OK) {
            solrConnection.disconnect()
        }
        else {
	    logger.error 'SOLR update failed! Request url:{}  Response code:{}  Response message:{}',
		solrRequestUrl, solrConnection.responseCode, solrConnection.responseMessage
	}
    }

    Map getSearchHighlight(FmFolder folder, List<String> categoryList) {

        String textSearch = categoryList?.find { it.startsWith('text:') }
	if (!textSearch) return

        // Parse the search terms into an operator, category and term list
	String operator = textSearch.split('::')[1].toUpperCase()
        textSearch = textSearch.split('::')[0]
	String categoryName = textSearch.split(':', 2)[0]
	String[] termList = textSearch.split(':', 2)[1].split('\\|')

        // Construct search query (with highlight parameters)
        String url = createSOLRQueryPath()
        String categoryQuery = createCategoryQueryString(categoryName, termList, operator)
        String solrQuery = 'q=' + createSOLRQueryString(URLEncoder.encode(categoryQuery), '', '')
        String highlight = 'hl=true&hl.fl=title+description&hl.fragsize=0' +
            '&hl.simple.pre=<mark><b>&hl.simple.post=</b></mark>'
        String parameters = [solrQuery, highlight].join('&')

        // Execute search
	NodeChild xml = executeSOLRFacetedQuery(url, parameters)

	String hlTitle
	String hlDescription
	List<String> hlFileIds = []
        // Search the response for the current folder and matching file ids
        def highlighting = xml.lst.find { it.@name == 'highlighting' }
        if (!(highlighting instanceof NoChildren)) {
	    for (NodeChild match in highlighting.lst) {
                String matchId = match.@name.text()
                if (matchId == folder.uniqueId) {
                    // Extract the highlighted title & description from the result
                    def hlTitleNode = match.arr.find { it.@name == 'title' }
                    if (!(hlTitleNode instanceof NoChildren)) {
                        hlTitle = hlTitleNode.str[0].text()
                    }
                    def hlDescriptionNode = match.arr.find { it.@name == 'description' }
                    if (!(hlDescriptionNode instanceof NoChildren)) {
                        hlDescription = hlDescriptionNode.str[0].text()
                    }
                }
                if (matchId.startsWith('FIL')) {
                    // File match found; add it for highlighting in filesTable
		    hlFileIds << matchId
                }
            }
        }

	[title      : hlTitle,
         description: hlDescription,
	 fileIds    : hlFileIds]
    }
}
