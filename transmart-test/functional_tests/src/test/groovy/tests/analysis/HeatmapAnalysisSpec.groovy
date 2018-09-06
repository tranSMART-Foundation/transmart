package tests.analysis

import static matchers.TableMatcher.table
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import geb.Page
import junit.framework.AssertionFailedError

import org.junit.Ignore
import org.junit.Test
import org.junit.Before

import functions.Constants

import pages.AnalyzeQuery
import pages.analyses.HeatmapAnalysisPage

import spock.lang.Stepwise

@Stepwise
public class HeatmapAnalysisSpec extends HeatmapSupportAbstract {
	
    def setupSpec() {
        loginTransmart(AnalyzeQuery)
    }

    def highDimExpectations = [
        expectedMarkerType: 'Gene Expression',
        expectedPlatform:   'GPL570',
        expectedSample:     'Human',
        expectedTissue:     'Lung',
    ]
	
    def params = [
        basePageClass:       AnalyzeQuery.class,
	modelPageClass:      HeatmapAnalysisPage.class,
	subsetNode:          Constants.GSE8581_KEY,
        biomarkerNode:       "${Constants.GSE8581_KEY}Biomarker Data\\Affymetrix Human Genome U133 Plus 2.0 Array\\Lung\\",
	analysisSelector:    "Heatmap",
	resultsOutputHeader: "Heatmap",
	resultsImageURLLabel: "Heatmap",
	*:                       highDimExpectations
    ]

    void testClinicalVariable() {
        given:
        setUpAnalysis(params)

        when:
        getHighDimPopup()

        then:
        confirmHighDimPopup()

        when:
        runHighDimPopup()

        then:
        confirmHighDimPageSettings()
    }

    // this one is failing and needs to be fixed
    // @Ignore
    void testResults() {

        given:
        setUpAnalysis(params)

        when:
        getHighDimPopup()
        runHighDimPopup()


        def startTime = System.currentTimeMillis()
        runAnalysis()
        def endTime = System.currentTimeMillis()
        def deltaTime = endTime - startTime
        def minutes = Math.floor(deltaTime/(1000 * 60))
        def seconds = Math.round((deltaTime - (minutes * 1000 *60))/1000)
        println("Heatmap: analysis ran for " + minutes + " minutes and " + seconds + " seconds")

        then:
        confirmAnalysisResults()
        println "trying to download Heatmap.png from '${resultsImageUrl}'"
        resultsImageLink.click()
        println "tring to download ZIP file from '${resultsDownloadData}'"
        resultsDownloadLink.click()
        println "all downloads tried"
    }

    def cleanupSpec() {
    }

}
