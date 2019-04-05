package org.transmart

import groovy.transform.CompileStatic
import org.transmart.biomart.BioAssayAnalysisData
import org.transmart.biomart.BioMarker

/**
 * @author mmcduffie
 */
@CompileStatic
class AssayAnalysisValue implements Comparable<AssayAnalysisValue> {

    BioAssayAnalysisData analysisData
    BioMarker bioMarker

    // indicator for the up/down regulation (i.e. gene lists and signatures). If null implies
    // we don't care about the up/down regulation such as for a pathway
    Double valueMetric

    /**
     * comparable interface implementation, sort on NPV
     */
    int compareTo(AssayAnalysisValue compare) {
        Double thisScore = analysisData.teaNormalizedPValue
        Double compScore = compare.analysisData.teaNormalizedPValue

        // handle invalid values
	if (compScore == null && thisScore != null) {
	    1
	}
	else if (thisScore == null && compScore != null) {
	    -1
	}
	else  if (thisScore == null && compScore == null) {
	    0
	}
	else {
	    thisScore <=> compScore
	}
    }
}
