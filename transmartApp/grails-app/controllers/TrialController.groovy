import com.recomdata.util.DomainObjectExcelHelper
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.transmart.AnalysisResult
import org.transmart.ExpAnalysisResultSet
import org.transmart.SearchFilter
import org.transmart.SearchResult
import org.transmart.TrialAnalysisResult
import org.transmart.TrialFilter
import org.transmart.biomart.BioAssayAnalysis
import org.transmart.biomart.ClinicalTrial
import org.transmart.biomart.Compound
import org.transmart.biomart.Disease
import org.transmart.biomart.Experiment
import org.transmart.plugin.shared.UtilService
import org.transmart.searchapp.SearchKeyword

/**
 * @author jliu
 */
@Slf4j('logger')
class TrialController {

    @Autowired private AnalysisDataExportService analysisDataExportService
    @Autowired private ClinicalTrialAnalysisTEAService clinicalTrialAnalysisTEAService
    @Autowired private FilterQueryService filterQueryService
    @Autowired private SearchService searchService
    @Autowired private TrialQueryService trialQueryService
    @Autowired private UtilService utilService

    @Value('${com.recomdata.search.paginate.max}')
    private int paginateMax

    @Value('${com.recomdata?.view.studyview:_clinicaltrialdetail}')
    private String studyview

    def showTrialFilter() {
	List<String> contentType = BioAssayAnalysis.executeQuery('''
				SELECT DISTINCT assayDataType
				FROM org.transmart.biomart.BioAssayAnalysis
				WHERE assayDataType IS NOT NULL''')

	List<Disease> diseases = filterQueryService.trialDiseaseFilter(sessionSearchFilter())
	List<Compound> compounds = filterQueryService.trialCompoundFilter(sessionSearchFilter())
	List<String> phases = filterQueryService.trialPhaseFilter()
	List<String> studyTypes = filterQueryService.studyTypeFilter()
	List<String> studyDesigns = filterQueryService.studyDesignFilter('Clinical Trial')

	render template: 'trialFilter', model: [studyPlatform: contentType,
                                                diseases     : diseases,
                                                compounds    : compounds,
                                                phases       : phases,
                                                studyTypes   : studyTypes,
		                                studyDesigns : studyDesigns]
    }

    def filterTrial(String checked) {
        // selected trials before this post
	TrialFilter trialFilter = sessionSearchFilter().trialFilter
	List<String> befCTrials = trialFilter.selectedtrials

	bindData trialFilter, params
	trialFilter.selectedtrials = []
	if (checked) {
	    List<String> allselected = checked.split(',') as List
            if (!allselected.contains('EmptyTrial')) { // EmptyTrial indicates All has been checked
		trialFilter.selectedtrials.addAll allselected
            }
        }
        else {
            // selecting no trials does not make sense since the result is always nothing!
            // In this case, assume the previous search trials will be used (usually this happens when user clicks filter button
            // before the tree has populated with trials)
	    trialFilter.selectedtrials = befCTrials
        }

	logger.info 'filterTrial:{}', trialFilter.selectedtrials
	SearchResult sResult = new SearchResult()

	sessionSearchFilter().datasource = 'trial'
	searchService.doResultCount sResult, sessionSearchFilter()

	render view: '/search/list', model: [searchresult: sResult, page: false]
    }

    def datasourceTrial() {
	Map<String, ?> paramMap = searchService.createPagingParamMap(params, paginateMax, 0)
	SearchResult sResult = new SearchResult(
	    trialCount: trialQueryService.countTrial(sessionSearchFilter()),
	    result: trialQueryService.queryTrial(false, sessionSearchFilter(), paramMap))
	sResult.result.analysisCount = trialQueryService.countAnalysis(sessionSearchFilter())
        sResult.result.expCount = sResult.trialCount

	render template: 'trialResult', model: [searchresult: sResult, page: false]
    }

    def datasourceTrialTEA() {
	SearchResult sResult = new SearchResult(
	    trialCount: trialQueryService.countTrial(sessionSearchFilter()),
	    result: clinicalTrialAnalysisTEAService.queryExpAnalysis(sessionSearchFilter()))
        sResult.result.expCount = sResult.trialCount
	render template: 'trialResult', model: [searchresult: sResult, page: false]
    }

    def showAnalysis(BioAssayAnalysis analysis) {
	render template: 'analysisdetail', model: [analysis: analysis]
    }

    def expDetail(ClinicalTrial clinicalTrial) {
	render template: 'clinicaltrialdetail', model: [clinicalTrial: clinicalTrial, search: 1]
    }

