import com.recomdata.search.query.AssayAnalysisDataQuery
import com.recomdata.search.query.ExperimentAssayAnalysisMVQuery
import com.recomdata.search.query.Query
import com.recomdata.tea.TEABaseResult
import com.recomdata.util.ElapseTimer
import groovy.transform.CompileStatic
import org.transmart.AnalysisResult
import org.transmart.AssayAnalysisValue
import org.transmart.ExpAnalysisResultSet
import org.transmart.ExperimentAnalysisFilter
import org.transmart.ExperimentAnalysisResult
import org.transmart.GlobalFilter
import org.transmart.SearchFilter
import org.transmart.biomart.BioAssayAnalysis
import org.transmart.biomart.BioAssayAnalysisData
import org.transmart.biomart.BioAssayAnalysisDataTea
import org.transmart.biomart.BioAssayPlatform
import org.transmart.biomart.BioMarker
import org.transmart.biomart.BioMarkerExpAnalysisMV
import org.transmart.biomart.Experiment

/**
 * @author mmcduffie
 * todo -- make a super class for experimentanalysisqueryservice and trialqueryservice
 */
@CompileStatic
class ExperimentAnalysisQueryService {

    static transactional = false

    /**
     * count experiment with criteria
     */
    int countExperiment(SearchFilter filter) {
        if (filter == null || filter.globalFilter.isTextOnly()) {
	    0
	}
	else {
	    BioAssayAnalysisData.executeQuery(createMVExperimentQuery('COUNT_EXP', filter))[0] as int
        }
    }

    int countExperimentMV(SearchFilter filter) {
        if (filter == null || filter.globalFilter.isTextOnly()) {
	    0
	}
	else if (!filter.expAnalysisFilter.isUsed()) {
	    BioMarkerExpAnalysisMV.executeQuery(createExpAnalysisMVQuery('COUNT_EXP', filter))[0] as int
	}
	else {
	    countExperiment filter
        }
    }

    /**
     * count number of analyses with TEA filtering criteria
     */
    int countTEAAnalysis(SearchFilter filter) {
	if (filter == null || filter.globalFilter.isTextOnly()) {
	    return 0
	}

	GlobalFilter gfilter = filter.globalFilter

	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad')
	query.addTable 'org.transmart.biomart.BioAssayAnalysisDataTea baad'
	query.addCondition " baad.experiment.type='Experiment'"
	query.addCondition ' baad.analysis.teaDataCount IS NOT NULL'
	query.createGlobalFilterCriteria gfilter
	createSubFilterCriteria filter.expAnalysisFilter, query
	query.addSelect 'COUNT(DISTINCT baad.analysis.id) '

	BioAssayAnalysisDataTea.executeQuery(query.generateSQL())[0] as int
    }

    int countAnalysis(SearchFilter filter) {
	if (filter == null || filter.globalFilter.isTextOnly()) {
	    0
	}
	else {
	    BioAssayAnalysisData.executeQuery(createMVExperimentQuery('COUNT_ANALYSIS', filter))[0] as int
	}
    }

    int countAnalysisMV(SearchFilter filter) {
	if (filter == null || filter.globalFilter.isTextOnly()) {
	    0
	}
	else if (!filter.expAnalysisFilter.isUsed()) {
	    BioMarkerExpAnalysisMV.executeQuery(createExpAnalysisMVQuery('COUNT_ANALYSIS', filter))[0] as int
	}
	else {
	    countAnalysis filter
	}
    }

