import com.recomdata.search.query.LiteratureDataQuery
import com.recomdata.search.query.Query
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.transmart.GlobalFilter
import org.transmart.LiteratureFilter
import org.transmart.SearchFilter
import org.transmart.biomart.BioDataExternalCode
import org.transmart.biomart.Literature

@Slf4j('logger')
class LiteratureQueryService {

    static transactional = false

    GlobalFilterService globalFilterService

    /**
     * Executes query to get record count for data in curated literature from Jubilant
     *
     * @param table - the table to query
     * @param namedParams - the named parameters for the query
     * @param gfilter - the global search filter
     * @param query - the constructed query
     *
     * @return the count of records
     */
    private int executeLitQueryCount(String table, Map namedParams,
				     GlobalFilter gfilter, LiteratureDataQuery query) {
        if (gfilter == null || gfilter.isTextOnly()) {
            return 0
        }

	query.addSelect 'count(distinct data.id)'
	query.addTable table
        if (namedParams.containsKey('dd2ID')) {
	    query.addTable 'JOIN data.diseases data_dis2'
        }
        query.createGlobalFilterCriteria(gfilter)

	Literature.executeQuery(query.generateSQL(), namedParams)[0]
    }

    /**
     * Executes query to get the data from curated literature from Jubilant
     *
     * @param table - the table to query
     * @param namedParams - the named parameters for the query
     * @param sfilter - the search filter
     * @param params - the paging parameters
     * @param query - the constructed query
     *
     * @return the results
     */
    private List executeLitQueryData(String table, Map namedParams, GlobalFilter gfilter,
	                             GrailsParameterMap params, LiteratureDataQuery query) {
        if (gfilter == null || gfilter.isTextOnly()) {
            return []
        }

	Map pagingParams
	if (params) {
	    pagingParams = globalFilterService.createPagingParamMap(params)
        }

	query.addSelect 'data'
	query.addTable table + ' JOIN fetch data.reference'
        if (namedParams.containsKey('dd2ID')) {
	    query.addTable 'JOIN data.diseases data_dis2'
        }
        query.createGlobalFilterCriteria(gfilter)

	if (pagingParams) {
	    Literature.executeQuery query.generateSQL(), namedParams, pagingParams
        }
        else {
	    Literature.executeQuery query.generateSQL(), namedParams
        }
    }

    /**
     * Executes query to get record count for summary data in curated literature from Jubilant
     *
     * @param table - the table to query
     * @param namedParams - the named parameters for the query
     * @param sfilter - the search filter
     * @param query - the constructed query
     *
     * @return the count of records
     */
    private int executeLitSumQueryCount(String table, Map namedParams, SearchFilter sfilter, LiteratureDataQuery query) {
        GlobalFilter gfilter = sfilter.globalFilter
        LiteratureFilter litFilter = sfilter.litFilter
        if (gfilter == null || gfilter.isTextOnly()) {
            return 0
        }

	query.addSelect 'count(distinct sumdata.id)'
	query.addTable table
	query.addTable 'org.transmart.biomart.LiteratureSummaryData sumdata'
	query.addCondition 'sumdata.target=data.reference.component'
	query.addCondition 'sumdata.diseaseSite=data.reference.diseaseSite'
        if (litFilter.hasAlterationType()) {
	    Set<String> types = litFilter.getSelectedAlterationTypes()
	    if (types) {
		query.addCondition 'data.alterationType in (:alterationTypes)'
		namedParams.alterationTypes = types
            }
            else {
		query.addCondition 'data.alterationType is null'
            }
        }
        if (namedParams.containsKey('dd2ID')) {
	    query.addTable 'JOIN data.diseases data_dis2'
        }
	query.createGlobalFilterCriteria gfilter

	Literature.executeQuery(query.generateSQL(), namedParams)[0]
    }

