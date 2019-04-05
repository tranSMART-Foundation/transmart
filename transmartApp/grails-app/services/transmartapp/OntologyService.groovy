package transmartapp

import groovy.util.logging.Slf4j
import i2b2.OntNode

@Slf4j('logger')
class OntologyService {

    static transactional = false

    def i2b2HelperService

    def searchOntology(searchTags, searchTerms, String tagSearchType, String returnType,
	               accessionsToInclude, String searchOperator) {

	List<Map> concepts = []
	List<OntNode> myNodes
	int myCount

	if (!searchTerms) {
	    searchTerms = null
        }
	logger.trace 'searching for: {} of type {} with searchterms:{}', searchTags, tagSearchType, searchTerms?.join(',')

        //Build queries for search terms and accessions to include
	String searchTermsHql = ''
	for (searchterm in searchTerms) {
            searchterm = searchterm?.trim()
            if (searchterm) {
		if (searchTermsHql) {
		    searchTermsHql += ' ' + searchOperator + ' '
                }
		String searchtermWild = '%' + searchterm.toLowerCase().replace("'", "''") + '%'
		searchTermsHql += "lower(o.name) like '" + searchtermWild + "' "
            }
        }
	if (!searchTermsHql) {
	    searchTermsHql = '2=1' //No free-text search terms, so this section of the query is always false
        }

	if (tagSearchType == 'ALL') {
	    String accessionSearch = ''
            if (accessionsToInclude) {
		accessionSearch += ''' OR (o.visualattributes = 'FAS' AND o.sourcesystemcd IN ('''
		accessionSearch += "'" + accessionsToInclude.join("','") + "'"
		accessionSearch += '))'
	    }

	    myCount = OntNode.executeQuery('''
					SELECT COUNT(DISTINCT o.id)
					from OntNode o
					WHERE (''' + searchTermsHql + ')' + '''
					''' + accessionSearch + '''
					  AND o.visualattributes NOT like '%H%' ''')[0]
	    myNodes = OntNode.executeQuery('''
					SELECT o
					from OntNode o
					WHERE (''' + searchTermsHql + ')' + '''
					''' + accessionSearch + '''
					  AND o.visualattributes NOT like '%H%' ''',
					[max: 100])
        }
        else {
	    List<String> allSystemCds = OntNode.executeQuery('''
					SELECT DISTINCT o.sourcesystemcd
					FROM OntNode o JOIN o.tags t
					WHERE t.tag IN (:tagArg)
					  AND t.tagtype =:tagTypeArg''',
					[tagArg: searchTags, tagTypeArg: tagSearchType], [max: 800])

	    myCount = OntNode.executeQuery('''
					SELECT COUNT(DISTINCT o.id)
					from OntNode o
					WHERE o.sourcesystemcd IN (:scdArg)
					AND (''' + searchTermsHql + ')' + '''
					AND o.visualattributes NOT like '%H%' ''',
					[scdArg: allSystemCds])[0]

	    myNodes = OntNode.executeQuery('''
					SELECT o
					from OntNode o
					WHERE o.sourcesystemcd IN (:scdArg)
					AND (''' + searchTermsHql + ')' + '''
					AND o.visualattributes NOT like '%H%' ''',
					[scdArg: allSystemCds], [max: 100])
        }

        //check the security
	Map<String, String> keys = [:]
	for (OntNode node in myNodes) {
	    keys[node.id] = node.securitytoken
	    logger.trace '{} security token: {}', node.id, node.securitytoken
	}

	Map access = i2b2HelperService.getAccess(keys)

	if (returnType == 'JSON') {
	    //build the JSON for the client
	    for (OntNode node in myNodes) {
		concepts << [level: node.hlevel,
			     key: '\\' + node.id.substring(0, node.id.indexOf('\\', 2)) + node.id,
			     name: node.name,
			     synonym_cd: node.synonymcd,
			     visualattributes: node.visualattributes,
			     totalnum: node.totalnum,
			     facttablecolumn: node.facttablecolumn,
			     tablename: node.tablename,
			     columnname: node.columnname,
			     columndatatype: node.columndatatype,
			     operator: node.operator,
			     dimcode: node.dimcode,
			     comment: node.comment,
			     tooltip: node.tooltip,
			     metadataxml: i2b2HelperService.metadataxmlToJSON(node.metadataxml),
			     access: access[node.id]]
            }

	    String resultText
            if (myCount < 100) {
		resultText = 'Found ' + myCount + ' results.'
            }
            else {
		resultText = 'Returned first 100 of ' + myCount + ' results.'
            }

	    return [concepts: concepts, resulttext: resultText]
        }

	if (returnType == 'accession') {
	    List<String> accessions = []
	    for (OntNode node in myNodes) {
                if (!accessions.contains(node.sourcesystemcd)) {
		    accessions << node.sourcesystemcd
                }
            }
            return accessions
        }

	if (returnType == 'path') {
	    List<String> ids = []

	    for (OntNode node in myNodes) {
		String key = '\\' + node.id.substring(0, node.id.indexOf('\\', 2)) + node.id // ?!
                if (!ids.contains(key)) {
		    ids << key
                }
            }
            return ids
        }
    }

    boolean checkSubjectLevelData(String accession) {
	OntNode.findBySourcesystemcd accession?.toUpperCase()
    }

    String getPathForAccession(String accession) {
	OntNode node = OntNode.findBySourcesystemcdAndHlevel(accession.toUpperCase(), 1L)
	('\\' + node.id.substring(0, node.id.indexOf('\\', 2)) + node.id).replace '\\', '\\\\'
    }
}
