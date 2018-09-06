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
import pages.smartr.SmartRBoxplotFetch

import pages.modules.CommonHeaderModule
import pages.modules.AnalyzeTreeModule

@Stepwise
class AnalyzePageSpec extends GebReportingSpecTransmart {

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
        queryDragNodeToSubset "${Constants.GSE8581_KEY}Subjects\\Lung Disease\\control\\", 1
        queryDragNodeToSubset "${Constants.GSE8581_KEY}Subjects\\Lung Disease\\chronic obstructive pulmonary disease\\", 2

        then:
        at(AnalyzeQuery)

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

        def getAge
        def getSex
        def getRace
        def getStats

        getAge = fetchSummaryAge(1)
        println "fetchSummaryAge 1 '${getAge}' points ${getAge['data points']}"
        getAge = fetchSummaryAge(2)
        println "fetchSummaryAge 2 '${getAge}' points ${getAge['data points']}"

        getSex = fetchSummarySex(1)
        println "fetchSummarySex 1 '${getSex}' male ${getSex['male']}"
        getSex = fetchSummarySex(2)
        println "fetchSummarySex 2 '${getSex}' male ${getSex['male']}"

        getRace = fetchSummaryRace(1)
        println "fetchSummaryRace 1 '${getRace}' caucasian ${getRace['caucasian']}"
        println "fetchSummaryRace 1 '${getRace}' caucasian ${getRace['caucasian']}"
        getRace = fetchSummaryRace(2)
        println "fetchSummaryRace 2 '${getRace}' caucasian ${getRace['caucasian']}"

        println "statSets[0] size ${statSets[0].find('table.analysis tr').size()}"
        println "statSets[1] size ${statSets[1].find('table.analysis tr').size()}"
        println "statSets[2] size ${statSets[2].find('table.analysis tr').size()}"

        getStats = fetchSummaryStats("Lung Disease",1)
        println "fetchSummaryStats 'Lung Disease' 1 '${getStats}' control ${getStats['control']}"

        getStats = fetchSummaryStats("Lung Disease",2)
        println "fetchSummaryStats 'Lung Disease' 2 '${getStats}' copd ${getStats['chronic obstructive pulmonary disease']}"

        getStats = fetchSummaryStats("control",1)
        println "fetchSummaryStats 'control' 1 '${getStats}' control ${getStats['control']}"

        getStats = fetchSummaryStats("control",2)
        println "fetchSummaryStats 'control' 2 '${getStats}' copd ${getStats['chronic obstructive pulmonary disease']}"

        then:
        at(AnalyzeSummary)
        statSets.size() == 3
        statSets[0].find('div.analysistitle').text() == 'Analysis of Lung Disease'
        statSets[1].find('div.analysistitle').text() == 'Analysis of control'
        statSets[2].find('div.analysistitle').text() == 'Analysis of chronic obstructive pulmonary disease'
    }

    def "view grid view"() {
        when:
        analyzeTab.tabGridview.click(AnalyzeGridview)

        println "GridView size ${gridview.size()}"
        println "GridHead size ${gridhead.size()}"
        println "GridCols size ${gridcols.size()} sort ${gridsort?.text()} up ${gridup?.text()} down ${griddown?.text()}"
        println "before click ${gridcols[3].classes()}"
        gridcols[3].click()
        println "after click ${gridcols[3].classes()} ${gridsort?.text()}"
        println "GridCols after click on 3 sort ${gridsort?.text()} up ${gridup?.text()} down ${griddown?.text()}"
        println "GridRow size ${gridrow.size()}"
        println "GridRow[0] size ${griddata(gridrow[0]).size()}"
        gridcols.eachWithIndex { it, i ->
            println "column ${i}: '${it.text()}'"
        }

        then:
        at(AnalyzeGridview)

        //try dragging in a leaf node,
        // a categorical node
        // a numeric node
        // a HDD node with a selected gene
        // a node (leaf/categorical) already included
        // a duplicate name (from Test Studies) to catch name correction
        // check in each case for a new column, and any previous changed columns
    }

    def "view export data"() {
        when:
        analyzeTab.tabExport.click(AnalyzeExport)
        
        println "exporttitle '${exporttitle}'"
        println "exporthead size ${exporthead.size()}"
        println "exportcols size ${exportcols.size()}"
        println "exportrow size ${exportrow.size()}"
        println "exportrow[0] size ${exportdata(exportrow[0]).size()}"
        
        //drag male adds 'male' tag in data like grid view
        // check export data popup can be captured and used
        
        then:
        at(AnalyzeExport)
    }
    
    def cleanupSpec() {
    }

}
