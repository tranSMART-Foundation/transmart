package tests

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import functions.Constants

import pages.UploadDataPage

import pages.modules.CommonHeaderModule

@Stepwise
class UploadDataPageSpec extends GebReportingSpecTransmart {

    // Assume a standard set of Upload Data tab metadata has been loaded
    // using transmart-data and supplied loading scripts

    def setupSpec() {
        loginTransmart(UploadDataPage)
    }

    def "start on the upload data tab"() {

        when:
        isAt(UploadDataPage)

        then:
        assert at(UploadDataPage)

    }

    def cleanupSpec() {
    }
    
}
