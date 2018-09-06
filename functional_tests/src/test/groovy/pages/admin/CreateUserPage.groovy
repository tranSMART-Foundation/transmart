package pages.admin

import geb.Page
import pages.modules.CommonHeaderModule
import pages.modules.UtilityModule

class CreateUserPage extends Page {

    static url = 'authUser/create'

    static at = {
        pageTitle.text() == 'Create User'
    }

    static content = {
        admin(wait: true) { $() }
        commonHeader { module CommonHeaderModule }
        utility { module UtilityModule }

        pageTitle { $('div#content').find('h1') }
        formCreateUser { $('form') }
        
        usernameField { $('input#username') }
        userRealNameField { $('input#userRealName') }
        passwordField { $('input#passwd') }
        enabledField { $('input#enabled') }
        roleAdminUserField { $('input#ROLE_ADMIN_USER') }
        roleStudyOwnerField { $('input#ROLE_STUDY_OWNER') }
        roleSpectatorField { $('input#ROLE_SPECTATOR') }
        roleDatasetExplorerAdminField { $('input#ROLE_DATASET_EXPLORER_ADMIN') }
        rolePublicUserField { $('input#ROLE_PUBLIC_USER') }
        changePasswordField { $('input#changePassword') }
        createButton { $('input[type=submit].save') }
    }
}

