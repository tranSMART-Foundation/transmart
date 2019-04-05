import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import com.recomdata.util.DomainObjectExcelHelper
import com.recomdata.util.ElapseTimer
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.transmart.AnalysisResult
import org.transmart.ExpAnalysisResultSet
import org.transmart.ExperimentAnalysisResult
import org.transmart.SearchFilter
import org.transmart.SearchResult
import org.transmart.biomart.BioAssayAnalysis
import org.transmart.biomart.BioAssayPlatform
import org.transmart.biomart.Disease
import org.transmart.biomart.Experiment
import org.transmart.plugin.shared.UtilService
import org.transmart.searchapp.SearchKeyword

/**
 * @author mmcduffie
 */
@Slf4j('logger')
class ExperimentAnalysisController {

    // session attribute
    private static final String TEA_PAGING_DATA = 'analListPaging'

    @Autowired private AnalysisDataExportService analysisDataExportService
    @Autowired private ExperimentAnalysisQueryService experimentAnalysisQueryService
    @Autowired private ExperimentAnalysisTEAService experimentAnalysisTEAService
    @Autowired private FilterQueryService filterQueryService
    @Autowired private FormLayoutService formLayoutService
    @Autowired private SearchService searchService
    @Autowired private UtilService utilService

    @Value('${com.recomdata.search.paginate.max:0}')
    private int paginateMax

    def showFilter() {
	SearchFilter filter = sessionSearchFilter()

	ElapseTimer stimer = new ElapseTimer()

	List<SearchKeyword> compounds = filterQueryService.experimentCompoundFilter('Experiment')
	List<Disease> diseases = filterQueryService.findExperimentDiseaseFilter(filter, 'Experiment')
	List<String> expDesigns = experimentAnalysisQueryService.findExperimentDesignFilter(filter)

        // no data?
        def celllines = [] //GeneExprAnalysis.executeQuery(queryCellLines.toString(),filter.gids)

        // no data?
        def expTypes = [] //experimentAnalysisQueryService.findExperimentTypeFilter()

	List<String> platformOrganisms = experimentAnalysisQueryService.findPlatformOrganizmFilter(filter)

	stimer.logElapsed 'Loading Exp Analysis Filters', true
        // note: removed datasource, celllines and expTypes since no data being retrieved (removed from filter page too)
	render template: 'expFilter', model: [
	    diseases: diseases,
	    compounds: compounds,
	    expDesigns: expDesigns,
	    platformOrganisms: platformOrganisms]
    }

    def filterResult() {
	SearchResult sResult = new SearchResult()
	sessionSearchFilter().datasource = 'experiment'
	bindData sessionSearchFilter().expAnalysisFilter, params

	searchService.doResultCount sResult, sessionSearchFilter()
	render view: '/search/list', model: [searchresult: sResult, page: false]
    }

    /**
     * summary result view
     */
    def datasourceResult() {
	ElapseTimer stimer = new ElapseTimer()

	Map<String, ?> paramMap = searchService.createPagingParamMap(params, paginateMax, 0)

	SearchResult sResult = new SearchResult(
	    experimentCount: experimentAnalysisQueryService.countExperimentMV(sessionSearchFilter()))

	int expAnalysisCount = experimentAnalysisQueryService.countAnalysisMV(sessionSearchFilter())

        stimer.logElapsed('Loading Exp Analysis Counts', true)

	sResult.result = experimentAnalysisQueryService.queryExperiment(sessionSearchFilter(), paramMap)
        sResult.result.analysisCount = expAnalysisCount
        sResult.result.expCount = sResult.experimentCount

	render template: 'experimentResult', model: [searchresult: sResult, page: false]
    }

