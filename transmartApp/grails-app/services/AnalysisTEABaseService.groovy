import com.recomdata.genesignature.TEAScoreManager
import com.recomdata.search.query.AssayAnalysisDataQuery
import com.recomdata.search.query.AssayAnalysisDataTeaQuery
import com.recomdata.search.query.Query
import groovy.util.logging.Slf4j
import org.transmart.AnalysisResult
import org.transmart.AssayAnalysisValue
import org.transmart.ExpAnalysisResultSet
import org.transmart.GlobalFilter
import org.transmart.KeywordSet
import org.transmart.SearchFilter
import org.transmart.biomart.BioAssayAnalysis
import org.transmart.biomart.BioAssayAnalysisData
import org.transmart.biomart.BioAssayAnalysisDataTea
import org.transmart.biomart.BioMarker
import org.transmart.biomart.BioMarkerCorrelationMV
import org.transmart.searchapp.SearchBioMarkerCorrelFastMV

/**
 * @author jliu
 */
@Slf4j('logger')
class AnalysisTEABaseService {

    static transactional = false

    /**
     * count analysis with criteria
     */
    int countAnalysis(SearchFilter filter) {
        if (filter == null || filter.globalFilter.isTextOnly()) {
	    0
	}
	else {
	    BioAssayAnalysisData.executeQuery(createCountQuery(filter))[0] as int
        }
    }

    /**
     * retrieve trials with criteria
     */
    ExpAnalysisResultSet queryExpAnalysis(SearchFilter filter) {
        if (filter == null || filter.globalFilter.isTextOnly()) {
            return null
	}

	GlobalFilter gfilter = filter.globalFilter
        def tResult = createResultObject()

	if (gfilter.bioMarkerFilters) {
	    Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad', setDistinct: true)
	    query.addTable 'org.transmart.biomart.BioAssayAnalysisDataTea baad'
	    query.addTable 'JOIN baad.featureGroup.markers baad_bm'
	    query.addCondition " baad.experimentType='" + getExpType() + "'"

	    query.addSelect 'baad'
	    query.addSelect 'baad_bm'
	    query.addSelect 'baad.experiment.id'
	    query.addSelect 'baad.experiment.accession'

            // expand biomarkers
	    query.createGlobalFilterCriteria gfilter, true
	    createSubFilterCriteria filter, query

	    List<Object[]> result = BioAssayAnalysisDataTea.executeQuery(query.generateSQL())

            // get up/down info from mv for all biomarkers
	    KeywordSet biomarkerFilters = gfilter.bioMarkerFilters
	    String mids = biomarkerFilters.keywordDataIdString
	    List<Long[]> updownResult = []

            // filter contain gene sig or list?
	    if (gfilter.geneSigListFilters) {

                // switch for gene sig or list
		String dynamicValuesQuery
		if (gfilter.geneSignatureFilters) {
		    dynamicValuesQuery = '''
							SELECT DISTINCT sbmcmv.assocBioMarkerId, sbmcmv.valueMetric
							FROM org.transmart.searchapp.SearchBioMarkerCorrelFastMV sbmcmv
							WHERE sbmcmv.domainObjectId in (''' + mids + ')'
                }
                else {
                    // always up regulated for gene list
		    dynamicValuesQuery = '''
							SELECT DISTINCT sbmcmv.assocBioMarkerId, 1 as valueMetric
							FROM org.transmart.searchapp.SearchBioMarkerCorrelFastMV sbmcmv
							WHERE sbmcmv.domainObjectId in (''' + mids + ')'
                }
		updownResult.addAll SearchBioMarkerCorrelFastMV.executeQuery(dynamicValuesQuery)
		logger.info 'number of search app biomarkers: {}', updownResult.size()
            }

            // add static biomarkers
            // make sure no homology gene is searched
	    List<Long[]> staticResult = BioMarkerCorrelationMV.executeQuery('''
					SELECT DISTINCT bmcmv.assoBioMarkerId as assocBioMarkerId, 0 as valueMetric
					FROM org.transmart.biomart.BioMarkerCorrelationMV bmcmv
					WHERE bmcmv.bioMarkerId in (''' + mids + ')' + '''
					AND bmcmv.correlType <>'HOMOLOGENE_GENE' ''')
	    logger.info 'number of static biomarkers: {}', staticResult.size()

            // merge to get complete gene list
	    updownResult.addAll staticResult
	    int bmCount = updownResult.size()
	    logger.info 'total biomarkers: {}', bmCount

            // build biomarker/metric map
	    Map<Long, Long> mvMap = [:]
	    Long testMetric
	    for (Long[] mv in updownResult) {
		testMetric = mvMap[mv[0]]
                if (testMetric == null) {
		    mvMap[mv[0]] = mv[1]
                }
                else {
                    // if no metric value, keep one with a value
		    if (testMetric == null && mv[1] != null) {
			mvMap[mv[0]] = mv[1]
		    }

                    // keep larger abs(fold change)
                    if (testMetric != null && mv[1] != null && Math.abs(mv[1]) > Math.abs(testMetric)) {
			logger.warn 'overriding metric value for biomarker: {} [ orig: {} new: {} ]',
			    mv[0], testMetric, mv[1]
			mvMap[mv[0]] = mv[1]
                    }
                }
            }
	    processAnalysisResult result, tResult, mvMap
        }
        else {
            logger.info 'in queryExpAnalysis() did not detect any biomarkers!'
	    List<Object[]> allAnalysis = getAllAnalyses(filter)
	    Map expMap = [:]

	    for (Object[] row in allAnalysis) {
		long analysisId = row[0]
		long expId = row[1]
		String expAccession = row[2]
		long countGene = row[3]

		AnalysisResult analysisResult = new AnalysisResult(
		    analysis: BioAssayAnalysis.get(analysisId),
		    experimentId: expId,
		    experimentAccession: expAccession,
		    bioMarkerCount: countGene)
                tResult.analysisResultList.add(analysisResult)

                // get top 50 biomarkers
		List<Object[]> bioMarkers = BioAssayAnalysisDataTea.getTop50AnalysisDataForAnalysis(analysisId)
		processAnalysisResultNoSort bioMarkers, analysisResult
            }
            tResult.analysisCount = tResult.analysisResultList.size()
            tResult.expCount = expMap.size()
        }

	List trialResult = []
	if (tResult != null) {
	    trialResult << tResult
	}

	new ExpAnalysisResultSet(
	    expAnalysisResults: trialResult, analysisCount: tResult.analysisCount,
	    expCount: tResult.expCount, groupByExp: false)
    }

