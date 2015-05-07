package pages

import pages.LoginPage

class LoginFailedPage extends LoginPage {
    // locked error message occurs in rare case that bad login is repeated within 10 min
    static at = {
        errorMessage != ''
    }
}