    String createExpAnalysisMVQuery(countType, SearchFilter filter) {
        if (filter == null || filter.globalFilter.isTextOnly()) {
            return ' WHERE 1=0'
        }

	GlobalFilter gfilter = filter.globalFilter

	Query query = new ExperimentAssayAnalysisMVQuery(mainTableAlias: 'baad')
	query.addTable 'org.transmart.biomart.BioMarkerExpAnalysisMV baad'

	query.createGlobalFilterCriteria gfilter
	createSubFilterCriteria filter.expAnalysisFilter, query

	if ('COUNT_EXP' == countType) {
	    query.addSelect 'COUNT(DISTINCT baad.experiment.id) '
        }
	else if ('COUNT_ANALYSIS' == countType) {
	    query.addSelect 'COUNT(DISTINCT baad.analysis.id) '
        }
	else if ('COUNT_ANALYSIS_TEA' == countType) {
	    query.addSelect 'COUNT(DISTINCT baad.analysis.id) '
	    createNPVCondition query
        }
        else {
            query.setDistinct = true
	    query.addSelect ' baad.experiment.id'
	    query.addSelect ' COUNT(DISTINCT baad.analysis.id)'
	    query.addGroupBy'baad.experiment.id'
	    query.addOrderBy ' COUNT(DISTINCT baad.analysis.id) DESC'
        }

	query.generateSQL()
    }

    /**
     * retrieve trials with criteria
     */
    ExpAnalysisResultSet queryExperiment(SearchFilter filter, Map<String, ?> paramMap) {

        if (filter == null || filter.globalFilter.isTextOnly()) {
            return new ExpAnalysisResultSet()
        }

	ElapseTimer elapseTimer = new ElapseTimer()

	GlobalFilter gfilter = filter.globalFilter

	Query query = new ExperimentAssayAnalysisMVQuery(mainTableAlias: 'baad')
	query.addTable 'org.transmart.biomart.BioMarkerExpAnalysisMV baad'

	query.createGlobalFilterCriteria gfilter
	createSubFilterCriteria filter.expAnalysisFilter, query

	query.addSelect ' baad.experiment.id'
	query.addSelect ' COUNT(DISTINCT baad.analysis.id)'
	query.addGroupBy 'baad.experiment.id'

	List<Object[]> result = BioAssayAnalysisData.executeQuery(query.generateSQL(), paramMap ?: [:]) as List<Object[]>
	    elapseTimer.logElapsed 'query Experiment:', true

	List<TEABaseResult> expResult = []
	for (Object[] row in result) {
	    expResult << new ExperimentAnalysisResult(
		experiment: Experiment.get((long) row[0]),
		analysisCount: (long) row[1], groupByExp: true)
	}

	new ExpAnalysisResultSet(expAnalysisResults: expResult, groupByExp: true)
    }

    String createMVExperimentQuery(countType, SearchFilter filter) {
        if (filter == null || filter.globalFilter.isTextOnly()) {
            return ' WHERE 1=0'
        }

	GlobalFilter gfilter = filter.globalFilter

	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad')
	query.addTable 'org.transmart.biomart.BioMarkerExpAnalysisMV baad'

	query.createGlobalFilterCriteriaMV gfilter
	createSubFilterCriteria filter.expAnalysisFilter, query

	if ('COUNT_EXP' == countType) {
	    query.addSelect 'COUNT(distinct baad.experiment.id) '
        }
	else if ('COUNT_ANALYSIS' == countType) {
	    query.addSelect 'COUNT(distinct baad.analysis.id) '
        }
	else if ('COUNT_ANALYSIS_TEA' == countType) {
	    query.addSelect 'COUNT(DISTINCT baad.analysis.id) '
	    createNPVCondition query
        }
	else {
	    query.setDistinct = true
	    query.addSelect ' baad.experiment.id'
	    query.addSelect ' COUNT(DISTINCT baad.analysis.id)'
	    query.addGroupBy 'baad.experiment.id'
	    query.addOrderBy ' COUNT(DISTINCT baad.analysis.id) DESC'
        }

	query.generateSQL()
    }

