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

    def util = new Utilities()

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
        /* add a random number to avoid being locked out by repeated failures */

        usernameField.value Constants.BAD_USERNAME+(Math.abs(new Random().nextInt() % 9999) + 1)
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

