package tests.analysis

import org.junit.Test
import pages.DatasetExplorerPage
import pages.analyses.BoxPlotPage
import tests.CheckLoginPageAbstract

public class BoxPlotWithANOVATests extends CheckLoginPageAbstract{

    private void runAnalysis(Map params) {
        goToPageMaybeLogin DatasetExplorerPage

        dragNodeToSubset params.subsetNode, 1, 1

        selectAnalysis 'Box Plot with ANOVA'
        page BoxPlotPage
        verifyAt()

        waitFor { analysisWidgetHeader }

        dragNodeToBox params.variable, independentVariableBox

        dragNodeToBox params.category, dependentVariableBox

        runButton.click()
        waitFor(8, message: "SurvivalAnalysis RunButton.click() - timed out") { resultOutput } // wait up to 8 seconds for result
    }


    @Test
    void boxPlotTest() {
        def params = [
                subsetNode:    Constants.GSE8581_KEY,
                variable:      "${Constants.GSE8581_KEY}Endpoints\\FEV1\\",
                category:      "${Constants.GSE8581_KEY}Endpoints\\Diagnosis\\"
        ]
        runAnalysis params

    }

}