    /**
     * tea result view
     */
    def datasourceResultTEA() {
	ElapseTimer stimer = new ElapseTimer()

	SearchResult sResult = new SearchResult(
	    experimentCount: experimentAnalysisQueryService.countExperimentMV(sessionSearchFilter()),
	    result: experimentAnalysisTEAService.queryExpAnalysis(sessionSearchFilter()))
	stimer.logElapsed 'Loading Exp TEA Counts', true
        sResult.result.expCount = sResult.experimentCount

	ExperimentAnalysisResult ear = sResult.result.expAnalysisResults[0]
	ear.pagedAnalysisList = pageTEAData(ear.analysisResultList, 0, paginateMax)

        // store in session for paging requests
	session.setAttribute TEA_PAGING_DATA, sResult

	render template: 'experimentResult', model: [searchresult: sResult, page: true]
    }

    /**
     * page TEA analysis view
     */
    def pageTEAAnalysisView() {

        // retrieve session data, page analyses
	SearchResult sResult = session.getAttribute(TEA_PAGING_DATA)
	ExperimentAnalysisResult ear = sResult.result.expAnalysisResults[0]
	ear.pagedAnalysisList = pageTEAData(ear.analysisResultList, params.int('offset'), params.int('max'))

	render template: 'experimentResult', model: [searchresult: sResult, page: true]
    }

    def expDetail(String accession, Long id) {
        logger.info '** action: expDetail called!'

	Experiment exp
	if (id) {
	    exp = Experiment.get(id)
        }
        else {
	    exp = Experiment.findByAccession(accession)
        }

	List<BioAssayPlatform> platforms = experimentAnalysisQueryService.getPlatformsForExperment(exp)
	Set<String> organisms = platforms*.organism

	List<FormLayout> formLayout = formLayoutService.getLayout('study')

        ExportTableNew table

        //Keep this if you want to cache the grid data
	//ExportTableNew table=(ExportTableNew) session.gridtable

        if (table == null) {
            table = new ExportTableNew()
        }

	ExportTableNew table2 = new ExportTableNew()
	table2.putColumn 'name', new ExportColumn('name', 'Name', '', 'String')
	table2.putColumn 'biosource', new ExportColumn('biosource', 'Biosource', '', 'String')
	table2.putColumn 'Technology', new ExportColumn('Technology', 'Technology', '', 'String')
	table2.putColumn 'Biomarkersstudied', new ExportColumn('Biomarkersstudied', 'Biomarkers studied', '', 'String')

        ExportRowNew newrow4 = new ExportRowNew()
	newrow4.put 'name', 'My Analysis'
	newrow4.put 'biosource', 'Endometrial tumor'
	newrow4.put 'Technology', 'IHC'
	newrow4.put 'Biomarkersstudied', 'PTEN'
	table2.putRow 'somerow', newrow4

	table.putColumn 'name', new ExportColumn('name', 'Name', '', 'String')
	table.putColumn 'biosource', new ExportColumn('biosource', 'Biosource', '', 'String')
	table.putColumn 'Technology', new ExportColumn('Technology', 'Technology', '', 'String')
	table.putColumn 'Biomarkersstudied', new ExportColumn('Biomarkersstudied', 'Biomarkers studied', '', 'String')

        ExportRowNew newrow = new ExportRowNew()
	newrow.put 'name', 'Assay 1'
	newrow.put 'biosource', 'Endometrial tumor'
	newrow.put 'Technology', 'IHC'
	newrow.put 'Biomarkersstudied', 'PTEN'

        ExportRowNew newrow2 = new ExportRowNew()
	newrow2.put 'name', 'Assay 2'
	newrow2.put 'biosource', 'Endometrial tumor'
	newrow2.put 'Technology', 'H&E'
	newrow2.put 'Biomarkersstudied', 'None'

        ExportRowNew newrow3 = new ExportRowNew()
	newrow3.put 'name', 'Assay 3'
	newrow3.put 'biosource', 'Endometrial tumor'
	newrow3.put 'Technology', 'nucleotide sequencing'
	newrow3.put 'Biomarkersstudied', 'AKT1; BRAF; ESR1; HRAS; KRAS;'

	table.putRow 'somerow', newrow
	table.putRow 'somerow2', newrow2
	table.putRow 'somerow3', newrow3

	session.gridtable = table

	logger.info 'formLayout = {}', formLayout
	render template: '/experiment/expDetail', model: [
	    layout: formLayout,
	    experimentInstance: exp,
	    expPlatforms: platforms,
	    expOrganisms: organisms,
	    search: 1,
	    jSONForGrid: table2.toJSON_DataTables('').toString(5),
	    jSONForGrid1: table.toJSON_DataTables('').toString(5)]
    }

