package tests

import tests.GebReportingSpecTransmart
import spock.lang.Stepwise

import functions.Constants

import pages.AnalyzeQuery
import pages.modules.AnalyzeTabModule
import pages.analyses.SurvivalAnalysisPage

import java.util.regex.Pattern
import java.util.regex.Matcher

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsInAnyOrder
import static org.hamcrest.Matchers.is

@Stepwise
class ScrapTests extends GebReportingSpecTransmart {

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
    def setupSpec() {
            
    // I'm just using this test as a convenient way to make specific tests of matching operators
    }
    
    def "Test analysis setup"() {
        given: "On Analyze Tab"
        println "look for analyze page"
        loginTransmart(AnalyzeQuery)

        when: "Survival analysis"

        queryDragNodeToSubset params.subsetNode, 1

        analyzeTab.tabWorkflows.click()

        selectAnalysis 'Survival Analysis'

        then: "On the survival analysis page"
        at(SurvivalAnalysisPage)

        when: "Set survival analysis params"
        waitFor { analysisWidgetHeader }

        
        workflowDragNodeToBox params.timeVariable, timeBox

        workflowDragNodeToBox params.categoryVariableDragged, categoryBox

        then:
        waitFor (15) {setGeneTarget(params.searchKeyword)}

    }

    def setGeneTarget(geneTarget) {
        categoryHighDimButton.click()

        waitFor { highDimPopup.dialog }

        //filling the form fields is not instantaneous:
        waitFor(1) { highDimPopup.tissue }

        if (params.expectedMarkerType) {
            println "test popup markerType '${highDimPopup.markerType}'"
            assertThat highDimPopup.markerType, is(params.expectedMarkerType)
        }
        if (params.expectedPlatform) {
            println "test popup gplPlatform '${highDimPopup.gplPlatform}'"
            assertThat highDimPopup.gplPlatform, is(params.expectedPlatform)
        }
        if (params.expectedSample) {
            println "test popup sample '${highDimPopup.sample}'"
            assertThat highDimPopup.sample, is(params.expectedSample)
        }
        if (params.expectedTissue) {
            println "test popup tissue '${highDimPopup.tissue}'"
            assertThat highDimPopup.tissue, is(params.expectedTissue)
        }

        highDimPopup.searchBox << geneTarget
        // preload
        waitFor(10) { highDimPopup.anySearchItem }
        println "Searching '${geneTarget}' results size ${highDimPopup.anySearchItem.size()}"
        highDimPopup.selectSearchItem geneTarget
        println "selecting gene"
        highDimPopup.applyButton.click()
        println "popup Apply clicked"
        
        def probe = $('div#displaydivCategoryVariable').text()
        println("test probe '${probe}' contains '${geneTarget}': " + probe.contains(geneTarget))

        probe.contains(geneTarget)

    }
}