    /**
     * template methods
     */
    String getExpType() {
	'Experiment'
    }

    def createResultObject() {}

    void createSubFilterCriteria(SearchFilter filter, Query query) {}

    def createNPVCondition(Query query) {
	query.addCondition 'baad.teaNormalizedPValue<=0.05'
    }

    private String createCountQuery(SearchFilter filter) {
	if (filter == null || filter.globalFilter.isTextOnly()) {
	    return ' WHERE 1=0'
	}

	GlobalFilter gfilter = filter.globalFilter

	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad')
	query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad '
	query.addCondition " baad.experiment.type='" + getExpType() + "'"

	query.createGlobalFilterCriteria gfilter
	createSubFilterCriteria(filter, query)

	query.addSelect 'COUNT(DISTINCT baad.analysis.id) '

	query.generateSQL()
    }

    /**
     * get count of relevant analyses in the search according to TEA criteria
     */
    int queryExpAnalysisCount(SearchFilter filter) {

	GlobalFilter gfilter = filter.globalFilter
	if (filter == null || gfilter.isTextOnly()) {
	    return 0
	}

	Query query = new AssayAnalysisDataTeaQuery(mainTableAlias: 'baad', setDistinct: true)
	query.addTable 'org.transmart.biomart.BioAssayAnalysisDataTea baad'
	query.addCondition " baad.experiment.type='" + getExpType() + "'"
	query.addCondition ' baad.analysis.teaDataCount IS NOT NULL'
	query.addSelect 'COUNT(DISTINCT baad.analysis.id)'

	query.createGlobalFilterCriteria gfilter, true
	createSubFilterCriteria filter, query

	BioAssayAnalysisDataTea.executeQuery(query.generateSQL())[0] as int
    }

