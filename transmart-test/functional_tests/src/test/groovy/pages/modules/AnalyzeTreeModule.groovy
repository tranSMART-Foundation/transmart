package pages.modules

import com.google.common.collect.*
import geb.Module
import geb.navigator.Navigator
import org.openqa.selenium.WebElement
import geb.waiting.WaitTimeoutException

import functions.Constants
import pages.modules.ScrollingModule

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

/**
 * Created by peter.rice@transmartfoundation.org on 25-jul-2017
 */
class AnalyzeTreeModule extends Module {

    static base = { $('div#navigateTermsPanel') }

    static content = {
        scrolling { module ScrollingModule }

        topTreeNode { $() }
        rootNode { $('div.x-tree-root-node') }
        topNodes { rootNode.children('li.x-tree-node') }
    
        treeNodes (wait: true) { startKey -> startKey.find("div[ext\\:tree-node-id] a").parent() }

        nodeText { contextKey ->
            $("div[ext\\:tree-node-id] a").find {
                it.parent().attr('ext:tree-node-id') == contextKey
            }[0]
        }
        boxConcepts (wait: true) { box ->
            println "collect boxConcepts"
            box.find('div.panelBoxListItem').collect {
                println "boxConcepts found '${it.attr('conceptid')}'"
                it.attr('conceptid')
            }
        }
    }

    static boolean treeCanExpand(node) {
        'x-tree-elbow-end-plus' in node.classes() ||
                'x-tree-elbow-plus' in node.classes()
    }

    def Boolean treeExpand(String conceptKey) {
        def parts = conceptKey.split('\\\\') as List

        def lastTreeNode = topTreeNode(); 
        def image = null
        println "treeExpand parts ${parts.size()} '${conceptKey}'"
        for (int i = 3; i < parts.size(); i++) {
            def key = parts[0..i].join('\\') + '\\'
            println "look for key ${i} '${key}'"
            def treeNodes = treeNodes(lastTreeNode);
            image = null
            treeNodes.each {nodeKey ->
                if(nodeKey.attr('ext:tree-node-id') == key) {
                    println "key found"
                    image = nodeKey.children('img').first()
                    lastTreeNode = nodeKey.next('ul')
                }
            }
            if(image == null) {
                println "key not found"
                return false
            }

            if (treeCanExpand(image)) {
                println "expanding node"
                def parent = image.parent()
                scrolling.scrollToBottom(parent)
                image.click()
            }
        }
        println "return image ${image}"
        return (image != null)
    }

    void treeDragNodeToBox(String conceptKey, Navigator targetBox, nodeMatcher = null, Boolean scrollUp = false) {

        println "treeDragNodeToBox '${conceptKey}'"
        if(!treeExpand(conceptKey)) {
            println "treeExpand failed"
            return
        }

        interact {
            def sourceNode = nodeText conceptKey
            println "sourceNode '${sourceNode}'"
            assert !sourceNode.empty
            assert !targetBox.empty
            println "location target X:${targetBox.getX()} Y:${targetBox.getY()} width ${targetBox.getWidth()} height ${targetBox.getHeight()}"
            scrollUp ? scrolling.scrollToTop(targetBox) : scrolling.scrollToBottom(targetBox)
            println "moved to target X:${targetBox.getX()} Y:${targetBox.getY()} width ${targetBox.getWidth()} height ${targetBox.getHeight()}"
            println "targetBox scrolled into view"

            println "location source X:${sourceNode.getX()} Y:${sourceNode.getY()} width ${sourceNode.getWidth()} height ${sourceNode.getHeight()}"
            scrollUp ? scrolling.scrollToTop(sourceNode) : scrolling.scrollToBottom(sourceNode)
            println "moved to source X:${sourceNode.getX()} Y:${sourceNode.getY()} width ${sourceNode.getWidth()} height ${sourceNode.getHeight()}"
            println "sourceNode scrolled into view"
            dragAndDrop sourceNode, targetBox
            println "sourceNode dragged to targetBox"
        }

        /* An expansion of the nodes may be needed for categorical variables,
         * and this takes some time - test them for startsWith
         * as leaf nodes are longer than the node we dropped*/
        try {
            waitFor {
                println "run test"
                if(nodeMatcher == null) {
                    boxConcepts(targetBox).findAll{
                        it.startsWith(conceptKey)
                    }
                } else {
                    nodeMatcher.matches(boxConcepts(targetBox))
                }
            }
        } catch (WaitTimeoutException wte) {
            // so we get a nice message
            if(nodeMatcher == null) {
                assertThat boxConcepts(targetBox), "findAll(it.startsWith('${conceptKey}'))"
            } else {
                assertThat boxConcepts(targetBox), nodeMatcher
            }
        }

    }

}
