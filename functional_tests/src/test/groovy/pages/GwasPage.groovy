package pages

import geb.Page
import geb.waiting.WaitTimeoutException
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

//import org.openqa.selenium.WebElement

class GwasPage extends Page {

    public static final String HEADER_TAB_NAME = 'GWAS'

    static url = 'GWAS/index'

    static at = {
        commonHeader.headerTab()?.text() == HEADER_TAB_NAME
    }

    static content = {
        gwas(wait: true) { $() }

        commonHeader { module CommonHeaderModule }
    }
    
}

