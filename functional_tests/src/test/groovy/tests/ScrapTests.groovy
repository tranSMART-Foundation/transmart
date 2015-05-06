package tests

import org.junit.Test
import pages.Constants
import pages.DatasetExplorerPage
import pages.analyses.SurvivalAnalysisPage

class ScrapTests extends CheckLoginPageAbstract {

    // I'm just using this test as a convenient way to make simple tests of matching operators

    @Test
    void simpleTest() {
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


        goToPageMaybeLogin DatasetExplorerPage

        dragNodeToSubset params.subsetNode, 1, 1

        selectAnalysis 'Survival Analysis'
        page SurvivalAnalysisPage
        verifyAt()

        waitFor { analysisWidgetHeader }

        dragNodeToBox params.timeVariable, timeBox

    }

}
