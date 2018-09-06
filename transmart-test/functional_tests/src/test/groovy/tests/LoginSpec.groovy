package tests

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import geb.navigator.Navigator

import junit.framework.AssertionFailedError

import functions.Constants

import pages.LoginFailedPage
import pages.LoginPage
import pages.BrowsePage

import pages.modules.LoginFormModule
import pages.modules.UtilityModule


@Stepwise
class LoginSpec extends GebReportingSpecTransmart {
    def setupSpec() {
    }
    

    def "go to login screen"() {

        when:
        
        if (Constants.AUTO_LOGIN_ENABLED) {

            println "Auto login enabled: we have to logout first"
            /* Auto login enabled: we have to logout first */

            to Constants.LANDING_PAGE.class

            println "At landing page"

//            at(Constants.LANDING_PAGE.class)

            utility { module UtilityModule }

//            utility.tableMenuUtilities.click()
//            utility.utilitiesDoLogout()
            println "selectLogout run"
            to(LoginPage)
        }
        else {
            println "Login required (no auto login)"
            to LoginPage
            println "should be at LoginPage"
        }

        then:
        
        assert at(LoginPage)
        println "assert at(LoginPage) passed"
    }
    
    def "login with bad credentials"() {
        /* Trying login page with bad credentials */
        /* add a random number to avoid being locked out by repeated failures */

        when:
        loginForm { module LoginFormModule }
        loginForm.usernameField.value Constants.BAD_USERNAME+(Math.abs(new Random().nextInt() % 9999) + 1)
        loginForm.passwordField.value Constants.BAD_PASSWORD

        loginForm.loginButtonFailed.click()

        then:
        assert at(LoginFailedPage)

        assert loginForm.usernameLabel.text() == "Username :" : "unexpected login prompt"
        assert loginForm.errorMessage.contains('Login has failed') ||
               loginForm.errorMessage.contains('Your account has been locked') :
                       "unexpected login error message"
        
    }

    def "login with good credentials"()
    {

        when:
        loginForm { module LoginFormModule }
        loginForm.usernameField.value Constants.GOOD_USERNAME
        loginForm.passwordField.value Constants.GOOD_PASSWORD

        loginForm.loginButtonLanding.click()

        then:
        assert at(Constants.LANDING_PAGE.class)
    }

    def cleanupSpec() {
    }

}
