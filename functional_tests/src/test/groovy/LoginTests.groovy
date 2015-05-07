import geb.junit4.GebReportingTest
import geb.navigator.Navigator
import org.junit.Test

import functions.Utilities

import pages.Constants
import pages.LoginFailedPage
import pages.LoginPage
import pages.BrowsePage

import pages.modules.CommonHeaderModule

class LoginTests extends GebReportingTest {

    def util = new LandingPageLogin()

    @Test
    void testFailedLogin() {

        if (Constants.AUTO_LOGIN_ENABLED) {

            /* Auto login enabled: we have to logout first */
            to Constants.LANDING_PAGE.class

            //Utility menu, logout
            selectLogout()

            assert at(LoginPage)
        }
        else {
    
            to LoginPage

        }
        
        /* Trying login page with bad credentials */

        usernameField.value Constants.BAD_USERNAME
        passwordField.value Constants.BAD_PASSWORD
        loginButton.click()

        assert at(LoginFailedPage)
    }

    @Test
    void testSuccessfulLogin() {

        if (Constants.AUTO_LOGIN_ENABLED) {
            /* Auto login enabled: we have to logout first */
            to Constants.LANDING_PAGE.class

            //Utility menu, logout
            selectLogout()

            assert at(LoginPage)
        }
        else {
            to LoginPage
	}

        usernameField.value Constants.GOOD_USERNAME
        passwordField.value Constants.GOOD_PASSWORD
        loginButton.click()

        assert at(Constants.LANDING_PAGE.class)
    }

    void selectLogout ()
    {
        commonHeader { module CommonHeaderModule }

        commonHeader.tableMenuUtilities.click()

        commonHeader.utilitiesDoLogout()
    }

}

