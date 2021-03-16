package tests

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import pages.modules.CommonHeaderModule

import functions.Constants

import pages.LoginPage
import pages.GwasPage

@Stepwise
class GwasPageSpec extends GebReportingSpecTransmart {

    def setupSpec() {
        println "setupSpec loginTransmart"
        loginTransmart(GwasPage)
        println "setupSpec loginTransmart GwasPage done"
    }
    

    // Assume a standard set of GWAS tab metadata has been loaded
    // using transmart-data and supplied loading scripts

    def "start on the gwas tab"() {

        when:
        isAt(GwasPage)

        then:
        assert at(GwasPage)

    }

    def cleanupSpec() {
    }

}
