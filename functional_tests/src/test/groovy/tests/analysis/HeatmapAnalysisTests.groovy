package tests.analysis;

import geb.Page
import geb.junit4.GebReportingTest

import java.util.Map

import junit.framework.AssertionFailedError

import org.junit.Test
import org.junit.Ignore

import pages.Constants
import pages.DatasetExplorerPage
import pages.analyses.HeatmapAnalysisPage
import pages.LoginPage

import static matchers.TableMatcher.table
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

public class HeatmapAnalysisTests extends GebReportingTest{
	
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

	private void setUpAnalysis() {
		goToPageMaybeLogin DatasetExplorerPage

		dragNodeToSubset params.subsetNode, 1, 1

		selectAnalysis 'Heatmap'
		page HeatmapAnalysisPage
		verifyAt()

		waitFor { analysisWidgetHeader }

		dragNodeToBox params.biomarkerNode, highDimBox
		
	}

	private void getHighDimPopup () {
		categoryHighDimButton.click()
		
		waitFor { highDimPopup.dialog }

		//filling the form fields is not instantaneous:
		waitFor(2) { highDimPopup.tissue }
		
	}
	
	private void confirmHighDimPopup () {
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
	}

	private void runHighDimPopup() {
		highDimPopup.applyButton.click()
	}	
    private void runAnalysis() {
        runButton.click()
        waitFor(60 * 6) { resultOutput } // may need to wait up to 6 min for result!
    }

	def highDimExpectations = [
		expectedMarkerType: 'Gene Expression',
		expectedPlatform:   'GPL570',
		expectedSample:     'Human',
		expectedTissue:     'Lung',
	]
	
	def params = [
		subsetNode:          Constants.GSE8581_KEY,
		biomarkerNode:       "${Constants.GSE8581_KEY}MRNA\\Biomarker Data\\Affymetrix Human Genome U133A 2.0 Array\\Lung\\",
		*:                       highDimExpectations
	]

	@Test
	void testSetup() {
		setUpAnalysis()
		assert at(HeatmapAnalysisPage.class)
	}

	@Test
	void testHighDimPopup() {
		setUpAnalysis()
		getHighDimPopup()
		confirmHighDimPopup()
		runHighDimPopup()
	}	
	
	@Ignore
	@Test
	void testClinicalVariable() {
		setUpAnalysis()
		getHighDimPopup()
		confirmHighDimPopup()
		runHighDimPopup()
		/* TODO: final assertions missing! */
		// confirm page settings
	}

	@Ignore
	@Test
	void testResults() {

		setUpAnalysis()
		getHighDimPopup()
		runHighDimPopup()

		runAnalysis()
		/* TODO: final assertions missing! */
		// confirm results values
	}
	
}