    /**
     * Executes query to get the summary data from curated literature from Jubilant
     *
     * @param table - the table to query
     * @param namedParams - the named parameters for the query
     * @param sfilter - the search filter
     * @param params - the paging parameters
     * @param query - the constructed query
     *
     * @return the summary results
     */
    private List executeLitSumQueryData(String table, Map namedParams, SearchFilter sfilter,
	                                GrailsParameterMap params, LiteratureDataQuery query) {
        GlobalFilter gfilter = sfilter.globalFilter
        LiteratureFilter litFilter = sfilter.litFilter
        if (gfilter == null || gfilter.isTextOnly()) {
            return []
        }

	String sort = params.sort ?: 'dataType'
	String dir = params.dir ?: 'ASC'
	Map pagingParams = globalFilterService.createPagingParamMap(params)

        query.setDistinct = true
	query.addSelect 'sumdata'
	query.addTable table
	query.addTable 'org.transmart.biomart.LiteratureSummaryData sumdata'
	query.addCondition 'sumdata.target=data.reference.component'
	query.addCondition 'sumdata.diseaseSite=data.reference.diseaseSite'
        if (litFilter.hasAlterationType()) {
	    Set<String> types = litFilter.getSelectedAlterationTypes()
	    if (types) {
		query.addCondition 'data.alterationType in (:alterationTypes)'
		namedParams.alterationTypes = types
            }
            else {
		query.addCondition 'data.alterationType is null'
            }
        }
	query.addOrderBy 'sumdata.' + sort + ' ' + dir
        if (namedParams.containsKey('dd2ID')) {
	    query.addTable 'JOIN data.diseases data_dis2'
        }
	query.createGlobalFilterCriteria gfilter

	Literature.executeQuery query.generateSQL(), namedParams, pagingParams
    }

    /*************************************************************************
     * Julilant Oncology Alteration methods
     *************************************************************************/
    int litJubOncAltCount(SearchFilter sfilter) {
	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
	executeLitQueryCount 'LiteratureAlterationData data',
            litAltConditions(sfilter.litFilter, 'JUBILANT_ONCOLOGY_ALTERATION', query),
	    sfilter.globalFilter, query
    }

    List litJubOncAltData(SearchFilter sfilter, GrailsParameterMap params) {
	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
	executeLitQueryData 'LiteratureAlterationData data',
            litAltConditions(sfilter.litFilter, 'JUBILANT_ONCOLOGY_ALTERATION', query),
	    sfilter.globalFilter, params, query
    }

    int litJubAsthmaAltCount(SearchFilter sfilter) {
	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
	executeLitQueryCount 'LiteratureAlterationData data',
            litAltConditions(sfilter.litFilter, 'JUBILANT_ASTHMA_ALTERATION', query),
	    sfilter.globalFilter, query
    }

    List litJubAsthmaAltData(SearchFilter sfilter, GrailsParameterMap params) {
	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
	executeLitQueryData 'LiteratureAlterationData data',
            litAltConditions(sfilter.litFilter, 'JUBILANT_ASTHMA_ALTERATION', query),
	    sfilter.globalFilter, params, query
    }

    int litJubOncAltSumCount(SearchFilter sfilter) {
	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
	executeLitSumQueryCount 'LiteratureAlterationData data',
            litAltConditions(sfilter.litFilter, 'JUBILANT_ONCOLOGY_ALTERATION', query),
	    sfilter, query
    }

    List litJubOncAltSumData(SearchFilter sfilter, GrailsParameterMap params) {
	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
	executeLitSumQueryData 'LiteratureAlterationData data',
            litAltConditions(sfilter.litFilter, 'JUBILANT_ONCOLOGY_ALTERATION', query),
	    sfilter, params, query
    }

