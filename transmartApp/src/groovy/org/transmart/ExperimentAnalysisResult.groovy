package org.transmart

import com.recomdata.tea.TEABaseResult
import groovy.transform.CompileStatic
import org.transmart.biomart.Experiment

/**
 * @author mmcduffie
 */
@CompileStatic
class ExperimentAnalysisResult extends TEABaseResult {

    Experiment experiment

    // current page rendering in session
    List<AnalysisResult> pagedAnalysisList
}
