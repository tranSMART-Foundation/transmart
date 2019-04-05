package org.transmart

import groovy.transform.CompileStatic
import org.transmart.biomart.BioAssayAnalysis

/**
 * @author mmcduffie
 */
@CompileStatic
class AnalysisResult implements Comparable<AnalysisResult> {

    // TEA metrics
    Double teaScore
    boolean bTeaScoreCoRegulated = false
    boolean bSignificantTEA = false
    int defaultTop = 5

    BioAssayAnalysis analysis
    Long experimentId
    String experimentAccession
    List<AssayAnalysisValue> assayAnalysisValueList = []
    long bioMarkerCount = 0

    int size() {
	assayAnalysisValueList.size()
    }

    String getGeneNames() {
	if (!assayAnalysisValueList) {
            return null
	}

        StringBuilder s = new StringBuilder()
	LinkedHashSet<String> nameSet = []
        // remove dup first
        for (value in assayAnalysisValueList) {
            def marker = value.bioMarker
            if (marker.isGene()) {
		nameSet << marker.name
            }
        }

        for (name in nameSet) {
	    if (s) {
		s << ', '
	    }
	    s << name
        }

	s
    }

    boolean showTop() {
        // bioMarkerCount was populated only when it's NOT searching for genes
	bioMarkerCount > defaultTop
    }

    List<AssayAnalysisValue> getAnalysisValueSubList() {
        if (showTop()) {
            def total = defaultTop
            if (assayAnalysisValueList.size() <= defaultTop) {
                total = assayAnalysisValueList.size()
            }
            if (total < 0) {
                total = 0
            }

	    assayAnalysisValueList.subList(0, total)
        }
        else {
            // show all
	    assayAnalysisValueList
        }
    }

    /**
     * comparable interface implementation, sort on TEAScore
     */
    int compareTo(AnalysisResult compare) {
        Double thisScore = teaScore
        Double compScore = compare.teaScore

        // handle invalid values
	if (compScore == null && thisScore != null) {
	    1
	}
	else if (thisScore == null && compScore != null) {
	    -1
	}
	else if (thisScore == null && compScore == null) {
	    0
	}
	else if (thisScore == compScore) {
            // if score is the same, sort on biomarker ct (desc)
	    -1 * assayAnalysisValueList.size().compareTo(compare.assayAnalysisValueList.size())
        }
        else {
	    thisScore.compareTo(compScore)
        }
    }

    /**
     * the TEA score is calculated as -log(teaScore) for UI purposes
     */
    double calcDisplayTEAScore() {
	if (teaScore != null) {
	    -Math.log(teaScore.doubleValue())
	}
    }
}
