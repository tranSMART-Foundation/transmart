package pages

import geb.Page
import geb.waiting.WaitTimeoutException
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

//import org.openqa.selenium.WebElement

class AnalyzePage extends Page {

    public static final String HEADER_TAB_NAME = 'Analyze'

    static url = 'datasetExplorer/index'

    static at = {
        currentHeaderTab()?.text() == HEADER_TAB_NAME
    }

    static content = {
        analyze(wait: true) { $() }
    }
    

    Navigator currentHeaderTab () { currentHeader().find('th', class: 'menuVisited') }
    Navigator headerTab () { currentHeader().find('table#menuLinks') }
// table rows
//Browse
//Analyze
//Sample Explorer
//Gene Signature/Lists
//GWAS
//Upload Data
//Admin
//(utilities script)
//Utilities id: utilitiesMenuButton
    
    Navigator currentHeader () { analyze.find('div#header-div') }
//class: menuDetail
//class: menuBar
    Navigator searchCategories () { analyze.find('div#search-categories') }
    Navigator searchAutoComplete () { analyze.find('div#search-ac') }

    Navigator panelFilter() { analyze.find('div#filter-browser') }
//list of filter content

    Navigator frameGpLogin() { analyze.find('div#gplogin') }

    Navigator frameAltGpLogin() { analyze.find('div#altgplogin') }

    Navigator dialogSaveReports() { analyze.find('div#saveReportsDialog') }

    Navigator dialogSaveSubsets() { analyze.find('div#saveSubsetsDialog') }

    Navigator panelWest() { analyze.find('div#westPanel') }
    Navigator searchBox() { analyze.find('div#box-search') }
    Navigator searchTitle() { analyze.find('div#title-search-div') }
    Navigator searchActive() { analyze.find('div#active-search-div') }

    Navigator navigateTerms() { analyze.find('div#navigateTermsPanel') }
    Navigator resultsNoAnalyze() { analyze.find('div#noAnalyzeResults') }//no results found
//also has the tree below but with no useful ID

    Navigator panelCenter() { analyze.find('div#centerPanel') }

    Navigator panelCenterMain() { analyze.find('div#centerMainPanel') }

    Navigator panelResultsTabs () { panelCenter().find('div#resultsTabPanel') }
// has further tabs for results

    Navigator resultsTabs_Query () { panelCenter().find('div#resultsTabPanel__queryPanel') }
// says 'Comparison'
    Navigator resultsTabs_Analysis () { panelCenter().find('div#resultsTabPanel__analysisPanel') }
    Navigator resultsTabs_AnalysisGrid () { panelCenter().find('div#resultsTabPanel__analysisGridPanel') }
    Navigator resultsTabs_DataAssociation () { panelCenter().find('div#resultsTabPanel__dataAssociationPanel') }
    Navigator resultsTabs_DataExport () { panelCenter().find('div#resultsTabPanel__analysisDataExportPanel') }
    Navigator resultsTabs_ExportJobs () { panelCenter().find('div#resultsTabPanel__analysisExportJobsPanel') }
    Navigator resultsTabs_AnalysisJobs () { panelCenter().find('div#resultsTabPanel__analysisJobsPanel') }
    Navigator resultsTabs_WorkspacePanel () { panelCenter().find('div#resultsTabPanel__workspacePanel') }
    Navigator resultsTabs_SampleExplorer () { panelCenter().find('div#resultsTabPanel__sampleExplorer') }
    Navigator resultsTabs_DallianceBrowser () { panelCenter().find('div#resultsTabPanel__dallianceBrowser') }
//'Genome browser'

    Navigator resultsTabsQuery () { panelCenter().find('div#queryPanel') }
//class:subsetpanel has 20 query boxes
//Save subset button
//Cancel job or Ready
    Navigator resultsTabsAnalysis () { panelCenter().find('div#analysisPanel') }
    Navigator resultsTabsAnalysisGrid () { panelCenter().find('div#analysisGridPanel') }
    Navigator resultsTabsDataAssociation () { panelCenter().find('div#dataAssociationPanel') }
    Navigator resultsTabsAnalysisdataExport () { panelCenter().find('div#analysisDataExportPanel') }
    Navigator resultsTabsAnalysisExportJobs () { panelCenter().find('div#analysisExportJobsPanel') }
    Navigator resultsTabsAnalysisJobs () { panelCenter().find('div#analysisJobsPanel') }
    Navigator resultsTabsWorkspace () { panelCenter().find('div#workspacePanel') }
    Navigator resultsTabsSampleExplorer () { panelCenter().find('div#sampleExplorer') }
    Navigator resultsTabsDallianceBrowser () { panelCenter().find('div#dallianceBrowser') }


    Navigator windowSetValue() { analyze.find('div#setValueWindow') }

    Navigator formExportDS() { analyze.find('div#exportdsform') }

    Navigator formExportGrid() { analyze.find('div#exportgridform') }

    Navigator dialogScripts() { analyze.find('script') } // includes help pages

    static boolean canExpand(node) {
        'x-tree-elbow-end-plus' in node.classes() ||
                'x-tree-elbow-plus' in node.classes()
    }

    def expand(String conceptKey) {
        def parts = conceptKey.split('\\\\') as List

        def image
        for (int i = 3; i < parts.size(); i++) {
            def key = parts[0..i].join('\\') + '\\'
            image = nodePlus(key)
            if (canExpand(image)) {
                image.click()
            }
        }
        image //return last image
    }

    void dragNodeToBox(String conceptKey, targetBox, nodeMatcher = null) {
        expand conceptKey

        interact {
            def sourceNode = nodeText conceptKey

            assert !sourceNode.empty
            assert !targetBox.empty

            dragAndDrop sourceNode, targetBox
        }
        if (nodeMatcher == null) {
            nodeMatcher = contains(conceptKey)
        }

        /* An expansion of the nodes may be needed for categorical variables,
         * and this takes some time */
        try {
            waitFor {
                nodeMatcher.matches(boxConcepts(targetBox))
            }
        } catch (WaitTimeoutException wte) {
            // so we get a nice message
            assertThat boxConcepts(targetBox), nodeMatcher
        }

    }

    void dragNodeToSubset(String conceptKey, int subset, int box) {
        dragNodeToBox conceptKey, subsetBox(subset, box)
    }

    void selectAnalysis(String analysis) {
        tabSeparator('Advanced Workflow').click()
        extButton('Analysis').click()
        menuItem(analysis).click()
    }
}
