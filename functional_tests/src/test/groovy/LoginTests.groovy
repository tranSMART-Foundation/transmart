import geb.junit4.GebReportingTest
import org.junit.Test
import pages.Constants
import pages.LoginFailedPage
import pages.LoginPage
import pages.SearchPage

class LoginTests extends GebReportingTest {

    @Test
    void testFailedLogin() {
		if (Constants.TEST_LOGIN) {
	        to LoginPage
	
	        usernameField.value Constants.BAD_USERNAME
	        passwordField.value Constants.BAD_PASSWORD
	        loginButton.click()
	
	        assert at(LoginFailedPage)
		} else {
	        println "no login test - testFailedLogin; login not enabled"
	        assert true;
		}
    }

    @Test
    void testSuccessfulLogin() {
		if (Constants.TEST_LOGIN) {
	        to LoginPage
	
	        usernameField.value Constants.GOOD_USERNAME
	        passwordField.value Constants.GOOD_PASSWORD
	        loginButton.click()
	
	        assert at(Constants.landingPage)
		} else {
	        println "no login test - testSuccessfulLogin; login not enabled"
	        assert true;
		}
    }
}
