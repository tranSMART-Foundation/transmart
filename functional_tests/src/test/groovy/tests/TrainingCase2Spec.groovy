package tests

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import functions.Constants

import pages.AnalyzeQuery
import pages.AnalyzeSummary
import pages.AnalyzeGridview
import pages.AnalyzeExport
import pages.AnalyzeSmartR
import pages.AnalyzeResultsPage
import pages.smartr.SmartRHeatmapFetch

import pages.modules.CommonHeaderModule
import pages.modules.AnalyzeTreeModule
import pages.modules.ScrollingModule

@Stepwise
class TrainingCase2Spec extends GebReportingSpecTransmart {

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
        queryDragNodeToSubset "${Constants.GSE35643_KEY}Subject\\Demographics\\Gender\\Male\\", 1
        queryDragNodeToSubset "${Constants.GSE35643_KEY}Subject\\Demographics\\Gender\\Male\\", 2
        println ""
        println "drag subset2"
        queryDragNodeToSubset "${Constants.GSE35643_KEY}Subject\\Medical History\\Atopy\\Atopic\\", 1
        queryDragNodeToSubset "${Constants.GSE35643_KEY}Subject\\Medical History\\Atopy\\Non-Atopic\\", 2

        then:
        at(AnalyzeQuery)

    }

    def "go to SmartR"() {
        when:
        analyzeTab.tabSmartR.click(AnalyzeSmartR)
        println "Selecting Heatmap SmartR"
        selectSmartRAnalysis("Heatmap Workflow")

        isAt(SmartRHeatmapFetch)
        println "On FetchData tab"

        then:
        at(SmartRHeatmapFetch)

        when:
        smartrTabs.each { it ->
            def finda = it.find('a')
            println "finda '${finda.text()}'"
        }
        println "highdim box '${smartrWorktabsFetchHighdim.attr('label')}'"

        analyzeTree.treeDragNodeToBox "${Constants.GSE35643_KEY}Subject\\Medical History\\Smoker Status\\", smartrWorktabsFetchCategoric
        analyzeTree.treeDragNodeToBox "${Constants.GSE35643_KEY}Subject\\Medical History\\Allergen\\", smartrWorktabsFetchCategoric

        analyzeTree.treeDragNodeToBox "${Constants.GSE35643_KEY}Biomarker Data\\Affymetrix Human Gene 1.0 ST Array [transcript (gene) version]\\bronchial airway smooth muscle (ASM) cells\\", smartrWorktabsFetchHighdim

        println "smartrTabsFetch text '${smartrTabsFetch.text()}'"
        println "smartrTabsFetch displayed '${smartrTabsFetch.isDisplayed()}'"
        println "smartrWorktabsFetchFetchbutton text '${smartrWorktabsFetchFetchbutton.text()}'"
        println "smartrTabsFetch displayed '${smartrWorktabsFetchFetchbutton.isDisplayed()}'"
        scrolling.scrollToBottom(smartrWorktabsFetchFetchbutton)
        println "smartrTabsFetch scrolled, displayed '${smartrWorktabsFetchFetchbutton.isDisplayed()}'"
        smartrWorktabsFetchFetchbutton.click()
        println "Fetch message size ${smartrWorktabsFetchComplete.size()}"
        println "Fetch message 0 '${smartrWorktabsFetchComplete[0].text()}'"
        waitFor(60*2) { smartrWorktabsFetchComplete.text().startsWith("Task complete!") }

        then:
        smartrWorktabsFetchComplete.text().startsWith("Task complete!")
        println "smartrTabsRun text '${smartrTabsRun.text()}'"
        println "smartrTabsRun displayed '${smartrTabsRun.isDisplayed()}'"
        smartrTabsRun.isDisplayed()
//
// check the summary statistics on the fetch page

        when:
        smartrTabsRun.click()
        waitFor { smartrWorktabsRunRunbutton.isDisplayed() }
        smartrWorktabsRunRunbutton.click()
//        waitFor(60*3) { smartrWorktabsRunPlot.isDisplayed() }

//        smartrWorktabsRunPlot.click()
        waitFor (60*3) { smartrHeatmapControlCluster }

        println "Cluster options size ${smartrHeatmapControlCluster.size()}"

        then:
        smartrHeatmapControlCluster.size() > 0
    }

    def cleanupSpec() {
    }

}
