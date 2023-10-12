package pages.modules

import com.google.common.collect.*
import geb.Module
import geb.navigator.Navigator
import org.openqa.selenium.WebElement

import functions.Constants

import pages.AnalyzeWorkflow

/**
 * Created by peter.rice@transmartfoundation.org on 25-jul-2017
 */
class AnalyzeTabModule extends Module {

    static base = { $('div#resultsTabPanel') }

    static content = {
        // Analyze tabs
        tabMenu { $('ul') }
        // 
        tabSelected { $('li.x-tab-strip-active') }
        // Comparison Query
        tabQuery { $('li#resultsTabPanel__queryPanel') }
        // Summary Statistics
        tabSummary { $('li#resultsTabPanel__analysisPanel') }
        // Grid View
        tabGridview { $('li#resultsTabPanel__analysisGridPanel') }
        // Advanced Workflows
        tabWorkflows(to: AnalyzeWorkflow) { $('li#resultsTabPanel__dataAssociationPanel') }
        // SmartR
        tabSmartR { $('li#resultsTabPanel__smartRPanel') }
        // Data Export
        tabExport { $('li#resultsTabPanel__analysisDataExportPanel') }
        // Export jobs
        tabExportJobs { $('li#resultsTabPanel__analysisExportJobsPanel') }
        // Analysis Jobs
        tabAnalysisJobs { $('li#resultsTabPanel__analysisJobsPanel') }
        // Workspace
        tabWorkspace { $('li#resultsTabPanel__workspacePanel') }
        // Image view
        tabImage { $('li#resultsTabPanel__analysisImagePanel') }
        // Galaxy export
        tabGalaxy { $('li#resultsTabPanel__GalaxyPanel') }
        // MetaCore
        tabMetacore { $('li#resultsTabPanel__metacoreEnrichmentPanel') }
        // genome browser
        tabDalliance { $('li#resultsTabPanel__dallianceBrowser') }
    }

    final static String ANALYZE_TAB_QUERY = 'Comparison'
    final static String ANALYZE_TAB_SUMMARY = 'Summary Statistics'
    final static String ANALYZE_TAB_GRIDVIEW = 'Grid View'
    final static String ANALYZE_TAB_WORKFLOWS = 'Advanced Workflow'
    final static String ANALYZE_TAB_SMARTR = 'SmartR'
    final static String ANALYZE_TAB_EXPORT = 'Data Export'
    final static String ANALYZE_TAB_EXPORTJOBS = 'Export Jobs'
    final static String ANALYZE_TAB_ANALYSISJOBS = 'Analysis Jobs'
    final static String ANALYZE_TAB_WORKSPACE = 'Workspace'
    final static String ANALYZE_TAB_IMAGE = 'Image View'
    final static String ANALYZE_TAB_GALAXY = 'Galaxy Export'
    final static String ANALYZE_TAB_METACORE = 'MetaCore'
    final static String ANALYZE_TAB_DALLIANCE = 'Genome Browser'

    private List<String> ANALYZE_TABS = []

    def AnalyzeTabModule() {
        ANALYZE_TABS.add(ANALYZE_TAB_QUERY)
        ANALYZE_TABS.add(ANALYZE_TAB_SUMMARY)
        ANALYZE_TABS.add(ANALYZE_TAB_GRIDVIEW)
        ANALYZE_TABS.add(ANALYZE_TAB_WORKFLOWS)
        ANALYZE_TABS.add(ANALYZE_TAB_SMARTR)
        ANALYZE_TABS.add(ANALYZE_TAB_EXPORT)
        ANALYZE_TABS.add(ANALYZE_TAB_EXPORTJOBS)
        ANALYZE_TABS.add(ANALYZE_TAB_ANALYSISJOBS)
        ANALYZE_TABS.add(ANALYZE_TAB_WORKSPACE)
        ANALYZE_TABS.add(ANALYZE_TAB_IMAGE)
        ANALYZE_TABS.add(ANALYZE_TAB_GALAXY)
        ANALYZE_TABS.add(ANALYZE_TAB_METACORE)
        ANALYZE_TABS.add(ANALYZE_TAB_DALLIANCE)
    }
        

}
