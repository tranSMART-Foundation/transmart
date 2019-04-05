import com.recomdata.search.query.AssayAnalysisDataQuery
import com.recomdata.search.query.Query
import com.recomdata.tea.TEABaseResult
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.slurpersupport.GPathResult
import org.transmart.AnalysisResult
import org.transmart.AssayAnalysisValue
import org.transmart.ExpAnalysisResultSet
import org.transmart.GlobalFilter
import org.transmart.SearchFilter
import org.transmart.TrialAnalysisResult
import org.transmart.TrialFilter
import org.transmart.biomart.BioAssayAnalysis
import org.transmart.biomart.BioAssayAnalysisData
import org.transmart.biomart.BioMarker
import org.transmart.biomart.ClinicalTrial

/**
 * @author mmcduffie
 */
@CompileStatic
class TrialQueryService {

    static transactional = false

    /**
     * count Analysis with criteria
     */
	int countTrial(SearchFilter filter) {
        if (filter == null || filter.globalFilter.isTextOnly()) {
	    0
	}
	else {
	    BioAssayAnalysisData.executeQuery(createQuery('COUNT_EXP', filter))[0] as int
        }
    }

    int countAnalysis(SearchFilter filter) {
        if (filter == null || filter.globalFilter.isTextOnly()) {
	    0
	}
	else {
	    BioAssayAnalysisData.executeQuery(createQuery('COUNT_ANALYSIS', filter))[0] as int
        }
    }

    /**
     * retrieve trials with criteria
     */
    ExpAnalysisResultSet queryTrial(boolean count, SearchFilter filter, Map paramMap) {

        if (filter == null || filter.globalFilter.isTextOnly()) {
	    return null
        }

	List<Object[]> result = BioAssayAnalysisData.executeQuery(
	    createQuery('DATA', filter),
	    paramMap ?: [:]) as List<Object[]>

	    List<TEABaseResult> trialResult = []
	for (Object[] row in result) {
	    trialResult << new TrialAnalysisResult(trial: ClinicalTrial.get((long) row[0]), analysisCount: (long) row[1], groupByExp: true)
        }

	new ExpAnalysisResultSet(expAnalysisResults: trialResult, groupByExp: true)
    }

    String createQuery(String countType, SearchFilter filter) {
        if (filter == null || filter.globalFilter.isTextOnly()) {
            return ' WHERE 1=0'
        }

	GlobalFilter gfilter = filter.globalFilter

	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad')
	query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad '
	query.addTable 'org.transmart.biomart.ClinicalTrial ct '
	query.addCondition 'baad.experiment.id = ct.id '

	query.createGlobalFilterCriteria gfilter
	createTrialFilterCriteria filter.trialFilter, query

        // handle switch scenarios
	if ('COUNT_EXP' == countType) {
	    query.addSelect 'COUNT(DISTINCT baad.experiment.id) '
        }
	else if ('COUNT_ANALYSIS' == countType) {
	    query.addSelect 'COUNT(DISTINCT baad.analysis.id) '
	}
	else if ('DATA' == countType) {
	    query.addTable 'JOIN baad.featureGroup.markers baad_bm'
	    query.addSelect 'DISTINCT baad.experiment.id, COUNT(distinct baad.analysis.id)  '
	    query.addGroupBy ' baad.experiment.id '
	    query.addOrderBy ' COUNT(distinct baad.analysis.id) DESC '
        }
        else {
	    query.addSelect 'DISTINCT baad.experiment.id, COUNT(distinct baad.analysis.id)  '
	    query.addGroupBy ' baad.experiment.id '
	    query.addOrderBy ' COUNT(distinct baad.analysis.id) DESC '
        }

	query.generateSQL()
    }

    /**
     * find distinct trial analyses with current filters
     */
    String createAnalysisIDSelectQuery(SearchFilter filter) {
        if (filter == null || filter.globalFilter.isTextOnly()) {
            return ' SELECT -1 FROM org.transmart.biomart.BioAssayAnalysisData baad WHERE 1 = 1 '
        }

	GlobalFilter gfilter = filter.globalFilter

	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad', setDistinct: true)
	query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad '
	query.addTable 'org.transmart.biomart.ClinicalTrial ct '
	query.addCondition 'baad.experiment.id = ct.id '

	query.createGlobalFilterCriteria gfilter
	createTrialFilterCriteria filter.trialFilter, query

	query.addSelect 'baad.analysis.id'

	query.generateSQL()
    }

