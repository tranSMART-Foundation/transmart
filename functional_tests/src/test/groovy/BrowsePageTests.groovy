import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import geb.junit4.GebReportingTest

import org.junit.Test
import org.junit.Ignore

import pages.modules.CommonHeaderModule

import functions.Utilities

import pages.Constants
import pages.LoginPage
import pages.BrowsePage
import pages.BrowseResultsPage

class BrowsePageTests extends GebReportingTest {

    //TODO: tests with Ignore need to be fixed

    def util = new Utilities()

    @Test
    void BrowseTab() {

        util.goToPageMaybeLogin(BrowsePage)

        assert at(BrowsePage)

    }

    // perhaps this test should be moved to a new Test file: UtilityTabTests ??
    @Ignore
    @Test
    void BrowseTabUtilities() {

        util.goToPageMaybeLogin(BrowsePage)

        assert at(BrowsePage)

        commonHeader { module CommonHeaderModule }

        commonHeader.tableMenuUtilities.click()

        // check the Utilities menu has the expected content
        commonHeader.utilitiesList()

        assert commonHeader.utilitiesHelp()
        assert commonHeader.utilitiesContact()
        assert commonHeader.utilitiesLogout()
        assert commonHeader.utilitiesPassword()
        assert commonHeader.utilitiesAbout()

        commonHeader.utilitiesDoLogout()

        assert at(LoginPage)

        usernameField.value Constants.GOOD_USERNAME
        passwordField.value Constants.GOOD_PASSWORD

        //this tests for the landing page or a failure
        loginButton.click()

        //make sure it was a success!
        assert at(Constants.LANDING_PAGE.class)
    }

}
