package pages.analyses

import com.google.common.collect.*
import geb.navigator.Navigator
import pages.AnalyzePage
import pages.modules.BinningModule
import pages.modules.HighDimensionPopupModule

class SurvivalAnalysisPage extends AnalyzePage {

    static at = {
        selectedAnalysis == 'Survival Analysis'
    }

    static content = {
        def parentContent = AnalyzePage.content
        parentContent.delegate = delegate
        parentContent.call()

        highDimPopup { module HighDimensionPopupModule }
        binning      { module BinningModule }

        analysisWidgetHeader {
            $('div#analysisWidget h2')
        }

        timeBox      { $('div#divTimeVariable') }
        categoryBox  { $('div#divCategoryVariable') }
        censoringBox { $('div#divCensoringVariable') }

        runButton    { $('input.runAnalysisBtn') }

        resultOutput { $('#analysisOutput form') }

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

        fittingSummaries {
            analysisHeaders('Survival Curve Fitting Summary').
                    collect { headerSpan ->
                        new SurvivalAnalysisSummary(
                                htmlTable: headerSpan.next('table.AnalysisResults'))
                    }
        }

        coxRegressionResults {
            analysisHeaders('Cox Regression Result').
                    collect { headerSpan ->
                        new CoxRegressionResult(
                                htmlTable: headerSpan.next('table.AnalysisResults'))
                    }
        }
    }
}

class SurvivalAnalysisSummary extends ForwardingTable<String, String, Integer> {

    Navigator htmlTable

    final static String NUMBER_OF_SUBJECTS_HEADER = 'Number of Subjects'
    final static String MAX_SUBJECTS_HEADER       = 'Max Subjects'
    final static String SUBJECTS_AT_START_HEADER  = 'Subjects at Start'
    final static String NUMBER_OF_EVENTS_HEADER   = 'Number of Events'
    final static String MEDIAN_TIME_VALUE_HEADER  = 'Median Time Value'
    final static String LOWER_RANGE_HEADER        = 'Lower Range of Time Variable, 95% Confidence Interval'
    final static String UPPER_RANGE_HEADER        = 'Upper Range of Time Variable, 95% Confidence Interval'

    @Lazy private Table delegate = buildTable()

    final static List<String> ALL_HEADERS =
        ImmutableList.of(NUMBER_OF_SUBJECTS_HEADER,
                MAX_SUBJECTS_HEADER, SUBJECTS_AT_START_HEADER,
                NUMBER_OF_EVENTS_HEADER, MEDIAN_TIME_VALUE_HEADER,
                LOWER_RANGE_HEADER, UPPER_RANGE_HEADER)

    private Table<String, String, Integer> buildTable() {
        def res = ImmutableTable.builder()

        htmlTable.find('tr').eachWithIndex { tr, trIndex ->
            if (trIndex == 0) {
                def expectedHeaders = ['Subset', *ALL_HEADERS].iterator()
                tr.children('th').each { th ->
                    assert th.text() == expectedHeaders.next()
                }
            } else {
                def rowHeader = tr.find('th').text()
                tr.children('td').eachWithIndex { td, index ->
                    res.put(rowHeader, ALL_HEADERS[index], td.text() as Integer)
                }
            }
        }

        res.build()
    }

    @Override
    protected Table<String, String, Integer> delegate() {
        delegate
    }
}

class CoxRegressionResult extends ForwardingMap<String, Object> {

    Navigator htmlTable

    final static String NUMBER_OF_SUBJECTS_HEADER = 'Number of Subjects'
    final static String NUMBER_OF_EVENTS_HEADER   = 'Number of Events'
    final static String LIKELIHOOD_RATIO_HEADER   = 'Likelihood ratio test'
    final static String WALD_HEADER               = 'Wald test'
    final static String LOGRANK_HEADER            = 'Score (logrank) test'

    final static List<String> ALL_HEADERS =
            ImmutableList.of(NUMBER_OF_SUBJECTS_HEADER,
                    NUMBER_OF_EVENTS_HEADER, LIKELIHOOD_RATIO_HEADER,
                    WALD_HEADER, LOGRANK_HEADER)

    @Lazy Map delegate = buildMap()

    private Map<String, Object> buildMap() {
        def res = ImmutableMap.builder()
        def headerIterator = ALL_HEADERS.iterator()

        htmlTable.find('tr').each { tr ->
            String header = headerIterator.next()
            assert tr.find('th').text() == header

            res.put header, tr.find('td').text()
        }

        res.build()
    }

    @Override
    protected Map<String, Object> delegate() {
        getDelegate()
    }
}
