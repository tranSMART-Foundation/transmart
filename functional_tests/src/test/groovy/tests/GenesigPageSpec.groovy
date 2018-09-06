package tests

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import pages.modules.CommonHeaderModule

import functions.Constants

import pages.LoginPage
import pages.GenesigPage

@Stepwise
class GenesigPageSpec extends GebReportingSpecTransmart {

    def setupSpec() {
        loginTransmart(GenesigPage)
    }

    // Assume a standard set of gene signature/list metadata has been loaded
    // using transmart-data and supplied loading scripts

    def "start on the gene signatures/lists tab"() {

        when:
        isAt(GenesigPage)

        then:
        println "Testing page"
        assert at(GenesigPage)

    }

    def cleanupSpec() {
    }

}
