package pages.smartr

import geb.Page
import geb.waiting.WaitTimeoutException
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule
import pages.modules.AnalyzeTreeModule
import pages.modules.AnalyzeTabModule
import pages.modules.AnalyzeModule

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

class SmartRBoxplotFetch extends Page {

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
        workflows { $('select#sr-workflowSelect option') }

        smartrControl { $('div[ng-controller=BoxplotController') }

        smartrTabs { smartrControl.find('li.heim-tab') }

        smartrTabsActive { smartrTabs.filter('li.ui-tabs-active') }
        smartrTabsFetch { smartrTabs.filter('li[aria-controls="fragment-_fetch-_data"]') }
        smartrTabsRun { smartrTabs.filter('li[aria-controls="fragment-_run-_analysis"]') }
        smartrFetch { smartrTabs.find('div[ng-controller=BoxplotController') }

        smartrWorktabs { smartrControl.find('workflow-tab') }
        smartrWorktabsActive { smartrWorktabs.filter('workflow-tab[aria-hidden=false]') }
        smartrWorktabsFetch { smartrWorktabs.filter('workflow-tab[tab-name="Fetch Data"]') }

        smartrWorktabsFetchHighdim { smartrWorktabsFetch.find('concept-box[label="High Dimensional Variables"]') }
        smartrWorktabsFetchNumeric { smartrWorktabsFetch.find('concept-box[label="Numerical Variables"]') }
        smartrWorktabsFetchCategoric { smartrWorktabsFetch.find('concept-box[label="(optional) Categorical Variables"]') }
        smartrWorktabsFetchBiomarker { smartrWorktabsFetch.find('concept-box[label="High Dimensional Variables"]') }
        smartrWorktabsFetchFetchbutton { smartrWorktabsFetch.find('fetch-button') }
        smartrWorktabsFetchComplete { smartrWorktabsFetchFetchbutton.find('span') }

        smartrWorktabsFetchSummary { smartrWorktabsFetch.find('summary-stats') }
        smartrWorktabsFetchSummaryPlot { smartrWorktabsFetchSummary.find('img') }
        smartrWorktabsFetchSummaryLabels { smartrWorktabsFetchSummary.find('tr').find('td', 1).find('ul') }
        smartrWorktabsFetchSummarySubset1 { smartrWorktabsFetchSummary.find('tr').find('td', 2).find('ul') }
        smartrWorktabsFetchSummarySubset2 { smartrWorktabsFetchSummary.find('tr').find('td', 3).find('ul') }

        smartrWorktabsRun { smartrWorktabs.filter('workflow-tab[tab-name="Run Analysis"]') }
        smartrWorktabsRunTransform { smartrWorktabsRun.find('div.heim-input-field') }
        smartrWorktabsRunRunbutton { smartrWorktabsRun.find('run-button') } // create plot
        smartrWorktabsRunPlot { smartrWorktabsRun.find('boxplot').find('run-button') }

        smartrBoxplotTitle { smartrWorktabsRun.find('text.gtitle') }
    }

    String activeTabName() {
        smartrTabsActive.text()
    }

    String activeWorktabName() {
        smartrWorktabsActive.text()
    }
    
}
