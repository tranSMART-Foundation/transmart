package tests
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import geb.junit4.GebReportingTest

import org.junit.Test

import pages.Constants
import pages.BrowsePage

class LandingPageTests extends CheckLoginPageAbstract {
	
	@Test
	void simpleLandingTest()
	{
		goToPageMaybeLogin(BrowsePage)
		assert at(Constants.LANDING_PAGE.class)
	}

}