    /**
     * Gathers the WHERE clause conditions for the Alteration data
     *
     * @param litFilter the LiteratureFilter
     * @param dataType (asthma or oncology)
     * @param query the LiteratureDataQuery that will be used to query the database
     *
     * @returns the named parameters for the query
     */
    private Map litAltConditions(LiteratureFilter litFilter, dataType, LiteratureDataQuery query) {
	Map namedParameters = [:]

	query.addCondition 'data.dataType= :dataType'
	namedParameters.dataType = dataType

        if (litFilter.hasDisease()) {
	    query.addCondition 'data_dis2.id= :dd2ID'
	    namedParameters.dd2ID = litFilter.bioDiseaseId
        }
        if (litFilter.hasDiseaseSite()) {
	    query.addCondition 'data.reference.diseaseSite in (:diseaseSites)'
	    namedParameters.diseaseSites = litFilter.diseaseSite
        }
        if (litFilter.hasComponent()) {
	    query.addCondition 'data.reference.component in (:compList)'
	    query.addCondition 'data.reference.geneId in (:geneList)'
	    namedParameters.compList = litFilter.pairCompList
	    namedParameters.geneList = litFilter.pairGeneList
        }
        if (litFilter.hasMutationType()) {
	    query.addCondition 'data.mutationType= :mutationType'
	    namedParameters.mutationType = litFilter.mutationType
        }
        if (litFilter.hasMutationSite()) {
	    query.addCondition 'data.mutationSites= :mutationSite'
	    namedParameters.mutationSite = litFilter.mutationSite
        }
        if (litFilter.hasEpigeneticType()) {
	    query.addCondition 'data.epigeneticType= :epigeneticType'
	    namedParameters.epigeneticType = litFilter.epigeneticType
        }
        if (litFilter.hasEpigeneticRegion()) {
	    query.addCondition 'data.epigeneticRegion= :epigeneticRegion'
	    namedParameters.epigeneticRegion = litFilter.epigeneticRegion
        }
        if (litFilter.hasAlterationType()) {
	    Set<String> types = litFilter.getSelectedAlterationTypes()
	    if (types) {
		query.addCondition 'data.alterationType in (:alterationTypes)'
		namedParameters.alterationTypes = types
            }
            else {
		query.addCondition 'data.alterationType is null'
            }
        }
        if (litFilter.hasMoleculeType()) {
	    query.addCondition 'data.reference.moleculeType= :moleculeType'
	    namedParameters.moleculeType = litFilter.moleculeType
        }
        if (litFilter.hasRegulation()) {
	    if (litFilter.regulation == 'Expression') {
		query.addCondition 'data.totalExpPercent is not null'
            }
	    else if (litFilter.regulation == 'OverExpression') {
		query.addCondition 'data.overExpPercent is not null'
            }
        }
        if (litFilter.hasPtmType()) {
	    query.addCondition 'data.ptmType= :ptmType'
	    namedParameters.ptmType = litFilter.ptmType
        }
        if (litFilter.hasPtmRegion()) {
	    query.addCondition 'data.ptmRegion= :ptmRegion'
	    namedParameters.ptmRegion = litFilter.ptmRegion
        }

	namedParameters
    }

    /*************************************************************************
     * Julilant Oncology Inhibitor methods
     *************************************************************************/
    int litJubOncInhCount(SearchFilter sfilter) {
	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
	executeLitQueryCount 'LiteratureInhibitorData data',
            litInhConditions(sfilter.litFilter, 'JUBILANT_ONCOLOGY_INHIBITOR', query),
	    sfilter.globalFilter, query
    }

    List litJubOncInhData(SearchFilter sfilter, GrailsParameterMap params) {
	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
	executeLitQueryData 'LiteratureInhibitorData data',
            litInhConditions(sfilter.litFilter, 'JUBILANT_ONCOLOGY_INHIBITOR', query),
	    sfilter.globalFilter, params, query
    }

    int litJubAsthmaInhCount(SearchFilter sfilter) {
        return 0
	// TODO: Uncomment this code after the asthma inhibitor data has been loaded.
	//      Query query = new LiteratureDataQuery(mainTableAlias:'data')
	//		return executeLitQueryCount('LiteratureInhibitorData data',
	//				litInhConditions(sfilter.litFilter, 'JUBILANT_ASTHMA_INHIBITOR', query),
	//				sfilter.globalFilter, query)
    }

    List litJubAsthmaInhData(SearchFilter sfilter, GrailsParameterMap params) {
	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
	executeLitQueryData 'LiteratureInhibitorData data',
            litInhConditions(sfilter.litFilter, 'JUBILANT_ASTHMA_INHIBITOR', query),
	    sfilter.globalFilter, params, query
    }

