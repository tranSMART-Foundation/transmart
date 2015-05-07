package pages

import pages.LoginPage

class LoginFailedPage extends LoginPage {
    // locked error message occures in rare case that bad login is repeated within 10 min
    static at = {
        topMessage == 'Please login...' && (
                errorMessage.contains('Login has failed')
                || errorMessage.contains('Your account has been locked')
        )
    }
}
