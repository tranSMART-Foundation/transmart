package pages

import pages.LoginPage

class LoginFailedPage extends LoginPage {
    static at = {
        topMessage == 'Please login...' &&
                errorMessage.contains('Login has failed')
    }
}
