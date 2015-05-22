package pages

import geb.Page
import geb.waiting.WaitTimeoutException
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

//import org.openqa.selenium.WebElement

class GenesigPage extends Page {

    public static final String HEADER_TAB_NAME = 'Gene Signature/Lists'

    static url = 'geneSignature/list'

    static at = {
        commonHeader.headerTab()?.text() == HEADER_TAB_NAME
    }

    static content = {
        genesig(wait: true) { $() }

        commonHeader { module CommonHeaderModule }
    }
    
}

