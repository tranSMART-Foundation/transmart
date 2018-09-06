package pages

import geb.Page
import pages.modules.CommonHeaderModule
import pages.modules.UtilityModule

class ChangeMyPasswordPage extends Page {

    static url = 'changeMyPassword/show'

    static at = {
        pageTitle.text() == 'Change My Password'
    }

    static content = {
        admin(wait: true) { $() }
        commonHeader { module CommonHeaderModule }
        utility { module UtilityModule }

        pageTitle { $('div.body h1') }
        
        oldPasswordField { $('input#oldPassword') }
        newPasswordField { $('input#newPassword') }
        newPasswordRepeatedField { $('input#newPasswordRepeated') }
        updateButton { $('input[type=submit].save') }
    }
}

