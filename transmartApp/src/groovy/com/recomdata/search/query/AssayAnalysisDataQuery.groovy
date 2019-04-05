package com.recomdata.search.query

import groovy.transform.CompileStatic
import org.transmart.GlobalFilter

/**
 * @author mmcduffie
 */
@CompileStatic
class AssayAnalysisDataQuery extends Query {

    /**
     * create biomarker table alias
     */
    String getBioMarkerTable() {
	mainTableAlias + '.featureGroup.markers '
    }

    /**
     *  criteria builder for disease,override default
     */
    void buildGlobalFilterDiseaseCriteria(GlobalFilter gfilter) {
	if (gfilter.diseaseFilters) {
	    String dAlias = mainTableAlias + '_dis'
	    String dtable = mainTableAlias + '.experiment.diseases ' + dAlias
	    addTable 'JOIN ' + dtable
	    addCondition dAlias + '.id IN (' + gfilter.diseaseFilters.keywordDataIdString + ') '
	}
    }

    /**
     *  criteria builder for compound,override default
     */

    void buildGlobalFilterCompoundCriteria(GlobalFilter gfilter) {
	if (gfilter.compoundFilters) {
	    String dAlias = mainTableAlias + '_cpd'
	    String dtable = mainTableAlias + '.experiment.compounds ' + dAlias
	    addTable 'JOIN ' + dtable
	    addCondition dAlias + '.id IN (' + gfilter.compoundFilters.keywordDataIdString + ') '
	}
    }

    /**
     *  criteria builder for experiment,override default
     */
    void buildGlobalFilterExperimentCriteria(GlobalFilter gfilter) {
	if (gfilter.trialFilters) {
	    addCondition mainTableAlias + '.experiment.id IN (' + gfilter.trialFilters.keywordDataIdString + ')'
        }
	if (gfilter.studyFilters) {
	    addCondition mainTableAlias + '.experiment.id IN (' + gfilter.studyFilters.keywordDataIdString + ')'
        }
    }
}
