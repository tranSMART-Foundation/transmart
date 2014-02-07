import geb.Page
import geb.junit4.GebReportingTest
import junit.framework.AssertionFailedError
import org.junit.Test
import pages.Constants
import pages.DatasetExplorerPage
import pages.LoginPage
import pages.analyses.CoxRegressionResult
import pages.analyses.SurvivalAnalysisPage
import pages.analyses.SurvivalAnalysisSummary

import static matchers.TableMatcher.table
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

class SurvivalAnalysisTests extends GebReportingTest{

    void login(Class<? extends Page> redirectionPage) {
        usernameField.value Constants.GOOD_USERNAME
        passwordField.value Constants.GOOD_PASSWORD

        loginButtonNoTo.click()

        at(redirectionPage)
    }

    void goToPageMaybeLogin(Class<? extends Page> page) {
        via page

        if (isAt(page)) {
            return
        } else if (isAt(LoginPage)) {
            login(DatasetExplorerPage)
        } else {
            throw new AssertionFailedError(
                    "Expected to be at either the LoginPage or $page")
        }
    }

    @Test
    void testClinicalVariable() {
        String gse8581Key = '\\\\Public Studies\\Public Studies\\GSE8581\\'
        String ageKey     = "${gse8581Key}Subjects\\Age (year)\\"
        String sexKey     = "${gse8581Key}Subjects\\Sex\\"
        String maleKey    = "${sexKey}male\\"
        String femaleKey  = "${sexKey}female\\"

        /* run the analysis */
        goToPageMaybeLogin DatasetExplorerPage

        dragNodeToSubset gse8581Key, 1, 1

        selectAnalysis 'Survival Analysis'
        page SurvivalAnalysisPage
        verifyAt()

        waitFor { analysisWidgetHeader }

        dragNodeToBox ageKey, timeBox
        dragNodeToBox sexKey, categoryBox,
                containsInAnyOrder(is(maleKey), is(femaleKey))

        runButton.click()
        waitFor(8) { resultOutput } // wait up to 8 seconds for result

        /* check cox regression result */
        def allCoxRegressionResults = coxRegressionResults
        assertThat allCoxRegressionResults.size(), is(1)
        def coxRegressionData = [
                (CoxRegressionResult.NUMBER_OF_SUBJECTS_HEADER): '58',
                (CoxRegressionResult.NUMBER_OF_EVENTS_HEADER):   '58',
                (CoxRegressionResult.LIKELIHOOD_RATIO_HEADER):   '0.05 on 1 df, p=0.8177',
                (CoxRegressionResult.WALD_HEADER):               '0.05 on 1 df, p=0.8176',
                (CoxRegressionResult.LOGRANK_HEADER):            '0.05 on 1 df, p=0.8176',
        ]
        assertThat allCoxRegressionResults[0], is(equalTo(coxRegressionData))

        /* check fitting summary */
        def allFittingSummaries = fittingSummaries
        assertThat allFittingSummaries.size(), is(1)

        def fittingSummaryRowHeaders = ['female', 'male']
        def fittingSummaryData =
                [[30, 30, 30, 30, 65, 61, 72],  //female
                 [28, 28, 28, 28, 68, 63, 73]]  //male

        assertThat allFittingSummaries[0], is(table(
                fittingSummaryRowHeaders,
                SurvivalAnalysisSummary.ALL_HEADERS,
                fittingSummaryData))
    }

}
