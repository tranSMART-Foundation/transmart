import com.recomdata.transmart.domain.searchapp.Subset
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.UtilService
import org.transmartproject.core.querytool.Item
import org.transmartproject.core.querytool.Panel
import org.transmartproject.core.querytool.QueryDefinition
import org.transmartproject.db.querytool.QueriesResourceService
import org.transmartproject.db.querytool.QueryDefinitionXmlService

@Slf4j('logger')
class SubsetController {

    @Autowired private QueriesResourceService queriesResourceService
    @Autowired private QueryDefinitionXmlService queryDefinitionXmlService
    @Autowired private SecurityService securityService
    @Autowired private UtilService utilService

    def getQueryForSubset(String subsetId) {
        Subset subset = Subset.get(subsetId)
	Map<String, String> result = [:]

        // We have to bypass core-api implementation tests for user permission
        // But we still need to be coherent in who can retrieve what
        // Publicity and user checks are still necessary
	if (!subset.deletedFlag && (subset.publicFlag || subset.creatingUser == securityService.currentUsername())) {
	    if (subset.queryID1 >= 0) {
		result.query1 = queryDefinitionXmlService.toXml(
		    queriesResourceService.getQueryDefinitionForResult(
			queriesResourceService.getQueryResultFromId(subset.queryID1)))
	    }
	    if (subset.queryID2 >= 0) {
		result.query2 = queryDefinitionXmlService.toXml(
		    queriesResourceService.getQueryDefinitionForResult(
			queriesResourceService.getQueryResultFromId(subset.queryID2)))
	    }
        }

	render(result as JSON)
    }

    def getQueryForResultInstance() {

	Map<String, String> result = [:]

        // We have to bypass core-api implementation tests for user permission
        // But we still need to be coherent in who can retrieve what
        // Publicity and user checks are still necessary

	Long param1 = params.long('1')
	if (param1 >= 0) {
	    result.query1 = queryDefinitionXmlService.toXml(
		queriesResourceService.getQueryDefinitionForResult(
		    queriesResourceService.getQueryResultFromId(param1)))
	}

	Long param2 = params.long('2')
	if (param2 >= 0) {
	    result.query2 = queryDefinitionXmlService.toXml(
		queriesResourceService.getQueryDefinitionForResult(
		    queriesResourceService.getQueryResultFromId(param2)))
	}

	render(result as JSON)
    }

    def save(String description, String study) {
	Subset subset = new Subset(
	    queryID1: params.long('result_instance_id1', -1),
	    queryID2: params.long('result_instance_id2', -1),
	    creatingUser: securityService.currentUsername(),
	    description: description,
	    study: study,
	    publicFlag: params.boolean('isSubsetPublic', false))
        boolean success = false

        try {
            success = subset.save(flush: true)
        }
	catch (e) {
	    logger.error '{}', utilService.errorStrings(subset)
        }

	render([success: success] as JSON)
    }

    def query(String subsetId) {
        Subset subset = Subset.get(subsetId)

	QueryDefinition queryID1 = queriesResourceService.getQueryDefinitionForResult(
            queriesResourceService.getQueryResultFromId(subset.queryID1))

	String displayQuery2
        if (subset.queryID2 != -1) {
	    QueryDefinition queryID2 = queriesResourceService.getQueryDefinitionForResult(
                queriesResourceService.getQueryResultFromId(subset.queryID2))
            displayQuery2 = generateDisplayOutput(queryID2)
        }

	render template: '/subset/query', model: [
	    query1: generateDisplayOutput(queryID1),
	    query2: displayQuery2]
    }

    private String generateDisplayOutput(QueryDefinition qd) {
	StringBuilder result = new StringBuilder()
	for (Panel p in qd.panels) {
	    if (result) {
		result << '<b>' << (p.invert ? 'NOT' : 'AND') << '</b><br/>'
            }
	    for (Item i in p.items) {
		result << i.conceptKey
		if (i.constraint) {
		    result << '( with constraints )'
                }
		result << '<br/>'
            }
        }
        result
    }

    def delete(String subsetId) {
	Subset subset = Subset.get(subsetId)
        subset.deletedFlag = true
        subset.save(flush: true)
        render subset.deletedFlag
    }

    def togglePublicFlag(String subsetId) {
	Subset subset = Subset.get(subsetId)
        subset.publicFlag = !subset.publicFlag
        subset.save(flush: true)
        render subset.publicFlag
    }

    def updateDescription(String subsetId, String description) {
	Subset subset = Subset.get(subsetId)
        subset.description = description
        subset.save(flush: true)
        render 'success'
    }
}
