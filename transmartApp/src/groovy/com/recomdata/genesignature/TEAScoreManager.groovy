package com.recomdata.genesignature

import com.recomdata.util.BinomialDistribution
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.transmart.AnalysisResult
import org.transmart.AssayAnalysisValue
import org.transmart.biomart.BioAssayAnalysisData
import org.transmart.biomart.BioMarker

/**
 * manager class for TEA scoring logic.
 * @author mmcduffie
 */
@CompileStatic
@Slf4j('logger')
class TEAScoreManager {

    // this is a cutoff for UI display
    static final double TEA_SIGNIFICANCE_CUTOFF = 0.05

    // number of genes used in entire search analysis
    int geneCount = 0

    /**
     * applies the TEA scoring algorithm for the specified AnalysisResult based on its value object list
     * The algorithm populates the AnalysisResult with the various TEA metrics
     */
    void assignTEAMetrics(AnalysisResult analysisResult) {

        // list of bio markers to score
	List<AssayAnalysisValue> valueList = analysisResult.assayAnalysisValueList
	if (!valueList) {
	    // analysisResult.TEAScore = null   TODO missing property
            return
        }

        // counters
        int pValCtUp = 0
        int pValCtDown = 0

        // log sums
        double pValSumUp = 0
        double pValSumDown = 0

	Double compFoldChg
	Double gsFoldChg
	Double npv

	Map<Long, BioMarker> mapMarkers = [:]
	for (AssayAnalysisValue value in valueList) {
	    BioAssayAnalysisData baad = value.analysisData
	    BioMarker bm = value.bioMarker

            // track each biomarker that has been evaluated
	    BioMarker currMarker = mapMarkers[bm.id]
	    if (currMarker) {
		logger.warn 'skipping duplicate bioMarker ({}): 1) Comp fold chg:{}; 2) NPV: {}; 3) Regulation fold chg: {}',
		    bm.name, compFoldChg, npv, gsFoldChg
                continue
            }

            // track evaluated marker
	    mapMarkers[bm.id] = bm

            // data used in calc
            compFoldChg = baad.foldChangeRatio
	    npv = baad.teaNormalizedPValue
            gsFoldChg = value.valueMetric

            if (gsFoldChg == null || gsFoldChg == 0) {
                // a) genes and pathways
                if (compFoldChg > 0) {
                    pValCtUp++
		    pValSumUp += -Math.log(npv)
                }
                else {
                    pValCtDown++
		    pValSumDown += -Math.log(npv)
                }
            }
            else {
                // b) gene lists and signatures
                if ((gsFoldChg > 0 && compFoldChg > 0) || (gsFoldChg < 0 && compFoldChg < 0)) {
                    pValCtUp++
		    pValSumUp += -Math.log(npv)
                }
                else {
                    pValCtDown++
		    pValSumDown += -Math.log(npv)
                }
            }
        }

        // final TEA scores (set initially to a large number)
        double TEAScoreUp = 1.1
        double TEAScoreDown = 1.1

	logger.info '1) up count: {}; down count: {}', pValCtUp, pValCtDown

        // up score
	if (pValCtUp > 0) {
	    TEAScoreUp = calcTEAScore(pValCtUp, pValSumUp, 'up')
	}

        // down score
	if (pValCtDown > 0) {
	    TEAScoreDown = calcTEAScore(pValCtDown, pValSumDown, 'down')
	}

	analysisResult.teaScore = Math.min(TEAScoreUp, TEAScoreDown)

        // enrichment status
	if (pValCtDown == 0 && pValCtUp > 0) {
	    analysisResult.bTeaScoreCoRegulated = true
	}

	if (pValCtDown > 0 && pValCtUp == 0) {
	    analysisResult.bTeaScoreCoRegulated = false
	}

	if (pValCtDown > 0 && pValCtUp > 0) {
	    analysisResult.bTeaScoreCoRegulated = TEAScoreUp <= TEAScoreDown
	}

        // significant TEA score?
        analysisResult.bSignificantTEA = analysisResult.teaScore.doubleValue() <= TEA_SIGNIFICANCE_CUTOFF
    }

    /**
     * calc TEA score for indicated side
     */
    double calcTEAScore(int sideCt, double sideSum, String side) {
	double pValAvg = Math.exp(-sideSum / sideCt)
	1 - new BinomialDistribution(geneCount, pValAvg).getCDF(sideCt)
    }
}