    /**
     * fetch analysis detail for given experiment
     */
    ExperimentAnalysisResult queryAnalysis(long expId, SearchFilter filter) {

	GlobalFilter gfilter = filter.globalFilter
	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad', setDistinct: true)
	query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad'
	query.addTable 'JOIN baad.featureGroup.markers baad_bm'
	query.addSelect 'baad'
	query.addSelect 'baad_bm'
	query.addCondition 'baad.experiment.id = ' + expId
        // expand biomarkers
	query.createGlobalFilterCriteria gfilter, true
	createSubFilterCriteria filter.expAnalysisFilter, query
	query.addOrderBy 'abs(baad.foldChangeRatio) DESC '
	query.addOrderBy 'baad.rvalue DESC '
	query.addOrderBy 'baad.rhoValue DESC '

	ExperimentAnalysisResult tResult = new ExperimentAnalysisResult(experiment: Experiment.load(expId))
	ElapseTimer stimer = new ElapseTimer()

	if (gfilter.bioMarkerFilters) {
	    List<Object[]> result = BioAssayAnalysisData.executeQuery(query.generateSQL()) as List<Object[]>
		stimer.logElapsed('Query Analysis with biomarker ', true)
	    processAnalysisResult result, tResult
        }
        else {
	    List<Object[]> allAnalysis = getAnalysesForExperiment(expId, filter)
	    for (Object[] row in allAnalysis) {
		long analysisId = (long) row[0]
		int countGene = (int) row[1]
		List<Object[]> result = BioAssayAnalysis.getTopAnalysisDataForAnalysis(analysisId, 50)
		AnalysisResult analysisResult = new AnalysisResult(
		    analysis: BioAssayAnalysis.get(analysisId),
		    bioMarkerCount: countGene)
		tResult.analysisResultList << analysisResult
		processAnalysisResultNoSort result, analysisResult
		stimer.logElapsed 'Query Analysis without biomarker ', true
            }
        }

	tResult
    }

    /**
     *  get ananlysis only
     */
    private List<Object[]> getAnalysesForExperiment(long clinicalTrialId, SearchFilter filter) {
        // need both filters here
	AssayAnalysisDataQuery query = createBaseQuery(filter, filter.expAnalysisFilter)
	query.addSelect 'baad.analysis.id'
	query.addTable 'JOIN baad.featureGroup.markers baad_bm'
	query.addSelect 'COUNT(DISTINCT baad_bm.id)'
	query.addCondition 'baad.experiment.id =' + clinicalTrialId
	query.addGroupBy 'baad.analysis'
	query.addOrderBy 'COUNT(DISTINCT baad_bm.id) DESC'

	BioAssayAnalysisData.executeQuery(query.generateSQL()) as List<Object[]>
    }

