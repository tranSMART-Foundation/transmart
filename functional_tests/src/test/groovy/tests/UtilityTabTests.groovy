import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import geb.junit4.GebReportingTest

import org.junit.Test

import pages.modules.CommonHeaderModule

import functions.Utilities

import functions.Constants

import pages.LoginPage
import pages.BrowsePage
import pages.BrowseResultsPage

import geb.spock.GebSpec
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class UtilityTabSpec extends GebReportingSpec {

    def "start on the Browse tab"() {

        def util = new Utilities()

        when:
        util.goToPageMaybeLogin(BrowsePage)

        then:
        assert at(BrowsePage)

    }

    def "click on the Utilities tab"() {
        commonHeader { module CommonHeaderModule }

        when:
        commonHeader.tableMenuUtilities.click()

        then:
        commonHeader.utilitiesMenuSize() == 5
        and:
        commonHeader.utilitiesHelp()
        and:
        commonHeader.utilitiesContact()
        and:
        commonHeader.utilitiesAbout()
        and:
        commonHeader.utilitiesPassword()
        and:
        commonHeader.utilitiesLogout()
    }

    def "logout gives login page"() {
        when:
        commonHeader.utilitiesDoLogout()

        then:
        assert at(LoginPage)
    }
    
    def "login as admin"() {
        when:
        usernameField.value Constants.ADMIN_USERNAME
        passwordField.value Constants.ADMIN_PASSWORD

        loginButton.click()

        then:
        assert at(Constants.LANDING_PAGE.class)

    }

}
