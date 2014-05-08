import geb.junit4.GebReportingTest;

import org.junit.Test


class DataExplorerPageTests extends GebReportingTest {

	@Test
	void simpleLandingTest() {
		// The first hit in the session always lands on the landingPage
		via (SearchPage)
		assert at(Constants.LANDING_PAGE.class)
	}
}
