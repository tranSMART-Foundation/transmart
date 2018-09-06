package tests

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import functions.Constants

import pages.AnalyzeQuery
import pages.AnalyzeSummary
import pages.AnalyzeSmartR
import pages.AnalyzeResultsPage
import pages.smartr.SmartRBoxplotFetch

import pages.modules.CommonHeaderModule
import pages.modules.AnalyzeTreeModule
import pages.analyses.BoxPlotPage

@Stepwise
class TrainingCase1Spec extends GebReportingSpecTransmart {

    def setupSpec() {
        loginTransmart(AnalyzeQuery)
    }

    def "start on Analyze page"() {

        when:
        isAt(AnalyzeQuery)

        then:
        assert at(AnalyzeQuery)
    }

    def "make query"() {
        when:
        println ""
        println "drag subset1"
        queryDragNodeToSubset "${Constants.GSE8581_KEY}Subjects\\Lung Disease\\chronic obstructive pulmonary disease\\", 1
        println ""
        println "drag subset2"
        queryDragNodeToSubset "${Constants.GSE8581_KEY}Subjects\\Lung Disease\\control\\", 2

        then:
        at(AnalyzeQuery)

    }

    def "view summary statistics"() {
        when:
        analyzeTab.tabSummary.click(AnalyzeSummary)

        println "summaryTitle ${summaryTitle.find('div.analysistitle').text()}"
        println "fetchSummaryTitle '${fetchSummaryTitle()}'"
        println "summaryQuery size ${summaryQuery.find('table.analysis tr').size()}"
        println "fetchSummaryQuery 1 ${fetchSummaryQuery(1)}"
        println "fetchSummaryQuery 2 ${fetchSummaryQuery(2)}"
        println "summaryTotal size ${summaryTotal.find('table.analysis tr').size()}"
        println "summaryAge size ${summaryAge.find('table.analysis tr').size()}"

        def getStats

        println "statSets[0] size ${statSets[0].find('table.analysis tr').size()}"
        println "statSets[1] size ${statSets[1].find('table.analysis tr').size()}"
        println "statSets[2] size ${statSets[2].find('table.analysis tr').size()}"

        def getStats1 = fetchSummaryStats("Lung Disease",1)
        def getStats2 = fetchSummaryStats("Lung Disease",2)
        def getControl1 = fetchSummaryStats("control",1)
        def getControl2 = fetchSummaryStats("control",2)

        def getAge1 = fetchSummaryAge(1)
        def getAge2 = fetchSummaryAge(2)
        def getSex1 = fetchSummarySex(1)
        def getSex2 = fetchSummarySex(2)
        def getRace1 = fetchSummaryRace(1)
        def getRace2 = fetchSummaryRace(2)

        then:
        at(AnalyzeSummary)
        // Subset identification (note: query concepts are in brackets)
        fetchSummaryQuery(1).endsWith('\\chronic obstructive pulmonary disease\\)')
        fetchSummaryQuery(2).endsWith('\\control\\)')

        // Subset size
        fetchSummaryTotal(0) == '0'  // in both subsets
        fetchSummaryTotal(1) == '17' // in subset 1
        fetchSummaryTotal(2) == '19' // in subset 2

        // Basic statistics: Age
        getAge1['data points'] == '17'
        getAge1['median'] == '64.0'

        getAge2['data points'] == '19'
        getAge2['median'] == '67.0'

        // Basic statistics: Sex
        getSex1['female'] == '8'
        getSex1['male'] == '9'

        getSex2['female'] == '13'
        getSex2['male'] == '6'

        // Basic statistics: Ethnicity
        getRace1.size() == 2
        getRace1['afro american'] == '2'
        getRace2.size() == 2
        getRace2['afro american'] == '1'

        // Basic statistics for subset queries
        statSets.size() == 3
        statSets[0].find('div.analysistitle').text() == 'Analysis of Lung Disease'
        statSets[1].find('div.analysistitle').text() == 'Analysis of chronic obstructive pulmonary disease'
        statSets[2].find('div.analysistitle').text() == 'Analysis of control'
        getStats1.size() == 3
        getStats1['control'] == '0'
        getStats1['chronic obstructive pulmonary disease'] == '17'
        getStats1['not specified'] == '0'

        getStats2.size() == 3
        getStats2['control'] == '19'
        getStats2['chronic obstructive pulmonary disease'] == '0'
        getStats2['not specified'] == '0'

        // getControl is the same data as getStats ... from the same node
        getControl1.size() == 3
        getControl1['control'] == '0'
        getControl1['chronic obstructive pulmonary disease'] == '17'
        getControl1['not specified'] == '0'

        getControl2.size() == 3
        getControl2['control'] == '19'
        getControl2['chronic obstructive pulmonary disease'] == '0'
        getControl2['not specified'] == '0'

   }

