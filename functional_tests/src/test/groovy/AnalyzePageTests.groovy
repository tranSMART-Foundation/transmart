import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import geb.junit4.GebReportingTest;

import org.junit.Test

import pages.modules.CommonHeaderModule

import functions.Utilities

import pages.Constants
import pages.LoginPage
import pages.AnalyzePage
import pages.AnalyzeResultsPage


class AnalyzePageTests extends GebReportingTest {

    def util = new Utilities()

    @Test
    void AnalyzeTab() {

        util.goToPageMaybeLogin(AnalyzePage)

        assert at(AnalyzePage)
    }

    @Test
    void AnalyzeTabActiveFiltersPanel() {

        util.goToPageMaybeLogin(AnalyzePage)

        assert at(AnalyzePage)        
    }
    
}
