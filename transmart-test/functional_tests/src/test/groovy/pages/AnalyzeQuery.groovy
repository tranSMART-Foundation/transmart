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

class AnalyzeQuery extends Page {

    static url = 'datasetExplorer/index'

    static at = {
        println "AnalyzeQuery test header '${commonHeader.currentMenuItem?.text()}' expect '${commonHeader.TOPMENU_ANALYZE}'"
        commonHeader.currentMenuItem?.text() == commonHeader.TOPMENU_ANALYZE
        println "AnalyzeQuery test tab '${analyzeTab.tabSelected.text()}' expect '${analyzeTab.ANALYZE_TAB_QUERY}'"
        analyzeTab.tabSelected.text() == analyzeTab.ANALYZE_TAB_QUERY
        querySubsets.size() >= 2
        analyzeTree.topNodes.size() > 0
        analyzeTree.nodeText('\\\\Public Studies\\Public Studies\\')
    }

    static content = {
        commonHeader { module CommonHeaderModule }
        analyzeTab { module AnalyzeTabModule }
        analyzeTree { module AnalyzeTreeModule }
        analyze { module AnalyzeModule }

        
        queryTable(wait: true) { $('table#queryTable') }
        querySubsets(wait: true)  { queryTable.find('div.panelModel') }
        querySubset1  { querySubsets.first() }
        querySubset2  { querySubsets.last() }
        queryPanels1   { querySubset1.find('div.panelBox', 0) }
        queryPanels2   { querySubset2.find('div.panelBox', 1) }

        queryHidome   { $('div#omicsFilterWindow') }
        queryHidomePanel   { $('div#omicsfilterPanel') }
        queryHidomeContent { $('div#highdimension-filter-content') }
        queryHidomeDiv   { queryHidome.children('div') }
        queryHidomeType   { queryHidomeDiv[0].find('span.x-window-header-text') }
        // 'Platform' text
        // 'Search in' select
        // 'Search term' input
        // 'Query on' select
        queryHidomeField { field ->
            println "queryHidomeField search for '${field}'"
            queryHidomeContent.find('tr').find {
                println "queryHidomeField test '${it.find('td')[0].text()}'"
                it.find('td')[0].text() == field
            }.find('td')[1]
        }
        queryHidomeSlider    { queryHidomeContent.find('div#highdimension-range') }
        queryHidomeSliderMin { queryHidomeContent.find('input#highdimension-amount-min') }
        queryHidomeSliderMax { queryHidomeContent.find('input#highdimension-amount-max') }
        
        queryHidomeSubjects  { queryHidomeContent.find('span#highdimension-filter-subjectcount') }
        queryHidomeGene      { geneName ->
            println "queryHidomeGene '${geneName}'"
            queryHidome.find('span.category-gene').find {
                println "compare '${it.text()}'"
                it.text() == geneName
            }
            
        }
        queryHidomeButton   { buttonName ->
            println "queryHidomeButton '${buttonName}'"
            queryHidomeDiv[1].children('div')[1].find('td.x-btn-center button').find{
                println "testing text '${it.text()}'"
                it.text() == buttonName
            }
        }

        subsetBox { int subset, int box ->
            queryTable.find("div.panelModel", subset-1).find("div.panelBox", box-1)
        }
        subsetLast { int subset ->
            queryTable.find("div[subset='${subset}'].panelModel").find('div.panelBox').last()
        }
        tabSeparator { String text ->
            $('span.x-tab-strip-text').find {
                it.text() == text
            }
        }
    }

    void queryDragNodeToSubset(String conceptKey, int subset) {
        analyzeTree.treeDragNodeToBox conceptKey, subsetLast(subset)
    }

    void queryDragNodeToSubsetBox(String conceptKey, int subset, int box) {
        analyzeTree.treeDragNodeToBox conceptKey, subsetBox(subset, box)
    }

}
