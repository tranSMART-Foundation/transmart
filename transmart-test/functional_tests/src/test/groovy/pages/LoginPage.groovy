package pages

import geb.Page

import junit.framework.AssertionFailedError

import functions.Constants
import pages.modules.LoginFormModule

class LoginPage extends Page {

    static url = 'login/forceAuth'

    static at = {
        println "LoginPage at..."
        println "usernameLabel '${loginForm.usernameLabel.text()}'"
        println "passwordLabel '${loginForm.passwordLabel.text()}'"
        loginForm.usernameLabel.text() == 'Username :'
    }

    static content = {
        topMessage (required: false) { $('h3')?.first()?.text()?.trim() }
        loginForm { module LoginFormModule }
    }

    void login(String userLevel = "guest") {
        if(userLevel == "guest") {
            loginGuest()
        }
        else if (userLevel == "admin") {
            loginAdmin()
        }
        else if (userLevel == "user") {
            loginUser()
        }
        else if (userLevel == "other") {
            loginOther()
        }
        else {
            assert userLevel : "Unknown userLevel ${userLevel}"
        }
    }
    
    void loginGuest() {

        loginForm.usernameField.value Constants.GUEST_USERNAME
        loginForm.passwordField.value Constants.GUEST_PASSWORD

        loginForm.loginButtonNoTo.click()

    }

    void loginUser() {

        loginForm.usernameField.value Constants.GOOD_USERNAME
        loginForm.passwordField.value Constants.GOOD_PASSWORD

        loginForm.loginButtonNoTo.click()

    }

    void loginAs(String username, String password) {
        loginForm.usernameField.value username
        loginForm.passwordField.value password
        loginForm.loginButtonNoTo.click()
    }

    void loginAdmin() {

        loginForm.usernameField.value Constants.ADMIN_USERNAME
        loginForm.passwordField.value Constants.ADMIN_PASSWORD

        loginForm.loginButtonNoTo.click()

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
            goToPageMaybeLogin(page, false)
        } else {
            throw new AssertionFailedError(
                    "Expected to be at either the LoginPage, $Constants.LANDING_PAGE or $page")
        }
    }

}


