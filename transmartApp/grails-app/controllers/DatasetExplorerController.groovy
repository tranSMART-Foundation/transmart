import com.recomdata.extensions.ExtensionsRegistry
import grails.converters.JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.transmart.plugin.shared.SecurityService
import transmartapp.OntologyService

class DatasetExplorerController {

    @Value('${com.recomdata.i2b2.subject.domain:}')
    private String i2b2Domain

    @Value('${com.recomdata.i2b2.subject.projectid:}')
    private String i2b2ProjectId

    @Value('${com.recomdata.i2b2.subject.username:}')
    private String i2b2Username

    @Value('${com.recomdata.i2b2.subject.password}')
    private String i2b2Password

    @Value('${com.recomdata.datasetExplorer.usePMHost:}')
    private String usePmHost

    @Value('${com.recomdata.datasetExplorer.genePatternURL:}')
    private String genePatternUrl

    @Value('${com.recomdata.datasetExplorer.genePatternEnabled:false}')
    private boolean enableGenePattern

    @Value('${com.recomdata.search.genepathway:}')
    private String searchGenepathway

    @Value('${com.recomdata.adminHelpURL:}')
    private String adminHelpUrl

    @Value('${org.transmartproject.helpUrls.index:}')
    private String helpManualUrl

    @Value('${org.transmartproject.helpUrls.analysisjobs:}')
    private String helpAnalysisjobsUrl

    @Value('${org.transmartproject.helpUrls.analyze:}')
    private String helpAnalyzeUrl

    @Value('${org.transmartproject.helpUrls.dataexport:}')
    private String helpDataexportUrl

    @Value('${org.transmartproject.helpUrls.exportjobs:}')
    private String helpExportjobsUrl

    @Value('${org.transmartproject.helpUrls.galaxy:}')
    private String helpGalaxyUrl

    @Value('${org.transmartproject.helpUrls.gridview:}')
    private String helpGridviewUrl

    @Value('${org.transmartproject.helpUrls.gwas:}')
    private String helpGwasUrl

    @Value('${org.transmartproject.helpUrls.highdimensional:}')
    private String helpHighdimensionalUrl

    @Value('${org.transmartproject.helpUrls.query:}')
    private String helpQueryUrl

    @Value('${org.transmartproject.helpUrls.sample:}')
    private String helpSampleUrl

    @Value('${org.transmartproject.helpUrls.savequery:}')
    private String helpSavequeryUrl

    @Value('${org.transmartproject.helpUrls.summary:}')
    private String helpSummaryUrl

    @Value('${org.transmartproject.helpUrls.tree:}')
    private String helpTreeUrl

    @Value('${org.transmartproject.helpUrls.workspace:}')
    private String helpWorkspaceUrl

    @Value('${org.transmartproject.helpUrls.hiDomePopUp:}')
    private String hidomePopupHelpUrl

    @Value('${com.recomdata.contactUs:}')
    private String contactUs

    @Value('${com.recomdata.appTitle:}')
    private String appTitle

    @Value('${com.recomdata.datasetExplorer.hideAcrossTrialsPanel:false}')
    private boolean hideAcrossTrialsPanel

    @Value('${ui.tabs.datasetExplorer.allData.hide:false}')
    private boolean hideAllData

    @Value('${ui.tabs.datasetExplorer.gridView.hide:false}')
    private boolean hideGridView

    @Value('${ui.tabs.datasetExplorer.dataExport.hide:false}')
    private boolean hideDataExport

    @Value('${ui.tabs.datasetExplorer.dataExportJobs.hide:false}')
    private boolean hideDataExportJobs

    @Value('${ui.tabs.datasetExplorer.analysisJobs.show:false}')
    private boolean analysisJobsEnabled

    @Value('${ui.tabs.datasetExplorer.workspace.hide:false}')
    private boolean hideWorkspace

    @Value('${ui.tabs.sampleExplorer.hide:false}')
    private boolean hideSampleExplorer

    @Value('${com.thomsonreuters.transmart.metacoreAnalyticsEnable:false}')
    private boolean metacoreAnalyticsEnabled

    @Value('${com.thomsonreuters.transmart.metacoreURL:}')
    private String metacoreUrl

    @Value('${ui.tabs.browse.hide:false}')
    private String hideBrowse

    @Value('${ui.tabs.datasetExplorer.xnatViewer.show:false}')
    private boolean xnatEnabled

    @Autowired private I2b2HelperService i2b2HelperService
    @Autowired private OntologyService ontologyService
    @Autowired private SecurityService securityService
    @Autowired private ExtensionsRegistry transmartExtensionsRegistry

    def index(String accession, String path) {
        //code for retrieving a saved comparison
	String pathToExpand
	//If we have an accession passed, retrieve its path
	if (accession) {
	    pathToExpand = ontologyService.getPathForAccession(accession)
        }

	def rwgSearchFilter = session.rwgSearchFilter
	def rwgSearchOperators = session.rwgSearchOperators

	String tokens = i2b2HelperService.getSecureTokensCommaSeparated()
	String initialaccess = new JSON(i2b2HelperService.getAccess(i2b2HelperService.getRootPathsWithTokens())).toString()
	boolean canSeeData = !hideAllData || securityService.principal().isAdminOrDseAdmin()

	render view: 'datasetExplorer', model: [
	    pathToExpand            : pathToExpand ?: path,
	    admin                   : securityService.principal().isAdmin(),
            tokens                  : tokens,
            initialaccess           : initialaccess,
            i2b2Domain              : i2b2Domain,
	    i2b2ProjectID           : i2b2ProjectId,
            i2b2Username            : i2b2Username,
            i2b2Password            : i2b2Password,
	    rwgSearchFilter         : rwgSearchFilter ? rwgSearchFilter.join(',,,') : '',
	    rwgSearchOperators      : rwgSearchOperators ? rwgSearchOperators.join(';') : '',
	    globalOperator          : session.globalOperator,
	    rwgSearchCategory       : session.searchCategory,
            debug                   : params.debug,
	    dseOpenedNodes          : session.dseOpenedNodes,
	    dseClosedNodes          : session.dseClosedNodes,
	    usePmHost               : usePmHost,
	    genePatternUrl          : genePatternUrl,
	    enableGenePattern       : enableGenePattern.toString(),
	    searchGenepathway       : searchGenepathway,
	    adminHelpUrl            : adminHelpUrl,
	    helpManualUrl           : helpManualUrl,
	    hidomePopupHelpUrl      : hidomePopupHelpUrl,
	    contactUs               : contactUs,
	    appTitle                : appTitle,
	    hideAcrossTrialsPanel   : hideAcrossTrialsPanel,
	    gridViewEnabled         : canSeeData && !hideGridView,
	    dataExportEnabled       : canSeeData && !hideDataExport,
	    dataExportJobsEnabled   : canSeeData && !hideDataExportJobs,
	    analysisJobsEnabled     : analysisJobsEnabled,
	    workspaceEnabled        : !hideWorkspace,
	    sampleExplorerEnabled   : !hideSampleExplorer,
	    metacoreAnalyticsEnabled: metacoreAnalyticsEnabled,
	    metacoreUrl             : metacoreUrl,
	    analysisTabExtensions   : transmartExtensionsRegistry.analysisTabExtensions as JSON,
	    hideBrowse              : hideBrowse,
	    xnatEnabled             : canSeeData && xnatEnabled
	]
    }

    def queryPanelsLayout() {
	render view: '_queryPanel'
    }
}
