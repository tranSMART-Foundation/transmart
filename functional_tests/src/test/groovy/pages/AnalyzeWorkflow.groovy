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

class AnalyzeWorkflow extends Page {

    static url = 'datasetExplorer/index'

    static at = {
        commonHeader.currentMenuItem?.text() == analyze.HEADER_TAB_NAME
        analyzeTab.tabSelected.text() == analyzeTab.ANALYZE_TAB_WORKFLOWS
    }

    static content = {
        commonHeader { module CommonHeaderModule }
        analyzeTab { module AnalyzeTabModule }
        analyzeTree { module AnalyzeTreeModule }
        analyze { module AnalyzeModule }

        selectedAnalysis (wait: true) {
            $('span#selectedAnalysis')?.text()
        }
        menuItem { String text ->
            $('a.x-menu-item').find {
                it.text().trim() == text
            }
        }
        boxConcepts (wait: true) { box ->
            box.find('div.panelBoxListItem').collect {
                it.attr('conceptid')
            }
        }
        analysisImages {
            $('#analysisOutput img')
        }
        extButton { String text ->
            $('button.x-btn-text').find {
                it.text() == text
            }
        }
    }

    Boolean workflowDragNodeToBox(String conceptKey, Navigator targetBox, nodeMatcher = null) {

        assert analyzeTree.expand(conceptKey) : "Failed to find concept '${conceptKey}' in NavigationTree"

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

    void selectAnalysis(String analysis) {
        extButton('Analysis').click()
        menuItem(analysis).click()
    }

}
