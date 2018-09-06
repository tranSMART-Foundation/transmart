package pages

import geb.Page
import geb.waiting.WaitTimeoutException
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule
import pages.modules.AnalyzeTreeModule
import pages.modules.AnalyzeTabModule
import pages.modules.AnalyzeModule

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

class AnalyzeSmartR extends Page {

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

        selectedAnalysis { workflows.value() }
        resetButton { $('button#sr-reset-btn').click() }

        menuItem { String text ->
            println "Looking for SmartR workflow '$text'"
            workflows.find {
                println "Checking SmartR workflow value '${it.value()}' text '${it.text()}'"
                it.value().trim() == text || it.text().trim() == text 
            }
        }
        boxConcepts (wait: true) { box ->
            box.find('div.panelBoxListItem').collect {
                it.attr('conceptid')
            }
        }
    }

    void selectSmartRAnalysis(String analysis) {
        menuItem(analysis).click()
    }

}
