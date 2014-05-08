package pages

import geb.Page
import geb.waiting.WaitTimeoutException
import pages.modules.NavigationBarModule

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

class DatasetExplorerPage extends Page {

    public static final String NAV_TAB_NAME = 'Dataset Explorer'

    static url = 'datasetExplorer/index'

    static at = {
        navigationBar.currentNavTab == NAV_TAB_NAME
    }

    static content = {
        navigationBar { module NavigationBarModule }
        nodePlus(wait: true) { contextKey ->
            /* couldn't get it to work without jquery */
            $("div[ext\\:tree-node-id]").find {
                it.jquery.attr('ext:tree-node-id') == contextKey
            }.children('img')[0]
        }
        nodeText(wait: true) { contextKey ->
            $("div[ext\\:tree-node-id] a").find {
                it.parent().jquery.attr('ext:tree-node-id') == contextKey
            }[0]
        }
        subsetBox(wait: true) { int subset, int box ->
            $("div#queryCriteriaDiv${subset}_${box}")
        }
        boxConcepts (wait: true) { box ->
            box.children('div').collect {
                it.attr('conceptid')
            }
        }
        tabSeparator { String text ->
            $('span.x-tab-strip-text').find {
                it.text() == text
            }
        }
        extButton { String text ->
            $('button.x-btn-text').find {
                it.text() == text
            }
        }
        menuItem { String text ->
            $('a.x-menu-item').find {
                it.text().trim() == text
            }
        }
        selectedAnalysis {
            $('span#selectedAnalysis')?.text()
        }
        analysisImages {
            $('#analysisOutput img')
        }
    }

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
