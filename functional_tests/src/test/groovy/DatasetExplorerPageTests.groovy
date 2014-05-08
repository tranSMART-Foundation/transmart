import geb.junit4.GebReportingTest

import org.junit.Test

import pages.Constants
import pages.DatasetExplorerPage

class DatasetExplorerPageTests extends GebReportingTest {

	@Test
	void simpleLandingTest() {
		// The first hit in the session always lands on the landingPage
		via (DatasetExplorerPage)
		assert at(Constants.LANDING_PAGE.class)
	}
}
