package pages.analyses

import pages.DatasetExplorerPage

class BoxPlotPage extends DatasetExplorerPage {

    static at = {
        selectedAnalysis == 'Box Plot with ANOVA'
    }

    static content = {
        def parentContent = DatasetExplorerPage.content
        parentContent.delegate = delegate
        parentContent.call()

        analysisWidgetHeader {
            $('div#analysisWidget h2')
        }

        independentVariableBox { $('div#divIndependentVariable') }
        dependentVariableBox { $('div#divDependentVariable') }

        runButton { $('input.runAnalysisBtn') }

        resultOutput { $('#analysisOutput form') }

        analysisHeaders { text ->
            $('span.AnalysisHeader').findAll {
                it.text() == text
            }
        }
    }
}
