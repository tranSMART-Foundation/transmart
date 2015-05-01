package tests

import geb.Page
import geb.junit4.GebReportingTest
import junit.framework.AssertionFailedError
import pages.Constants
import pages.LoginPage

/**
 * Created by weymouth on 5/1/15.
 */
abstract class CheckLoginPageAbstract extends GebReportingTest {

    def params = []

    void login(Class<? extends Page> redirectionPage) {
        usernameField.value Constants.GOOD_USERNAME
        passwordField.value Constants.GOOD_PASSWORD

        loginButtonNoTo.click()

        at(redirectionPage)
    }

    void goToPageMaybeLogin(Class<? extends Page> page, boolean firstCall = true) {
        via page

        if (isAt(page)) {
            return
        } else if (isAt(LoginPage)) {
            login(page)
        } else if (isAt(Constants.LANDING_PAGE.class)) {
            if (!firstCall) {
                throw new AssertionFailedError('Redirection loop')
            }
            /* if auto-login is on, we're unfortunately forwarded here */
            println "Autologin: landing page = " + Constants.LANDING_PAGE
            goToPageMaybeLogin(page, false)
        } else {
            throw new AssertionFailedError(
                    "Expected to be at either the LoginPage, $Constants.LANDING_PAGE or $page")
        }
    }

}
