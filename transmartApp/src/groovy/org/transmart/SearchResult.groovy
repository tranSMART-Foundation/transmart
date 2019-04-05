package org.transmart

import groovy.transform.CompileStatic

/**
 * @author mmcduffie
 */
@CompileStatic
class SearchResult {

    Map countmap = [:]

    // trial tab
    int analysisCount = 0
    int trialCount = 0

    // mRNA tab
    int mRNAAnalysisCount = 0
    int experimentCount = 0
    int allAnalysiCount = 0

    int documentCount = 0
    int litJubOncAltCount = 0
    int litJubOncInhCount = 0
    int litJubOncIntCount = 0
    int litJubAsthmaAltCount = 0
    int litJubAsthmaInhCount = 0
    int litJubAsthmaIntCount = 0
    int litJubAsthmaPECount = 0
    int resultCount = 0
    int profileCount = 0
    def summary
    def result
    String resultType

    int totalCount() {
	experimentCount + literatureCount() + trialCount + documentCount + profileCount
    }

    int litJubOncCount() {
	litJubOncAltCount + litJubOncInhCount + litJubOncIntCount
    }

    int litJubAsthmaCount() {
	litJubAsthmaAltCount + litJubAsthmaInhCount + litJubAsthmaIntCount + litJubAsthmaPECount
    }

    int literatureCount() {
	litJubOncCount() + litJubAsthmaCount()
    }
}
