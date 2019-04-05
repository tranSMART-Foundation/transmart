import org.springframework.beans.factory.annotation.Autowired
import org.transmart.plugin.shared.SecurityService
import org.transmart.searchapp.GeneSignature
import org.transmart.searchapp.SearchKeyword

/**
 * @author mmcduffie
 */
class SearchHelpController {

    GeneSignatureService geneSignatureService
    @Autowired
    private SecurityService securityService

    def list() {}

    def loadPathways(String step, String datasource) {
	doPathways step, datasource, true
    }

    def listAllPathways(String step, String datasource) {
	doPathways step, datasource, false
    }

    private doPathways(String step, String datasource, boolean cache) {
	Map cacheArgs = cache ? [cache: 'read-only'] : [:]

	step = step ?: 'A-C'
	List<String> datasources = SearchKeyword.executeQuery('''
				select distinct k.dataSource
				from org.transmart.searchapp.SearchKeyword k
				where k.dataCategory='PATHWAY'
				order by upper(k.dataSource)''',
							      cacheArgs)
	String defaultsource = datasources[0] ?: 'GeneGo'
	datasource = datasource ?: defaultsource

	Map queryArgs = [datasource: datasource]
	String hql = 'select k from org.transmart.searchapp.SearchKeyword k where dataSource=:datasource'
	if ('Other' == step) {
	    hql += ''' and upper(substr(k.keyword, 1, 1)) not between 'A' and 'Z' '''
	}
	else {
	    hql += ' and upper(substr(k.keyword, 1, 1)) between :between1 and :between2'
	    queryArgs.between1 = step.substring(0, 1)
	    queryArgs.between2 = step.substring(step.length() - 1)
	}
	hql += 'order by upper(k.keyword)'

	List<SearchKeyword> pathways = SearchKeyword.executeQuery(
	    hql, queryArgs, cacheArgs)

	render view: 'pathwayhelp', model: [
	    pathways          : pathways,
	    datasources       : datasources,
	    selecteddatasource: datasource]
    }

    def listAllTrials() {
	List<Object[]> all = SearchKeyword.executeQuery('''
				SELECT s, e
				FROM SearchKeyword s, Experiment e
				WHERE s.dataCategory='TRIAL'
				  AND s.bioDataId=e.id
				ORDER BY s.keyword''')
	render view: 'trialhelp', model: [trials: all]
    }

    def listAllDiseases() {
	List<SearchKeyword> all = SearchKeyword.findAllByDataCategory(
	    'DISEASE', [sort: 'keyword', cache: 'read-only'])
	render view: 'diseasehelp', model: [diseases: all]
    }

    def listAllCompounds() {
	List<Object[]> all = SearchKeyword.executeQuery('''
				SELECT s, c
				FROM SearchKeyword s, Compound c
				WHERE s.dataCategory='COMPOUND'
				  AND s.bioDataId=c.id
				ORDER BY s.keyword''')
	render view: 'compoundhelp', model: [compounds: all]
    }

    /**
     * list all gene signatures and gene list versions user has permission to use in search
     */
    def listAllGeneSignatures() {

        // signatures user has search access
	List<GeneSignature> signatures = geneSignatureService.listPermissionedGeneSignatures(
	    securityService.currentUserId(), securityService.principal().isAdmin())

	Map<Long, SearchKeyword> mapKeywordsGS = [:]
	Map<Long, SearchKeyword> mapKeywordsGL = [:]
	SearchKeyword keyword

	for (GeneSignature gs in signatures) {
	    keyword = SearchKeyword.findByUniqueId(gs.uniqueId)
	    mapKeywordsGS[gs.id] = keyword

	    if (gs.foldChgMetricConceptCode != GeneSignatureService.METRIC_CODE_GENE_LIST) {
		keyword = SearchKeyword.findByUniqueId(GeneSignature.DOMAIN_KEY_GL + ':' + gs.id)
		mapKeywordsGL[gs.id] = keyword
            }
        }

	render view: 'geneSigHelp', model: [
	    signatures: signatures,
	    gsMap     : mapKeywordsGS,
	    glMap     : mapKeywordsGL]
    }
}
