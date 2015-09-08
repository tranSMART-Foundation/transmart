package pages.admin

import geb.Page
import pages.modules.CommonHeaderModule

class CreateUserPage extends Page {

    static url = '/transmart/authUser/create'

    static at = {
        title == 'Create User'
    }

    static content = {
        admin(wait: true) { $() }
        commonHeader { module CommonHeaderModule }

        usernameField { $('input#username') }
        userRealNameField { $('input#userRealName') }
        passwordField { $('input#passwd') }
        enabledField { $('input#enabled') }
        rolePublicUserField { $('input#ROLE_PUBLIC_USER') }
        changePasswordField { $('input#changePassword') }
        createButton { $('input[type=submit].save') }
    }
}

