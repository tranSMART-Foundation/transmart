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
        categoryBox { $('div#divDependentVariable') }  // dependentVariableBox

        runButton { $('input.runAnalysisBtn') }

        resultOutput { $('#analysisOutput form') }

        analysisHeaders { text ->
            $('span.AnalysisHeader').any {
                it.text() == text
            }
        }
    }
}
