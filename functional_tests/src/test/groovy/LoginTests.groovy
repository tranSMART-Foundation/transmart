import geb.junit4.GebReportingTest
import org.junit.Test
import pages.Constants
import pages.LoginFailedPage
import pages.LoginPage
import pages.SearchPage

class LoginTests extends GebReportingTest {

    @Test
    void testFailedLogin() {
        to LoginPage

        usernameField.value Constants.BAD_USERNAME
        passwordField.value Constants.BAD_PASSWORD
        loginButton.click()

        assert at(LoginFailedPage)
    }

    @Test
    void testSuccessfulLogin() {
        to LoginPage

        usernameField.value Constants.GOOD_USERNAME
        passwordField.value Constants.GOOD_PASSWORD
        loginButton.click()

        assert at(SearchPage)
    }
}
