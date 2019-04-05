import grails.converters.JSON
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Value
import org.transmartproject.db.log.AccessLogService

/**
 * @author MMcDuffie
 */
class SampleExplorerController implements InitializingBean {

    AccessLogService accessLogService
    SampleService sampleService
    SolrService solrService

    @Value('${sampleExplorer.idfield:}')
    private String idfield

    @Value('${com.recomdata.solr.maxNewsStories:0}')
    private int maxNewsStories

    @Value('${com.recomdata.solr.numberOfSuggestions:0}')
    private int numberOfSuggestions

    private Map fieldMapping

    /**
     * If we hit just the index, log an event and redirect to the list page.
     */
    def index() {
	accessLogService.report 'SampleExplorer-Summary', 'Sample Explorer summary page'
        redirect(action: 'list')
    }

    //We'll take a result_instance_id and dump all the sample IDs for those patients into another table.
    def generateSampleCohort() {
	sampleService.generateSampleCollection params.result_instance_id
        render true
    }

    //Render the data grid screen based on the samples linked to the result_instance_id.
    def showCohortSamples() {
	render view: 'sampleExplorer', model: [
	    sampleRequestType : 'cohort',
	    columnData        : fieldMapping as JSON,
	    result_instance_id: params.result_instance_id]
    }

    /**
     * Display all the summary links.
     */
    def list() {
	render view: 'sampleExplorer', model: [
	    sampleRequestType: 'search',
	    columnData       : fieldMapping as JSON]
    }

    /**
     * Shows the page that has different groups for each category, and the links to filter with.
     */
    def showTopLevelListPage() {
	// Call the solr service to get a map that looks like category:[item:count].
	// We pass in an empty string because we want all the documents in the solr search.
	Map termMap = solrService.facetSearch('', verifyFieldList(), 'sample')

	render template: 'searchTopLevel', model: [termsMap: termMap]
    }

    def showMainSearchPage() {
	// pass in the top X news stories so we can draw them on the screen.
	render template: 'categorySearch', model: [
	    newsUpdates: NewsUpdate.list(max: maxNewsStories, sort: 'updateDate', order: 'desc')]
    }

    /**
     * Show the box to the west that has the category links with checkboxes.
     */
    def showWestPanelSearch() {
	// looks like category:[item:count].
	Map termMap = solrService.facetSearch(request.JSON.SearchJSON, fieldMapping, 'sample')

	render template: 'categorySearchWithCheckboxes', model: [
	    termsMap: termMap,
	    JSONData: request.JSON.SearchJSON]
    }

    /**
     * Draws the simple HTML page that has the DIV that gets populated by the ExtJS datagrid.
     */
    def showDataSetResults() {
	boolean includeCohortInformation = false

	Map sampleSummary = [:]

        if (request.JSON?.showCohortInformation == 'TRUE') {
            sampleSummary = sampleService.loadSampleStatisticsObject(request.JSON?.result_instance_id)
            includeCohortInformation = true
        }

	render template: 'dataSetResults', model: [
	    includeCohortInformation: includeCohortInformation,
	    sampleSummary: sampleSummary]
    }

    /**
     * Pull a result set from Solr using a query based on the JSON data passed in. Returns results as JSON.
     */
    def getDataSetResults() {
	String selectedResultColumns

        if (request.JSON.PanelNumber) {
            //In the JSON result there is a list of the columns we expect to get back.
	    selectedResultColumns = request.JSON.SearchJSON['GridColumnList' + request.JSON.PanelNumber].join(',').replace('"', '')

            selectedResultColumns = selectedResultColumns.replace('GridColumnList' + request.JSON.PanelNumber, '')
        }
        else {
            //In the JSON result there is a list of the columns we expect to get back.
	    selectedResultColumns = request.JSON.SearchJSON.GridColumnList.join(',').replace('"', '')
        }

	Map results = solrService.pullResultsBasedOnJson(request.JSON.SearchJSON, selectedResultColumns, false, 'sample')

	render(results as JSON)
    }

    /**
     * Returns a JSON object representing the items that match the users search.
     */
    def loadSearch(String query, String callback) {
        //Grab the categories from the form. They might be 'All'.
	String category = query.substring(0, query.indexOf(':'))

        //If all categories are being searched, look in session to get the list.
	if (category == 'all') {
	    category = verifyFieldList().join(',')
	}

        //Grab the value to search for.
	String values = query.substring(query.indexOf(':') + 1)

        //Get the list of possible results.
	Map results = solrService.suggestTerms(category, values, numberOfSuggestions.toString(), 'sample')

        //Render the results as JSON.
	render callback + '(' + (results as JSON) + ')'
    }

