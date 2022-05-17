package pages

import geb.Page
import geb.navigator.Navigator

import pages.modules.BrowseTreeModule
import pages.modules.CommonHeaderModule
import pages.modules.UtilityModule

class BrowsePage extends Page {
    static url = 'RWG/index'

    static at = {
        println "BrowsePage test"
        println "BrowsePage test '${commonHeader.currentMenuItem?.text()}' expect '${commonHeader.TOPMENU_BROWSE}'"
        commonHeader.currentMenuItem?.text() == commonHeader.TOPMENU_BROWSE
	println "browseTree.programs.size test"
	println "browseTree ${browseTree}"
	println "browseTree.programs ${browseTree?.programs}"
	println "browseTree.programs.size value ${browseTree?.programs?.size()}"
        browseTree?.programs?.size() >= 0
    }

    static content = {
        visited { $('th.menuVisited') }
        browseTree { module BrowseTreeModule }
        commonHeader { module CommonHeaderModule }
        utility { module UtilityModule }
    }

}
