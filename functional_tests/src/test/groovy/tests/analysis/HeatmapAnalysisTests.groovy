package tests.analysis

import static matchers.TableMatcher.table
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import geb.Page
import junit.framework.AssertionFailedError

import org.junit.Ignore
import org.junit.Test
import org.junit.Before

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


    //TODO: tests with Ignore need to be fixed

    @Before
	void testSetup() {
		super.setUpAnalysis(params)
		super.confirmLandingPage()
	}

	// this one is failing and needs to be fixed
	@Ignore
	@Test
	void testClinicalVariable() {
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


		println("Heatmap: Starting analysis run. . .")
		def startTime = System.currentTimeMillis()
		super.runAnalysis()
		def endTime = System.currentTimeMillis()
		def deltaTime = endTime - startTime
		def minutes = Math.floor(deltaTime/(1000 * 60))
		def seconds = Math.round((deltaTime - (minutes * 1000 *60))/1000)
		ptintln("Heatmap: analysis ran for " + minutes + " minutes and " + seconds + " seconds")
		println("Heatmap: After analysis run: confirm results")
		super.confirmAnalysisResults()
	}
	
}
