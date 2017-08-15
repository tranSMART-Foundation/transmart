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
        commonHeader.currentMenuItem?.text() == analyze.HEADER_TAB_NAME
        analyzeTab.tabSelected.text() == analyzeTab.ANALYZE_TAB_QUERY
        querySubsets.size() == 2
        analyzeTree.topNodes.size() > 0
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

        subsetBox { int subset, int box ->
            queryTable.find("div.panelModel", subset-1).find("div.panelBox", box-1)
        }
        subsetLast { int subset ->
            queryTable.find("div[subset='${subset}'].panelModel").find('div.panelBox').last()
        }
        boxConcepts (wait: true) { box ->
            box.find('div.panelBoxListItem').collect {
                it.attr('conceptid')
            }
        }
        tabSeparator { String text ->
            $('span.x-tab-strip-text').find {
                it.text() == text
            }
        }
    }

    void queryDragNodeToBox(String conceptKey, Navigator targetBox, nodeMatcher = null) {

        if(!analyzeTree.expand(conceptKey)) {
            return
        }

        interact {
            def sourceNode = analyzeTree.nodeText conceptKey

            assert !sourceNode.empty
            assert !targetBox.empty

            analyzeTree.scrollToBottom(sourceNode)
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
                
    void queryDragNodeToSubset(String conceptKey, int subset) {
        queryDragNodeToBox conceptKey, subsetLast(subset)
    }

    void queryDragNodeToSubsetBox(String conceptKey, int subset, int box) {
        queryDragNodeToBox conceptKey, subsetBox(subset, box)
    }

}

