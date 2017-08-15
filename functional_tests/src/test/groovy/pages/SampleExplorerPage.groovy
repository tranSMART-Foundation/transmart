package pages

import geb.Page
import geb.waiting.WaitTimeoutException
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule
import pages.modules.UtilityModule

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

//import org.openqa.selenium.WebElement

class SampleExplorerPage extends Page {

    public static final String HEADER_TAB_NAME = 'Sample Explorer'

    static url = 'sampleExplorer/list'

    static at = {
        commonHeader.currentMenuItem()?.text() == HEADER_TAB_NAME
        byId.text() == "By ID"
    }

    static content = {
        sampleExplorer(wait: true) { $() }

        commonHeader { module CommonHeaderModule }
        utility { module UtilityModule }

        byId  { $("tbody", text: "By ID") }
    }
    
}

