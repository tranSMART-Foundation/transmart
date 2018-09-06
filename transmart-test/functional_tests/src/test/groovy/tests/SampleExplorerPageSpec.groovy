package tests

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import pages.modules.CommonHeaderModule

import functions.Constants

import pages.LoginPage
import pages.SampleExplorerPage

@Stepwise
class SampleExplorerPageSpec extends GebReportingSpecTransmart {

    def setupSpec() {
        loginTransmart(SampleExplorerPage)
    }

    // Assume a standard set of sample explorer tab metadata has been loaded
    // using transmart-data and supplied sql loading scripts

    def "start on the sample explorer tab"() {

        when:
        isAt(SampleExplorerPage)

        then:
        assert at(SampleExplorerPage)

    }

    def cleanupSpec() {
    }

}
