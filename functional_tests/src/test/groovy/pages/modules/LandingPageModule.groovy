package pages.modules

import geb.Module
import geb.Page

import pages.Constants
import pages.LoginPage

/**
 * Created by price on 19-12-2014.
 */
class LandingPageModule extends Module {

    private toLandingPage() {

        if (!Constants.AUTO_LOGIN_ENABLED) {

            to LoginPage

            usernameField.value Constants.GOOD_USERNAME
            passwordField.value Constants.GOOD_PASSWORD
            loginButton.click()

            assert at(Constants.LANDING_PAGE.class)
        }
        else {
            to Constants.LANDING_PAGE.class
        }

    }

}

