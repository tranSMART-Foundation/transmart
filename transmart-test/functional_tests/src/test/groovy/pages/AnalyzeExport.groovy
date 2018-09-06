package pages

import geb.Page
import geb.waiting.WaitTimeoutException
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule
import pages.modules.AnalyzeTreeModule
import pages.modules.AnalyzeTabModule
import pages.modules.AnalyzeModule
import pages.AnalyzeWorkflow

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

class AnalyzeExport extends Page {

    static url = 'datasetExplorer/index'

    static at = {
        commonHeader.currentMenuItem?.text() == commonHeader.TOPMENU_ANALYZE
        analyzeTab.tabSelected.text() == analyzeTab.ANALYZE_TAB_EXPORT
        analyzeTree.topNodes.size() > 0
        exporttitle.text().startsWith("Instructions:")
    }

    static content = {
        commonHeader { module CommonHeaderModule }
        analyzeTab { module AnalyzeTabModule }
        analyzeTree { module AnalyzeTreeModule }
        analyze { module AnalyzeModule }

        exportall { $('div#dataTypesGridPanel table') }
        exporttitle { $('div#dataTypesGridPanel div.x-panel-header') }
        exporthead { exportall.find('tr.x-grid3-hd-row') }
        exportcols { exporthead.find('td') }

        exportrow { exportall.find('table.x-grid3-row-table tr') }
        exportdata  { thisrow -> thisrow.find('td.x-grid3-cell') }
    }

}