    /**
     * Renders the trial details in the pop up window when a user right clicks a trial in datasetExplorer
     */
    def trialDetailByTrialNumber() {
	String trialNumber = params.id.toUpperCase()
	boolean istrial = true
	Experiment exp = ClinicalTrial.findByTrialNumber(trialNumber)
        if (exp == null) {
            exp = Experiment.findByAccession(trialNumber)
            istrial = false
        }

	SearchKeyword sk = SearchKeyword.findByKeyword(trialNumber)
	Long skid = sk?.id

	if (exp) {
            if (istrial) {
		if (studyview.startsWith('_')) {
		    render template: studyview.substring(1), model: [clinicalTrial: exp, searchId: skid]
                }
                else {
		    render view: studyview, model: [clinicalTrial: exp, searchId: skid]                }
	    }
            else {
		render template: '/experiment/expDetail', model: [experimentInstance: exp, searchId: skid]
            }
        }
        else {
            logger.warn 'Experiment is null, indicating that to the user...'
	    render view: '/experiment/noresults'
        }
    }

    def getTrialAnalysis(Long id) {
	render template: 'trialAnalysis', model: [
	    trialresult: trialQueryService.queryTrialAnalysis(id, sessionSearchFilter())]
    }

    def trialFilterJSON() {
        // need to mark  trial with data
        // tmp solution

	boolean filtercheck = !sessionSearchFilter().trialFilter.newFilter
	List<String> selectedTrials = sessionSearchFilter().trialFilter.selectedtrials
	boolean rootcheck = filtercheck ? selectedTrials.contains('EmptyTrial') : true

	List<Object[]> triallist = ClinicalTrial.executeQuery('''
				SELECT b.id, b.trialNumber, b.title
				FROM org.transmart.biomart.ClinicalTrial b, org.transmart.searchapp.SearchKeyword s
				WHERE s.bioDataId=b.id
				ORDER BY b.trialNumber''')

	List<Map> ctriallist = []
	for (Object[] trial in triallist) {
            boolean c = true
	    long trialid = trial[0]
	    String trialnum = trial[1]
	    String  trialtitle = trial[2]
            if (filtercheck) {
                c = selectedTrials.contains(String.valueOf(trialid))
            }

	    String tooltip = trialtitle == null ? trialnum : trialtitle
	    String name = trialnum
            if (trialtitle != null) {
                int maxSize = 95
                int len = trialtitle.length() > maxSize ? maxSize : trialtitle.length()
                if (len < maxSize) {
                    name += ' - ' + trialtitle.substring(0, len)
                }
                else {
                    name += ' - ' + trialtitle.substring(0, len) + '...'
                }
            }
	    ctriallist << [text: name, id: String.valueOf(trialid), leaf: true, checked: c,
			   uiProvider: 'Ext.tree.CheckboxUI', qtip: tooltip]
        }
	sessionSearchFilter().trialFilter.newFilter = false

	render([[text      : 'All Trials',
                 id        : 'EmptyTrial',
                 leaf      : false,
                 uiProvider: 'Ext.tree.CheckboxUI',
                 checked   : rootcheck,
                 qtip      : 'All trials',
		 children  : ctriallist]] as JSON)
    }

    def downloadanalysisexcel(BioAssayAnalysis geneexpr) {
	String filename = geneexpr.shortDescription.replace('<', '-').replace('>', '-')
	filename = filename.replace(':', '-').replace('"', '-').replace('/', '-')
        filename = filename.replace('\\', '-').replace('?', '-').replace('*', '-')
        if (filename.length() > 50) {
            filename = filename.substring(0, 50)
        }
        filename += '.xls'
	utilService.sendDownload response, 'application/vnd.ms-excel; charset=utf-8', filename,
	    analysisDataExportService.renderAnalysisInExcel(geneexpr)
    }

    // download search result to GPE file for Pathway Studio
    def downloadanalysisgpe(BioAssayAnalysis analysis) {
	utilService.sendDownload response, null, 'expression.gpe',
	    analysisDataExportService.renderAnalysisInExcel(analysis)
    }

    def downloadStudy() {
	logger.info 'Downloading the Trial Study view'
	Map<ClinicalTrial, List<AnalysisResult>> trialMap = [:]

	ExpAnalysisResultSet result = trialQueryService.queryTrial(false, sessionSearchFilter(), null)
	for (TrialAnalysisResult tar in result.expAnalysisResults) {
	    trialMap[tar.trial] = trialQueryService.queryTrialAnalysis(tar.trial.id, sessionSearchFilter()).analysisResultList
        }

	DomainObjectExcelHelper.downloadToExcel response, 'trialstudyviewexport.xls',
	    analysisDataExportService.createExcelTrialStudyView(new SearchResult(result: result), trialMap)
    }

    def downloadAnalysisTEA() {
	logger.info 'Downloading the Trial TEA Analysis view'
	DomainObjectExcelHelper.downloadToExcel response, 'trialteaviewexport.xls',
	    analysisDataExportService.createExcelTrialTEAView(
	    new SearchResult(result: clinicalTrialAnalysisTEAService.queryExpAnalysis(sessionSearchFilter())))
    }

    private SearchFilter sessionSearchFilter() {
	session.searchFilter
    }
}
