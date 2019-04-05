package org.transmart

import groovy.transform.CompileStatic

/**
 * @author mmcduffie
 */
@CompileStatic
class TrialFilter {

    List<String> selectedtrials = []
    boolean newFilter = true

    String platform
    Double foldChange
    Double pvalue
    Double rvalue
    Long bioDiseaseId
    Long bioCompoundId
    String phase
    String studyType
    String studyDesign
    String status

    boolean hasSelectedTrials() {
	selectedtrials
    }

    String createTrialInclause() {
	StringBuilder s = new StringBuilder()
        for (n in selectedtrials) {
	    if (s) {
		s << ','
            }
	    s << "'" << n << "'"
        }
	s
    }

    boolean hasPlatform() {
	platform
    }

    boolean hasFoldChange() {
	foldChange
    }

    boolean hasPValue() {
	pvalue
    }

    boolean hasRValue() {
	rvalue != null
    }

    boolean hasDisease() {
	bioDiseaseId
    }

    boolean hasCompound() {
	bioCompoundId > 0
    }

    boolean hasPhase() {
	phase
    }

    boolean hasStudyType() {
	studyType
    }

    boolean hasStudyDesign() {
	studyDesign
    }

    String createListTrialInclause() {
	selectedtrials*.toString()
    }

    def marshal() {}
}
