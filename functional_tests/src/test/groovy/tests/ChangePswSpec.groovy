package tests

import functions.Constants
import org.junit.Test
import pages.ChangeMyPasswordPage
import pages.admin.CreateUserPage

class ChangePswSpec extends CheckLoginPageAbstract {

    @Test
    void testChangePasswordPage() {
        loginAs(Constants.ADMIN_USERNAME, Constants.ADMIN_USERNAME)
        to CreateUserPage
        String username = 'testChangePasswordPage' + System.currentTimeMillis()
        usernameField.value username
        userRealNameField.value username
        passwordField.value username
        changePasswordField.value true
        enabledField.value true
        createButton.click()
        logout()

        loginAs(username, username)
        at(ChangeMyPasswordPage)
        oldPasswordField.value username
        def changedPsw = username + '-Changed'
        newPasswordField.value changedPsw
        newPasswordRepeatedField.value changedPsw
        updateButton.click()
        logout()

        loginAs(username, changedPsw)
        at(Constants.LANDING_PAGE.class)
    }

    @Test
    void testNoChangePasswordPage() {
        loginAs(Constants.ADMIN_USERNAME, Constants.ADMIN_USERNAME)
        to CreateUserPage
        String username = 'testNoChangePasswordPage' + System.currentTimeMillis()
        usernameField.value username
        userRealNameField.value username
        passwordField.value username
        changePasswordField.value false
        enabledField.value true
        createButton.click()
        logout()

        loginAs(username, username)
        at(Constants.LANDING_PAGE.class)
    }

}

