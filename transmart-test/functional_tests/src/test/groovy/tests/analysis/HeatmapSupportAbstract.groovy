package tests.analysis

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import geb.Page

import junit.framework.AssertionFailedError

import functions.Constants

import pages.LoginPage
import pages.AnalyzeWorkflow
import pages.modules.AnalyzeTabModule
import pages.modules.AnalyzeTreeModule
import pages.analyses.HeatmapAnalysisPage
import tests.GebReportingSpecTransmart

abstract class HeatmapSupportAbstract extends GebReportingSpecTransmart {

	Boolean setUpAnalysis(Map in_params) {
		params = in_params
		
		loginTransmart params.basePageClass

		queryDragNodeToSubset params.subsetNode, 1
                // select advanced workflows
                // check workflow page
                analyzeTab.tabWorkflows.click()
		verifyAt()
		selectAnalysis params.analysisSelector
		page params.modelPageClass
		verifyAt()

		waitFor { analysisWidgetHeader }
                
		if(!analyzeTree.treeDragNodeToBox(params.biomarkerNode, highDimBox)) {
                    return false
                }
	}
	
	void confirmLandingPage() {
		assert at(params.modelPageClass)
	}

	void getHighDimPopup () {
		categoryHighDimButton.click()
		
		waitFor { highDimPopup.dialog }

		//filling the form fields is not instantaneous:
		waitFor(2) { highDimPopup.tissue }
		
	}
	
	void confirmHighDimPopup () {
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

	void runHighDimPopup() {
		highDimPopup.applyButton.click()
	}
	
	void confirmHighDimPageSettings() {
                assert at(params.modelPageClass)
                if (params.expectedMarkerType) {
                    assert highDimDisplay.first().text().contains(params.expectedMarkerType);
		}
		if (params.expectedPlatform) {
                    assert highDimDisplay.first().text().contains(params.expectedPlatform);
		}
		if (params.expectedSample) {
                    assert highDimDisplay.first().text().contains(params.expectedSample);
		}
		if (params.expectedTissue) {
                    assert highDimDisplay.first().text().contains(params.expectedTissue);
		}
	}
	
	void runAnalysis() {
		runButton.click()
		waitFor(60 * 20) { resultOutputHeader } // may need to wait up to 20 min for result!
	}
	
	void confirmAnalysisResults() {
            assert at(params.modelPageClass)
            assert resultOutputHeader.text() == params.resultsOutputHeader
            assert resultOutputHint.text() == "Click on the heatmap image to open it in a new window as this may increase readability."
            assert resultsImageUrl.contains(params.resultsImageURLLabel)
	}
}
