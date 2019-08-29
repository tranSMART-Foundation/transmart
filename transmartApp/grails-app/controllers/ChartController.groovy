import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.jfree.chart.servlet.ServletUtilities
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.UtilService
import org.transmartproject.core.users.User
import org.transmartproject.db.log.AccessLogService

import javax.servlet.ServletException

@Slf4j('logger')
class ChartController {

    AccessLogService accessLogService
    ChartService chartService
    User currentUserBean
    HighDimensionQueryService highDimensionQueryService
    I2b2HelperService i2b2HelperService
    @Autowired private SecurityService securityService
    @Autowired private UtilService utilService

    def index() {}

    def displayChart(String filename) {
	logger.trace 'Trying to display:{}', filename
	if (!filename) {
            throw new ServletException("Parameter 'filename' must be supplied")
        }

        //  This is to prevent access to the rest of the file system
        filename = ServletUtilities.searchReplace(filename, '..', '')

        File file = new File(System.getProperty('java.io.tmpdir'), filename)
        if (!file.exists()) {
	    throw new ServletException("File '" + file.absolutePath + "' does not exist")
        }

	ServletUtilities.sendTempFile file, response
    }

    /**
     * Get the counts for the children of the passed in concept key.
     */
    def childConceptPatientCounts(String concept_key) {
	logger.trace 'Called childConceptPatientCounts action in ChartController'
	logger.trace 'User is:{}', securityService.currentUsername()
	logger.trace 'Requested counts for parent_concept_path={}', concept_key

	Map counts = i2b2HelperService.getChildrenWithPatientCountsForConcept(concept_key)
	Map access = i2b2HelperService.getChildrenWithAccessForUserNew(concept_key)
	logger.trace 'access:{}', access as JSON
	logger.trace 'counts = {}', counts as JSON

	render([counts: counts, accesslevels: access, test1: 'works'] as JSON)
    }

    /**
     * Get the patient count for a concept
     */
    def conceptPatientCount(String concept_key) {
	render i2b2HelperService.getPatientCountForConcept(concept_key).toString()
    }

    /**
     * Get the distribution histogram for a concept
     */
    def conceptDistribution(String concept_key) {

	accessLogService.report 'DatasetExplorer-Set Value Concept Histogram',
	    'Concept:' + concept_key

        // We need to force computation for an empty instance ID
	Map analysis = chartService.getConceptAnalysis(
	    i2b2HelperService.getConceptKeyForAnalysis(concept_key ?: null),
	    omicsParams,
	    [1: [exists: true, instance: ''], 2: [exists: false], commons: [:]],
	    [width: 245, height: 180])

	render analysis.commons.conceptHisto.toString()
    }

    def conceptDistributionForSubset(String concept_key) {

	accessLogService.report 'DatasetExplorer-Set Value Concept Histogram for subset',
	    'Concept:' + params.concept_key

	Map analysis = chartService.getConceptAnalysis(
	    i2b2HelperService.getConceptKeyForAnalysis(concept_key ?: null),
	    omicsParams,
	    chartService.getSubsetsFromRequest(params),
	    [width: 245, height: 180])

	render analysis.commons.conceptHisto.toString()
    }

    def conceptDistributionWithValues(String concept_key) {
	accessLogService.report 'DatasetExplorer-Concept Distribution With Values',
	    'Concept:' + concept_key

        // We retrieve the highdimension parameters from the client, if they were passed
        Map omicsparams = [:]
        params.findAll { k, v ->
            k.startsWith("omics_")
        }.each { k, v ->
            omicsParams[k] = v
        }

	Map analysis = chartService.getConceptAnalysis(
	    i2b2HelperService.getConceptKeyForAnalysis(concept_key ?: null),
	    omicsParams,
	    [1: [exists: true, instance: ''], 2: [exists: false], commons: [:]],
	    [width: 245, height: 180])

	render(analysis as JSON)
    }

    /**
     * Gets an analysis for a concept key and comparison
     */
    def analysis(String concept_key) {

	accessLogService.report 'DatasetExplorer-Analysis by Concept',
	    'RID1:' + params.result_instance_id1 +
	    ' RID2:' + params.result_instance_id2 +
	    ' Concept:' + concept_key

	concept_key = concept_key ?: null

	Map concepts = [:]
	concepts[concept_key] = chartService.getConceptAnalysis(
	    i2b2HelperService.getConceptKeyForAnalysis(concept_key),
	    omicsParams,
	    chartService.getSubsetsFromRequest(params), null)

	render template: 'conceptsAnalysis', model: [concepts: concepts]
    }

    /**
     * Get the basic statistics for the subset comparison and render them
     */
    def basicStatistics() {

	accessLogService.report 'DatasetExplorer-Basic Statistics',
	    'RID1:' + params.result_instance_id1 + ' RID2:' + params.result_instance_id2

	session.removeAttribute 'gridtable'

	Map<Object, Map> subsets = chartService.computeChartsForSubsets(
	    chartService.getSubsetsFromRequest(params))
	Map<String, Map> concepts = chartService.getConceptsForSubsets(subsets)
	concepts.putAll chartService.getHighDimensionalConceptsForSubsets(subsets)

	render template: 'summaryStatistics', model: [
	    subsets: subsets,
	    concepts: concepts]
    }

