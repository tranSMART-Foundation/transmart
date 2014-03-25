import geb.Page;
import geb.junit4.GebReportingTest
import junit.framework.AssertionFailedError
import org.junit.Test
import pages.Constants
import pages.DatasetExplorerPage
import pages.LoginPage
import pages.SearchPage

class SearchPageTests extends GebReportingTest {
	
	void login(Class<? extends Page> redirectionPage) {
		usernameField.value Constants.GOOD_USERNAME
		passwordField.value Constants.GOOD_PASSWORD

		loginButtonNoTo.click()

		at(redirectionPage)
	}
	
	void goToPageMaybeLogin(Class<? extends Page> page, boolean firstCall = true) {
		via page

		if (isAt(page)) {
			return
		} else if (isAt(LoginPage)) {
			login(page)
		} else if (isAt(Constants.LandingPage)) {
			if (!firstCall) {
				throw new AssertionFailedError('Redirection loop')
			}
			/* if auto-login is on, we're unfortunately forwarded here */
			goToPageMaybeLogin(page, false)
		} else {
			throw new AssertionFailedError(
					"Expected to be at either the LoginPage, $Constants.LandingPage or $page")
		}
	}

	@Test
	void landingOnSearchTest() {
		goToPageMaybeLogin SearchPage
		assert at(SearchPage)
	}

}
