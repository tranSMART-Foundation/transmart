package tests

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import functions.Constants

import pages.modules.CommonHeaderModule
import pages.modules.UtilityModule
import pages.ChangeMyPasswordPage
import pages.LoginPage
import pages.admin.AdminPage
import pages.BrowsePage
import pages.admin.CreateUserPage

@Stepwise
class ChangePswSpec extends GebReportingSpecTransmart {

    def setupSpec() {
        to LoginPage
        loginAdmin()
        at(Constants.LANDING_PAGE.class)

    }

    def "test ChangePassword page"() {
        when:
        commonHeader { module CommonHeaderModule }

        commonHeader.topMenuFind('Admin').click(AdminPage)

        usersOptionsCreate.click(CreateUserPage)

        then:
        isAt(CreateUserPage)

        when:
        String username = 'testChangePasswordPage-' + System.currentTimeMillis()
        usernameField.value username
        userRealNameField.value username
        passwordField.value username
        // known issue:
        // first attempt to set a CheckBox has no effect
        // all later attempts succeed
        // reason unknown (waiting for something? activating checkboxes?)
        // leave the duplicate call here until fixed
        enabledField.value(true)

        enabledField.value(true)
        changePasswordField.value(true)

        createButton.click()

        utility.utilitiesDoLogout()

        then:
        at(LoginPage)

        when:
        loginAs(username, username)

        then:

// redirected to change password after login
        at(ChangeMyPasswordPage)

        when:
        oldPasswordField.value username
        def changedPsw = username + '-Changed'
        newPasswordField.value changedPsw
        newPasswordRepeatedField.value changedPsw
        updateButton.click()

        utility.utilitiesDoLogout()

        then:
        at LoginPage

        when:
        loginAs(username, changedPsw)

        then:
        at(Constants.LANDING_PAGE.class)

        cleanup:
        utility.utilitiesDoLogout()
    }

    def "test no change Change Password page" () {
        to LoginPage
        loginAdmin()
        at(Constants.LANDING_PAGE.class)

        when:
        commonHeader.topMenuFind('Admin').click(AdminPage)

        usersOptionsCreate.click(CreateUserPage)

        then:
        isAt(CreateUserPage)

        when:
        String username = 'testNoChangePasswordPage-' + System.currentTimeMillis()
        usernameField.value username
        userRealNameField.value username
        passwordField.value username

        // set first CheckBox twice (see above)
        enabledField.value true

        enabledField.value true
        changePasswordField.value false

        createButton.click()
        utility.utilitiesDoLogout()

        then:
        at(LoginPage)

        when:
        loginAs(username, username)

        then:
        at(Constants.LANDING_PAGE.class)

        when:
        utility.utilitiesDoPassword()

        then:
        at(ChangeMyPasswordPage)

        when:
        oldPasswordField.value username
        def alteredPsw = username + '-Altered'
        newPasswordField.value alteredPsw
        newPasswordRepeatedField.value alteredPsw
        updateButton.click()
        utility.utilitiesDoLogout()

        then:
        at LoginPage

        when:
        loginAs(username, alteredPsw)

        then:
        at(Constants.LANDING_PAGE.class)

        cleanup:
        utility.utilitiesDoLogout()
    }

    def cleanupSpec() {
    }

}

