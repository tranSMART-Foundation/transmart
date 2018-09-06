package pages

import geb.Page
import geb.navigator.Navigator

import pages.modules.BrowseTreeModule
import pages.modules.CommonHeaderModule
import pages.modules.ScrollingModule
import pages.modules.UtilityModule

class BrowseAnalysis extends Page {

    public static final String HEADER_TAB_NAME = 'Browse'

    static url = 'RWG/index'

    static at = {
        commonHeader.currentMenuItem?.text() == commonHeader.TOPMENU_BROWSE
        browseTree.programs.size() > 0
        println "Found ${browseTree.programs.size()} programs"
        println "Test program view metadataHead '${metadataHead.text()}'"
        metadataHead.text().startsWith("Analysis: ")
    }

    static content = {
        folderViewer   { $('div#folder-viewer') }

        metadataViewer { $('div#metadata-viewer') }
        metadataDesc   { metadataViewer.find('div.description') }
        metadataHead   { metadataViewer.find('h3.rdc-h3') }
        metadataDetail { metadataViewer.find('h4.rdc-h4') }
        metadataTable  { metadataViewer.find('table.details-table') }

        assocViewer    { metadataViewer.find('div.dataTables_wrapper') }
        assocTitle     { metadataViewer.find('div.gridTitle') }
        assocTable     {assocViewer.find('table.dataTable') } // 2 tables: head then rows

        browseTree { module BrowseTreeModule }
        commonHeader { module CommonHeaderModule }
        scrolling { module ScrollingModule }
        utility { module UtilityModule }
    }


}
