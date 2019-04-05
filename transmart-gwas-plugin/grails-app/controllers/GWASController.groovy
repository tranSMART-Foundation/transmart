import com.recomdata.grails.plugin.gwas.GwasWebService
import grails.converters.JSON
import groovy.time.TimeCategory
import groovy.util.logging.Slf4j
import groovy.xml.StreamingMarkupBuilder
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.transmart.biomart.BioAssayAnalysis
import org.transmart.biomart.Experiment
import org.transmart.plugin.shared.SecurityService
import org.transmart.searchapp.BioAnalysisAttributeLineage
import org.transmart.searchapp.SearchTaxonomy
import org.transmart.searchapp.SearchTaxonomyRels
import org.transmartproject.db.log.AccessLogService

import javax.xml.transform.OutputKeys
import javax.xml.transform.Result
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

@Slf4j('logger')
class GWASController {

    private static final List<String> BROWSE_NAMES = [
	'Analyses',
	'Study',
	'Data Type',
	'Region of Interest'].asImmutable()

    private static final Map<String, String> dataTypes =
	[GWAS: 'GWAS',
	 EQTL: 'eQTL',
	 'Metabolic GWAS': 'Metabolic GWAS',
	 'GWAS Fail': 'GWAS Fail'].asImmutable()

    @Autowired private AccessLogService accessLogService
    @Autowired private GwasWebService gwasWebService
    def i2b2HelperService
    def searchKeywordService
    @Autowired private SecurityService securityService

    @Value('${com.rwg.solr.scheme:}')
    private String solrScheme

    @Value('${com.rwg.solr.host:}')
    private String solrHost

    @Value('${com.rwg.solr.path:}')
    private String solrPath

    def index() {}

    /**
     * START: Methods for the faceted search filter
     */

    /*
     * Retrieve the SOLR field name equivalent for a term name.
     * Set to uppercase and replace spaces with underscores.    */

    private String getSOLRCategoryName(String field) {
	field.toUpperCase().replace(' ', '_')
    }

    /**
     * Execute the SOLR query to get the analyses for the trial that match the given search criteria
     * @param solrRequestUrl - the base URL for the SOLR request
     * @param solrQueryParams - the query string for the search, to be passed into the data for the POST request
     * @return analysis Ids
     */
    private List<String> executeSOLRTrialAnalysisQuery(String solrRequestUrl, String solrQueryParams) {

	List<String> analysisIds = []

        def docs   // will store the document nodes from the xml response in here

	URLConnection solrConnection = solrQuery(solrQueryParams)
        if (solrConnection.responseCode == solrConnection.HTTP_OK) {
            def xml

            solrConnection.inputStream.withStream {
		xml = new XmlSlurper().parse(it)
            }

            // retrieve all the document nodes from the xml
            docs = xml.result.find { it.@name == 'response' }.doc
        }
        else {
	    throw new Exception('SOLR Request failed! Request url:' + solrRequestUrl + '  Response code:' +
		solrConnection.responseCode + '  Response message:' + solrConnection.responseMessage)
        }

        solrConnection.disconnect()

        // put analysis id for each document into a list to pass back
        for (docNode in docs) {
	    analysisIds << docNode.str.find { it.@name == 'ANALYSIS_ID' }.text()
        }

	analysisIds
    }

