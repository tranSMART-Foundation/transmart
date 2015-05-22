import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import geb.junit4.GebReportingTest

import org.junit.Test

import pages.modules.CommonHeaderModule

import functions.Constants

import pages.LoginPage
import pages.BrowsePage
import pages.BrowseResultsPage

import functions.Utilities

import geb.spock.GebSpec
import geb.spock.GebReportingSpec
import spock.lang.Stepwise

@Stepwise
class BrowsePageSpec extends GebReportingSpec {

    // Assume a standard set of browse tab metadata has been loaded
    // using transmart-data and supplied sql loading scripts

    def util = new Utilities()

    def "start on the browse tab"() {

        when:
        util.goToPageMaybeLogin(BrowsePage)

        then:
        assert at(BrowsePage)

    }


}