    /**
     * Gathers the WHERE clause conditions for the Inhibitor data
     *
     * @param litFilter the LiteratureFilter
     * @param dataType (asthma or oncology)
     * @param query the LiteratureDataQuery that will be used to query the database
     *
     * @returns the named parameters for the query
     */
    private Map litInhConditions(LiteratureFilter litFilter, dataType, LiteratureDataQuery query) {
	Map namedParameters = [:]

	query.addCondition 'data.dataType= :dataType'
	namedParameters.dataType = dataType

        if (litFilter.hasDisease()) {
	    query.addCondition 'data_dis2.id= :dd2ID'
	    namedParameters.dd2ID = litFilter.bioDiseaseId
        }
        if (litFilter.hasDiseaseSite()) {
	    query.addCondition 'data.reference.diseaseSite in (:diseaseSites)'
	    namedParameters.diseaseSites = litFilter.diseaseSite
        }
        if (litFilter.hasComponent()) {
	    query.addCondition 'data.reference.component in (:compList)'
	    query.addCondition 'data.reference.geneId in (:geneList)'
	    namedParameters.compList = litFilter.pairCompList
	    namedParameters.geneList = litFilter.pairGeneList
        }
        if (litFilter.hasTrialType()) {
	    query.addCondition 'data.trialType= :trialType'
	    namedParameters.trialType = litFilter.trialType
        }
        if (litFilter.hasTrialPhase()) {
	    query.addCondition 'data.trialPhase= :trialPhase'
	    namedParameters.trialPhase = litFilter.trialPhase
        }
        if (litFilter.hasInhibitorName()) {
	    query.addCondition 'data.inhibitor= :inhibitor'
	    namedParameters.inhibitor = litFilter.inhibitorName
        }
        if (litFilter.hasTrialExperimentalModel()) {
	    query.addCondition 'data.trialExperimentalModel= :trialExperimentalModel'
	    namedParameters.trialExperimentalModel = litFilter.trialExperimentalModel
        }

	namedParameters
    }

    /*************************************************************************
     * Julilant Oncology Interaction methods
     *************************************************************************/
    int litJubOncIntCount(SearchFilter sfilter) {
	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
	String table = 'org.transmart.biomart.LiteratureInteractionData data'
        if (sfilter.litFilter.hasExperimentalModel()) {
            table += ' LEFT OUTER JOIN data.inVivoModel invivo LEFT OUTER JOIN data.inVitroModel invitro'
        }
	executeLitQueryCount table,
            litIntConditions(sfilter.litFilter, 'JUBILANT_ONCOLOGY_INTERACTION', query),
	    sfilter.globalFilter, query
    }

    List litJubOncIntData(SearchFilter sfilter, GrailsParameterMap params) {
	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
	String table = 'org.transmart.biomart.LiteratureInteractionData data'
        if (sfilter.litFilter.hasExperimentalModel()) {
            table += ' LEFT OUTER JOIN data.inVivoModel invivo LEFT OUTER JOIN data.inVitroModel invitro'
        }

	executeLitQueryData table,
            litIntConditions(sfilter.litFilter, 'JUBILANT_ONCOLOGY_INTERACTION', query),
	    sfilter.globalFilter, params, query
    }

    int litJubAsthmaIntCount(SearchFilter sfilter) {
	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
	String table = 'org.transmart.biomart.LiteratureInteractionData data'
        if (sfilter.litFilter.hasExperimentalModel()) {
            table += ' LEFT OUTER JOIN data.inVivoModel invivo LEFT OUTER JOIN data.inVitroModel invitro'
        }

	executeLitQueryCount table,
            litIntConditions(sfilter.litFilter, 'JUBILANT_ASTHMA_INTERACTION', query),
	    sfilter.globalFilter, query
    }

