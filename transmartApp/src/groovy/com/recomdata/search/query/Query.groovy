package com.recomdata.search.query

import groovy.transform.CompileStatic
import org.transmart.GlobalFilter
import org.transmart.KeywordSet

/**
 * @author mmcduffie
 */
@CompileStatic
class Query {

    boolean setDistinct
    String mainTableAlias

    LinkedHashSet<String> selectClause = []
    LinkedHashSet<String> fromClause = []
    LinkedHashSet<String> whereClause = []
    LinkedHashSet<String> groupbyClause = []
    LinkedHashSet<String> orderbyClause = []

    void addSelect(String columnWithAlias) {
	selectClause << columnWithAlias.trim()
    }

    void addTable(String tableWithAlias) {
	fromClause << tableWithAlias.trim()
    }

    void addCondition(String condition) {
	whereClause << condition.trim()
    }

    void addGroupBy(String groupby) {
	groupbyClause << groupby.trim()
    }

    void addOrderBy(String orderby) {
	orderbyClause << orderby.trim()
    }

    /**
     * create criteria based on globalfilter objects
     */
    void createGlobalFilterCriteria(GlobalFilter gfilter, boolean expandBioMarkers = true) {

        // biomarkers
	buildGlobalFilterBioMarkerCriteria gfilter, expandBioMarkers

        // disease
	buildGlobalFilterDiseaseCriteria gfilter
        // compound
	buildGlobalFilterCompoundCriteria gfilter
        // trials
        // by default not all query handles trials
	buildGlobalFilterExperimentCriteria gfilter
        // free text -
	buildGlobalFilterFreeTextCriteria gfilter

        // gene signature

        // studies
	buildGlobalFilterStudyCriteria gfilter
    }

    void createGlobalFilterCriteriaMV(GlobalFilter gfilter) {

        // biomarkers
	buildGlobalFilterBioMarkerCriteriaMV gfilter

        // disease
	buildGlobalFilterDiseaseCriteria gfilter
        // compound
	buildGlobalFilterCompoundCriteria gfilter
        // trials
        // by default not all query handles trials
	buildGlobalFilterExperimentCriteria gfilter
        // free text -
	buildGlobalFilterFreeTextCriteria gfilter

        // gene signature

        // studies
	buildGlobalFilterStudyCriteria gfilter
    }

    /**
     * default criteria builder for biomarkers
     */
    void buildGlobalFilterBioMarkerCriteria(GlobalFilter gfilter, boolean expandBioMarkers) {
	KeywordSet biomarkerFilters = gfilter.bioMarkerFilters
	if (biomarkerFilters) {
	    String markerAlias = mainTableAlias + '_bm'
	    String markerTable = getBioMarkerTable() + markerAlias
	    addTable 'JOIN ' + markerTable
            if (expandBioMarkers) {
		addCondition createExpandBioMarkerCondition(markerAlias, gfilter)
            }
            else {
		addCondition markerAlias + '.id IN (' + biomarkerFilters.getKeywordDataIdString() + ') '
            }
        }
    }

    void buildGlobalFilterBioMarkerCriteriaMV(GlobalFilter gfilter) {
	if (gfilter.bioMarkerFilters) {
	    addCondition createExpandBioMarkerConditionMV(mainTableAlias, gfilter)
        }
    }

    /**
     * create biomarker table alias
     */
    String getBioMarkerTable() {
	mainTableAlias + '.markers '
    }
    /**
     * default criteria builder for disease
     */
    void buildGlobalFilterDiseaseCriteria(GlobalFilter gfilter) {
	if (gfilter.diseaseFilters) {
	    String dAlias = mainTableAlias + '_dis'
	    String dtable = mainTableAlias + '.diseases ' + dAlias
	    addTable 'JOIN ' + dtable
	    addCondition dAlias + '.id IN (' + gfilter.getDiseaseFilters().getKeywordDataIdString() + ') '
        }
    }

    void buildGlobalFilterFreeTextCriteria(GlobalFilter gfilter) {
        if (gfilter.isTextOnly()) {
	    addCondition ' 1 = 0'
        }
    }

    /**
     * default criteria builder for compound
     */
    void buildGlobalFilterCompoundCriteria(GlobalFilter gfilter) {
	if (gfilter.compoundFilters) {
	    String dAlias = mainTableAlias + '_cpd'
	    String dtable = mainTableAlias + '.compounds ' + dAlias
	    addTable 'JOIN ' + dtable
	    addCondition dAlias + '.id IN (' + gfilter.compoundFilters.keywordDataIdString + ') '
        }
    }

