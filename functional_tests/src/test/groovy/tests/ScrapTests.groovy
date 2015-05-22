package tests

import org.junit.Ignore
import org.junit.Test
import org.openqa.selenium.WebElement

import functions.Constants

import pages.AnalyzePage
import pages.analyses.SurvivalAnalysisPage

import java.util.regex.Pattern
import java.util.regex.Matcher

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.is

class ScrapTests extends CheckLoginPageAbstract {

    // I'm just using this test as a convenient way to make specific tests of matching operators
    @Ignore
    @Test
    void highDimensionalPanelTests() {
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
                categoryVariableDragged: "${Constants.GSE8581_KEY}MRNA\\Biomarker Data\\GPL570\\Lung\\",
                searchKeyword:           'TP53',
                *:                       highDimExpectations,
                binningParams:           binningParams,
        ]


        goToPageMaybeLogin AnalyzePage

        dragNodeToSubset params.subsetNode, 1, 1

        selectAnalysis 'Survival Analysis'
        page SurvivalAnalysisPage
        verifyAt()

        waitFor { analysisWidgetHeader }

        dragNodeToBox params.timeVariable, timeBox

        dragNodeToBox params.categoryVariableDragged, categoryBox

        waitFor (15) {setGeneTarget(params.searchKeyword)}
    }

    def setGeneTarget(geneTarget) {
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

        highDimPopup.searchBox << geneTarget
        // preload
        waitFor(10) { highDimPopup.anySearchItem }
        highDimPopup.selectSearchItem geneTarget
        highDimPopup.applyButton.click()

        def probe = $('div#displaydivCategoryVariable').text()
        println("contains: " + probe.contains(geneTarget))

        probe.contains(geneTarget)

    }
}