    List litJubAsthmaIntData(SearchFilter sfilter, GrailsParameterMap params) {
	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
	String table = 'org.transmart.biomart.LiteratureInteractionData data'
        if (sfilter.litFilter.hasExperimentalModel()) {
            table += ' LEFT OUTER JOIN data.inVivoModel invivo LEFT OUTER JOIN data.inVitroModel invitro'
        }

	executeLitQueryData table,
            litIntConditions(sfilter.litFilter, 'JUBILANT_ASTHMA_INTERACTION', query),
	    sfilter.globalFilter, params, query
    }

    /**
     * Gathers the WHERE clause conditions for the Interaction data
     *
     * @param litFilter the LiteratureFilter
     * @param dataType (asthma or oncology)
     * @param query the LiteratureDataQuery that will be used to query the database
     *
     * @returns the named parameters for the query
     */
    private Map litIntConditions(LiteratureFilter litFilter, dataType, LiteratureDataQuery query) {
	Map namedParameters = [:]

	query.addCondition 'data.dataType= :dataType'
	namedParameters.dataType = dataType

        if (litFilter.hasDisease()) {
	    query.addCondition 'data_dis2.id= :dd2ID'
	    namedParameters.dd2ID = litFilter.bioDiseaseId
        }
        if (litFilter.hasDiseaseSite()) {
	    query.addCondition 'data.reference.diseaseSite in (:diseaseSites)'
	    namedParameters.diseaseSites = litFilter.diseaseSite
        }
        if (litFilter.hasComponent()) {
	    query.addCondition 'data.reference.component in (:compList)'
	    query.addCondition 'data.reference.geneId in (:geneList)'
	    namedParameters.compList = litFilter.pairCompList
	    namedParameters.geneList = litFilter.pairGeneList
        }
        if (litFilter.hasSource()) {
	    query.addCondition 'data.sourceComponent= :sourceComponent'
	    namedParameters.sourceComponent = litFilter.source
        }
        if (litFilter.hasTarget()) {
	    query.addCondition 'data.targetComponent= :targetComponent'
	    namedParameters.targetComponent = litFilter.target
        }
        if (litFilter.hasExperimentalModel()) {
	    query.addCondition '(invivo.experimentalModel= :experimentalModel or invitro.experimentalModel= :experimentalModel)'
	    namedParameters.experimentalModel = litFilter.experimentalModel
        }
        if (litFilter.hasMechanism()) {
	    query.addCondition 'data.mechanism= :mechanism'
	    namedParameters.mechanism = litFilter.mechanism
        }

	namedParameters
    }

    /*************************************************************************
     * Julilant Protein Effect methods
     *************************************************************************/
    int litJubAsthmaPECount(SearchFilter sfilter) {
	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
	executeLitQueryCount 'LiteratureProteinEffectData data',
            litPEConditions(sfilter.litFilter, 'JUBILANT_ASTHMA_PROTEIN_EFFECT', query),
	    sfilter.globalFilter, query
    }

    List litJubAsthmaPEData(SearchFilter sfilter, GrailsParameterMap params) {
	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
	executeLitQueryData 'LiteratureProteinEffectData data',
            litPEConditions(sfilter.litFilter, 'JUBILANT_ASTHMA_PROTEIN_EFFECT', query),
	    sfilter.globalFilter, params, query
    }

    /**
     * Gathers the WHERE clause conditions for the Interaction data
     *
     * @param litFilter the LiteratureFilter
     * @param dataType (asthma or oncology)
     * @param query the LiteratureDataQuery that will be used to query the database
     *
     * @returns the named parameters for the query
     */
    private Map<String, Object> litPEConditions(LiteratureFilter litFilter, dataType, LiteratureDataQuery query) {
	Map<String, Object> namedParameters = [:]

	query.addCondition 'data.dataType= :dataType'
	namedParameters.dataType = dataType

        if (litFilter.hasDisease()) {
	    query.addCondition 'data_dis2.id= :dd2ID'
	    namedParameters.dd2ID = litFilter.bioDiseaseId
        }
        if (litFilter.hasDiseaseSite()) {
	    query.addCondition 'data.reference.diseaseSite in (:diseaseSites)'
	    namedParameters.diseaseSites = litFilter.diseaseSite
        }
        if (litFilter.hasComponent()) {
	    query.addCondition 'data.reference.component in (:compList)'
	    query.addCondition 'data.reference.geneId in (:geneList)'
	    namedParameters.compList = litFilter.pairCompList
	    namedParameters.geneList = litFilter.pairGeneList
        }

	namedParameters
    }

