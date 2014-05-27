package pages

import geb.Page
import pages.modules.NavigationBarModule

class LoginPage extends Page {

    static url = 'login/forceAuth'

    static at = {
        topMessage == 'Please login...'
    }

    static content = {
        topMessage { $('h3')?.text()?.trim() }
        loginButtonNoTo { $('input#loginButton') }
        loginButton(to: [BrowsePage, LoginFailedPage]) { $('input#loginButton') }
        usernameField { $('input#j_username') }
        passwordField { $('input#j_password') }
        errorMessage  { $('div.login_message').text() }
    }
}

class LoginFailedPage extends LoginPage {
    static at = {
        topMessage == 'Please login...' &&
                errorMessage.contains('Login has failed')
    }
}