    def analysisGrid(String concept_key, String result_instance_id1, String result_instance_id2) {

	boolean s1 = result_instance_id1
	boolean s2 = result_instance_id2

	accessLogService.report 'DatasetExplorer-Grid Analysis Drag',
	    'RID1:' + result_instance_id1 + ' RID2:' + result_instance_id2 + ' Concept:' + concept_key

        //XXX: session is a questionable place to store this because it breaks multi-window/tab nav
	ExportTableNew table = (ExportTableNew) session.gridtable
	if (!table) {
            table = new ExportTableNew()
	    if (s1) {
		i2b2HelperService.addAllPatientDemographicDataForSubsetToTable(
		    table, result_instance_id1, 'subset1')
	    }
	    if (s2) {
		i2b2HelperService.addAllPatientDemographicDataForSubsetToTable(
		    table, result_instance_id2, 'subset2')
	    }

            List<String> keys = i2b2HelperService.getConceptKeysInSubsets(result_instance_id1, result_instance_id2)
            Set<String> uniqueConcepts = i2b2HelperService.getDistinctConceptSet(result_instance_id1, result_instance_id2)

	    for (String key in keys) {
		if (!i2b2HelperService.isHighDimensionalConceptKey(key)) {
		    if (s1) {
			i2b2HelperService.addConceptDataToTable(table, key, result_instance_id1)
		    }
		    if (s2) {
			i2b2HelperService.addConceptDataToTable(table, key, result_instance_id2)
		    }
                }
            }

	    List highDimConcepts = highDimensionQueryService.getHighDimensionalConceptSet(
		result_instance_id1, result_instance_id2)
	    for (it in highDimConcepts) {
		if (s1) {
		    highDimensionQueryService.addHighDimConceptDataToTable(
			table, it, result_instance_id1)
		}
		if (s2) {
		    highDimensionQueryService.addHighDimConceptDataToTable(
			table, it, result_instance_id2)
		}
            }
        }

	if (concept_key) {
	    if (omicsParams) {
		omicsParams.concept_key = concept_key
		if (s1) {
		    highDimensionQueryService.addHighDimConceptDataToTable(
			table, omicsParams, result_instance_id1)
		}
		if (s2) {
		    highDimensionQueryService.addHighDimConceptDataToTable(
			table, omicsParams, result_instance_id2)
		}
            }
            else {
		String parentConcept = i2b2HelperService.lookupParentConcept(
		    i2b2HelperService.keyToPath(concept_key))
		Set<String> cconcepts = i2b2HelperService.lookupChildConcepts(
		    parentConcept, result_instance_id1, result_instance_id2)

		List<String> conceptKeys = []
		String prefix = concept_key.substring(0, concept_key.indexOf('\\', 2))

		if (cconcepts) {
		    for (String cc in cconcepts) {
			conceptKeys << prefix + i2b2HelperService.getConceptPathFromCode(cc)
                    }
                }
		else {
		    conceptKeys << concept_key
		}

		for (String ck in conceptKeys) {
		    if (s1) {
			i2b2HelperService.addConceptDataToTable(table, ck, result_instance_id1)
		    }
		    if (s2) {
			i2b2HelperService.addConceptDataToTable(table, ck, result_instance_id2)
		    }
		}
            }
	}

	session.gridtable = table

	render table.toJSONObject().toString(5)
    }

    def reportGridTableExport() {

	ExportTableNew gridTable = session.gridtable
	String exportedVariablesCsv = gridTable.columnMap.entrySet().collectAll { "${it.value.label} (id = ${it.key})" }.join(', ')
	String trialsCsv = gridTable.rows.collectAll { ExportRowNew it -> it.TRIAL }.unique().join(', ')

        accessLogService.report(currentUserBean, 'Grid View Data Export',
				eventMessage: "User (IP: ${request.getHeader('X-FORWARDED-FOR') ?: request.remoteAddr}) just exported" +
				" data for tieal(s) (${trialsCsv}): variables (${exportedVariablesCsv}) measurements for the" +
				" folowing patients set(s): " +
				[params.result_instance_id1, params.result_instance_id2].findAll().join(', '),
				requestURL: request.forwardURI)

        render 'ok'
    }

    def clearGrid() {
	session.removeAttribute 'gridtable'
	session.removeAttribute 'expdsfilename'
	render'grid cleared!'
    }

    def exportGrid() {
	byte[] bytes = ((ExportTableNew) session.gridtable).toCSVbytes()
	utilService.sendDownload response, 'text/csv', 'export.csv', bytes
    }

    private Map getOmicsParams() {
	params.findAll { String k, v -> k.startsWith('omics_') }
    }
}
