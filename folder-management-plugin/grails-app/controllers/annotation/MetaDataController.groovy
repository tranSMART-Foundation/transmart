package annotation

import grails.converters.JSON
import org.transmart.biomart.BioAssayPlatform
import org.transmart.biomart.ConceptCode

class MetaDataController {

    static allowedMethods = [save: 'POST', update: 'POST', delete: 'POST']
    static defaultAction = 'list'

    def searchKeywordService

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
    }

    def searchAction() {
	redirect action: 'list', params: params
    }

    /**
     * Find the top 10 concepts with a case-insensitive LIKE
     */
    def extSearch() {
	String value = params.term ? params.term.toUpperCase() : ''
	String codeTypeName = params.codeTypeName ?: ''

	List<ConceptCode> conceptCodes = ConceptCode.executeQuery('''
		FROM ConceptCode cc
		WHERE cc.codeTypeName = :codeTypeName
		  and upper(cc.codeName) LIKE :codeName
		order by codeTypeName''',
		[codeTypeName: codeTypeName, codeName: value + '%'],
		[max: 10])

	List<Map> itemlist = []
        for (conceptCode in conceptCodes) {
            if (conceptCode.uniqueId != null && conceptCode.codeName != null) {
		itemlist << [id: conceptCode.uniqueId, keyword: conceptCode.codeName,
			     sourceAndCode: conceptCode.uniqueId, category: '', display: '']
            }
        }

	render(itemlist as JSON)
    }

    /**
     * Find the top 10 compounds with a case-insensitive LIKE
     */
    def bioCompoundSearch() {
        render searchKeywordService.findSearchKeywords('COMPOUND', params.term, 10) as JSON
    }

    /**
     * Find the top 10 diseases with a case-insensitive LIKE
     */
    def bioDiseaseSearch() {
        render searchKeywordService.findSearchKeywords('DISEASE', params.term, 10) as JSON
    }

    /**
     * Find the top 10 genes with a case-insensitive LIKE
     */
    def bioMarkerSearch() {
        render searchKeywordService.findSearchKeywords('GENE', params.term, 10) as JSON
    }

    /**
     * Find the top 10 biosources with a case-insensitive LIKE
     */
    def biosourceSearch = {
        render searchKeywordService.findSearchKeywords('BIOSOURCE', params.term, 10) as JSON
    }

    /**
     * Find the top 10 diseases, genes, pathways, observations or concepts with a case-insensitive LIKE
     */
    def programTargetSearch() {

	String value = params.term ? params.term.toUpperCase() : ''

	List<Map> itemlist = []
	itemlist.addAll searchKeywordService.findSearchKeywords('DISEASE', params.term, 10)
	itemlist.addAll searchKeywordService.findSearchKeywords('GENE', params.term, 10)
	itemlist.addAll searchKeywordService.findSearchKeywords('PATHWAY', params.term, 10)
	itemlist.addAll searchKeywordService.findSearchKeywords('OBSERVATION', params.term, 10)

	List<ConceptCode> conceptCodes = ConceptCode.executeQuery('''
		FROM ConceptCode cc
		WHERE cc.codeTypeName = :codeTypeName
		and  upper(cc.codeName) LIKE :codeName
		order by codeTypeName''',
		[codeTypeName: 'PROGRAM_TARGET_PATHWAY_PHENOTYPE', codeName: value + '%'],
		[max: 10])
        for (conceptCode in conceptCodes) {
	    itemlist << [id: conceptCode.uniqueId, label: conceptCode.codeName,
			 sourceAndCode: conceptCode.uniqueId, categoryId: 'PROGRAM_TARGET',
			 category: 'Program Target', display: '']
        }

	render(itemlist as JSON)
    }

    def bioAssayPlatformSearch() {
        Map pagingMap = [max: 20]

	Map paramMap = [:]
        def itemlist = []

	String value = params.term.toUpperCase()
	StringBuilder sb = new StringBuilder('from BioAssayPlatform p where 1=1 ')

	if (value && value != 'NULL') {
	    sb << ' and upper(p.name) like :term '
	    paramMap.term = value + '%'
        }

	if (params.vendor && params.vendor != 'null') {
	    sb << ' and p.vendor = :vendor '
	    paramMap.vendor = params.vendor
        }

	if (params.measurement && params.measurement != 'null') {
	    sb << ' and p.platformType = :measurement '
	    paramMap.measurement = params.measurement
        }

	if (params.technology && params.technology != 'null') {
	    sb << ' and p.platformTechnology = :technology '
	    paramMap.technology = params.technology
        }

	sb << 'order by platformType, vendor, platformTechnology, name'

	List<BioAssayPlatform> platforms = BioAssayPlatform.executeQuery(sb.toString(), paramMap, pagingMap)
        for (platform in platforms) {
	    //+ " -- [MEASUREMENT::"+platform.platformType + " VENDOR::" + platform.vendor + " TECH::" + platform.platformTechnology + "]"
            String filterString = ''

	    if (!params.measurement || params.measurement == 'null') {
                filterString += ' MEASUREMENT::' + platform.platformType
            }

	    if (!params.technology || params.technology == 'null') {
                filterString += ' TECHNOLOGY::' + platform.platformTechnology
            }

	    if (!params.vendor || params.vendor == 'null') {
                filterString += ' VENDOR::' + platform.vendor
            }

	    if (filterString) {
                filterString = ' -- [' + filterString + ']'
            }

	    itemlist << [id: platform.uniqueId, label: platform.name + filterString, category: 'PLATFORM', display: 'Platform']
	}

	render(itemlist as JSON)
    }
}