    private AssayAnalysisDataQuery createBaseQuery(SearchFilter filter, ExperimentAnalysisFilter subFilter) {
	GlobalFilter gfilter = filter.globalFilter
	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad', setDistinct: true)
	query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad'
	if (filter) {
	    query.createGlobalFilterCriteria gfilter, true
	}
	if (subFilter) {
	    createSubFilterCriteria subFilter, query
	}
	query
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

    /**
     * process experiment analysis
     */
    private void processAnalysisResult(List<Object[]> result, ExperimentAnalysisResult tar) {
	Map<Long, AnalysisResult> analysisResultMap = [:]
	for (Object[] row in result) {
	    BioAssayAnalysisData analysisData = (BioAssayAnalysisData) row[0]
	    BioMarker biomarker = (BioMarker) row[1]
	    AnalysisResult aresult = analysisResultMap[analysisData.analysis.id]
            if (aresult == null) {
		aresult = new AnalysisResult(analysis: BioAssayAnalysis.get(analysisData.analysis.id))
		analysisResultMap[analysisData.analysis.id] = aresult
            }
	    aresult.assayAnalysisValueList << new AssayAnalysisValue(analysisData: analysisData, bioMarker: biomarker)
        }

	tar.analysisResultList.addAll analysisResultMap.values().sort { AnalysisResult a, AnalysisResult b ->
	    a == b ? 0 : (((double) a.size()) / ((double) a.analysis.dataCount)) > (((double) b.size()) / ((double) b.analysis.dataCount)) ? -1 : 1
        }
    }

    /**
     * find experiment platforms
     */
    List<BioAssayPlatform> getPlatformsForExperment(Experiment experiment) {
	BioAssayAnalysisData.executeQuery '''
		SELECT DISTINCT baad.assayPlatform
		FROM org.transmart.biomart.BioAssayAnalysisData baad
		WHERE baad.experiment =?''',
		[experiment]
    }

    List<String> findPlatformOrganizmFilter(SearchFilter filter) {
	GlobalFilter gfilter = filter.globalFilter
	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad', setDistinct: true)
	query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad'
	query.addSelect 'baad.assayPlatform.organism'
	query.addCondition query.mainTableAlias + ".experiment.type='Experiment'"
	query.createGlobalFilterCriteria gfilter, true

	BioAssayAnalysisData.executeQuery query.generateSQL()
    }

    /**
     * load experiment type filter
     */
    List<String> findExperimentTypeFilter() {
	Experiment.executeQuery'''
		SELECT DISTINCT exp.type
		FROM org.transmart.biomart.Experiment exp
		WHERE exp.type IS NOT NULL'''
    }

    /**
     * load experiment design filter
     */
    List<String> findExperimentDesignFilter(SearchFilter filter) {
	GlobalFilter gfilter = filter.globalFilter
	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad', setDistinct: true)
	String alias = query.mainTableAlias + '_dis'
	query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad'
	query.addTable 'org.transmart.biomart.BioDataAttribute bda'
	query.addCondition 'baad.experiment.id = bda.bioDataId'
	query.addSelect 'bda.propertyValue'
	query.addOrderBy 'bda.propertyValue'
	query.addCondition query.mainTableAlias + ".experiment.type='Experiment'"
	query.addCondition "bda.propertyCode='Experiment Design'"
	query.addCondition 'bda.propertyValue IS NOT NULL'
	query.createGlobalFilterCriteria gfilter, true

	BioAssayAnalysisData.executeQuery query.generateSQL()
    }

    /**
     * TEA filter criteria for NPV
     */
    private void createNPVCondition(Query query) {
	query.addCondition 'baad.teaNormalizedPValue<=0.05'
    }

    void createSubFilterCriteria(ExperimentAnalysisFilter expfilter, Query query) {
        if (expfilter.filterDisease()) {
	    String alias = query.mainTableAlias + '_dis'
	    query.addTable 'JOIN ' + query.mainTableAlias + '.experiment.diseases ' + alias
	    query.addCondition alias + '.id = ' + expfilter.bioDiseaseId
        }

        if (expfilter.filterCompound()) {
	    String alias = query.mainTableAlias + '_cpd'
	    query.addTable 'JOIN ' + query.mainTableAlias + '.experiment.compounds ' + alias
	    query.addCondition alias + '.id = ' + expfilter.bioCompoundId
        }

        if (expfilter.filterExpDesign()) {
	    String alias = query.mainTableAlias + '.experiment'
	    query.addTable 'org.transmart.biomart.BioDataAttribute bda'
	    query.addCondition alias + '.id = bda.bioDataId'
	    query.addCondition "bda.propertyValue ='" + expfilter.expDesign + "'"
        }

        if (expfilter.filterSpecies()) {
	    query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad_platform'
	    query.addCondition " baad_platform.assayPlatform.organism ='" + expfilter.species + "'"
	}

        // fold change on BioAssayAnalysisData
        if (expfilter.filterFoldChange()) {
	    query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad_foldChange'
	    String symbol = 'abs(baad_foldChange.foldChangeRatio)' // abs value
	    query.addCondition ' ((' + symbol + ' >=' + expfilter.foldChange + ' )' + ' OR ' + symbol + ' IS NULL )'
        }

        // pvalue on BioAssayAnalysisData
        if (expfilter.filterPValue()) {
	    query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad_pValue'
	    query.addCondition ' (baad_pValue.preferredPvalue <= ' + expfilter.pvalue + ')'
        }
    }
}