    /*************************************************************************
     * Jubilant Queries to return URNs for Pathway Studio integration
     *************************************************************************/
    String findGeneURN(String name) {

	if (!name.contains('.')) {
	    List<String> result = BioDataExternalCode.executeQuery('''
			select bdec.code
			from BioDataExternalCode bdec
			where bdec.bioDataId in (
				select innerb.bioDataId
				from BioDataExternalCode innerb
				where upper(innerb.code) = ?
			)
			and bdec.codeType = ?''',
			[name.toUpperCase(), 'URN'])
            if (result[0] != null) {
		return result[0]
            }
        }
    }

    String findSmallMolURN(String name) {
	List<String> result = BioDataExternalCode.executeQuery('''
		select bdec.code
		from BioDataExternalCode bdec
			where bdec.bioDataId = (
			select c.id
			from Compound c
			where upper(c.codeName) = ?
			  and c.productCategory = ?
		)
		and bdec.codeSource = ?
		and bdec.codeType = ?''',
		[name.toUpperCase(), 'Small Molecule', 'ARIADNE', 'URN'])
        if (result.size() == 1) {
	    return result[0]
        }
    }

    String findDiseaseURN(String name) {
	logger.info 'Calling findDiseaseURN for {}', name
	List<String> result = BioDataExternalCode.executeQuery('''
		select bdec.code
		from BioDataExternalCode bdec
		where bdec.bioDataId in (
			select bdecInner.bioDataId
			from BioDataExternalCode bdecInner
			where upper(bdecInner.code) = ?
			  and bdecInner.codeSource = ?
			  and bdecInner.codeType = ?
			  and bdecInner.bioDataType = ?
		)
		and bdec.codeSource = ?
		and bdec.codeType = ?
		and bdec.bioDataType = ?''',
                [name.toUpperCase(), 'ARIADNE', 'ALIAS', 'BIO_DISEASE', 'ARIADNE', 'URN', 'BIO_DISEASE'])
        if (result.size() == 1) {
	    return result[0]
        }

	logger.warn 'Unable to find the Disease URN.  Size = {}', result.size()
    }

    /*************************************************************************
     * Jubilant Oncology Filter Queries
     *************************************************************************/
    private List executeJubOncologyQueryFilter(String column, String table, SearchFilter searchFilter) {
	GlobalFilter gfilter = searchFilter.globalFilter
        if (gfilter == null || gfilter.isTextOnly()) {
            return []
        }

	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
        query.setDistinct = true
	query.addSelect 'data.' + column
	query.addTable table + ' data'
	query.addCondition ' data.' + column + ' is not null '
	query.addOrderBy ' data.' + column
	query.createGlobalFilterCriteria gfilter

	Literature.executeQuery query.generateSQL()
    }

    List diseaseList(SearchFilter searchFilter) {
	GlobalFilter gfilter = searchFilter.globalFilter
        if (gfilter == null || gfilter.isTextOnly()) {
            return []
        }

	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
        query.setDistinct = true
	query.addSelect 'data_dis2'
	query.addTable 'org.transmart.biomart.Literature data'
	query.addTable 'JOIN data.diseases data_dis2'
	query.addOrderBy 'data_dis2.preferredName'
	query.createGlobalFilterCriteria gfilter, true

	Literature.executeQuery query.generateSQL()
    }

    List diseaseSiteList(SearchFilter searchFilter) {
	executeJubOncologyQueryFilter 'reference.diseaseSite',
	    'org.transmart.biomart.Literature', searchFilter
    }

