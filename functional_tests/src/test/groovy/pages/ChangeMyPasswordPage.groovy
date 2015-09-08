package pages

import geb.Page
import pages.modules.CommonHeaderModule

class ChangeMyPasswordPage extends Page {

    static url = '/transmart/changeMyPassword/show'

    static at = {
        title == 'Change My Password'
    }

    static content = {
        admin(wait: true) { $() }
        commonHeader { module CommonHeaderModule }

        oldPasswordField { $('input#oldPassword') }
        newPasswordField { $('input#newPassword') }
        newPasswordRepeatedField { $('input#newPasswordRepeated') }
        updateButton { $('input[type=submit].save') }
    }
}

