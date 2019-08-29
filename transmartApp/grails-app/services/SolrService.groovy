import PatientSampleCollection
import groovy.util.logging.Slf4j
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import org.springframework.beans.factory.annotation.Value

@Slf4j('logger')
class SolrService {

    private static final List<String> ESCAPE_CHARS = ['\\', '+', '-', '!', '(', ')', '{', '}',
	                                              '[', ']', '^', '"', '~', '*', '?', ':'].asImmutable()

    static transactional = false

    @Value('${com.recomdata.solr.baseURL:}')
    private String solrServerUrl

    @Value('${com.recomdata.solr.maxRows:0}')
    private int solrMaxRows

    /**
     * Runs a faceted search on the term provided and return a map with map[term]=facet_count
     * @param solrServer Base URL for the solr server.
     * @param fieldList "|" separated list of fields.
     */
    Map facetSearch(json, Map fieldMap, String coreName) {

	String solrQuery = generateSolrQueryFromJson(json, false)

        //If the query is empty, change it to be 'return all results' in Solr language.
	if (solrQuery == '(())') {
	    solrQuery = '*:*'
	}

	Map<String, String> counts = [:]

	// map of {termType: {term : (count)}} that is passed to the view.
	Map termMap = [:]

	for (currentTerm in fieldMap.columns) {

	    Map args = [path: '/solr/' + coreName + '/select/',
			query: [q: solrQuery,
			        facet: 'true',
			        'facet.field': currentTerm.dataIndex,
			        'facet.sort': 'index']]
	    querySolr(args) { xml ->
		for (outerlst in xml.lst) {
                    if (outerlst.@name == 'facet_counts') {
			for (innerlst in outerlst.lst) {
                            if (innerlst.@name == 'facet_fields') {
				for (innermostlst in innerlst.lst) {
                                    if (innermostlst.@name == currentTerm.dataIndex) {
					for (termItem in innermostlst.int) {
					    counts[termItem.@name.toString()] = termItem.toString()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

	    //The term map goes under the current category.
	    termMap[currentTerm.dataIndex] = [counts: counts, displayName: currentTerm.header]

	    counts.clear()
        }

	termMap
    }

    /**
     * Takes a map in an expected solr layout category:[item:count] and reorders
     * it so that the item specified by the second parameter is on top of that categories list.
     * @param mapToModify A map in the expected solr format category:[item:count,item:count]
     * @param termToFloat the name of the item that should be preserved on top.
     */
    Map floatTopValue(Map mapToModify, String termToFloat) {
	for (termList in mapToModify) {
            def valueRemoved = termList.value.remove(termToFloat)
            if (valueRemoved) {
		termList.value = [(termToFloat): valueRemoved] + termList.value
	    }
        }

	mapToModify
    }

    /**
     * Pulls 'documents' from solr based on the passed in JSON Criteria.
     * @param json looks like {"SearchJSON":{"Pathology":["Liver, Cancer of","Colorectal Cancer"]}}
     * @param resultColumns columns to return
     */
    Map pullResultsBasedOnJson(json, String resultColumns, boolean enforceEmpty, String coreName) {
	String solrQuery
	if (json.Records) {
	    solrQuery = generateSolrQueryFromJsonDetailed(json, enforceEmpty)
        }
        else {
	    solrQuery = generateSolrQueryFromJson(json, enforceEmpty)
        }

        //If the query is empty, abort here.
	if (solrQuery == '(())') {
	    return [results: []]
	}

        //Construct the rest of the query based on the columns we want back and the number of rows we want.
        solrQuery += '&fl=' + resultColumns + '&sort=id desc&rows=' + solrMaxRows

	logger.debug 'pullResultsBasedOnJson - solr Query to be run: {}', solrQuery

        //We want [results:[{'Pathology':'blah','Tissue':'blah'},{'Pathology':'blah','Tissue':'blah'}]]

	Map results = [:]

	Map args = [path: '/solr/' + coreName + '/select/',
		    query: [q: solrQuery]]
	querySolr(args) { xml ->
	    for (resultDoc in xml.result.doc) {

		// the text for each column in the output.
		StringBuilder text = new StringBuilder()

		for (it in resultDoc.str) {
		    if (text) {
			text << '|'
		    }

		    //Add tag name : tag value to the map key.
		    text << it.@name << '?:?:?' << it
		}

		String key = text.toString()

		if (results[key] == null) {
		    results[key] = 0
                }

		results[key]++
            }
        }

	List<Map> resultMaps = []

	Map finalMap = [results: resultMaps]

	//Now that we have this ugly map, convert it to a meaningful map that can be parsed into JSON.
	for (entry in results) {

	    Map map = [:]

	    for (String it in entry.key.toString().tokenize('|')) {
		//Within each '|' there is a funky set of characters that delimits the field:value.
		List<String> keyValueBreak = it.tokenize('?:?:?')
		map[keyValueBreak[0]] = keyValueBreak[1]
            }

	    //Each value of the parent map is actually a count of how many of items matching the key were found.
	    map.count = entry.value

	    resultMaps << map
        }

	finalMap
    }

    /**
     * Runs a solr 'terms' query with a prefix on the provided list of columns and return some results.
     * @param solrServer Base URL for the solr server.
     * @param fieldList ',' separated list of fields that we search for the term within.
     * @param termPrefix search for values like this prefix.
     * @return map that looks like [Pathology:[SomeDisease:22,SomeOtherDisease:33],Tissue:[Skin:32]]
     */
    def suggestTerms(String fieldList, String termPrefix, String numberOfSuggestions, coreName) {

	List<Map> rows = []
	Map result = [rows: rows]

	if (termPrefix) {
	    Map args = [path: '/solr/' + coreName + '/suggest',
			query: ['spellcheck.q': termPrefix,
			        'spellcheck.count': '10']]
	    querySolr(args) { xml ->
		for (outerlst in xml.lst) {
                    if (outerlst?.@name == 'spellcheck') {
			for (innerlst in outerlst.lst.lst.arr) {
			    for (termItem in innerlst.str) {
				rows << [id:  'STR|' + termItem,
					 source: '',
					 keyword: termItem.toString(),
					 synonyms: '',
					 category: 'STR',
					 display: 'Term']
                            }
                        }
                    }
                }
            }
        }

	result
    }

    /**
     * Based on the JSON object, run a query and return only the IDs.
     * @param solrServer Base URL for the solr server.
     * @param json looks like {"SearchJSON":{"Pathology":["Liver, Cancer of","Colorectal Cancer"]}}
     */
    List<String> getIDList(json, String coreName) {
	String solrQuery
	if (json.Records) {
	    solrQuery = generateSolrQueryFromJsonDetailed(json, false)
        }
        else {
	    solrQuery = generateSolrQueryFromJson(json, false)
        }

        //If the query is empty, abort here.
	if (solrQuery == '(())') {
	    return []
	}

        //Construct the rest of the query based on the columns we want back and the number of rows we want.
        solrQuery += '&fl=id&rows=' + solrMaxRows

	List<String> ids = []

	logger.debug 'getIDList - solr Query to be run: {}', solrQuery

	Map args = [path: '/solr/' + coreName + '/select/',
		    query: [q: solrQuery]]
	querySolr(args) { xml ->
	    for (resultDoc in xml.result.doc) {
		for (it in resultDoc.str) {
		    ids << it.toString()
                }
            }
        }

	ids
    }

    /**
     * All the available fields from Solr.
     */
    List<String> getCategoryList(String fieldExclusionList, String coreName) {
	List<String> names = []

        //The luke request handler returns schema data.
	Map args = [path: '/solr/' + coreName + '/schema?wt=xml']
	querySolr(args) { xml ->
	    for (xmlField in xml.schema.fields) {
                if (!(fieldExclusionList.contains(xmlField.name.toString() + '|'))) {
		    names << xmlField.name.toString()
                }
            }
        }

	names
    }

    /**
     * Does the actual work of parsing the JSON data and creating the solr Query with criteria.
     */
    private String generateSolrQueryFromJson(json, enforceEmpty) {
	StringBuilder query = new StringBuilder()

        def allColumnsInGrid
	List columnsInQuery = []

	for (Map.Entry category in json) {
	    String key = category.key.toString()

            //Only add to the query if the category has values.
	    if (category.value &&
		key != 'count' &&
		!key.startsWith('GridColumnList') &&
		!key.startsWith('result_instance_id')) {
		columnsInQuery << category.key

		if (query) {
		    query << ') AND ('
		}

		boolean needOr = false

		for (categoryItem in category.value) {
		    if (needOr) {
			query << ' OR '
		    }

		    query << key << ':"' << escapeCharList(categoryItem.toString()) << '"'

		    needOr = true
                }
            }
	    else if (key.startsWith('GridColumnList')) {
                allColumnsInGrid = category.value
            }
        }

	String solrQuery = '((' + query + '))'

	// After creating the string, loop through the columns in the grid and see if we
	// need to enforce empty values for columns not contained in the search object.
        if (enforceEmpty) {
	    for (currentColumn in allColumnsInGrid[0]) {
                if (!columnsInQuery.contains(currentColumn) && currentColumn.toString() != 'count') {
		    solrQuery += " AND -" + currentColumn + ":[* TO *] "
                }
            }
        }

	if (json != '' && json.result_instance_id) {
	    List<String> ids = PatientSampleCollection.executeQuery('''
				select id
				from PatientSampleCollection
				where resultInstanceId=:resultInstanceId''',
				[resultInstanceId: json.result_instance_id])
	    solrQuery = idListForSampleSpecificQuery(solrQuery, ids)
        }

	solrQuery
    }

    /**
     * Does the actual work of parsing the JSON data and creating the solr Query
     * with criteria. The difference between this and generateSolrQueryFromJson
     * is that this looks for JSON Criteria that needs to be interpreted
     * as (1 AND 2 AND 3) OR (4 AND 5 AND 6).
     * @param json looks like "Records":[{"Pathology":"Rheumatoid Arthritis","Tissue":"Synovial Membrane","DataSet":"GSE13837","DataType":"Gene Expression","Source_Organism":"Homo Sapiens","Sample_Treatment":"Tumor Necrosis Factor","Subject_Treatment":"Not Applicable","BioBank":"No","Timepoint":"Hour 0","count":3}]
     */
    private String generateSolrQueryFromJsonDetailed(json, boolean enforceEmpty) {
        def allColumnsInGrid
	List columnsInQuery

	StringBuilder query = new StringBuilder()

	if (json.GridColumnList) {
	    allColumnsInGrid = json.GridColumnList
        }

	for (Map record in json.Records) {

            columnsInQuery = []

	    if (query) {
		query << ') OR ('
	    }

	    boolean needAnd = false

	    for (Map.Entry category in record) {
		String key = category.key.toString()

		if (category.value &&
		    key != 'count' &&
		    !key.startsWith('GridColumnList') &&
		    !key.startsWith('result_instance_id')) {
		    columnsInQuery << category.key

		    if (needAnd) {
			query << ' AND '
                    }

		    //For each of the values in this category, add onto the search string. There should be only one in the detailed case.
		    for (categoryItem in category.value) {
			query << key << ':"' << escapeCharList(categoryItem.toString()) << '"'
                    }

		    needAnd = true
		}
            }

	    // After creating the string, loop through the columns in the grid and see if we
	    // need to enforce empty values for columns not contained in the search object.
            if (enforceEmpty && allColumnsInGrid) {
		for (currentColumn in allColumnsInGrid) {
                    if (!columnsInQuery.contains(currentColumn) && currentColumn.toString() != 'count') {
			query << " AND -" << currentColumn << ":[* TO *] "
                    }
                }
            }
        }

	String solrQuery = '((' + query + '))'

	if (json != '' && json.result_instance_id) {
	    List<String> ids = PatientSampleCollection.executeQuery('''
					select id
					from PatientSampleCollection
					where resultInstanceId=:resultInstanceId''',
					[resultInstanceId: json.result_instance_id])
	    solrQuery = idListForSampleSpecificQuery(solrQuery, ids)
        }

	solrQuery
    }

    private String idListForSampleSpecificQuery(String solrQuery, List<String> idValuesForSpecificSampleQuery) {
	if (!idValuesForSpecificSampleQuery) {
	    return ' id:(0) '
        }

        if (solrQuery == '(())') {
            solrQuery = ' id:('
        }
        else {
            solrQuery += ' AND id:('
        }

	solrQuery + idValuesForSpecificSampleQuery.join(' OR ') + ')'
    }

    private String escapeCharList(String stringToEscapeIn) {
	for (String s in ESCAPE_CHARS) {
	    stringToEscapeIn = stringToEscapeIn.replace(s, '\\' + s)
	}
	stringToEscapeIn
    }

    Map buildSubsetList(json) {

	Map result = [:]

	for (subset in json) {
	    result[subset.key] = getIDList(subset.value)
        }

        //Make sure subsets are in order.
	result.sort { it.key }
    }

    int getFacetCountForField(String columnToRetrieve, String resultInstanceId, String coreName) {

	List<String> sampleIds = PatientSampleCollection.executeQuery('''
				select id
				from PatientSampleCollection
				where resultInstanceId=:resultInstanceId''',
				[resultInstanceId: resultInstanceId])
	int count = 0

	if (sampleIds) {
	    String solrQuery = 'id:(' + sampleIds.join(' OR ') + ')'

	    logger.debug 'getFacetMapForField - {} - solr Query to be run: {}', columnToRetrieve, solrQuery

	    Map args = [path: '/solr/' + coreName + '/select/',
			query: [q: solrQuery,
			        facet: 'true',
			        rows: '0',
			        'facet.field': columnToRetrieve,
			        'facet.limit': '-1',
			        'facet.mincount': '1']]
	    querySolr(args) { xml ->
		for (outerlst in xml.lst) {
                    if (outerlst.@name == 'facet_counts') {
			for (innerlst in outerlst.lst) {
                            if (innerlst.@name == 'facet_fields') {
				for (innermostItem in innerlst.lst) {
				    for (countValue in innermostItem.int) {
                                        if (countValue.toString() != '0') {
					    count++
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

	count
    }

    private void querySolr(Map args, Closure handler) {
	new HTTPBuilder(solrServerUrl).get(args) { HttpResponseDecorator response, xml ->
	    //We should probably do something with the status.
	    if (response.status != 200) {
		logger.error 'Response status from solr web service call: {}', response.status
	    }

	    handler(xml)
	}
    }
}
