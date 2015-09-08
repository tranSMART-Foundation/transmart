import geb.junit4.GebReportingTest

import org.junit.Test

import pages.modules.CommonHeaderModule

import functions.Utilities
import functions.Constants
import pages.AnalyzePage
import pages.SampleExplorerPage
import pages.GenesigPage
import pages.GwasPage
import pages.UploadDataPage
import pages.admin.AdminPage

class LandingPageTests extends GebReportingTest {

    def util = new Utilities()

    @Test
    void LandingPageTest()
    {
        util.goToPageMaybeLogin Constants.LANDING_PAGE.class

        assert at(Constants.LANDING_PAGE.class)
    }

    @Test
    void LandingPageTestAnalyze()
    {
        util.goToPageMaybeLogin Constants.LANDING_PAGE.class

        assert at(Constants.LANDING_PAGE.class)

        commonHeader { module CommonHeaderModule }

        commonHeader.topMenuFind('Analyze').click()

        assert at(AnalyzePage)

    }

    @Test
    void LandingPageTestSampleExplorer()
    {
        util.goToPageMaybeLogin Constants.LANDING_PAGE.class

        assert at(Constants.LANDING_PAGE.class)

        commonHeader { module CommonHeaderModule }

        commonHeader.topMenuFind('Sample Explorer').click()

        assert at(SampleExplorerPage)

    }


    @Test
    void LandingPageTestGeneSigList()
    {
        util.goToPageMaybeLogin Constants.LANDING_PAGE.class

        assert at(Constants.LANDING_PAGE.class)

        commonHeader { module CommonHeaderModule }

        commonHeader.topMenuFind('Gene Signature/Lists').click()

        assert at(GenesigPage)

    }

    void LandingPageTestGwas()
    {
        util.goToPageMaybeLogin Constants.LANDING_PAGE.class

        assert at(Constants.LANDING_PAGE.class)

        commonHeader { module CommonHeaderModule }

        commonHeader.topMenuFind('GWAS').click()

        assert at(GwasPage)

    }

    void LandingPageTestUploadData()
    {
        util.goToPageMaybeLogin Constants.LANDING_PAGE.class

        assert at(Constants.LANDING_PAGE.class)

        commonHeader { module CommonHeaderModule }

        commonHeader.topMenuFind('Upload Data').click()

        assert at(UploadDataPage)

    }

    void LandingPageTestAdmin()
    {
        util.goToPageMaybeLogin Constants.LANDING_PAGE.class

        assert at(Constants.LANDING_PAGE.class)

        if(Constants.AUTO_LOGIN_ENABLED) {
            util.selectLogout()
            util.login()
        }

        commonHeader { module CommonHeaderModule }

        commonHeader.topMenuFind('Admin').click()

        assert at(AdminPage)

    }

}
