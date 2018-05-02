package pages.modules

import com.google.common.collect.*
import geb.Module
import geb.navigator.Navigator
import org.openqa.selenium.WebElement

import functions.Constants

import pages.LoginFailedPage
import pages.ChangeMyPasswordPage

/**
 * Created by peter.rice@transmartfoundation.org on 4-jul-2016
 */
class LoginFormModule extends Module {

//    static base = { $('form#loginForm').parent() } // message and form children of this level

    static content = {

// login button with possible result pages after click
        loginButtonNoTo        { $('input#loginButton') }

        loginButton(to: [Constants.LANDING_PAGE.class, LoginFailedPage])
                               { $('input#loginButton') }
        loginButtonLanding(to: [Constants.LANDING_PAGE.class, ChangeMyPasswordPage])
                               { $('input#loginButton') }
        loginButtonFailed(to: LoginFailedPage)
                               { $('input#loginButton') }
// labels
                               
//        usernameLabel { $('label.for')[0].value() }
//        passwordLabel { $('label.for')[1].value() }

        usernameLabel { $('label[for=j_username]') }
        passwordLabel { $('label[for=j_password]') }

// input fields
        usernameField          { $('input#j_username') }
        passwordField          { $('input#j_password') }

// messages
        topMessage(wait: true) { $('h3')?.first()?.text()?.trim() }
        errorMessage           { $('div.login_message').text() }
    }

}
