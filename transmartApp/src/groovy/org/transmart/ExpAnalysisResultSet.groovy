package org.transmart

import com.recomdata.tea.TEABaseResult
import groovy.transform.CompileStatic

/**
 * @author mmcduffie
 */
@CompileStatic
class ExpAnalysisResultSet {
    List<TEABaseResult> expAnalysisResults = []
    Long analysisCount
    Long expCount
    boolean groupByExp = false
}