    /**
     * Returns a JSON object representing the available solr fields. Used mainly to populate picklists.
     */
    def loadCategories(String callback) {
        //Get the field list from session, or retrieve it from Solr.
	Map fieldList = verifyFieldList()

        //Initialize the map with the all value.
	List<Map> rows = [[value: 'all', label: 'all']]
	Map categoryMap = [rows: rows]

	// put the field list into a format that the pick list expects. Each field gets a label and value entry.
	for (it in fieldList.columns) {
	    rows << [value: it.dataIndex, label: it.header]
        }

	render callback + '(' + (categoryMap as JSON) + ')'
    }

    /**
     * For the samples specified we want to gather all the data residing in SOLR for them.
     */
    def bioBank() {

	List<String> fullColumnList = fieldMapping.columns*.dataIndex
	Map columnPrettyNameMapping = loadFieldPrettyNameMapping()

	Map results = solrService.pullResultsBasedOnJson(request.JSON.SearchJSON,
							 fullColumnList.join(',').replace('"', ''),
							 true, 'sample')

	render template: 'BioBankList', model: [
	    samples: results.results,
	    columnPrettyNameMapping: columnPrettyNameMapping]
    }

    def sampleContactScreen() {
	Map fullDataGroupedByContact = [:]
	Map columnPrettyNameMapping = loadFieldPrettyNameMapping()
	Map contactSampleIdMap = [:]

	if (!idfield) {
	    throw new Exception('SOLR ID Field Configuration not set!')
        }

	// pull all the columns for the data referenced in the Search JSON.
	def fullColumnList = fieldMapping.columns*.dataIndex

	Map allSamples = solrService.pullResultsBasedOnJson(request.JSON.SearchJSON,
							    fullColumnList.join(',').replace('"', ''),
							    true, 'sample')

        //Get the distinct contact fields for this data.
	Map contacts = solrService.pullResultsBasedOnJson(request.JSON.SearchJSON, 'CONTACT', true, 'sample')

	// group the data by the contact field. Loop through the contact data outside, then the actual data inside.
	for (currentContact in contacts.results) {

            if (currentContact.CONTACT) {
                fullDataGroupedByContact[currentContact.CONTACT] = []
                contactSampleIdMap[currentContact.CONTACT] = []
            }
            else {
                currentContact.CONTACT = 'NO_CONTACT'
                fullDataGroupedByContact['NO_CONTACT'] = []
                contactSampleIdMap['NO_CONTACT'] = []
            }

	    // loop through the actual results and group our contacts under their respective contact hash entry.
	    for (currentSample in allSamples.results) {

		if (currentSample[idfield] && (currentSample['CONTACT'] == currentContact.CONTACT)) {
		    contactSampleIdMap[currentContact.CONTACT] << currentSample[idfield]

		    fullDataGroupedByContact[currentContact.CONTACT] << currentSample
                }
		else if (currentSample[idfield] && !currentSample['CONTACT']) {
		    contactSampleIdMap.NO_CONTACT << currentSample[idfield]

		    fullDataGroupedByContact['NO_CONTACT'] << currentSample
                }
            }
        }

	render template: 'sampleContactInfo', model: [
	    allSamplesByContact: fullDataGroupedByContact,
	    contactSampleIdMap: contactSampleIdMap,
	    columnPrettyNameMapping: columnPrettyNameMapping]
    }

    def sampleValidateAdvancedWorkflow() {
	// first retrieve the list of Sample ID's for the dataset we have selected.

	//Get the list of Sample IDs based on the criteria in the JSON object.
	//get an ID list per subset. The JSON we recieved should be [1:[category:[]]]
        def subsetList = request.JSON.SearchJSON

	Map result = [:]

	for (subset in subsetList) {
	    result[subset.key] = solrService.getIDList(subset.value, 'sample')
        }

	render([SampleIdList: result.sort { it.key }] as JSON)
    }

    private Map verifyFieldList() {
        //This field list always has all the fields we want to display.
	List columnConfigsToRemove = []

	Map copy = [:] + fieldMapping

	for (currentColumn in copy.columns) {
            if (!currentColumn.mainTerm) {
		columnConfigsToRemove << currentColumn
            }
        }

	copy.columns.removeAll columnConfigsToRemove

	copy
    }

    private Map loadFieldPrettyNameMapping() {
	Map map = [:]

	for (currentColumn in fieldMapping.columns) {
	    map[currentColumn.dataIndex] = currentColumn.header
	}

	map
    }

    void afterPropertiesSet() {
	fieldMapping = (grailsApplication.config.sampleExplorer.fieldMapping ?: [:]).asImmutable()
	assert fieldMapping: 'Field Mapping Configuration not set!'
    }
}
