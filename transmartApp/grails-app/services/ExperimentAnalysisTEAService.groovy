import com.recomdata.search.query.Query
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.ExperimentAnalysisResult
import org.transmart.SearchFilter

/**
 * @author mmcduffie
 * todo -- make a super class for experimentanalysisqueryservice and trialqueryservice
 */
@CompileStatic
class ExperimentAnalysisTEAService extends AnalysisTEABaseService {

    static transactional = false

    @Autowired private ExperimentAnalysisQueryService experimentAnalysisQueryService

    String getExpType() {
	'Experiment'
    }

    ExperimentAnalysisResult createResultObject() {
	new ExperimentAnalysisResult()
    }

    void createSubFilterCriteria(SearchFilter filter, Query query) {
	experimentAnalysisQueryService.createSubFilterCriteria filter.expAnalysisFilter, query
    }
}
