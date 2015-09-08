package pages

import geb.Page

import functions.Constants

class LoginPage extends Page {

    static url = 'login/forceAuth'

    static at = {
        topMessage == 'Please login...'
    }

    static content = {
        topMessage { $('h3')?.text()?.trim() }
        loginButtonNoTo { $('input#loginButton') }
        loginButton(to: [Constants.LANDING_PAGE.class, LoginFailedPage]) { $('input#loginButton') }
        loginButtonLanding(to: [Constants.LANDING_PAGE.class, ChangeMyPasswordPage]) { $('input#loginButton') }
        loginButtonFailed(to: LoginFailedPage) { $('input#loginButton') }
        usernameField { $('input#j_username') }
        passwordField { $('input#j_password') }
        errorMessage  { $('div.login_message').text() }
    }
}


