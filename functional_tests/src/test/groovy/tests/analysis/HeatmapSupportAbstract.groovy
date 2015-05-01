package tests.analysis

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import geb.Page
import geb.junit4.GebReportingTest
import junit.framework.AssertionFailedError
import pages.Constants
import pages.LoginPage
import tests.CheckLoginPageAbstract

abstract class HeatmapSupportAbstract extends CheckLoginPageAbstract {

	private void setUpAnalysis(Map in_params) {
		params = in_params
		
		goToPageMaybeLogin params.basePageClass

		dragNodeToSubset params.subsetNode, 1, 1

		selectAnalysis params.analysisSelector
		page params.modelPageClass
		verifyAt()

		waitFor { analysisWidgetHeader }

		dragNodeToBox params.biomarkerNode, highDimBox
	}
	
	private void confirmLandingPage() {
		assert at(params.modelPageClass)
	}

	private void getHighDimPopup () {
		categoryHighDimButton.click()
		
		waitFor { highDimPopup.dialog }

		//filling the form fields is not instantaneous:
		waitFor(2) { highDimPopup.tissue }
		
	}
	
	private void confirmHighDimPopup () {
		println("Page = " + page)
		if (params.expectedMarkerType) {
			assertThat highDimPopup.markerType, is(params.expectedMarkerType)
		}
		println("After assert params.expectedMarkerType")
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
	
	private void confirmHighDimPageSettings() {
		assert at(params.modelPageClass)
		if (params.expectedMarkerType) {
			assert highDimDisplay.text().contains(params.expectedMarkerType);
		}
		if (params.expectedPlatform) {
			assert highDimDisplay.text().contains(params.expectedPlatform);
		}
		if (params.expectedSample) {
			assert highDimDisplay.text().contains(params.expectedSample);
		}
		if (params.expectedTissue) {
			assert highDimDisplay.text().contains(params.expectedTissue);
		}
	}
	
	private void runAnalysis() {
		runButton.click()
		waitFor(60 * 6) { resultOutputHeader } // may need to wait up to 6 min for result!
	}
	
	private void confirmAnalysisResults() {
		assert at(params.modelPageClass)
		assert resultOutputHeader.text() == params.resultsOutoutHeader
		assert resultsImageUrl.contains(params.resultsImageURLLabel)
	}


}
