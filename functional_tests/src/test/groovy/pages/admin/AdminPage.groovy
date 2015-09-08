package pages.admin

import geb.Page
import geb.waiting.WaitTimeoutException
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

//import org.openqa.selenium.WebElement

class AdminPage extends Page {

    public static final String HEADER_TAB_NAME = 'Admin'

    static url = '/transmart/accessLog/index'

    static at = {
        currentHeaderTab()?.text() == HEADER_TAB_NAME
    }

    static content = {
        admin(wait: true) { $() }

        commonHeader { module CommonHeaderModule }
    }
}

