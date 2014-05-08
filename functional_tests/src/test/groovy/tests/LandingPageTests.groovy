package tests
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import geb.junit4.GebReportingTest

import org.junit.Test

import pages.Constants
import pages.SearchPage

class LandingPageTests extends GebReportingTest {
	
	@Test
	void simpleLandingTest()
	{
		// The first hit in the session always lands on the landingPage
		via (SearchPage)
		assert at(Constants.LANDING_PAGE.class)
	}

}
