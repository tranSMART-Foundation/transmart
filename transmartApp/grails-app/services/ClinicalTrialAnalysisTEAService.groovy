import com.recomdata.search.query.AssayAnalysisDataQuery
import com.recomdata.search.query.Query
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.GlobalFilter
import org.transmart.SearchFilter
import org.transmart.TrialAnalysisResult

/**
 * @author mmcduffie
 */
@CompileStatic
class ClinicalTrialAnalysisTEAService extends AnalysisTEABaseService {

    static transactional = false

    @Autowired private TrialQueryService trialQueryService

    String getExpType() {
	'Clinical Trial'
    }

    TrialAnalysisResult createResultObject() {
	new TrialAnalysisResult()
    }

    void createSubFilterCriteria(SearchFilter filter, Query query) {
	trialQueryService.createTrialFilterCriteria filter.trialFilter, query
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
	createSubFilterCriteria filter, query

	query.addSelect 'baad.analysis.id'

	query.generateSQL()
    }
}
