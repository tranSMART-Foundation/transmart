package pages

import geb.Page
import geb.waiting.WaitTimeoutException
import geb.navigator.Navigator

import pages.modules.CommonHeaderModule
import pages.modules.AnalyzeTreeModule
import pages.modules.AnalyzeTabModule
import pages.modules.AnalyzeModule
import pages.AnalyzeWorkflow

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.contains

class AnalyzeSummary extends Page {

    static url = 'datasetExplorer/index'

    static at = {
        println "AnalyzeSummary test header '${commonHeader.currentMenuItem?.text()}' expect '${commonHeader.TOPMENU_ANALYZE}'"
        commonHeader.currentMenuItem?.text() == commonHeader.TOPMENU_ANALYZE
        println "AnalyzeSummary test tab '${analyzeTab.tabSelected.text()}' expect '${analyzeTab.ANALYZE_TAB_SUMMARY}'"
        analyzeTab.tabSelected.text() == analyzeTab.ANALYZE_TAB_SUMMARY
        println "Found ${analyzeTree.topNodes.size()} nodes in analyzeTree"
        analyzeTree.topNodes.size() > 0
        println "tables '${tables.size()}'"
        println "summary '${summary}'"
        println "summaryTitle '${summaryTitle}'"
        println "summaryTitle.text '${summaryTitle.text()}'"
        summaryTitle.text() == "Summary Statistics"
    }

    static content = {
        commonHeader { module CommonHeaderModule }
        analyzeTab { module AnalyzeTabModule }
        analyzeTree { module AnalyzeTreeModule }
        analyze { module AnalyzeModule }

        tables(wait:true) { $('div.analysis').children('table') }
        summary { tables[0] }
        stats { tables[1] }

        summaryRows  { summary.children('tbody').children('tr') }
        summaryTitle  { summaryRows[0] }
        summaryQuery  { summaryRows[1] }
        summaryTotal  { summaryRows[2] }
        summaryAge  { summaryRows[3] } // histogram, table, table, boxplot
        summarySex  { summaryRows[4] } // pie + table, pie + table
        summaryRace { summaryRows[5] } // pie + table, pie + table

        statSets   { stats.children('tbody').children('tr') }
        statSets0  { statSets[0] }
        statSets1  { statSets[1] }
        statSets2  { statSets[2] }
    }

    String fetchSummaryTitle() {
        return summaryTitle.find('div.analysistitle').text()
    }

    String fetchSummaryQuery(int isubset = 0) {
        def rows = summaryQuery.find('div.analysis tr')
        if(isubset) {
            return rows[2*isubset - 1].text()
        } else {
            return rows[1].text()
        }
    }

    String fetchSummaryTotal(int isubset = 0) {
        def row = summaryTotal.find('table.analysis tr')[1].find('td')
        if(isubset) {
            return row[2*(isubset-1)].text()
        } else {
            return row[1].text()
        }
    }

    Map fetchSummaryAge(int isubset) {
        def tables = summaryAge.find('table.analysis')
        def graphs = summaryAge.find('svg')
        def ageHist = graphs[0]
        def ageBox = graphs[1]

        def subtable = tables[isubset-1]
        Map result = [:]
        subtable.find('tr').each { it -> 
            String[] rowsplit = it.text().split(': ')
            result.put(rowsplit[0].toLowerCase(),rowsplit[1])
        }
        return result
    }

    Map fetchSummarySex(int isubset) {
        def tables = summarySex.find('table.analysis')
        def graphs = summarySex.find('svg')

        def subtable = tables[isubset-1]
        Map result = [:]
        def cols = []
        subtable.find('tr').each { it -> 
            cols = it.find('td')
            if(cols)
                result.put(cols[0].text().toLowerCase(),cols[1].text())
        }
        return result
    }

    Map fetchSummaryRace(int isubset) {
        def tables = summaryRace.find('table.analysis')
        def graphs = summaryRace.find('svg')

        def subtable = tables[isubset-1]
        Map result = [:]
        def cols = []
        subtable.find('tr').each { it -> 
            cols = it.find('td')
            if(cols)
                result.put(cols[0].text().toLowerCase(),cols[1].text())
        }
        return result
    }

    Map fetchSummaryStats(String nodeName, int isubset) {
        println "fetchSummaryStats '${nodeName}' subset ${isubset}"
        def stats = statSets.find { it.find('div.analysistitle').text() == "Analysis of "+nodeName }
        assert stats
        println "fetchSummaryStats '${nodeName}' size ${stats.size()}"
        def subtable = stats.find('table.analysis')[isubset-1]

        Map result = [:]
        def cols = []
        subtable.find('tr').each { it -> 
            cols = it.find('td')
            if(cols)
                result.put(cols[0].text().toLowerCase(),cols[1].text())
        }
        return result
    }

}