    /**
     *   Execute a SOLR query to retrieve all the analyses for a certain trial that match the given criteria
     */
    private List<Map> querySOLRTrialAnalysis() {
	List sessionFilter = session.solrSearchFilter
	String trialNumber = params.trialNumber

        // create a copy of the original list (we don't want to mess with original filter params)
	List filter = [] + sessionFilter

	filter << 'STUDY_ID;' + trialNumber
	String nonfacetedQueryString = createSOLRNonfacetedQueryString(filter)

        // TODO create a conf setting for max rows
        String solrQueryString = createSOLRQueryString(nonfacetedQueryString, '', '', 10000, false)
	List<String> analysisIds = executeSOLRTrialAnalysisQuery(createSOLRQueryPath(), solrQueryString)

	List<Object[]> results = BioAssayAnalysis.executeQuery('''
		select b.id AS bioAssayAnalysis, b.shortDescription, b.longDescription, b.name,c.sensitiveDesc, b.etlId
		from org.transmart.biomart.BioAssayAnalysis b LEFT JOIN b.ext c
		WHERE b.id in (:ids)
		ORDER BY b.longDescription''', [ids: analysisIds])

        // retrieve the analyses that are of type Time Course by checking the taxonomy
	List<Long> timeCourseAnalyses = BioAnalysisAttributeLineage.executeQuery('''
		select b1.bioAnalysisAttribute.bioAssayAnalysisID
		from org.transmart.searchapp.BioAnalysisAttributeLineage b1
		where b1.bioAnalysisAttribute.bioAssayAnalysisID in (:ids)
		  and lower(b1.ancestorTerm.termName) = lower('Time Course')''',
		[ids: analysisIds])

	List<Map> analysisList = []
        for (r in results) {
	    analysisList << [id: r[0], shortDescription: r[1], longDescription: r[2], name: r[3],
			     sensitiveDesc: r[4], study: r[5], isTimeCourse: timeCourseAnalyses.contains(r[0])]
        }

	analysisList
    }

    private void addDynaNode(SearchTaxonomy parentNode, JSONArray json, boolean isCategory,
                             String categoryName, String uniqueTreeId, JSONObject initialFacetCounts) {

	JSONArray children = new JSONArray()

        // generate the id for use in tree and for link to active terms
        // if there is a link to an active term, use that as id (i.e. search_keyword_id)
        // if not, use the id from the search_taxonomy table prepended with a letter (to ensure that there are no id collisions)
        def id
        if (parentNode?.searchKeywordId) {
            id = parentNode.searchKeywordId
        }
        else {
            id = 'X' + parentNode.id
        }

	Map parent = [
	    termName    : parentNode.termName,
	    id          : id,
	    // create the key that matches what we use in javascript to identify search terms
	    // assuming for now that the category and the category display are the same (with category being all caps); may
	    // need to break this out into separate fields
	    key         : categoryName + '|' + categoryName.toUpperCase() + ';' + parentNode.termName + ';' + id,
	    // if category, then display as folder and don't show checkbox; other levels, not a folder and show checkbox
	    isFolder    : isCategory,
	    hideCheckbox: isCategory,
	    isCategory  : isCategory,
	    categoryName: categoryName + '|' + categoryName.toUpperCase(),
	    // create a uniqueTreeId for each node so we can identify it from it's copies
	    //  (id and key are not unique amongst copies)
	    uniqueTreeId: uniqueTreeId
	]

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
            // get the json object for the category
	    JSONObject jo = initialFacetCounts.get(getSOLRCategoryName(categoryName))

	    String idString = id

	    // retrieve the count for the term id if it exists in the json object, otherwise none found so it's zero
	    int count = jo.has(idString) ? jo.getInt(idString) : 0
	    parent.facetCount = count
	    parent.initialFacetCount = count

            // if the initial count is zero, don't add to tree
            if (count == 0) {
                return
            }

            // include facet count in title for non-category nodes
	    parent.title = "parentNode.termName ($count)"
        }

	int childIndex = 1
        if (parentNode?.children) {
            // recursively add each child
	    for (SearchTaxonomy childNode in parentNode.children) {
		addDynaNode childNode, children, false, categoryName, uniqueTreeId + ':' + childIndex, initialFacetCounts
                childIndex++
            }
        }

	parent.children = children