    List componentList(SearchFilter searchFilter) {
        // Need to filter and send only genes/proteins
	GlobalFilter gfilter = searchFilter.globalFilter
        if (gfilter == null || gfilter.isTextOnly()) {
            return []
        }

	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
        query.setDistinct = true
	query.addSelect 'data.reference.component'
	query.addSelect 'data.reference.geneId'
	query.addTable 'org.transmart.biomart.Literature data'
	query.addCondition 'data.reference.geneId is not null'
	query.addOrderBy 'data.reference.component'
	query.createGlobalFilterCriteria gfilter

	Literature.executeQuery query.generateSQL()
    }

    List mutationTypeList(SearchFilter searchFilter) {
	executeJubOncologyQueryFilter 'mutationType',
	    'org.transmart.biomart.LiteratureAlterationData', searchFilter
    }

    List mutationSiteList(SearchFilter searchFilter) {
	executeJubOncologyQueryFilter 'mutationSites',
	    'org.transmart.biomart.LiteratureAlterationData', searchFilter
    }

    List epigeneticTypeList(SearchFilter searchFilter) {
	executeJubOncologyQueryFilter 'epigeneticType',
	    'org.transmart.biomart.LiteratureAlterationData', searchFilter
    }

    List epigeneticRegionList(SearchFilter searchFilter) {
	executeJubOncologyQueryFilter 'epigeneticRegion',
	    'org.transmart.biomart.LiteratureAlterationData', searchFilter
    }

    List moleculeTypeList(SearchFilter searchFilter) {
	executeJubOncologyQueryFilter 'reference.moleculeType',
	    'org.transmart.biomart.Literature', searchFilter
    }

    List ptmTypeList(SearchFilter searchFilter) {
	executeJubOncologyQueryFilter 'ptmType',
	    'org.transmart.biomart.LiteratureAlterationData', searchFilter
    }

    List ptmRegionList(SearchFilter searchFilter) {
	executeJubOncologyQueryFilter 'ptmRegion',
	    'org.transmart.biomart.LiteratureAlterationData', searchFilter
    }

    List sourceList(SearchFilter searchFilter) {
	executeJubOncologyQueryFilter 'sourceComponent',
	    'org.transmart.biomart.LiteratureInteractionData', searchFilter
    }

    List targetList(SearchFilter searchFilter) {
	executeJubOncologyQueryFilter 'targetComponent',
	    'org.transmart.biomart.LiteratureInteractionData', searchFilter
    }

    List experimentalModelList(SearchFilter searchFilter) {
	GlobalFilter gfilter = searchFilter.globalFilter
        if (gfilter == null || gfilter.isTextOnly()) {
            return []
        }

	Query query = new LiteratureDataQuery(mainTableAlias: 'data')
        query.setDistinct = true
	query.addSelect 'mv.experimentalModel'
	query.addTable 'org.transmart.biomart.LiteratureInteractionData data'
	query.addTable 'org.transmart.biomart.LiteratureInteractionModelMV mv'
	query.addCondition 'data.id = mv.id'
	query.addOrderBy 'mv.experimentalModel'
	query.createGlobalFilterCriteria gfilter

	Literature.executeQuery query.generateSQL()
    }

    List mechanismList(SearchFilter searchFilter) {
	executeJubOncologyQueryFilter 'mechanism',
	    'org.transmart.biomart.LiteratureInteractionData', searchFilter
    }

    List trialTypeList(SearchFilter searchFilter) {
	executeJubOncologyQueryFilter 'trialType',
	    'org.transmart.biomart.LiteratureInhibitorData', searchFilter
    }

    List trialPhaseList(SearchFilter searchFilter) {
	executeJubOncologyQueryFilter 'trialPhase',
	    'org.transmart.biomart.LiteratureInhibitorData', searchFilter
    }

    List inhibitorNameList(SearchFilter searchFilter) {
	executeJubOncologyQueryFilter 'inhibitor',
	    'org.transmart.biomart.LiteratureInhibitorData', searchFilter
    }

    List trialExperimentalModelList(SearchFilter searchFilter) {
	executeJubOncologyQueryFilter 'trialExperimentalModel',
	    'org.transmart.biomart.LiteratureInhibitorData', searchFilter
    }
}
