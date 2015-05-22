import geb.junit4.GebReportingTest
import geb.navigator.Navigator
import org.junit.Test

import functions.Constants

import pages.LoginFailedPage
import pages.LoginPage
import pages.BrowsePage

import pages.modules.CommonHeaderModule

import geb.spock.GebSpec
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class LoginSpec extends GebReportingSpec {

    def "go to login screen"() {

        when:
        
        if (Constants.AUTO_LOGIN_ENABLED) {

            /* Auto login enabled: we have to logout first */
            to Constants.LANDING_PAGE.class

            //Utility menu, logout
            selectLogout()

        }
        else {
    
            to LoginPage

        }

        then:
        
        assert at(LoginPage)
    }
    
    def "login with bad credentials"() {
        /* Trying login page with bad credentials */
        /* add a random number to avoid being locked out by repeated failures */

        when:
        usernameField.value Constants.BAD_USERNAME+(Math.abs(new Random().nextInt() % 9999) + 1)
        passwordField.value Constants.BAD_PASSWORD

        loginButtonFailed.click()

        then:
        assert at(LoginFailedPage)

        assert topMessage == 'Please login...' : "unexpected login prompt"
        assert errorMessage.contains('Login has failed') ||
               errorMessage.contains('Your account has been locked') :
                       "unexpected login error message"
        
    }

    def "login with good credentials"()
    {

        when:
        usernameField.value Constants.GOOD_USERNAME
        passwordField.value Constants.GOOD_PASSWORD
        loginButtonLanding.click()

        then:
        assert at(Constants.LANDING_PAGE.class)
    }

   void selectLogout ()
    {
        
        commonHeader { module CommonHeaderModule }

        commonHeader.tableMenuUtilities.click()

        commonHeader.utilitiesDoLogout()
        assert at(LoginPage)
    }

}

