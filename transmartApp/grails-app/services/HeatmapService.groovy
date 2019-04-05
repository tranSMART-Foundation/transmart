import com.recomdata.search.query.AssayAnalysisDataQuery
import com.recomdata.search.query.Query
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.HeatmapDataValue
import org.transmart.SearchFilter
import org.transmart.biomart.BioAssayAnalysisData

/**
 * @author mmcduffie
 */
@Slf4j('logger')
class HeatmapService {

    static transactional = false

    @Autowired private TrialQueryService trialQueryService

    List createHeatMapData(SearchFilter sfilter, String method, String dataType, boolean searchTopGene,
	                   List<Long> searchGeneList, List<Long> searchAnalysisIds) {

	if (!searchAnalysisIds) {
	    logger.warn 'Search analysis IDS are null, returning null'
            return null
        }

        // TODO: Check heatmap filter to determine if it's 100 gene or not
	logger.info 'Find all bioMarkers to be used in heatmaps'
	logger.info 'Check for top gene'

	List<Object[]> resultList
	int total = 50
        if (searchTopGene) {
	    logger.info 'Run top genes heatmap'
            resultList = findTopBioMarkers(sfilter, method, dataType, total, searchAnalysisIds)
	    logger.info 'Total top genes:{}', resultList.size()
        }
	else if (searchGeneList) {
	    logger.info 'Run data search'
            resultList = findHeatmapFilterBioMarker(sfilter, method, dataType, searchGeneList, searchAnalysisIds)
        }
        else {
	    logger.info 'Run global search'
            resultList = findGlobalFilterBioMarker(sfilter, method, dataType)
        }

	if (!resultList) {
	    logger.warn 'Result list is empty from the search, returning null'
            return null
        }

	List markerList = []
	for (Object[] row in resultList) {
            markerList.add(row[0])
        }

	Query dataQuery = new AssayAnalysisDataQuery(mainTableAlias: 'baad', setDistinct: true)
	dataQuery.addTable 'org.transmart.biomart.BioAssayAnalysisData baad'
	dataQuery.addTable 'JOIN baad.featureGroup.markers baad_bm'
	dataQuery.addSelect 'baad_bm.id, baad_bm.name, baad.analysis.id,baad.foldChangeRatio, baad.rvalue, baad.rhoValue '
        trialQueryService.createTrialFilterCriteria(sfilter.trialFilter, dataQuery)
	dataQuery.addCondition 'baad.analysis.id IN (:analysisIds)'
	dataQuery.addCondition 'baad_bm.id IN(:ids)'
	dataQuery.addOrderBy 'baad_bm.name'

	if (method) {
	    dataQuery.addCondition " baad.analysis.analysisMethodCode = '" + method + "'"
	}
	if (dataType) {
	    dataQuery.addCondition " baad.analysis.assayDataType = '" + dataType + "'"
	}

	List<Object[]> dataList = BioAssayAnalysisData.executeQuery(
	    dataQuery.generateSQL(),
	    [analysisIds: searchAnalysisIds, ids: markerList, max: 2000])
	logger.info 'Total found: {}', dataList.size()

	Map dataMarkerMap = [:]
	for (Object[] data in dataList) {
	    HeatmapDataValue value = new HeatmapDataValue(
		bioMarkerId: data[0],
                bioMarkerName: data[1],
                assayAnalysisId: data[2],
                foldChangeRatio: data[3],
                rvalue: data[4],
                rhoValue: data[5])

	    def markerId = value.bioMarkerId
	    List valueList = dataMarkerMap[markerId]
            if (valueList == null) {
                valueList = []
		dataMarkerMap[markerId] = valueList
            }
	    valueList << value
        }

	List sortedDataList = []
        for (marker in markerList) {
	    if (dataMarkerMap.containsKey(marker)) {
		sortedDataList.addAll dataMarkerMap[marker]
            }
	}
	sortedDataList
    }

    /**
     * search by top genes
     */
    def findTopBioMarkers(SearchFilter sfilter, String method, String dataType, int total, List<Long> searchAnalysisIds) {

	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad', setDistinct: true)
	query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad'
	query.addTable 'JOIN baad.featureGroup.markers baad_bm'
	query.addSelect 'baad_bm.id'
	query.addSelect 'COUNT(DISTINCT baad.analysis.id) '
	query.addCondition 'baad.analysis.id IN (:ids)'
	trialQueryService.createTrialFilterCriteria sfilter.trialFilter, query
	query.addCondition "baad_bm.bioMarkerType='GENE'"

	if (method) {
	    query.addCondition " baad.analysis.analysisMethodCode = '" + method + "'"
	}
	if (dataType) {
	    query.addCondition " baad.analysis.assayDataType = '" + dataType + "'"
	}
	query.addGroupBy 'baad_bm.id'
	query.addOrderBy 'COUNT(DISTINCT baad.analysis.id) DESC'

	BioAssayAnalysisData.executeQuery query.generateSQL(), [ids: searchAnalysisIds, max: total]
    }

    /**
     * search by gene and pathways in the global filter
     */
    def findGlobalFilterBioMarker(SearchFilter sfilter, String method, String dataType) {

	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad', setDistinct: true)
	query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad'
	query.addTable 'JOIN baad.featureGroup.markers baad_bm'
	query.addSelect 'baad_bm.id'
	query.addSelect 'COUNT(DISTINCT baad.analysis.id) '
	query.createGlobalFilterCriteria sfilter.globalFilter, true

	if (method) {
	    query.addCondition " baad.analysis.analysisMethodCode = '" + method + "'"
	}
	if (dataType) {
	    query.addCondition " baad.analysis.assayDataType = '" + dataType + "'"
	}

	query.addGroupBy 'baad_bm.id'
	query.addOrderBy 'COUNT(DISTINCT baad.analysis.id) DESC'

	BioAssayAnalysisData.executeQuery query.generateSQL(), [max: 100]
    }

    /**
     * search by gene and pathways in the heatmap filter
     */
    def findHeatmapFilterBioMarker(SearchFilter sfilter, String method, String dataType, geneIds, searchAnalysisIds) {

	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad', setDistinct: true)
	query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad'
	query.addTable 'JOIN baad.featureGroup.markers baad_bm'
	query.addSelect 'baad_bm.id'
	query.addSelect 'COUNT(DISTINCT baad.analysis.id)'
	query.addCondition 'baad_bm.id IN(:ids)'
	query.addCondition 'baad.analysis.id IN (:analysisIds)'

	if (method) {
	    query.addCondition " baad.analysis.analysisMethodCode = '" + method + "'"
	}
	if (dataType) {
	    query.addCondition " baad.analysis.assayDataType = '" + dataType + "'"
	}

	query.addGroupBy 'baad_bm.id'
	query.addOrderBy 'COUNT(DISTINCT baad.analysis.id) DESC'

	BioAssayAnalysisData.executeQuery query.generateSQL(),
	    [ids: geneIds, analysisIds: searchAnalysisIds, max: 100]
    }
}
