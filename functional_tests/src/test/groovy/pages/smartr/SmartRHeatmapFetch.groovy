package pages.smartr

import geb.Page
import geb.waiting.WaitTimeoutException
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule
import pages.modules.AnalyzeTreeModule
import pages.modules.AnalyzeTabModule
import pages.modules.AnalyzeModule
import pages.modules.ScrollingModule

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

class SmartRHeatmapFetch extends Page {

    static url = 'datasetExplorer/index'

    static at = {
        println "AnalyzeQuery test header '${commonHeader.currentMenuItem?.text()}' expect '${commonHeader.TOPMENU_ANALYZE}'"
        commonHeader.currentMenuItem?.text() == commonHeader.TOPMENU_ANALYZE
        println "AnalyzeQuery test tab '${analyzeTab.tabSelected.text()}' expect '${analyzeTab.ANALYZE_TAB_SMARTR}'"
        analyzeTab.tabSelected.text() == analyzeTab.ANALYZE_TAB_SMARTR
    }

    static content = {
        commonHeader { module CommonHeaderModule }
        analyzeTab { module AnalyzeTabModule }
        analyzeTree { module AnalyzeTreeModule }
        analyze { module AnalyzeModule }
        scrolling { module ScrollingModule }
        workflows { $('select#sr-workflowSelect option') }

        smartrControl { $('div[ng-controller=HeatmapController') }

        smartrTabs { smartrControl.find('li.heim-tab') }

        smartrTabsActive { smartrTabs.filter('li.ui-tabs-active') }
        smartrTabsFetch { smartrTabs.filter('li[aria-controls="fragment-_fetch-_data"]') }
        smartrTabsRun { smartrTabs.filter('li[aria-controls="fragment-_run-_analysis"]') }
        smartrFetch { smartrTabs.find('div[ng-controller=HeatmapController') }

        smartrWorktabs { smartrControl.find('workflow-tab') }
        smartrWorktabsActive { smartrWorktabs.filter('workflow-tab[aria-hidden=false]') }
        smartrWorktabsFetch { smartrWorktabs.filter('workflow-tab[tab-name="Fetch Data"]') }

        smartrWorktabsFetchHighdim { smartrWorktabsFetch.find('concept-box[label="High Dimensional Variables"] div.sr-drop-input') }
        smartrWorktabsFetchNumeric { smartrWorktabsFetch.find('concept-box[label="Numerical Variables"] div.sr-drop-input') }
        smartrWorktabsFetchCategoric { smartrWorktabsFetch.find('concept-box[label="(optional) Categoric Variables"] div.sr-drop-input') }
        smartrWorktabsFetchBiomarker { smartrWorktabsFetch.find('biomarker-selection div#heim-input-list-identifiers') }
        smartrWorktabsFetchFetchbutton { smartrWorktabsFetch.find('fetch-button') }
        smartrWorktabsFetchComplete { smartrWorktabsFetchFetchbutton.find('span')[0] }

        smartrWorktabsFetchSummary { smartrWorktabsFetch.find('summary-stats') }
        smartrWorktabsFetchSummaryPlot { smartrWorktabsFetchSummary.find('img') }
        smartrWorktabsFetchSummaryLabels { smartrWorktabsFetchSummary.find('tr').find('td', 1).find('ul') }
        smartrWorktabsFetchSummarySubset1 { smartrWorktabsFetchSummary.find('tr').find('td', 2).find('ul') }
        smartrWorktabsFetchSummarySubset2 { smartrWorktabsFetchSummary.find('tr').find('td', 3).find('ul') }

        smartrWorktabsRun { smartrWorktabs.filter('workflow-tab[tab-name="Run Analysis"]') }
        smartrWorktabsRunTransform { smartrWorktabsRun.find('div.heim-input-field') }
        smartrWorktabsRunRunbutton { smartrWorktabsRun.find('run-button') } // create plot
        smartrWorktabsRunPlot { smartrWorktabsRun.find('heatmap-plot') }
        smartrWorktabsControls { smartrWorktabsRun.find('workflow-controls') }

        smartrHeatmapControlCluster { smartrWorktabsControls.find('select#sr-heatmap-cluster-select') }
    }

    String activeTabName() {
        smartrTabsActive.text()
    }

    String activeWorktabName() {
        smartrWorktabsActive.text()
    }
    
}
