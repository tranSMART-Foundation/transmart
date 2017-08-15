package pages

import geb.Page
import geb.waiting.WaitTimeoutException
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule
import pages.modules.UtilityModule

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

//import org.openqa.selenium.WebElement

class GwasPage extends Page {

    public static final String HEADER_TAB_NAME = 'GWAS'

    static url = '/transmart/GWAS/index'

    static at = {
        commonHeader.currentMenuItem?.text() == HEADER_TAB_NAME
        gwasTabs.size() == 8
        gwasResultsTable.size() > 0
    }

    static content = {
        gwas(wait: true) { $() }
        gwasResults(wait: true) { $('div#results-div') }
        gwasResultsTable(wait: true) { gwasResults.find('tr') }
        gwasTabs { $('div#menu_bar').children('div.toolbar-item') }
        commonHeader { module CommonHeaderModule }
        utility { module UtilityModule }
    }
    
}

