package tests.analysis;

import static matchers.TableMatcher.table
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import geb.Page
import junit.framework.AssertionFailedError

import org.junit.Ignore
import org.junit.Test

import pages.Constants
import pages.DatasetExplorerPage
import pages.LoginPage
import pages.analyses.HeatmapAnalysisPage

public class HeatmapAnalysisTests extends HeatmapSupportAbstract {
	
	def highDimExpectations = [
		expectedMarkerType: 'Gene Expression',
		expectedPlatform:   'GPL570',
		expectedSample:     'Human',
		expectedTissue:     'Lung',
	]
	
	def params = [
		basePageClass:       DatasetExplorerPage.class,
		modelPageClass:      HeatmapAnalysisPage.class,
		subsetNode:          Constants.GSE8581_KEY,
		biomarkerNode:       "${Constants.GSE8581_KEY}MRNA\\Biomarker Data\\Affymetrix Human Genome U133A 2.0 Array\\Lung\\",
		analysisSelector:    "Heatmap",
		resultsOutoutHeader: "Heatmap",
		resultsImageURLLabel: "Heatmap",
		*:                       highDimExpectations
	]

	@Test
	void testSetup() {
		super.setUpAnalysis(params)
		super.confirmLandingPage()
	}

	// this one is failing and needs to be fixed
	@Ignore
	@Test
	void testClinicalVariable() {
		super.setUpAnalysis(params)
		super.getHighDimPopup()
		super.confirmHighDimPopup()
		super.runHighDimPopup()
		super.confirmHighDimPageSettings()
	}

	// this one is failing and needs to be fixed
	@Ignore
	@Test
	void testResults() {

		println("Running: HeatmapAnalysisTests - testResults; this test takes a long time to run, approximatly 6 min.")
		println("  You may want to disable it, via @Ignore, during development")
		
		super.setUpAnalysis(params)
		super.getHighDimPopup()
		super.runHighDimPopup()

		println("Starting analysis run. . .")
		super.runAnalysis()
		println("After analysis run: confirm results")
		super.confirmAnalysisResults()
	}
	
}