    TrialAnalysisResult queryTrialAnalysis(long clinicalTrialId, SearchFilter filter) {

	GlobalFilter gfilter = filter.globalFilter

	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad', setDistinct: true)
	query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad'
	query.addTable 'JOIN baad.featureGroup.markers baad_bm'
	query.addSelect 'baad'
	query.addSelect 'baad_bm'
	query.addCondition 'baad.experiment.id = ' + clinicalTrialId
        // expand biomarkers
        query.createGlobalFilterCriteria(gfilter, true)

	createTrialFilterCriteria filter.trialFilter, query

	TrialAnalysisResult tResult = new TrialAnalysisResult(trial: ClinicalTrial.get(clinicalTrialId))
	if (gfilter.bioMarkerFilters) {
	    List<Object[]> result = BioAssayAnalysisData.executeQuery(query.generateSQL()) as List<Object[]>
		processAnalysisResult result, tResult
        }
        else {
	    List<Object[]> allAnalysis = getAnalysesForExpriment(clinicalTrialId, filter)
	    for (Object[] row in allAnalysis) {
		long analysisId = (long) row[0]
		long countGene = (long) row[1]
		List<Object[]> result = BioAssayAnalysis.getTopAnalysisDataForAnalysis(analysisId, 50)
		AnalysisResult analysisResult = new AnalysisResult(analysis: BioAssayAnalysis.get(analysisId), bioMarkerCount: countGene)
		tResult.analysisResultList << analysisResult
		processAnalysisResultNoSort result, analysisResult
            }
        }

	tResult
    }

    private AssayAnalysisDataQuery createBaseQuery(SearchFilter filter, TrialFilter trialFilter) {
	GlobalFilter gfilter = filter.globalFilter

	AssayAnalysisDataQuery query = new AssayAnalysisDataQuery(mainTableAlias: 'baad', setDistinct: true)
	query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad'
	if (filter) {
	    query.createGlobalFilterCriteria gfilter, true
	}
	if (trialFilter) {
	    createTrialFilterCriteria trialFilter, query
	}
	query
    }

    /**
     *  get ananlysis only
     */
    List<Object[]> getAnalysesForExpriment(clinicalTrialId, SearchFilter filter) {
        // need both filters here
	Query query = createBaseQuery(filter, filter.trialFilter)
	query.addSelect 'baad.analysis.id'
	query.addTable 'JOIN baad.featureGroup.markers baad_bm'
	query.addSelect 'COUNT(DISTINCT baad_bm.id)'
	query.addCondition 'baad.experiment.id =' + clinicalTrialId
	query.addGroupBy 'baad.analysis'
	query.addOrderBy 'COUNT(DISTINCT baad_bm.id) DESC'

	BioAssayAnalysisData.executeQuery query.generateSQL()
    }

    /**
     * process analysis result
     */
    private void processAnalysisResultNoSort(List<Object[]> result, AnalysisResult aresult) {
	for (Object[] row in result) {
	    BioAssayAnalysisData analysisData = (BioAssayAnalysisData) row[0]
	    BioMarker biomarker = (BioMarker) row[1]
	    aresult.assayAnalysisValueList << new AssayAnalysisValue(analysisData: analysisData, bioMarker: biomarker)
        }
    }

    @CompileDynamic
    private void processAnalysisResult(List<Object[]> result, TrialAnalysisResult tar) {
	Map<Long, AnalysisResult> analysisResultMap = [:]

	for (Object[] row in result) {
	    BioAssayAnalysisData analysisData = (BioAssayAnalysisData) row[0]
	    BioMarker biomarker = (BioMarker) row[1]
	    AnalysisResult aresult = analysisResultMap[analysisData.analysisId]
            if (aresult == null) {
                aresult = new AnalysisResult(analysis: analysisData.analysis)
		analysisResultMap[analysisData.analysis.id] = aresult
            }
	    aresult.assayAnalysisValueList << new AssayAnalysisValue(analysisData: analysisData, bioMarker: biomarker)
        }

	tar.analysisResultList.addAll analysisResultMap.values().sort { AnalysisResult a, AnalysisResult b ->
	    a == b ? 0 :
		(((double) a.size()) / ((double) a.analysis.dataCount)) > (((double) b.size()) / ((double) b.analysis.dataCount)) ?
		-1 : 1
	}
    }

