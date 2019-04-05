package com.recomdata.search.query

import groovy.transform.CompileStatic
import org.transmart.GlobalFilter

/**
 * @author mmcduffie
 */
@CompileStatic
class LiteratureDataQuery extends Query {

    /**
     *  criteria builder for experiment,override default
     */
    void buildGlobalFilterExperimentCriteria(GlobalFilter gfilter) {
	if (gfilter.trialFilters) {
	    addCondition '1 = 0'
        }
    }

    void buildGlobalFilterStudyCriteria(GlobalFilter gfilter) {
	if (gfilter.studyFilters) {
	    addCondition '1 = 0'
        }
    }
}
