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
        println "BrowsePage test '${commonHeader.currentMenuItem?.text()}' == '${commonHeader.TOPMENU_BROWSE}'"
        commonHeader.currentMenuItem?.text() == commonHeader.TOPMENU_BROWSE
        browseTree.programs.size() > 0
    }

    static content = {
        visited { $('th.menuVisited') }
        browseTree { module BrowseTreeModule }
        commonHeader { module CommonHeaderModule }
        utility { module UtilityModule }
    }

}
