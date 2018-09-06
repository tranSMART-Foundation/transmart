package tests

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import functions.Constants

import pages.AnalyzeQuery
import pages.BrowsePage
import pages.GenesigPage
import pages.GwasPage
import pages.LoginPage
import pages.SampleExplorerPage
import pages.UploadDataPage
import pages.admin.AdminPage

//import pages.modules.CommonHeaderModule
//import pages.modules.UtilityModule

@Stepwise
class LandingPageSpec extends GebReportingSpecTransmart {

    def setupSpec() {
        loginTransmart(Constants.LANDING_PAGE.class)
    }
    
    def "test each page on main header tabs from the landing page"() {
            
        when: "Starting on landing page"
 
        then: "User is at landing page"

        assert at(Constants.LANDING_PAGE.class)
        report("LandingLanding")
    }

    def "goto Analyze page"() {

        when: "User clicks tab 'Analyze'"

        if(commonHeader.currentMenuItem?.text() != commonHeader.TOPMENU_ANALYZE) {
            commonHeader.topMenuFind('Analyze').click()
        }
        
        then: "User is at AnalyzeQuery"

        assert at(AnalyzeQuery)
        report("LandingAnalyze")
    }

    def "goto Browse page"() {
        when: "User clicks tab 'Browse'"

        commonHeader.topMenuFind('Browse').click()

        then: "User is at BrowsePage"

        assert at(BrowsePage)
        report("LandingBrowse")
    }

    def "goto SampleExplorer page"() {

        when: "User clicks tab 'Sample Explorer'"

        commonHeader.topMenuFind('Sample Explorer').click()

        then: "User is at SampleExplorerPage"

        assert at(SampleExplorerPage)
        report("LandingSample")
    }


    def "goto Genesignature page"() {
        when: "User clicks tab 'Gene Signature/Lists'"

//        commonHeader { module CommonHeaderModule }

        commonHeader.topMenuFind('Gene Signature/Lists').click()

        then: "User is at GenesigPage"

        assert at(GenesigPage)
        report("LandingGenesig")
    }

    def "goto GWAS page"() {
        when: "User clicks 'GWAS' tab"

//        commonHeader { module CommonHeaderModule }

        commonHeader.topMenuFind('GWAS').click()

        then: "User is at GWAS page"
        assert at(GwasPage)
        report("LandingGwas")
    }

    def "goto UploadData page"() {
        when: "User clicks tab 'Upload Data'"

//        commonHeader { module CommonHeaderModule }

        commonHeader.topMenuFind('Upload Data').click()

        then: "User is at UploadDataPage"
        assert at(UploadDataPage)
        report("LandingUploadData")
    }

    def "goto Admin page"() {
        when: "User is administrator and at tab 'Admin'"

        if(!commonHeader.topMenuFind('Admin')) {
            utility.utilitiesDoLogout()
            at(LoginPage)
            loginAdmin()
            at(Constants.LANDING_PAGE.class)
        }

        commonHeader.topMenuFind('Admin').click()
        at(AdminPage)

        report("LandingAdmin")
        scrolling.scrollToBottom(packageOptionsSupport)
        report("LandingAdminScrolled")
        
        then: "User is at AdminPage"
        assert at(AdminPage)
        
    }

    def cleanupSpec() {
    }
}
