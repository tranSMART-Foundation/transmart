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

class SmartRBoxplotResult extends Page {

    static url = 'datasetExplorer/index'

    static at = {
        commonHeader.currentMenuItem?.text() == commonHeader.TOPMENU_ANALYZE
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
        smartrTabsActive { smartrTabs.find('li.ui-tabs-active') }
        smartrTabsFetch { smartrTabs.find('li.ui-tabs-active[text="Fetch Data"]') }
        smartrFetch { smartrTabs.find('div[ng-controller=BoxplotController') }

        smartrWorktabs { smartrControl.find('workflow-tab') }
        smartrWorktabsActive { smartrWorktabs.find('workflow-tab[aria-hidden=false]') }

        smartrWorktabsFetch { smartrWorktabs.find('workflow-tab[tab-name="Fetch Data"]') }
        smartrWorktabsFetchHighdim { smartrWorktabsFetch.find('concept-box[label="High Dimensional Variables"]') }
        smartrWorktabsFetchNumeric { smartrWorktabsFetch.find('concept-box[label="Numerical Variables"]') }
        smartrWorktabsFetchCategoric { smartrWorktabsFetch.find('concept-box[label="(optional) Categorical Variables"]') }
        smartrWorktabsFetchBiomarker { smartrWorktabsFetch.find('concept-box[label="High Dimensional Variables"]') }
        smartrWorktabsFetchFetchbutton { smartrWorktabsFetch.find('fetch-button') }

        smartrWorktabsRun { smartrWorktabs.find('workflow-tab[tab-name="Run Analysis"]') }
        smartrWorktabsRunTransform { smartrWorktabsRun.find('div.heim-input-field') }
        smartrWorktabsRunRunbutton { smartrWorktabsRun.find('run-button') }
        smartrWorktabsRunPlot { smartrWorktabsRun.find('boxplot') }
    }

    String activeTabName() {
        smartrTabsActive.text()
    }

    String activeWorktabName() {
        smartrWorktabsActive.text()
    }
    
}
