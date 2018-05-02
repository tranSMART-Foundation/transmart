package tests.analysis

import geb.Page
import geb.junit4.GebReportingTest

import junit.framework.AssertionFailedError

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import functions.Constants

import pages.AnalyzeQuery
import pages.AnalyzeWorkflow
import pages.modules.AnalyzeTabModule
import pages.modules.AnalyzeTreeModule
import pages.analyses.CoxRegressionResult
import pages.analyses.SurvivalAnalysisPage
import pages.analyses.SurvivalAnalysisSummary

import static matchers.TableMatcher.table
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

@Stepwise
class SurvivalAnalysisSpec extends GebReportingSpecTransmart {

    def setupSpec() {
        loginTransmart(AnalyzeQuery)
    }

    def testClinicalVariable() {
        String sexKey     = "${Constants.GSE8581_KEY}Subjects\\Sex\\"
        def params = [
            subsetNode:              Constants.GSE8581_KEY,
                      timeVariable:            "${Constants.GSE8581_KEY}Subjects\\Age\\",
                      categoryVariableDragged: sexKey,
                      categoryVariables:       [
                          "${sexKey}male\\",
                          "${sexKey}female\\"]
        ]

        when:
        runAnalysis params

        then:        /* check cox regression result */
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
                [28, 28, 28, 28, 68, 63, 73]
            ]  //male
            assertThat allFittingSummaries[0],
            is(table( fittingSummaryRowHeaders,
                      SurvivalAnalysisSummary.ALL_HEADERS,
                      fittingSummaryData))
    }

    def testMrnaCategoryEvenlySpaced() {
        String sexKey     = "${Constants.GSE8581_KEY}Subjects\\Sex\\"

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
            subsetNode:              Constants.GSE8581_KEY,
            timeVariable:            "${Constants.GSE8581_KEY}Subjects\\Age\\",
            categoryVariableDragged: "${Constants.GSE8581_KEY}Biomarker Data\\Affymetrix Human Genome U133 Plus 2.0 Array\\Lung\\",
            searchKeyword:           'TP53',
            *:                       highDimExpectations,
            binningParams:           binningParams,
        ]

        when:
        to AnalyzeQuery
        runAnalysis params

        then:                   /* TODO: final assertions missing! */
        assertThat(0,is(0))
    }

    private void runAnalysis(Map params) {

        queryDragNodeToSubset params.subsetNode, 1

        analyzeTab.tabWorkflows.click()
        selectAnalysis 'Survival Analysis'
        page SurvivalAnalysisPage
        verifyAt()

        waitFor { analysisWidgetHeader }

        analyzeTree.treeDragNodeToBox params.timeVariable, timeBox

        if (params.categoryVariables) { // check all expected variables were found
            analyzeTree.treeDragNodeToBox params.categoryVariableDragged, categoryBox,
            containsInAnyOrder(params.categoryVariables.collect { is it as String })
        } else {                // leaf node
            analyzeTree.treeDragNodeToBox params.categoryVariableDragged, categoryBox
        }

        if (params.searchKeyword) {
            // there are some problems with timing, so wait
            // for searckKeyword popup
            waitFor (60*3, message: "SurvivalAnalysis - HighDimensional case - pathway/gene selection timed out")
                    {setSearchTarget(params)}
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
        waitFor(60*3, message: "SurvivalAnalysis RunButton.click() - timed out") { resultOutput } // wait up to 3 mins for result
    }

    def setSearchTarget(params) {
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

        def searchTarget = params.searchKeyword

        highDimPopup.searchBox << searchTarget

        // preload
        waitFor(10) { highDimPopup.anySearchItem }

        highDimPopup.selectSearchItem searchTarget
        highDimPopup.applyButton.click()

        def probe = $('div#displaydivCategoryVariable').text()
        println("contains: " + probe.contains(searchTarget))

        probe.contains(searchTarget)

    }

}