    def "go to SmartR"() {
        when:
        analyzeTab.tabSmartR.click(AnalyzeSmartR)
        println "Selecting Boxplot SmartR"
        selectSmartRAnalysis("Boxplot Workflow")

        isAt(SmartRBoxplotFetch)
        println "On FetchData tab"

        then:
        at(SmartRBoxplotFetch)

        when:
        println "smartrTabs.size smartrTabs.size()"
        smartrTabs.each { it ->
            def finda = it.find('a')
            println "finda '${finda.text()}'"
        }
        
        analyzeTree.treeDragNodeToBox "${Constants.GSE8581_KEY}Endpoints\\FEV1\\", smartrWorktabsFetchNumeric

        println "smartrTabsFetch text '${smartrTabsFetch.text()}'"
        println "smartrTabsFetch displayed '${smartrTabsFetch.isDisplayed()}'"

        smartrWorktabsFetchFetchbutton.click()
        waitFor { smartrWorktabsFetchComplete.text().startsWith("Task complete!") }

        then:
        smartrWorktabsFetchComplete.text().startsWith("Task complete!")
        println "smartrTabsRun text '${smartrTabsRun.text()}'"
        println "smartrTabsRun displayed '${smartrTabsRun.isDisplayed()}'"
        smartrTabsRun.isDisplayed()

        when:
        smartrTabsRun.click()
        waitFor { smartrWorktabsRunRunbutton.isDisplayed() }
        smartrWorktabsRunRunbutton.click()

        waitFor { smartrBoxplotTitle }

        then:
        smartrBoxplotTitle.text().startsWith("Boxplots (raw)")
    }

    def "run boxplot advanced workflow"() {
        given:

        String diagnosisKey = "${Constants.GSE8581_KEY}Endpoints\\Diagnosis\\"
        def categoryVariables = [
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
        ]
        def resultHeaders = ["Box Plot", "ANOVA Result", "Pairwise t-Test p-Values"]

        when:
        analyzeTab.tabWorkflows.click()
        println "selectAnalysis analysisHeader"
        selectAnalysis "Box Plot with ANOVA"
        println "page BoxPlotPage"
        page BoxPlotPage

        then:
        println "verifyAt()"
        verifyAt()

        when:
        println "waitFor analysisWidgetHeader"
        waitFor { analysisWidgetHeader }
        println ""
        println "drop first independentVariable"
        analyzeTree.treeDragNodeToBox "${Constants.GSE8581_KEY}Subjects\\Lung Disease\\chronic obstructive pulmonary disease\\", independentVariableBox 
        println ""
        println "drop second independentVariable"
        analyzeTree.treeDragNodeToBox "${Constants.GSE8581_KEY}Subjects\\Lung Disease\\control\\", independentVariableBox
        println ""
        println "drop dependentVariable"
        analyzeTree.treeDragNodeToBox "${Constants.GSE8581_KEY}Endpoints\\FEV1\\", dependentVariableBox

        runButton.click()
        waitFor(20, message: "Boxplot with ANOVA RunButton.click() - timed out") { resultOutput } // wait up to 20 seconds for result
        
        waitFor(60 * 3) { analysisHeaders('ANOVA Result') } // may need to wait up to 3 min for result!

        def pvalue = analysisPvalue.text()
        def fvalue = analysisFvalue.text()

        println "Pvalue ${pvalue} Fvalue ${fvalue}"

        def group1 = fetchAnalysisGroup(1)
        def group2 = fetchAnalysisGroup(2)
        println "group 1 ${group1}"
        println "group 2 ${group2}"

        def pair12 = fetchAnalysisPair(1,2)
        println "Pair 1v2 ${pair12}"
        
        then:
        resultHeaders.each {
            assert analysisHeaders(it)
        }
        assert pvalue == "8.29e-07"
        assert fvalue == "36.2"
        assert pair12 == pvalue
        assert group1['Mean'] == "1.27"
        assert group2['n'] == "19"
    }

    def cleanupSpec() {
    }

}
