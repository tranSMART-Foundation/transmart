package pages

import geb.Page
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule

// $ function is $(css-selector,index-or-range,attribute-or-text-matchers)

class BrowsePage extends Page {
    public static final String HEADER_TAB_NAME = 'Browse'

    static url = 'RWG/index'

    static at = {
        commonHeader.headerTab()?.text() == HEADER_TAB_NAME
    }

    static content = {
        browse(wait: true)       { $() }
        divColorBox              { $('div#colorbox') }
        divSidebar               { $('div#sidebar') }
        divMain                  { $('div#main') }
        divHiddenItems           { $('div#hiddenitems') }
        divExportOverlay         { $('div#exportOverlay') }
        divEditMetadataOverlay   { $('div#editMetadataOverlay') }
        divCreateAnalysisOverlay { $('div#createAnalysisOverlay') }
        divCreateAssayOverlay    { $('div#createAssayOverlay') }
        divCreateFolderOverlay   { $('div#createFolderOverlay') }
        divCreateStudyOverlay    { $('div#createStudyOverlay') }
        divCreateProgramOverlay  { $('div#createProgramOverlay') }
        divBrowsePopups          { $('div#divBrowsePopups') }
        divPlotOptions           { $('div#divPlotOptions') }
        divSidebarToggle         { $('div#sidebartoggle') }
        divFilterBrowser         { $('div#filter-browser') }

        commonHeader { module CommonHeaderModule }

    }

}
