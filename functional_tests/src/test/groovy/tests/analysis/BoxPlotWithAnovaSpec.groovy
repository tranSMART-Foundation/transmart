package tests.analysis

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import functions.Constants

import pages.AnalyzeQuery
import pages.AnalyzeWorkflow
import pages.modules.AnalyzeTabModule
import pages.analyses.BoxPlotPage

import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.is

@Stepwise
class BoxPlotWithAnovaSpec extends GebReportingSpecTransmart {

    def setupSpec() {
        loginTransmart(AnalyzeQuery)
    }

    def "Execute boxplot"() {

        when:
        println "executing boxPlotTest"
        def params = setParams()

        setUpAnalysisSubPage 'Box Plot with ANOVA', params

        runAnalysis params
        waitFor(60 * 3) { resultOutput } // may need to wait up to 3 min for result!

        then:
        verifyPage()
    }

    private void verifyPage() {
        def resultHeaders = ["Box Plot", "ANOVA Result", "Pairwise t-Test p-Values"]
        resultHeaders.each {
            assert analysisHeaders(it)
        }

    }

    private void runAnalysis(Map params) {

        println "runAnalysis dragNodeToBox ${params.variable}, independentVariableBox"
        workflowDragNodeToBox params.variable, independentVariableBox

        println "workflowDragNodeToBox ${params.categoryVariableDragged}, categoryBox, "+
                "containsInAnyOrder(params.categoryVariables.collect { is it as String })"
        workflowDragNodeToBox params.categoryVariableDragged, categoryBox,
                containsInAnyOrder(params.categoryVariables.collect { is it as String })

        println "runButton.click()"
        runButton.click()
        println "waitFor results"
        waitFor(8, message: "SurvivalAnalysis RunButton.click() - timed out") { resultOutput } // wait up to 8 seconds for result
    }

    private setUpAnalysisSubPage(String analysisHeader, Map params) {
        println "dragNodeToSubset ${params.subsetNode}, 1"
        
        queryDragNodeToSubset params.subsetNode, 1

        analyzeTab.tabWorkflows.click()
        println "selectAnalysis analysisHeader"
        selectAnalysis analysisHeader
        println "page BoxPlotPage"
        page BoxPlotPage

        println "verifyAt()"
        verifyAt()

        println "waitFor analysisWidgetHeader"
        waitFor { analysisWidgetHeader }
    }

    private setParams() {
        println "executing setParams"
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

    def cleanupSpec() {
    }
    
}
