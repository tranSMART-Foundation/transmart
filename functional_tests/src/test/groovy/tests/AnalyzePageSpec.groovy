package tests

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import functions.Constants

import pages.AnalyzeQuery
import pages.AnalyzeResultsPage
import pages.LoginPage

import pages.modules.CommonHeaderModule

@Stepwise
class AnalyzePageSpec extends GebReportingSpecTransmart {

    def setupSpec() {
        loginTransmart(AnalyzeQuery)
    }

    def "start on Analyze page"() {

        when:
        isAt(AnalyzeQuery)

        then:
        assert at(AnalyzeQuery)
    }

    def "check Active Filters panel"() {

        when:
        isAt(AnalyzeQuery)

        then:
        assert at(AnalyzeQuery)
    }
    
    def cleanupSpec() {
    }

}
