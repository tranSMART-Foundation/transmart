package org.transmart

import com.recomdata.tea.TEABaseResult
import groovy.transform.CompileStatic
import org.transmart.biomart.ClinicalTrial
import org.transmart.biomart.ContentReference

/**
 * @author mmcduffie
 */
@CompileStatic
class TrialAnalysisResult extends TEABaseResult {
    ClinicalTrial trial

    ContentReference getProtocol() {
	for (ContentReference cr in trial.files) {
            if (cr.type == 'Protocol') {
		return cr
            }
        }
    }

    List<ContentReference> getFiles() {
	List<ContentReference> files = []
	for (ContentReference cr in trial.files) {
            if (cr.type != 'Protocol') {
		files << cr
            }
        }
	files
    }

    boolean hasResult() {
	analysisCount
    }
}
