package com.recomdata.tea

import groovy.transform.CompileStatic
import org.transmart.AnalysisResult

/**
 * Base class for result classes for trials and experiments
 * @author jspencer
 */
@CompileStatic
class TEABaseResult {

    Long expCount

    long analysisCount = 0
    long inSignificantAnalCount = 0

    // contains all analyses
    List<AnalysisResult> analysisResultList = []

    // subset of above (insignificant TEA analyses)
    List<AnalysisResult> insigAnalResultList = []

    // if results should be groupd by experiment
    boolean groupByExp = false

    // count of biomarkers included in the search
    long bioMarkerCt = 0

    /**
     * set list of insignificat TEA analyses
     */
    void populateInsignificantTEAAnalysisList() {
	for (AnalysisResult it in analysisResultList) {
	    if (!it.bSignificantTEA) {
		insigAnalResultList << it
	    }
	}
	insigAnalResultList.sort()
        inSignificantAnalCount = insigAnalResultList.size()
    }
}
