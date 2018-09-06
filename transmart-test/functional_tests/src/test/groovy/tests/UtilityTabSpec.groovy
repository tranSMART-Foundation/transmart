package tests

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import pages.modules.LoginFormModule
import pages.modules.UtilityModule

import functions.Constants

import pages.LoginPage
import pages.BrowsePage
import pages.BrowseResultsPage

@Stepwise
class UtilityTabSpec extends GebReportingSpecTransmart {

    def setupSpec() {
        loginTransmart(Constants.LANDING_PAGE.class)
    }
    
    def "start on the Browse tab"() {

        when:
        isAt(Constants.LANDING_PAGE.class)

        then:
        assert at(Constants.LANDING_PAGE.class)
    }

    def "click on the Utilities tab"() {
        utility { module UtilityModule }

        when:
        utility.tableMenuUtilities.click()
        int menuSize = utility.utilitiesMenuSize()

// Report a Bug displayed if bugreportURL is configured
// With 5 items, check all the others
// With 6 items, also check for 'report a Bug'

        then:
        menuSize == 5 || menuSize == 6
        and:
        utility.utilitiesHelp()
        and:
        menuSize == 5 || utility.utilitiesBug()
        and:
        utility.utilitiesContact()
        and:
        utility.utilitiesAbout()
        and:
        utility.utilitiesPassword()
        and:
        utility.utilitiesLogout()
    }

    def "logout gives login page"() {
        when:
        utility.utilitiesDoLogout()

        then:
        assert at(LoginPage)
    }
    
    def "login as admin"() {
        when:
        loginForm { module LoginFormModule }
            
        loginForm.usernameField.value Constants.ADMIN_USERNAME
        loginForm.passwordField.value Constants.ADMIN_PASSWORD

        loginForm.loginButton.click()

        then:
        assert at(Constants.LANDING_PAGE.class)

    }

    def cleanupSpec() {
    }

}
