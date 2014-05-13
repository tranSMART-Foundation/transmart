package pages.analyses

import com.google.common.collect.*
import geb.navigator.Navigator
import pages.DatasetExplorerPage
import pages.modules.BinningModule
import pages.modules.HighDimensionPopupModule

class HeatmapAnalysisPage extends DatasetExplorerPage {
	
    static at = {
        selectedAnalysis == 'Heatmap'
    }

    static content = {
        def parentContent = DatasetExplorerPage.content
        parentContent.delegate = delegate
        parentContent.call()

        highDimPopup { module HighDimensionPopupModule }

        analysisWidgetHeader {
            $('div#analysisWidget h2')
        }

        highDimBox      { $('div#divIndependentVariable') }
		
		highDimDisplay  { $('div#displaydivIndependentVariable') }
		
        runButton    { $('input.runAnalysisBtn') }

		resultOutputHeader { $('#analysisOutput').find('h2') }
		
		resultsImageUrl { $('#analysisOutput img').@src }

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
