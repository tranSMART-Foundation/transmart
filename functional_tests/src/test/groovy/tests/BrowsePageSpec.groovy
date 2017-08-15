package tests

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import functions.Constants

import pages.LoginPage
import pages.BrowsePage
import pages.BrowseResultsPage

import pages.modules.CommonHeaderModule

@Stepwise
class BrowsePageSpec extends GebReportingSpecTransmart {

    def setupSpec() {
        loginTransmart(BrowsePage)
    }
    // Assume a standard set of browse tab metadata has been loaded
    // using transmart-data and supplied sql loading scripts

    def "start on the browse tab"() {

        when:
        at(BrowsePage)

        then:
        assert at(BrowsePage)

        when:
        programOpen('Public Studies')

        then:

        // some check on the studies now open
        println "Checks needed"
    }

    def cleanupSpec() {
    }

}