    /**
     *  get ananlysis only
     */
    List<Object[]> getAllAnalyses(SearchFilter filter) {
        // need both filters here
	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad', setDistinct: true)
	query.addTable 'org.transmart.biomart.BioAssayAnalysisDataTea baad'
	query.addCondition " baad.experiment.type='" + getExpType() + "'"
	query.addCondition ' baad.analysis.teaDataCount IS NOT NULL'

	query.addSelect 'baad.analysis.id'
	query.addSelect 'baad.experiment.id'
	query.addSelect 'baad.experiment.accession'

	query.addSelect 'baad.analysis.teaDataCount'
	query.addOrderBy 'baad.analysis.teaDataCount DESC'
	query.createGlobalFilterCriteria filter.globalFilter, true
	createSubFilterCriteria filter, query

	BioAssayAnalysisDataTea.executeQuery query.generateSQL()
    }

    /**
     * process analysis result
     */
    private void processAnalysisResultNoSort(List<Object[]> result, AnalysisResult aresult) {
	for (Object[] row in result) {
	    BioAssayAnalysisData analysisData = (BioAssayAnalysisData) row[0]
	    BioMarker biomarker = (BioMarker) row[1]
            aresult.assayAnalysisValueList.add(new AssayAnalysisValue(analysisData: analysisData, bioMarker: biomarker))
        }
    }

    /**
     * process each analysis
     */
    private void processAnalysisResult(List<Object[]> result, tar, Map<Long, Long> mvMap) {
	Map<Long, AnalysisResult> analysisResultMap = [:]
	Map expMap = [:]

	for (Object[] row in result) {
	    BioAssayAnalysisDataTea analysisData = (BioAssayAnalysisDataTea) row[0]
	    BioMarker biomarker = (BioMarker) row[1]

	    Long mvlookup = mvMap[biomarker.id]
	    long aid = analysisData.analysisId
	    AnalysisResult aresult = analysisResultMap[aid]
	    long expId = row[2]
	    String expAccession = row[3]

            if (aresult == null) {
		aresult = new AnalysisResult(
		    analysis: BioAssayAnalysis.get(aid),
		    experimentId: expId,
		    experimentAccession: expAccession)
		analysisResultMap[aid] = aresult
	    }
	    aresult.assayAnalysisValueList << new AssayAnalysisValue(
		analysisData: analysisData,
		bioMarker: biomarker,
		valueMetric: mvlookup)
        }

	Collection<AnalysisResult> aResults = analysisResultMap.values()

        // populate model
        tar.bioMarkerCt = mvMap.size()
        tar.expCount = expMap.size()
        tar.analysisCount = analysisResultMap.size()

        // don't run TEA service for single gene
        if (tar.bioMarkerCt <= 1) {
	    tar.analysisResultList << aResults
	    // TODO: sort by analyses by what in this case?
        }
        else {
            // TEA ranking service
	    tar.analysisResultList.addAll assignTEAScoresAndRank(aResults, tar.bioMarkerCt?.intValue())
            tar.populateInsignificantTEAAnalysisList()
        }
    }

    /**
     * Applies the TEA scoring algorithm to each AnalysisResult object and its associated collection of bio markers.
     * This function assigns the TEA metrics to each AnalysisResult and returns the supplied list in ascending TEA score order
     */
    List<AnalysisResult> assignTEAScoresAndRank(Collection<AnalysisResult> analyses, int geneCount) {

	List<AnalysisResult> rankedAnalyses = []

	TEAScoreManager scoreManager = new TEAScoreManager(geneCount: geneCount)

        // score each analysis
	for (AnalysisResult ar in analyses) {
	    scoreManager.assignTEAMetrics ar
	    rankedAnalyses << ar
	    ar.assayAnalysisValueList.sort()
        }

	rankedAnalyses.sort()
    }
}