    /**
     * default criteria builder for experiment
     */
    void buildGlobalFilterExperimentCriteria(GlobalFilter gfilter) {
    }

    /**
     * default criteria builder for study
     */
    void buildGlobalFilterStudyCriteria(GlobalFilter gfilter) {
    }

    /**
     * generate a Hibernate Query from this query object
     *
     * TODO rename to generateHql()
     */
    String generateSQL() {
        StringBuilder s = new StringBuilder('SELECT ')
        if (setDistinct) {
	    s << ' DISTINCT '
        }
	s << createClause(selectClause, ', ', null)
	s << ' FROM '
        // create from clause but don't put a separator if JOIN presents
	s << createClause(fromClause, ', ', 'JOIN')
	if (whereClause) {
	    s << ' WHERE '
	    s << createClause(whereClause, ' AND ', null)
	}
	if (groupbyClause) {
	    s << ' GROUP BY '
	    s << createClause(groupbyClause, ', ', null)
	}
	if (orderbyClause) {
	    s << ' ORDER BY '
	    s << createClause(orderbyClause, ', ', null)
        }
	s
    }

    /**
     * create clause
     */
    String createClause(LinkedHashSet<String> clause, String separator, String ignoreSepString) {

        StringBuilder s = new StringBuilder()
        for (sc in clause) {
	    if (sc) {
		if (ignoreSepString == null || !sc.trim().startsWith(ignoreSepString)) {
		    if (s) {
			s << separator
                    }
                }

		s << ' ' << sc
            }
	}

	s
    }

    String createExpandBioMarkerSubQuery(ids) {
	'''
		SELECT DISTINCT bdc.associatedBioDataId
		FROM org.transmart.biomart.BioDataCorrelation bdc
		WHERE bdc.bioDataId in (''' + ids + ')'
    }

    /**
     * link biomarkers to those defined in the materialized views which exposes domain objects to search
     */
    String createExpandBioMarkerCondition(String markerAlias, GlobalFilter gfilter) {

        /*
         // query to use if only using 1 MV from searchapp
	 s.append(markerAlias).append(".id IN (")
	 s.append("SELECT DISTINCT sbmcmv.assocBioMarkerId FROM org.transmart.searchapp.SearchBioMarkerCorrelFastMV sbmcmv ")
	 s.append(" WHERE sbmcmv.domainObjectId in (").append(ids).append(")")
         */

        // aggregate ids from both static and refresh MVs
        StringBuilder s = new StringBuilder()
	s << "("
	if (gfilter.geneSigListFilters) {
	    s << markerAlias << ".id IN ("
	    s << "SELECT DISTINCT sbmcmv.assocBioMarkerId FROM org.transmart.searchapp.SearchBioMarkerCorrelFastMV sbmcmv "
	    s << " WHERE sbmcmv.domainObjectId in (" << gfilter.geneSigListFilters.keywordDataIdString << "))"
        }
	if (gfilter.genePathwayFilters) {
            if (s.length() > 1) {
		s << " OR "
            }
	    s << markerAlias << ".id IN ("
	    s << "SELECT DISTINCT bmcmv.assoBioMarkerId FROM org.transmart.biomart.BioMarkerCorrelationMV bmcmv "
	    s << " WHERE bmcmv.bioMarkerId in (" << gfilter.genePathwayFilters.keywordDataIdString << ")) "
        }
	s << ")"
	s
    }

    String createExpandBioMarkerConditionMV(String markerAlias, GlobalFilter gfilter) {

        // aggregate ids from both static and refresh MVs
        StringBuilder s = new StringBuilder()
	s << "("
	if (gfilter.geneSigListFilters) {
	    s << markerAlias << ".id IN ("
	    s << "SELECT DISTINCT sbmcmv.assocBioMarkerId FROM org.transmart.searchapp.SearchBioMarkerCorrelFastMV sbmcmv "
	    s << " WHERE sbmcmv.domainObjectId in (" << gfilter.geneSigListFilters.keywordDataIdString << "))"
        }
	if (gfilter.genePathwayFilters) {
            if (s.length() > 1) {
		s << " OR "
            }
	    s << markerAlias << ".id IN ("
	    s << "SELECT DISTINCT bmcmv.assoBioMarkerId FROM org.transmart.biomart.BioMarkerCorrelationMV bmcmv "
	    s << " WHERE bmcmv.bioMarkerId in (" << gfilter.genePathwayFilters.keywordDataIdString << ")) "
        }
	s << ")"
	s
    }

    String toString() {
	generateSQL()
    }
}
