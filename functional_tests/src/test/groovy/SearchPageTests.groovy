import geb.Page;
import geb.junit4.GebReportingTest
import junit.framework.AssertionFailedError
import org.junit.Test
import pages.Constants
import pages.DatasetExplorerPage
import pages.LoginPage
import pages.SearchPage

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

class SearchPageTests extends GebReportingTest {

	//Note: for now, these tests assume auto-login
	//TODO: these test need to be made robust in the face of a possible login requirement 	

	@Test
	void landingOnSearchTest() {
		goToPageMaybeLogin SearchPage
		assert at(SearchPage)
	}

}
