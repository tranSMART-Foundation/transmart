import com.recomdata.search.query.AssayDataStatsQuery
import com.recomdata.search.query.AssayStatsExpMarkerQuery
import com.recomdata.search.query.Query
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Value
import org.transmart.ExpressionProfileFilter
import org.transmart.GlobalFilter
import org.transmart.SearchFilter
import org.transmart.biomart.BioAssayDataStatistics
import org.transmart.biomart.BioAssayStatsExpMarker
import org.transmart.biomart.BioMarker
import org.transmart.biomart.Disease

/**
 * @author mmcduffie
 */
@CompileStatic
class ExpressionProfileQueryService {

    static transactional = false

    @Value('${com.recomdata.search.gene.max:0}')
    private int geneMax

    /**
     * count experiment with criteria
     */
    int countExperiment(SearchFilter filter) {
        if (filter == null || filter.globalFilter.isEmpty() || filter.globalFilter.isTextOnly()) {
	    0
	}
	else {
	    BioAssayStatsExpMarker.executeQuery(createCountQuery(filter))[0] as int
        }
    }

    String createCountQuery(SearchFilter filter) {
        if (filter == null || filter.globalFilter.isEmpty() || filter.globalFilter.isTextOnly()) {
            return ' WHERE 1=0'
        }

	GlobalFilter gfilter = filter.globalFilter

	Query query = new AssayStatsExpMarkerQuery(mainTableAlias: 'asemq')
	query.addTable 'org.transmart.biomart.BioAssayStatsExpMarker asemq'
	query.addCondition "asemq.experiment.type='Experiment'"

	query.createGlobalFilterCriteria gfilter
	createSubFilterCriteriaForMarker(filter.exprProfileFilter, query)
	query.addSelect 'COUNT(DISTINCT asemq.experiment.id)'

	query.generateSQL()
    }

    List<Object[]> queryStatisticsDataExpField(SearchFilter filter) {
	Query query = new AssayDataStatsQuery(mainTableAlias: 'bads', setDistinct: true)
	query.addTable 'org.transmart.biomart.BioAssayDataStatistics bads'
	GlobalFilter gfilter = filter.globalFilter
        // expand biomarkers
	query.createGlobalFilterCriteria gfilter, true
	createSubFilterCriteria filter.exprProfileFilter, query
	query.addSelect 'bads'
	query.addSelect 'bads.experiment.accession'
	query.addOrderBy 'bads.experiment.accession'

	BioAssayDataStatistics.executeQuery query.generateSQL(), [max: 500]
    }

    List<BioMarker> listBioMarkers(SearchFilter filter) {
	Query query = new AssayStatsExpMarkerQuery(mainTableAlias: 'asemq', setDistinct: true)
	query.addTable 'org.transmart.biomart.BioAssayStatsExpMarker asemq'
	query.addCondition "asemq.experiment.type='Experiment'"
	GlobalFilter gfilter = filter.globalFilter
        // expand biomarkers
	query.createGlobalFilterCriteria gfilter, true
        createSubFilterCriteriaForMarker(filter.exprProfileFilter, query)
	query.addSelect 'asemq.marker'
	query.addOrderBy 'asemq.marker.name'

	BioAssayStatsExpMarker.executeQuery query.generateSQL(), [max: geneMax]
    }

    List<Disease> listDiseases(SearchFilter filter) {
	Query query = new AssayDataStatsQuery(mainTableAlias: 'bads', setDistinct: true)
	query.addTable 'org.transmart.biomart.BioAssayDataStatistics bads'
	query.addCondition "bads.experiment.type='Experiment'"
	query.addTable 'JOIN bads.experiment.diseases bads_dis'
	GlobalFilter gfilter = filter.globalFilter
	query.createGlobalFilterCriteria gfilter
	createSubFilterCriteria filter.exprProfileFilter, query
	query.addSelect 'bads_dis'
	query.addOrderBy 'bads_dis.preferredName'

	BioAssayDataStatistics.executeQuery query.generateSQL()
    }

    /**
     * get probesets filtered by marker (i.e. gene) and disease
     */
    List<String> getProbesetsByBioMarker(BioMarker marker, Disease disease) {
	BioAssayDataStatistics.executeQuery '''
		SELECT distinct bads.featureGroupName
		FROM org.transmart.biomart.BioAssayDataStatistics bads
		JOIN bads.featureGroup.markers bads_bm
		JOIN bads.experiment.diseases bads_dis
		WHERE bads_bm.id =:bmid and bads_dis.id=:disid''',
		[bmid: marker.id, disid: disease.id]
    }

    void createSubFilterCriteria(ExpressionProfileFilter exprfilter, Query query) {
        // disease
        if (exprfilter.filterDisease()) {
	    String alias = query.mainTableAlias + '_dis'
	    query.addTable 'JOIN ' + query.mainTableAlias + '.experiment.diseases ' + alias
	    query.addCondition alias + '.id = ' + exprfilter.bioDiseaseId
        }

        // biomarker
        if (exprfilter.filterBioMarker()) {
	    String alias = query.mainTableAlias + '_bm'
	    query.addTable 'JOIN ' + query.mainTableAlias + '.featureGroup.markers ' + alias
	    query.addCondition alias + '.id = ' + exprfilter.bioMarkerId
        }

        // probeset
        if (exprfilter.filterProbeSet()) {
	    query.addCondition query.mainTableAlias + ".featureGroupName='" + exprfilter.probeSet + "'"
        }
    }

    void createSubFilterCriteriaForMarker(ExpressionProfileFilter exprfilter, Query query) {
        // disease
        if (exprfilter.filterDisease()) {
	    String alias = query.mainTableAlias + '_dis'
	    query.addTable 'JOIN ' + query.mainTableAlias + '.experiment.diseases ' + alias
	    query.addCondition alias + '.id = ' + exprfilter.bioDiseaseId
        }

        // biomarker
        if (exprfilter.filterBioMarker()) {
	    String alias = query.mainTableAlias + '.marker'
	    query.addCondition alias + '.id = ' + exprfilter.bioMarkerId
        }
    }
}