        // add parent map to json array
        json.put(parent)
    }

    /*
     * Create the JSON string used as the 'children' of the taxonomy DynaTree
     */
    def getDynatree() {

	SearchTaxonomy rootNode = null

        // loop through every node, and link it to its parent and children to create tree
	for (SearchTaxonomy node in SearchTaxonomy.list()) {
	    for (SearchTaxonomyRels rel in SearchTaxonomyRels.list()) {
                if (rel.parent) {   // non root node
                    // check if relationship represents a parent rel for the current node, and if so add the
                    // child to the node's children list
                    if (node.id == rel.parent.id) {
			node.children << rel.child
                    }

                    // check if relationship represents a child rel for the current node, and if so add the
                    // parent to the node's parent list
                    if (node.id == rel.child.id) {
			node.parents << rel.parent
		    }
                }
		else { // root node found
                    rootNode = rel.child
                }
            }
        }

        JSONArray categories = new JSONArray()

        if (rootNode?.children) {

	    List<SearchTaxonomy> acceptableList = []
	    for (SearchTaxonomy categoryNode in rootNode.children) {
		if (acceptableForBrowse(categoryNode)) {
		    acceptableList << categoryNode
		}
	    }
	    rootNode.children = acceptableList
			
	    List<String> categoriesList = []
            // loop thru all children of root and create a list of categories to be used for initial facet search
	    for (SearchTaxonomy categoryNode in rootNode.children) {
                // SOLR equivalent field is all uppercases with underscores instead of spaces
		categoriesList << getSOLRCategoryName(categoryNode.termName)
            }

            // retrieve initial facet counts to be used in tree
            JSONObject initialFacetCounts = getInitialFacetResults(categoriesList)

            // CREATE JSON ARRAY FOR TREE
	    int nodeIndex = 1

            // loop thru all children of root and add to JSON array for categories (addNode will recursively add children)
	    for (SearchTaxonomy categoryNode in rootNode.children) {

                // give each node a unique id within tree (id and key are not necessarily unique)
                // the unique id will be a concatenation of the parent's unique id + the index of this child's index in children list
                // e.g. category nodes will be 1,2,3; their children will be 1:1, 1:2, 1:3, 2:1, ...; their children 1:1:1, 1:1:2, ...
                String uniqueTreeId = nodeIndex

		addDynaNode categoryNode, categories, true, categoryNode.termName, uniqueTreeId, initialFacetCounts
                nodeIndex++
            }

        }

	response.contentType = 'text/json'
	response.outputStream << categories.toString()
    }
	
    /**
     * Determine if the given category node is acceptable as a top level node. Must be supported 
     * by the GUI and by SOLR. As of July 2014, the only categories supported by the GUI are:
     * Analyses, Study, Data Type, Region of Interest, eQTL Transcript Gene; of those only
     * items in SearchTaxonomy domain will be recognized. Further, the SOLR queries must also
     * be considered. See TMART-208 for details.
     */
    private boolean acceptableForBrowse(SearchTaxonomy categoryNode) {
	BROWSE_NAMES.contains categoryNode.termName
    }
	
    /**
     * Create a query string for the category in the form of (<cat1>:'term1' OR <cat1>:'term2')
     */
    private String createCategoryQueryString(String category, String termList) {

        // create a query for the category in the form of (<cat1>:'term1' OR <cat1>:'term2')
	StringBuilder categoryQuery = new StringBuilder()
        for (t in termList.tokenize('|')) {
	    if (categoryQuery) {
		categoryQuery << ' OR '
            }
	    categoryQuery << category << ':"' << cleanForSOLR(t) << '"'
        }

	'(' + categoryQuery + ')'
    }

    /**
     * Create the SOLR query string for the faceted fields (i.e. those that are in tree) that
     *   are not being filtered on
     * It will be of form facet.field=<cat1>&facet.field=<cat2>
     */
    private String createSOLRFacetedFieldsString(List<String> facetFieldsParams) {
	String facetedFields = ''
        // loop through each regular query parameter
        for (ff in facetFieldsParams) {

            //This list should be in a config, but we don't facet on some of the fields.
            if (ff != 'REGION_OF_INTEREST' && ff != 'GENE' && ff != 'SNP' && ff != 'EQTL_TRANSCRIPT_GENE') {
                // skip TEXT search fields (these wouldn't be in tree so throw exception since this should never happen)
                if (ff == 'TEXT') {
                    throw new Exception('TEXT field encountered when creating faceted fields string')
                }

		String ffClause = 'facet.field=' + ff
		if (!facetedFields) {
		    facetedFields = ffClause
                }
                else {
		    facetedFields += '&' + ffClause
                }
            }

        }

	facetedFields
    }

    /**
     * Create the SOLR query string for the faceted fields (i.e. those that are in tree) that are being filtered
     * It will be of form facet=true&facet.field=(!ex=c1)<cat1>&facet.field=(!ex=c2)<cat2>&
     *     fq={!tag=c1}(<cat1>:'term1' OR <cat1>:'term2')&.... )
     * Each category query gets tagged in fq clauses {!tag=c1}, and then the category query is excluded
     *   for determining the facet counts (!ex=c1) in facet.field clauses
     */
    private String createSOLRFacetedQueryString(String[] facetQueryParams) {
	StringBuilder facetedQuery = new StringBuilder()
        for (qp in facetQueryParams) {

            // each queryParam is in form cat1:term1|term2|term3
	    String[] parts = qp.split(';')
	    String category = parts[0]
	    String termList = parts[1]

            // skip TEXT search fields (these wouldn't be in tree so throw exception since this should never happen)
            if (category == 'TEXT') {
                throw new Exception('TEXT field encountered when creating faceted search string')
            }

	    String categoryQueryString = createCategoryQueryString(category, termList)

	    String categoryTag = '{!tag=' + category + '}'
	    String fqClause = 'fq=' + categoryTag + categoryQueryString

	    String categoryExclusion = '{!ex=' + category + '}'
	    String ffClause = 'facet.field=' + categoryExclusion + category

	    String categoryClause = ffClause + '&' + fqClause

	    if (facetedQuery) {
		facetedQuery << '&'
            }
	    facetedQuery << categoryClause
        }

	facetedQuery
    }

    /**
     * Create the SOLR query string for the nonfaceted fields (i.e. those that are not in tree)
     * It will be of form ((<cat1>:'term1' OR <cat1>:'term2') AND ( (<cat2>:'term3') ) AND () .. )
     */
    private String createSOLRNonfacetedQueryString(List<String> queryParams) {
	StringBuilder nonfacetedQuery = new StringBuilder()
        for (qp in queryParams) {

            //Ignore REGIONs here - used later in analysis filter
            if (qp.startsWith('REGION') || qp.startsWith('GENE') || qp.startsWith('SNP') || qp.startsWith('PVALUE') || qp.startsWith('TRANSCRIPTGENE')) {
                continue
            }

            // each queryParam is in form cat1:term1|term2|term3
	    String[] parts = qp.split(';')
	    String category = parts[0]
	    String termList = parts[1]

	    String categoryQueryString = createCategoryQueryString(category, termList)

            // add category query to main nonfaceted query string using ANDs between category clauses
	    if (nonfacetedQuery) {
		nonfacetedQuery << ' AND '
            }
	    nonfacetedQuery << categoryQueryString
        }

        // use all query if no params provided
	if (!nonfacetedQuery) {
	    nonfacetedQuery << '*:*'
        }

	'q=(' + nonfacetedQuery + ')'
    }

    /**
     * Execute the SOLR faceted query
     * @param solrQueryParams - the query string for the faceted search, to be passed into the data for the POST request
     * @param ids if not null, return nothing but add the analysis ids
     * @return JSONObject containing the facet counts
     */
    private JSONObject executeSOLRFacetedQuery(String solrQueryParams, List<Long> ids = null) {

        JSONObject facetCounts = new JSONObject()

        def facetCategoryNodes   // will store the facet category nodes from the xml response in here

	URLConnection solrConnection = solrQuery(solrQueryParams)
        if (solrConnection.responseCode == solrConnection.HTTP_OK) {
            def xml

            solrConnection.inputStream.withStream {
		xml = new XmlSlurper().parse(it)
            }

	    if (ids != null) {
                def analysisIds = xml.result.doc.str.findAll { it.@name == 'ANALYSIS_ID' }
                solrConnection.disconnect()
                for (analysisId in analysisIds) {
		    ids << analysisId.text() as long
                }
		return
            }

            // retrieve all the category nodes for the facet fields (contain subnodes which have the actual counts)
            facetCategoryNodes = xml.lst.find { it.@name == 'facet_counts' }.lst.find {
                it.@name == 'facet_fields'
            }.lst
        }
        else {
	    throw new Exception('SOLR Request failed! Request url:' + createSOLRQueryPath() + '  Response code:' +
		solrConnection.responseCode + '  Response message:' + solrConnection.responseMessage)
        }

        solrConnection.disconnect()

        // put counts for each category/term into a json string to pass back
        for (catNode in facetCategoryNodes) {
            // retrieve the category name from the xml node
	    String catName = catNode.@name

            JSONObject catArray = new JSONObject()   // json object for current category
            for (countNode in catNode.int) {
		String searchKeywordId = countNode.@name
		catArray[searchKeywordId] = countNode.text()
            }

            // add category array object to all objects
	    facetCounts.put(catName, catArray)
        }

	facetCounts
    }

    /**
     *  pretty prints the GPathResult NodeChild
     */
    private String outputFormattedXml(node) {
        def xml = new StreamingMarkupBuilder().bind {
            mkp.declareNamespace('': node.namespaceURI())
            mkp.yield(node)
        }

	Transformer transformer = TransformerFactory.newInstance().newTransformer()
	transformer.setOutputProperty OutputKeys.INDENT, 'yes'

        // figured this out by looking at Xalan's serializer.jar
        // org/apache/xml/serializer/output_xml.properties
	transformer.setOutputProperty '{http\u003a//xml.apache.org/xalan}indent-amount', '2'

	Result result = new StreamResult(new StringWriter())
        transformer.transform(new StreamSource(new ByteArrayInputStream(xml.toString().bytes)), result)

	result.writer.toString()
    }

    /**
     * Create the SOLR query string for the faceted query
     * @param nonfacetedQueryString - the portion of the URL containing the non faceted query string
     * @param facetedQueryString - the portion of the URL containing the faceted query string
     * @param facetedFieldsString - the portion of the URL containing the faceted fields string
     * @param maxRows - max number of result rows to return (default to 0
     */
    private String createSOLRQueryString(String nonfacetedQueryString, String facetedQueryString,
	                                 String facetedFieldsString, int maxRows = 0, boolean facetFlag = true) {
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
     * Create the base URL for the SOLR request
     * @return string containing the base URL for the SOLR query
     */
    private String createSOLRQueryPath() {
	new URI(solrScheme, solrHost, solrPath, '', '').toURL()
    }

    //Get analyses for current SOLR query and store them in session
    def getFacetResultsForTable() {

	List<String> queryParams = request.getParameterValues('q') as List

        // save all the filter params to a session List variable
	List<String> sessionFilterParams = [] + queryParams

	//fq params are also faceted and also filtered on
	sessionFilterParams.addAll request.getParameterValues('fq')

	session.solrSearchFilter = sessionFilterParams

	logger.info 'facet search: {}', params

        // build the SOLR query
	String nonfacetedQueryString = ''
        try {
	    nonfacetedQueryString = createSOLRNonfacetedQueryString(sessionFilterParams)
        }
	catch (e) {
	    logger.error e.message, e
        }

        //TODO Patch job - if this is a *.* query, prevent it from running with a sentinel value
	if (nonfacetedQueryString == 'q=(*:*)') {
	    session.solrAnalysisIds = [-1]
	    render 'NONE'
            return
        }

	List<Long> analysisIds = []
	executeSOLRFacetedQuery nonfacetedQueryString, analysisIds
	session.solrAnalysisIds = analysisIds

	render analysisIds.join(',')
    }

    /**
     * Load the search results for the given search terms (used for AJAX calls)
     * @return JSON object containing facet counts
     */
    def getFacetResults() {

	Date startTime = new Date()

        // q params are filtered on but not faceted
	List<String> queryParams = request.getParameterValues('q') as List

        //fq params are also faceted and also filtered on
	String[] facetQueryParams = request.getParameterValues('fq')

        // save all the filter params to a session List variable
	List<String> sessionFilterParams = [] + queryParams
	sessionFilterParams.addAll facetQueryParams
	session.solrSearchFilter = sessionFilterParams

	logger.info 'facet search: {}', params

        // build the SOLR query
	String nonfacetedQueryString = createSOLRNonfacetedQueryString(queryParams)
	String facetedQueryString = createSOLRFacetedQueryString(facetQueryParams)
	// ff params are faceted, but not filtered on
	String facetedFieldsString = createSOLRFacetedFieldsString(request.getParameterValues('ff') as List)

        String solrQueryString = createSOLRQueryString(nonfacetedQueryString, facetedQueryString, facetedFieldsString)
	JSONObject facetCounts = executeSOLRFacetedQuery(solrQueryString)

	String html = loadSearchResults(facetCounts.STUDY_ID, startTime)

	render([facetCounts: facetCounts, html: html] as JSON)
    }

    /**
     * Load the initial facet results for the tree (no filters)
     * @return JSON object containing facet counts
     */
    private JSONObject getInitialFacetResults(List<String> categoriesList) {
        // initial state of the significant field is checked, so need to add the search field to the SOLR query to get the initial facet coutns
        //  and save the search term to the session variable so that is applied to the query to get the analysis list
	List<String> queryParams = []
	session.solrSearchFilter = queryParams
	logger.info 'Initial facet search: {}', queryParams

	// get the base query string (i.e. "q=(*:*)" since no filters for initial search
	String solrQueryString = createSOLRQueryString(createSOLRNonfacetedQueryString(queryParams),
		'', createSOLRFacetedFieldsString(categoriesList))
	executeSOLRFacetedQuery solrQueryString
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
	render(searchKeywordService.findSearchCategories() as JSON)
    }

    // Return search keywords
    def searchAutoComplete(String category, Long max, String term) {
	category = category ?: 'ALL'
	logger.info 'searchKeywordService.findSearchKeywords: {}', category
	render(searchKeywordService.findSearchKeywords(category, term, max ?: 15) as JSON)
    }

    // Load the search results for the given search terms using the new annotation tables
    // return the html string to be rendered for the results panel
    private String loadSearchResults(studyCounts, Date startTime) {
	Map<Experiment, Integer> exprimentAnalysis = [:] // the trial objects and the number of analysis per trial
	int total = 0 // Running total of analysis to show in the top banner

	boolean studyWithResultsFound = false
	Map<String, Long> secObjs = gwasWebService.getExperimentSecureStudyList()
	for (String studyId in studyCounts.keys().sort()) {

	    int c = studyCounts[studyId].toInteger()
            if (c > 0) {
                studyWithResultsFound = true

		Experiment experiment = Experiment.get(studyId)
		if (!experiment) {
		    logger.warn 'Unable to find an experiment for {}', studyId
                }
                else {
                    if(secObjs.containsKey(experiment.accession)){
			if (gwasWebService.getGWASAccess(experiment.accession) != 'Locked') {
                            // evaluate if user has access rights to this private study
			    exprimentAnalysis[experiment] = c
                            total += c
                        }
                        else {
			    logger.warn 'Restrict access for {}', studyId
                        }
                    }
                    else {
			exprimentAnalysis[experiment] = c
                        total += c
                    }
                }
            }
        }

	if (!exprimentAnalysis) {
            studyWithResultsFound = false
        }

	String html
        if (!studyWithResultsFound) {
	    html = render(template: '/search/noResult').toString()
        }
        else {
	    html = render(template: 'experiments',
			  model: [experiments  : exprimentAnalysis,
			          analysisCount: total,
			          duration     : TimeCategory.minus(new Date(), startTime)]).toString()
        }

	html
    }

    // Load the trial analysis for the given trial
    def getTrialAnalysis(String trialNumber) {
	accessLogService.report 'Loading trial analysis', trialNumber
	render template: 'analysis', model: [aList: querySOLRTrialAnalysis()]
    }

    def getPieChart() {
	render template: 'pie'
    }

    /**
     * Render a UI where the user can pick a data type.
     */
    def browseDataTypesMultiSelect() {
	render template: 'dataTypesBrowseMulti', model: [dataTypes: dataTypes]
    }

    private String cleanForSOLR(String t) {
	t.replace('&', '%26').replace('(', '\\(').replace(')', '\\)')
    }

    private URLConnection solrQuery(String solrQueryParams) {
	URLConnection solrConnection = new URL(createSOLRQueryPath()).openConnection()
	solrConnection.requestMethod = 'POST'
	solrConnection.doOutput = true

	Writer w = new OutputStreamWriter(solrConnection.outputStream)
	w.write solrQueryParams
	w.flush()
	w.close()

	solrConnection
    }
}