    /**
     * trial filter criteria
     */
    void createTrialFilterCriteria(TrialFilter trialfilter, Query query) {

        // disease
        if (trialfilter.hasDisease()) {
	    String alias = query.mainTableAlias + '_dis'
	    query.addTable 'JOIN ' + query.mainTableAlias + '.experiment.diseases ' + alias
	    query.addCondition alias + '.id = ' + trialfilter.bioDiseaseId
        }

        // compound
        if (trialfilter.hasCompound()) {
	    String alias = query.mainTableAlias + '_cpd'
	    query.addTable 'JOIN ' + query.mainTableAlias + '.experiment.compounds ' + alias
	    query.addCondition alias + '.id = ' + trialfilter.bioCompoundId
        }

        // design
        if (trialfilter.hasStudyDesign()) {
	    query.addTable 'org.transmart.biomart.ClinicalTrial ct '
	    query.addCondition 'baad.experiment.id = ct.id '
	    query.addCondition "ct.design = '" + trialfilter.studyDesign + "'"
        }

        // type
        if (trialfilter.hasStudyType()) {
	    query.addTable 'org.transmart.biomart.ClinicalTrial ct '
	    query.addCondition 'baad.experiment.id = ct.id '
	    query.addCondition "ct.studyType = '" + trialfilter.studyType + "'"
        }

        // study phase
        if (trialfilter.hasPhase()) {
	    query.addTable 'org.transmart.biomart.ClinicalTrial ct '
	    query.addCondition 'baad.experiment.id = ct.id '
	    query.addCondition "ct.studyPhase = '" + trialfilter.phase + "'"
        }

	boolean firstWhereItem = true
        StringBuilder s = new StringBuilder()

        // fold change on BioAssayAnalysisData
        if (trialfilter.hasFoldChange()) {
	    firstWhereItem = false
	    s << '( abs(baad.foldChangeRatio) >= ' << trialfilter.foldChange << ' OR baad.foldChangeRatio IS NULL)'
        }

        // preferred p value on BioAssayAnalysisData
        if (trialfilter.hasPValue()) {
	    if (firstWhereItem) {
		s << ' (baad.preferredPvalue <= ' << trialfilter.pvalue << ' )'
            }
            else {
		s << ' AND (baad.preferredPvalue <= ' << trialfilter.pvalue << ' )'
            }
        }
        //		 rvalue on BioAssayAnalysisData
        if (trialfilter.hasRValue()) {
	    if (firstWhereItem) {
		s << ' ((baad.rvalue >= abs(' << trialfilter.rvalue <<
		    ')) OR (baad.rhoValue>=abs(' << trialfilter.rvalue <<
		    ')) OR baad.rhoValue IS NULL)'
            }
            else {
		s << ' AND (baad.rvalue >= abs(' << trialfilter.rvalue <<
		    ')) OR (baad.rhoValue>=abs(' << trialfilter.rvalue <<
		    ')) OR baad.rhoValue IS NULL)'
            }
        }

        // platform filter
        if (trialfilter.hasPlatform()) {
	    if (firstWhereItem) {
		s << " (baad.analysis.assayDataType = '" << trialfilter.platform << "')"
            }
            else {
		s << " AND (baad.analysis.assayDataType = '" << trialfilter.platform << "')"
            }
        }

        // add filter criteria
	if (s) {
	    query.addCondition s.toString()
	}

        // clinical trials
        if (trialfilter.hasSelectedTrials()) {
	    query.addTable 'org.transmart.biomart.ClinicalTrial ct '
	    query.addCondition 'baad.experiment.id = ct.id '
	    query.addCondition 'ct.id in (' + trialfilter.createTrialInclause() + ')'
        }
    }

    /**
     * Execute the SOLR query to get the analyses for the trial that match the given search criteria
     * @param solrRequestUrl - the base URL for the SOLR request
     * @param solrQueryParams - the query string for the search, to be passed into the data for the POST request
     * @return the analysis Ids
     */
    @CompileDynamic
    private List<Long> executeSOLRTrialAnalysisQuery(String solrRequestUrl, String solrQueryParams) {

	List<Long> analysisIds = []

        // submit request
	URLConnection solrConnection = new URL(solrRequestUrl).openConnection()
        solrConnection.requestMethod = 'POST'
        solrConnection.doOutput = true

        // add params to request
	Writer dataWriter = new OutputStreamWriter(solrConnection.outputStream)
        dataWriter.write(solrQueryParams)
        dataWriter.flush()
        dataWriter.close()

	if (solrConnection.responseCode != solrConnection.HTTP_OK) {
	    throw new Exception('SOLR Request failed! Request url:' + solrRequestUrl + '  Response code:' +
				solrConnection.responseCode + '  Response message:' + solrConnection.responseMessage)
        }

	GPathResult xml = solrConnection.inputStream.withStream { new XmlSlurper().parse(it) }
	def docs = xml.result.find { it.@name == 'response' }.doc
        solrConnection.disconnect()

        // put analysis id for each document into a list to pass back
        for (docNode in docs) {
            def analysisIdNode = docNode.str.find { it.@name == 'ANALYSIS_ID' }
	    analysisIds << analysisIdNode.text() as Long
        }

	analysisIds
    }

    /**
     *   Execute a SOLR query to retrieve all the analyses for a certain trial that match the given criteria
     */
    @CompileDynamic
    List<Map> querySOLRTrialAnalysis(String trialNumber, List<String> sessionFilter) {
	RWGController rwgController = new RWGController() // TODO WTF?

        // create a copy of the original list (we don't want to mess with original filter params)
	List<String> filter = [] + sessionFilter

	filter << 'STUDY_ID:' + trialNumber
	String nonfacetedQueryString = rwgController.createSOLRNonfacetedQueryString(filter)

        String solrRequestUrl = rwgController.createSOLRQueryPath()

        // TODO create a conf setting for max rows
        String solrQueryString = rwgController.createSOLRQueryString(nonfacetedQueryString, '', '', 10000, false)
	List<Long> analysisIds = executeSOLRTrialAnalysisQuery(solrRequestUrl, solrQueryString)

	List<Object[]> results = BioAssayAnalysis.executeQuery('''
				select b.id, b.shortDescription, b.longDescription, b.name
				from org.transmart.biomart.BioAssayAnalysis b
				where b.id in (:analysisIds)
				ORDER BY b.longDescription''',
				[analysisIds: analysisIds])

	List<Map> analysisList = []
	for (Object[] row in results) {
	    analysisList << [id: row[0], shortDescription: row[1], longDescription: row[2], name: row[3], isTimeCourse: false]
        }

	analysisList
    }
}
