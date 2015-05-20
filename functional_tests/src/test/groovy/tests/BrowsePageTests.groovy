import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import geb.junit4.GebReportingTest

import org.junit.Test

import pages.modules.CommonHeaderModule

import functions.Constants

import pages.LoginPage
import pages.BrowsePage
import pages.BrowseResultsPage

class BrowsePageTests extends GebReportingTest {

    // Assume a standard set of browse tab metadata has been loaded
    // using transmart-data and supplied sql loading scripts

    @Test
    void BrowseTab() {

        util.goToPageMaybeLogin(BrowsePage)

        assert at(BrowsePage)

    }


}
