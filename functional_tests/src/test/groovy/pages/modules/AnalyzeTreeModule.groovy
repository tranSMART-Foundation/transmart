package pages.modules

import com.google.common.collect.*
import geb.Module
import geb.navigator.Navigator
import org.openqa.selenium.WebElement

import functions.Constants

/**
 * Created by peter.rice@transmartfoundation.org on 25-jul-2017
 */
class AnalyzeTreeModule extends Module {

    static base = { $('div#navigateTermsPanel') }

    static content = {
        topTreeNode { $() }
        rootNode { $('div.x-tree-root-node') }
        topNodes { rootNode.children('li.x-tree-node') }
    
        treeNodes { startKey -> startKey.find("div[ext\\:tree-node-id] a").parent() }

        nodeText { contextKey ->
            $("div[ext\\:tree-node-id] a").find {
                it.parent().jquery.attr('ext:tree-node-id') == contextKey
            }[0]
        }
    }

    static boolean canExpand(node) {
        'x-tree-elbow-end-plus' in node.classes() ||
                'x-tree-elbow-plus' in node.classes()
    }

    def Boolean expand(String conceptKey) {
        def parts = conceptKey.split('\\\\') as List

        def lastTreeNode = topTreeNode(); 
        def image = null
        for (int i = 3; i < parts.size(); i++) {
            def key = parts[0..i].join('\\') + '\\'
            def treeNodes = treeNodes(lastTreeNode);
            image = null
            treeNodes.each {nodeKey ->
                if(nodeKey.attr('ext:tree-node-id') == key) {
                    image = nodeKey.children('img').first()
                    lastTreeNode = nodeKey.next('ul')
                }
            }
            if(image == null) {
                return false
            }

            if (canExpand(image)) {
                def parent = image.parent()
                scrollToBottom(parent)
                image.click()
            }
        }
        return (image != null)
    }

    void scrollToBottom(Navigator hiddenNode) {
        WebElement element = hiddenNode.firstElement()
        browser.driver.executeScript("arguments[0].scrollIntoView(false);", element)
    }
}
