import com.recomdata.search.query.AssayAnalysisDataQuery
import com.recomdata.search.query.Query
import groovy.transform.CompileStatic
import org.transmart.GlobalFilter
import org.transmart.SearchFilter
import org.transmart.biomart.BioAssayAnalysisData
import org.transmart.biomart.ClinicalTrial
import org.transmart.biomart.Compound
import org.transmart.biomart.Disease
import org.transmart.biomart.Experiment
import org.transmart.searchapp.SearchKeyword

/**
 * @author mmcduffie
 */
@CompileStatic
class FilterQueryService {

    static transactional = false

    List<Disease> trialDiseaseFilter(SearchFilter filter) {
	findExperimentDiseaseFilter filter, 'Clinical Trial'
    }

    List<Compound> trialCompoundFilter(SearchFilter filter) {
	findExperimentCompoundFilter filter, 'Clinical Trial'
    }

    List<Disease> findExperimentDiseaseFilter(SearchFilter filter, experimentType) {
	GlobalFilter gfilter = filter.globalFilter

	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad', setDistinct: true)
	String alias = query.mainTableAlias + '_dis'
        query.addTable('org.transmart.biomart.BioAssayAnalysisData baad')
        query.addTable('JOIN ' + query.mainTableAlias + '.experiment.diseases ' + alias)
        query.addSelect(alias)
        query.addOrderBy(alias + '.preferredName')
        query.addCondition(query.mainTableAlias + ".experiment.type='" + experimentType + "'")
        query.createGlobalFilterCriteria(gfilter, true)

	BioAssayAnalysisData.executeQuery(query.generateSQL()) as List<Disease>
	}

    List<SearchKeyword> experimentCompoundFilter(String experimentType) {
	SearchKeyword.executeQuery('''
		SELECT distinct sk
		FROM org.transmart.searchapp.SearchKeyword sk, org.transmart.biomart.Experiment exp
		JOIN exp.compounds cpd
		WHERE sk.bioDataId = cpd.id
		  AND exp.type=?
		ORDER BY sk.keyword''',
		[experimentType])
	}

    List<Compound> findExperimentCompoundFilter(SearchFilter filter, String experimentType) {
	GlobalFilter gfilter = filter.globalFilter

	Query query = new AssayAnalysisDataQuery(mainTableAlias: 'baad', setDistinct: true)
	String alias = query.mainTableAlias + '_cpd'
	query.addTable 'org.transmart.biomart.BioAssayAnalysisData baad'
	query.addTable 'JOIN ' + query.mainTableAlias + '.experiment.compounds ' + alias
	query.addSelect alias
	query.addOrderBy alias + '.genericName'
	query.addCondition query.mainTableAlias + ".experiment.type='" + experimentType + "'"
	query.createGlobalFilterCriteria gfilter, true

	BioAssayAnalysisData.executeQuery query.generateSQL()
    }

    List<String> studyTypeFilter() {
	ClinicalTrial.executeQuery '''
		SELECT distinct exp.studyType
		from org.transmart.biomart.ClinicalTrial exp
		WHERE exp.studyType IS NOT NULL
		ORDER BY exp.studyType'''
    }

    List<String> trialPhaseFilter() {
	ClinicalTrial.executeQuery '''
		SELECT distinct exp.studyPhase
		FROM org.transmart.biomart.ClinicalTrial exp
		WHERE exp.studyPhase IS NOT NULL
		ORDER BY exp.studyPhase'''
    }

    List<String> studyDesignFilter(String experimentType) {
	Experiment.executeQuery '''
		SELECT DISTINCT exp.design
		FROM org.transmart.biomart.Experiment exp
		WHERE exp.type=?
		AND exp.design IS NOT NULL
		ORDER BY exp.design''',
		[experimentType]
    }
}
