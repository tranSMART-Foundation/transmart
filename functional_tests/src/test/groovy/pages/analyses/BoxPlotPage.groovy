package pages.analyses

import pages.AnalyzeWorkflow

class BoxPlotPage extends AnalyzeWorkflow {

    static at = {
        selectedAnalysis == 'Box Plot with ANOVA'
    }

    static content = {
        def parentContent = AnalyzeWorkflow.content
        parentContent.delegate = delegate
        parentContent.call()

        analysisWidgetHeader {
            $('div#analysisWidget h2')
        }

        independentVariableBox { $('div#divIndependentVariable') }
        dependentVariableBox { $('div#divDependentVariable') }  // dependentVariableBox

        runButton { $('input.runAnalysisBtn') }

        resultOutput { $('#analysisOutput form') }

        analysisHeaders { text ->
            $('span.AnalysisHeader').findAll {
                it.text() == text
            }
        }
        analysisOutput { $('table.AnalysisResults') }
        analysisStats { analysisOutput[0].find('tr') }
        analysisGroups { analysisOutput[1].find('tr') }
        analysisPairs { analysisOutput[2].find('tr') }
        analysisPvalue  { analysisStats.find('td')[0] }
        analysisFvalue  { analysisStats.find('td')[1] }
        analysisGroup  { analysisGroups('td')[0] }
    }

    Map fetchAnalysisGroup(int igroup) {
        def rows = analysisGroups
        println "fetchAnalysisGroup(${igroup}) size ${rows.size()}"
        Map result = [:]
        def cols = []
        rows[0].find('th').eachWithIndex { it,i -> 
            cols[i] = it.text()
        }
        rows[igroup].find('td').eachWithIndex  { it,i -> 
            result.put(cols[i],it.text())
        }
        return result
    }

    def fetchAnalysisPair(int igroup, int jgroup) {
        def rows = analysisPairs
        println "fetchAnalysisGroup(${igroup},${jgroup}) size ${rows.size()}"
        // first item is 'th' so count columns from zero
        return rows[jgroup-1].find('td')[igroup-1].text()
    }
}
