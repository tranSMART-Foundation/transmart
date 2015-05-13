package functions

import geb.junit4.GebReportingTest
import junit.framework.AssertionFailedError

import geb.Module
import geb.Page

import functions.Constants

import pages.LoginPage

import pages.modules.CommonHeaderModule

/**
 * Created by price on 19-12-2014.
 */

class Utilities extends GebReportingTest {
    
    void toLandingPage() {

        via Constants.LANDING_PAGE.class

        if(at(LoginPage)) {

            usernameField.value Constants.GOOD_USERNAME
            passwordField.value Constants.GOOD_PASSWORD
            loginButton.click()

            assert at(Constants.LANDING_PAGE.class)
        }

    }

    void login(Class<? extends Page> redirectionPage) {

        usernameField.value Constants.GOOD_USERNAME
        passwordField.value Constants.GOOD_PASSWORD

        loginButtonNoTo.click()

        at(redirectionPage)
    }

    void goToPageMaybeLogin(Class<? extends Page> page, boolean firstCall = true) {
        via page

        if (isAt(page)) {
            /* Already at required page */
            return
        } else if (isAt(LoginPage)) {
            /* At login page - login with user/password */
            login(page)
        } else if (isAt(Constants.LANDING_PAGE.class)) {
            /* Unexpectedly at landing page when not wanted */
            if (!firstCall) {
                throw new AssertionFailedError('Redirection loop')
            }
            /* if auto-login is on, we're unfortunately forwarded here */
            goToPageMaybeLogin(page, false)
        } else {
            throw new AssertionFailedError(
                    "Expected to be at either the LoginPage, $Constants.LANDING_PAGE or $page")
        }
    }

}


