package tests.analysis

import org.junit.Test

import functions.Constants

import pages.DatasetExplorerPage
import pages.analyses.BoxPlotPage

import tests.CheckLoginPageAbstract

import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.is

public class BoxPlotWithANOVATests extends CheckLoginPageAbstract{

    @Test
    void boxPlotTest() {

        def params = setParams()

        setUpAnalysisSubPage 'Box Plot with ANOVA', params

        runAnalysis params

        verifyPage()
    }

    private void verifyPage() {
        assert(true)
    }

    private void runAnalysis(Map params) {

        dragNodeToBox params.variable, independentVariableBox

        dragNodeToBox params.categoryVariableDragged, categoryBox,
                containsInAnyOrder(params.categoryVariables.collect { is it as String })

        runButton.click()
        waitFor(8, message: "SurvivalAnalysis RunButton.click() - timed out") { resultOutput } // wait up to 8 seconds for result
    }

    private setUpAnalysisSubPage(String analysisHeader, Map params) {
        goToPageMaybeLogin DatasetExplorerPage

        dragNodeToSubset params.subsetNode, 1, 1

        selectAnalysis analysisHeader
        page BoxPlotPage
        verifyAt()

        waitFor { analysisWidgetHeader }
    }

    private setParams() {

        String diagnosisKey = "${Constants.GSE8581_KEY}Endpoints\\Diagnosis\\"

        return [
                subsetNode:    Constants.GSE8581_KEY,
                variable:      "${Constants.GSE8581_KEY}Endpoints\\FEV1\\",
                categoryVariableDragged:      diagnosisKey,
                categoryVariables:       [
                        "${diagnosisKey}carcinoid\\",
                        "${diagnosisKey}emphysema\\",
                        "${diagnosisKey}giant bullae\\",
                        "${diagnosisKey}Giant Cell Tumor\\",
                        "${diagnosisKey}hematoma\\",
                        "${diagnosisKey}inflammation\\",
                        "${diagnosisKey}lymphoma\\",
                        "${diagnosisKey}metastatic non-small cell adenocarcinoma\\",
                        "${diagnosisKey}metastatic renal cell carcinoma\\",
                        "${diagnosisKey}no malignancy\\",
                        "${diagnosisKey}non-small cell adenocarcinoma\\",
                        "${diagnosisKey}non-small cell squamous cell carcinoma\\",
                        "${diagnosisKey}NSC-Mixed\\",
                        "${diagnosisKey}Unknown\\"
                ]]
    }

}
