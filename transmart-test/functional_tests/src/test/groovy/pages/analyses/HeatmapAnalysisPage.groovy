package pages.analyses

import com.google.common.collect.*
import geb.navigator.Navigator
import pages.AnalyzeWorkflow
import pages.modules.BinningModule
import pages.modules.HighDimensionPopupModule

class HeatmapAnalysisPage extends AnalyzeWorkflow {
	
    static at = {
        println "HeatmapAnalysisPage selectedAnalysis '${selectedAnalysis}'"
        selectedAnalysis == 'Heatmap'
    }

    static content = {
        def parentContent = AnalyzeWorkflow.content
        parentContent.delegate = delegate
        parentContent.call()

        highDimPopup { module HighDimensionPopupModule }

        analysisWidgetHeader {
            println "finding analysisWidgetHeader"
            $('div#analysisWidget h2')
        }

        highDimBox(wait: true)      {
            println "finding highDimBox in ${$().tag()} size ${$().size()} as ${$('div#divIndependentVariable').attr('id')}"
            def saveme = $('div#divIndependentVariable')
            println "found highDimBox as ${saveme.tag()} id ${saveme.attr('id')} class ${saveme.attr('class')}"
            $('div#divIndependentVariable')
        }

        highDimDisplay  { $('div#displaydivIndependentVariable') }
		
        runButton    { $('input.runAnalysisBtn') }

        resultOutputHeader { $('#analysisOutput').find('h2') }

        resultOutputHint { $('#analysisOutput').find('div.plot_hint') }
		
        resultsImageLink { $('#analysisOutput img') }
        resultsImageUrl { $('#analysisOutput img').@src }
        resultsDownloadLink { $('a.AnalysisLink') }
        resultsDownloadData { $('a.AnalysisLink').@href }

        categoryHighDimButton {
            $('div.highDimContainer div.highDimBtns button').find {
                it.text() == 'High Dimensional Data'
            }
        }

        analysisHeaders { text ->
            $('span.AnalysisHeader').findAll {
                it.text() == text
            }
        }

    }
}
