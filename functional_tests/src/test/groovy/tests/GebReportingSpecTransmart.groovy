package tests

import geb.spock.GebReportingSpec
import geb.Page
import spock.lang.Stepwise

import junit.framework.AssertionFailedError
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import functions.Constants

import pages.BrowsePage
import pages.LoginPage

import pages.modules.CommonHeaderModule

class GebReportingSpecTransmart extends GebReportingSpec {

    void loginTransmart(Class<? extends Page> page, String userLevel = "guest", boolean firstCall = true) {
        via page
        if (isAt(page)) {
            /* Already at required page */
            return
        } else if (!Constants.AUTO_LOGIN_ENABLED && isAt(LoginPage, false)) {
            /* At login page - login with user/password */
            LoginPage.login(userLevel)
            to page
        } else if (Constants.AUTO_LOGIN_ENABLED && isAt(Constants.LANDING_PAGE.class)) {
            /* Unexpectedly at landing page when not wanted */
            if (!firstCall) {
                throw new AssertionFailedError('Redirection loop')
            }
            /* if auto-login is on, we're unfortunately forwarded here */
            loginTransmart(page, userLevel, false)
        } else {
            throw new AssertionFailedError(
                    "Expected to be at either the LoginPage, Landing page ${Constants.LANDING_PAGE.class} or required page ${page.class}")
        }
    }


}
