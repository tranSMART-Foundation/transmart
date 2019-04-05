package com.recomdata.search.query

import groovy.transform.CompileStatic
import org.transmart.GlobalFilter
import org.transmart.KeywordSet

/**
 * @author mmcduffie
 */
@CompileStatic
class AssayStatsExpMarkerQuery extends AssayAnalysisDataQuery {

    /**
     * default criteria builder for biomarkers
     */
    void buildGlobalFilterBioMarkerCriteria(GlobalFilter gfilter, boolean expandBioMarkers) {
	KeywordSet biomarkerFilters = gfilter.bioMarkerFilters
	if (biomarkerFilters) {
	    String markerAlias = mainTableAlias + '.marker'
            if (expandBioMarkers) {
		addCondition createExpandBioMarkerCondition(markerAlias, gfilter)
            }
            else {
		addCondition markerAlias + '.id IN (' + biomarkerFilters.keywordDataIdString + ') '
            }
        }
    }
}
