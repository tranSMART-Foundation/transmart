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

class AnalyzeGridview extends Page {

    static url = 'datasetExplorer/index'

    static at = {
        commonHeader.currentMenuItem?.text() == commonHeader.TOPMENU_ANALYZE
        analyzeTab.tabSelected.text() == analyzeTab.ANALYZE_TAB_GRIDVIEW
        analyzeTree.topNodes.size() > 0
        gridtitle.text() == "Grid View"
    }

    static content = {
        commonHeader { module CommonHeaderModule }
        analyzeTab { module AnalyzeTabModule }
        analyzeTree { module AnalyzeTreeModule }
        analyze { module AnalyzeModule }

        gridview { $('div#gridView table') }
        gridtitle { $('div#gridView div.x-panel-header') }
        gridhead { gridview.find('tr.x-grid3-hd-row') }
        gridcols { gridhead.find('td') }

        gridsort(required: false) { gridcols.findAll { it.hasClass('sort-asc') || it.hasClass('sort-desc') } }
        gridup(required: false) { gridcols.findAll{ it.hasClass('sort-asc') } }
        griddown(required: false) { gridcols.findAll{ it.hasClass('sort-desc') } }
        
        gridrow { gridview.find('table.x-grid3-row-table tr') }
        griddata  { thisrow -> thisrow.find('td.x-grid3-cell') }
    }

}

