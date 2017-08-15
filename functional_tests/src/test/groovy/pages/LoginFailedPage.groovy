package pages

import pages.LoginPage
import pages.modules.LoginFormModule

class LoginFailedPage extends LoginPage {
    // locked error message occurs in rare case that bad login is repeated within 10 min
    static at = {
        loginForm.errorMessage != ''
    }

    static content = {
        topMessage (required: false) { $('h3')?.first()?.text()?.trim() }
        loginForm { module LoginFormModule }
    }

}