    def getAnalysis(Long id) {
	render template: '/trial/trialAnalysis', model: [
	    trialresult: experimentAnalysisQueryService.queryAnalysis(id, sessionSearchFilter())]
    }

    // download search result into excel
    def downloadanalysisexcel(BioAssayAnalysis analysis) {
	utilService.sendDownload response, 'application/vnd.ms-excel; charset=utf-8',
	    'pre_clinical.xls', analysisDataExportService.renderAnalysisInExcel(analysis)
    }

    //	 download search result to GPE file for Pathway Studio
    def downloadanalysisgpe(BioAssayAnalysis analysis) {
	utilService.sendDownload response, 'application/vnd.ms-excel; charset=utf-8',
	    'expression.gpe', analysisDataExportService.renderAnalysisInExcel(analysis)
    }

    /**
     * page the tea analysis data
     */
    private List<AnalysisResult> pageTEAData(List<AnalysisResult> analysisList, int offset, int pageSize) {

	List<AnalysisResult> pagedData = []
        int numRecs = analysisList.size()
	int lastIndex = offset + pageSize <= numRecs ? offset + pageSize - 1 : numRecs

	ListIterator<AnalysisResult> it = analysisList.listIterator(offset)
        while (it.hasNext()) {
	    AnalysisResult ar = it.next()
	    if (!ar.analysis.isAttached()) {
		ar.analysis.attach()
	    }

	    pagedData << ar
            int nextIdx = it.nextIndex()
	    if (nextIdx > lastIndex) {
		break
            }
	}
	logger.info 'Paged data: start Idx: {}; last idx: {}; size: {}', offset, lastIndex, pagedData.size()
	pagedData
    }

    def downloadAnalysis() {
	logger.info 'Downloading the Experimental Analysis (Study) view'
	Map<Experiment, List<AnalysisResult>> eaMap = [:]

	ExpAnalysisResultSet result = experimentAnalysisQueryService.queryExperiment(sessionSearchFilter(), null)
	for (ExperimentAnalysisResult ear in result.expAnalysisResults) {
	    ExperimentAnalysisResult analysisRS = experimentAnalysisQueryService.queryAnalysis(ear.experiment.id, sessionSearchFilter())
	    eaMap[ear.experiment] = analysisRS.analysisResultList
        }
	DomainObjectExcelHelper.downloadToExcel response, 'analysisstudyviewexport.xls',
	    analysisDataExportService.createExcelEAStudyView(new SearchResult(result: result), eaMap)
    }

    def downloadAnalysisTEA() {
	logger.info 'Downloading the Experimental Analysis TEA view'
	DomainObjectExcelHelper.downloadToExcel response, 'analysisteaviewexport.xls',
	    analysisDataExportService.createExcelEATEAView(
	    new SearchResult(result: experimentAnalysisTEAService.queryExpAnalysis(sessionSearchFilter())))
    }

    /**
     * Renders a UI where the user can pick an experiment from the experiments in the system.
     * Selection of multiple studies is allowed.
     */
    def browseAnalysisMultiSelect() {
	List<Object[]> analyses = BioAssayAnalysis.executeQuery('''
				select id, name, etlId
				from BioAssayAnalysis b
				order by b.name''')
	render template: 'browseMulti', model: [analyses: analyses]
    }

    private SearchFilter sessionSearchFilter() {
	session.searchFilter
    }
}
