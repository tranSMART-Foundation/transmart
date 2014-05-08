package tests
import geb.Page
import geb.junit4.GebReportingTest
import junit.framework.AssertionFailedError
import org.junit.Test
import pages.Constants
import pages.DatasetExplorerPage
import pages.LoginPage
import pages.SearchPage
import pages.analyses.CoxRegressionResult
import pages.analyses.SurvivalAnalysisPage
import pages.analyses.SurvivalAnalysisSummary

import static matchers.TableMatcher.table
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

class SurvivalAnalysisTests extends GebReportingTest{

    public static final String GSE8581_KEY = '\\\\Public Studies\\Public Studies\\GSE8581\\'

    void login(Class<? extends Page> redirectionPage) {
        usernameField.value Constants.GOOD_USERNAME
        passwordField.value Constants.GOOD_PASSWORD

		loginButtonNoTo.click()

		at(redirectionPage)
	}

	void goToPageMaybeLogin(Class<? extends Page> page, boolean firstCall = true) {
		via page

        if (isAt(page)) {
            return
        } else if (isAt(LoginPage)) {
            login(page)
        } else if (isAt(Constants.LANDING_PAGE.class)) {
            if (!firstCall) {
                throw new AssertionFailedError('Redirection loop')
            }
            /* if auto-login is on, we're unfortunately forwarded here */
            println "Autologin: landing page = " + Constants.LANDING_PAGE
            goToPageMaybeLogin(page, false)
        } else {
            throw new AssertionFailedError(
                    "Expected to be at either the LoginPage, $Constants.LANDING_PAGE or $page")
        }
    }


    private void runAnalysis(Map params) {
        goToPageMaybeLogin DatasetExplorerPage

        dragNodeToSubset params.subsetNode, 1, 1

		selectAnalysis 'Survival Analysis'
		page SurvivalAnalysisPage
		verifyAt()

		waitFor { analysisWidgetHeader }

		dragNodeToBox params.timeVariable, timeBox

		if (params.categoryVariables) {
			dragNodeToBox params.categoryVariableDragged, categoryBox,
					containsInAnyOrder(params.categoryVariables.collect { is it as String })
		} else {
			dragNodeToBox params.categoryVariableDragged, categoryBox
		}

		if (params.searchKeyword) {
			categoryHighDimButton.click()

			waitFor { highDimPopup.dialog }

			//filling the form fields is not instantaneous:
			waitFor(1) { highDimPopup.tissue }

			if (params.expectedMarkerType) {
				assertThat highDimPopup.markerType, is(params.expectedMarkerType)
			}
			if (params.expectedPlatform) {
				assertThat highDimPopup.gplPlatform, is(params.expectedPlatform)
			}
			if (params.expectedSample) {
				assertThat highDimPopup.sample, is(params.expectedSample)
			}
			if (params.expectedTissue) {
				assertThat highDimPopup.tissue, is(params.expectedTissue)
			}

			highDimPopup.searchBox.value params.searchKeyword
			highDimPopup.selectSearchItem params.searchKeyword
			highDimPopup.applyButton.click()
		}

		if (params.binningParams) {
			binning.enableBinning()

			Map binParams = params.binningParams
			if (binParams.numberOfBins) {
				binning.numberOfBins.value binParams.numberOfBins
			}
			if (binParams.autoAssignment) {
				if (binParams.autoAssignment == 'evenly spaced') {
					binning.selectEvenlySpacedBins()
				} else if (binParams.autoAssignment == 'evenly distributed') {
					binning.selectEvenlyDistributedPopulation()
				} else {
					throw new IllegalArgumentException()
				}
			}
		}

        runButton.click()
        waitFor(8) { resultOutput } // wait up to 8 seconds for result
    }

	@Test
	void testClinicalVariable() {
		String sexKey     = "${GSE8581_KEY}Subjects\\Sex\\"
		def params = [
			subsetNode:              GSE8581_KEY,
			timeVariable:            "${GSE8581_KEY}Subjects\\Age\\",
			categoryVariableDragged: sexKey,
			categoryVariables:       [
				"${sexKey}male\\",
				"${sexKey}female\\"]
		]

		runAnalysis params

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
				[
					[30, 30, 30, 30, 65, 61, 72],
					//female
					[28, 28, 28, 28, 68, 63, 73]]  //male
		assertThat allFittingSummaries[0], is(table(
				fittingSummaryRowHeaders,
				SurvivalAnalysisSummary.ALL_HEADERS,
				fittingSummaryData))
	}

	@Test
	void testMrnaCategoryEvenlySpaced() {
		String sexKey     = "${GSE8581_KEY}Subjects\\Sex\\"

		def highDimExpectations = [
			expectedMarkerType: 'Gene Expression',
			expectedPlatform:   'GPL570',
			expectedSample:     'Human',
			expectedTissue:     'Lung',
		]

		def binningParams = [
			numberOfBins:       2,
			autoAssignment:     'evenly spaced'
		]

		def params = [
			subsetNode:              GSE8581_KEY,
			timeVariable:            "${GSE8581_KEY}Subjects\\Age\\",
			categoryVariableDragged: "${GSE8581_KEY}MRNA\\Biomarker Data\\Affymetrix Human Genome U133A 2.0 Array\\Lung\\",
			searchKeyword:           'TP53',
			*:                       highDimExpectations,
			binningParams:           binningParams,
		]

		runAnalysis params

		/* TODO: final assertions missing! */
	}
}
