package org.transmart

import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.biomart.BioAssayPlatform
import org.transmart.biomart.BioDataExternalCode
import org.transmart.biomart.ConceptCode
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.UtilService
import org.transmart.searchapp.GeneSignature
import org.transmart.searchapp.SearchKeyword
import org.transmart.searchapp.SearchKeywordTerm

/**
 * @author mmcduffie
 */
@Slf4j('logger')
class SearchKeywordService {

    @Autowired private SecurityService securityService
    @Autowired private UtilService utilService

    //Hard-coded list of items that we consider filter categories... configure in Config/database?
    private static final List<Map> filtercats = [

        [codeTypeName: 'THERAPEUTIC_DOMAIN', category: 'THERAPEUTIC_DOMAIN', displayName: 'Program Therapeutic Domain'],
        [codeTypeName: 'PROGRAM_INSTITUTION', category: 'PROGRAM_INSTITUTION', displayName: 'Program Institution'],

        [codeTypeName: 'STUDY_PHASE', category: 'STUDY_PHASE', displayName: 'Study Phase'],
        [codeTypeName: 'STUDY_OBJECTIVE', category: 'STUDY_OBJECTIVE', displayName: 'Study Objective'],
        [codeTypeName: 'STUDY_DESIGN', category: 'STUDY_DESIGN', displayName: 'Study Design'],
        [codeTypeName: 'STUDY_BIOMARKER_TYPE', category: 'STUDY_BIOMARKER_TYPE', displayName: 'Study Biomarker Type'],
        [codeTypeName: 'STUDY_ACCESS_TYPE', category: 'STUDY_ACCESS_TYPE', displayName: 'Study Access Type'],
        [codeTypeName: 'STUDY_INSTITUTION', category: 'STUDY_INSTITUTION', displayName: 'Study Institution'],

        [codeTypeName: 'ASSAY_TYPE_OF_BM_STUDIED', category: 'ASSAY_TYPE_OF_BM_STUDIED', displayName: 'Assay Type of Biomarkers'],
        [category: 'ASSAY_MEASUREMENT_TYPE', displayName: 'Assay Measurement Type', useText: true, platformProperty: 'platformType'],
        [category: 'ASSAY_TECHNOLOGY', displayName: 'Assay Technology', prefix: true, useText: true, platformProperty: 'platformTechnology'],
        [category: 'ASSAY_VENDOR', displayName: 'Assay Vendor', prefix: true, useText: true, platformProperty: 'vendor'],
        [category: 'ASSAY_PLATFORM_NAME', displayName: 'Assay Platform Name', useText: true, platformProperty: 'name'],

        [category: 'ANALYSIS_MEASUREMENT_TYPE', displayName: 'Analysis Measurement Type', useText: true, platformProperty: 'platformType'],
        [category: 'ANALYSIS_TECHNOLOGY', displayName: 'Analysis Technology', prefix: true, useText: true, platformProperty: 'platformTechnology'],
        [category: 'ANALYSIS_VENDOR', displayName: 'Analysis Vendor', prefix: true, useText: true, platformProperty: 'vendor'],
        [category: 'ANALYSIS_PLATFORM_NAME', displayName: 'Analysis Platform Name', useText: true, platformProperty: 'name'],

        [codeTypeName: 'FILE_TYPE', category: 'FILE_TYPE', displayName: 'File type']
    ].asImmutable()

    /** Finds all of the search categories pertaining to search keywords */
    List<Map<String, String>> findSearchCategories() {
	logger.debug 'Finding all of the search categories...'

	List<String> results = SearchKeyword.createCriteria().list {
            projections {
		distinct 'dataCategory'
            }
	    order 'dataCategory', 'asc'
        }

	logger.debug 'Categories found: {}', results.size()

	List<Map<String, String>> categories = []

        for (result in results) {
	    categories << [category: result]
        }

	categories
    }

    List<Map> findFilterCategories() {

	List<Map> categories = []

	for (Map filtercat in filtercats) {

	    logger.debug 'findFilterCategories {}', filtercat
	    Set<Map> choices = new TreeSet<>(new Comparator<Map>() {
		int compare(Map m1, Map m2) {
		    m1.name.compareTo m2.name
		}
	    })

            if (filtercat.platformProperty) {
		List<String> results = BioAssayPlatform.createCriteria().list {
                    projections {
			distinct filtercat.platformProperty
                    }
		    order filtercat.platformProperty, 'asc'
                }
		for (String result in results) {
		    if (filtercat.platformProperty) {
			choices << [name: result, uid: result]
		    }
                }
            }
            else {
		List<ConceptCode> results
		if (filtercat.prefix) {
		    results = ConceptCode.findAllByCodeTypeNameLike(filtercat.codeTypeName + ':%', [sort: 'codeName', order: 'asc'])
                }
		else {
		    results = ConceptCode.findAllByCodeTypeName(filtercat.codeTypeName, [sort: 'codeName', order: 'asc'])
                }
		for (ConceptCode result in results) {
		    if (filtercat.useText) {
			choices << [name: result.codeName, uid: result.codeName]
                    }
                    else {
			choices << [name: result.codeName, uid: result.bioDataUid.uniqueId[0]]
		    }
                }
            }

            if (choices) {
		categories << [category: filtercat, choices: choices]
            }
        }

	logger.debug 'findFilterCategories result.size {}', categories.size()

	categories
    }

    /** Searches for all keywords for a given term (like %il%) */
    List<Map> findSearchKeywords(String category, String term, int max) {
	logger.debug 'Finding matches for {} in {}', term, category

	List results = SearchKeywordTerm.createCriteria().list {
	    if (term) {
		like 'keywordTerm', term.toUpperCase() + '%'
            }

	    if ('GENE_OR_SNP' == category) {
                searchKeyword {
                    or {
			eq 'dataCategory', 'GENE'
			eq 'dataCategory', 'SNP'
                    }
                }
            }
            else if ('ALL'.compareToIgnoreCase(category) != 0) {
                searchKeyword {
		    eq 'dataCategory', category.toUpperCase()
                }
            }

	    if (!securityService.principal().isAdmin()) {
		logger.debug 'User is not an admin so filter out gene lists or signatures that are not public'
                or {
		    isNull 'ownerAuthUserId'
		    eq 'ownerAuthUserId', securityService.currentUserId()
                }
            }
	    maxResults max
	    order 'rank', 'asc'
	    order 'termLength', 'asc'
	    order 'keywordTerm', 'asc'
        }
	logger.debug 'Search keywords found: {}', results.size()

	List<Map> keywords = []
	List<String> dupeList = [] // store category:keyword for a duplicate check until DB is cleaned up

        for (result in results) {
            def sk = result
            //////////////////////////////////////////////////////////////////////////////////
            // HACK:  Duplicate check until DB is cleaned up
	    String dupeKey = sk.searchKeyword.displayDataCategory + ':' + sk.searchKeyword.keyword +
                ':' + sk.searchKeyword.bioDataId
            if (dupeKey in dupeList) {
		logger.debug 'Found duplicate: {}', dupeKey
                continue
            }
            else {
		logger.debug 'Found new entry, adding to the list: {}', dupeList
                dupeList << dupeKey
            }
            ///////////////////////////////////////////////////////////////////////////////////

	    Map m = [label: sk.searchKeyword.keyword, category: sk.searchKeyword.displayDataCategory,
		     categoryId: sk.searchKeyword.dataCategory]

	    if ('GENE_OR_SNP' == category || 'SNP' == category) {
		m.id = sk.searchKeyword.id
            }
            else {
		m.id == sk.searchKeyword.uniqueId
            }

            if ('TEXT'.compareToIgnoreCase(sk.searchKeyword.dataCategory) != 0) {
		List<BioDataExternalCode> synonyms = BioDataExternalCode.findAllWhere(bioDataId: sk.searchKeyword.bioDataId, codeType: 'SYNONYM')
		StringBuilder synList = new StringBuilder()
                for (synonym in synonyms) {
		    if (synList) {
			synList << ', '
                    }
                    else {
			synList << '('
                    }
		    synList << synonym.code
                }
		if (synList) {
		    synList << ')'
                }
		m.synonyms = synList.toString()
            }
	    keywords << m
        }

	// Get results from Bio Concept Code table

	if (category == 'ALL') {
            results = ConceptCode.createCriteria().list {
		if (term) {
		    like 'bioConceptCode', term.toUpperCase().replace(' ', '_') + '%'
                }
                or {
		    'in' 'codeTypeName', filtercats*.codeTypeName
                }
		maxResults max
		order 'bioConceptCode', 'asc'
            }
	    logger.debug 'Bio concept code keywords found: {}', results.size()

            for (result in results) {
                //Get display name by category
                def cat = filtercats.find { result.codeTypeName.startsWith(it.codeTypeName) }

		Map m = [label: result.codeName, category: cat.displayName, categoryId: cat.category,
			 id: cat.useText ? result.codeName : result.bioDataUid.uniqueId[0]]
		if (!keywords.find { it.id == m.id }) {
		    keywords << m
                }
            }

            //If we're not over the maximum result threshold, query the platform table as well
            if (keywords.size() < max) {

                //Perform a query for each platform field
		for (Map cat in filtercats) {
                    if (cat.platformProperty) {
                        results = BioAssayPlatform.createCriteria().list {
			    ilike cat.platformProperty, term + '%'
			    maxResults max
			    order cat.platformProperty, 'asc'
			}
			logger.debug 'Platform {} keywords found: {}', cat.platformProperty, results.size()

			for (result in results) {
			    Map m = [label: result[cat.platformProperty], category: cat.displayName,
				     categoryId: cat.category, id: result[cat.platformProperty]]
			    if (!keywords.find { it.id == m.id && it.category == m.category }) {
				keywords << m
			    }
			}
		    }
		}
	    }
	}

	logger.debg 'findSearchKeywords result {}', keywords

	keywords
    }

    /**
     * convert pathways to a list of genes
     */
    List<SearchKeyword> expandPathwayToGenes(String pathwayIds) {
	if (!pathwayIds) {
            return []
        }

	SearchKeyword.executeQuery '''
				select DISTINCT k
				from org.transmart.searchapp.SearchKeyword k, org.transmart.biomart.BioDataCorrelation c
				where k.bioDataId=c.associatedBioDataId and c.bioDataId in (''' + pathwayIds + ')' + '''
				ORDER BY k.keyword
		'''
    }

    List<SearchKeyword> expandAllListToGenes(String pathwayIds, Long max = null) {
	if (!pathwayIds) {
            return []
        }

	List<SearchKeyword> result = []
        // find pathways
	String hql = '''
				select DISTINCT k
			from org.transmart.searchapp.SearchKeyword k, org.transmart.biomart.BioDataCorrelation c
			where k.bioDataId=c.associatedBioDataId
			  and c.bioDataId in (''' + pathwayIds + ')' + '''
			ORDER BY k.keyword'''
	if (max != null) {
	    result.addAll SearchKeyword.executeQuery(hql, [max: max])
	}

        // find gene sigs
	String hql2 = '''
			select DISTINCT k from org.transmart.searchapp.SearchKeyword k, org.transmart.searchapp.SearchBioMarkerCorrelFastMV c
			where k.bioDataId=c.assocBioMarkerId
			and c.domainObjectId in (''' + pathwayIds + ')' + '''
			ORDER BY k.keyword'''
        if (result.size() < max) {
	    result.addAll SearchKeyword.executeQuery(hql2, [max: (max - result.size())])
        }
        else {
	    result.addAll SearchKeyword.executeQuery(hql)
	    result.addAll SearchKeyword.executeQuery(hql2)
        }

	result
    }

    /**
     * update GeneSignature/List link to search
     */
    @Transactional
    void updateGeneSignatureLink(GeneSignature gs, String domainKey, boolean flush) {
        // find keyword record
        SearchKeyword keyword = SearchKeyword.findByBioDataIdAndDataCategory(gs.id, domainKey)

        // delete search keywords
	if (gs.deletedFlag ||
	    (domainKey == GeneSignature.DOMAIN_KEY_GL && gs.foldChgMetricConceptCode.bioConceptCode != 'NOT_USED') ||
	    (domainKey == GeneSignature.DOMAIN_KEY && gs.foldChgMetricConceptCode.bioConceptCode == 'NOT_USED')) {
	    keyword?.delete(flush: flush)
        }
        else {
            // add if does not exist
            if (keyword == null) {
                keyword = createSearchKeywordFromGeneSig(gs, domainKey)
            }
            else {
                // update keyword
                keyword.keyword = gs.name
                keyword.ownerAuthUserId = gs.publicFlag ? null : gs.createdByAuthUser.id
		for (SearchKeywordTerm term in keyword.terms) {
		    term.keywordTerm = gs.name.toUpperCase()
		    term.ownerAuthUserId = gs.publicFlag ? null : gs.createdByAuthUser.id
                }
            }

	    if (!keyword.save(flush: flush)) {
		logger.error '{}', utilService.errorStrings(keyword)
            }
        }
    }

    /**
     * create a new SearchKeyword for a GeneSignature
     */
    private SearchKeyword createSearchKeywordFromGeneSig(GeneSignature gs, String domainKey) {

	String displayName = domainKey == GeneSignature.DOMAIN_KEY ? GeneSignature.DISPLAY_TAG : GeneSignature.DISPLAY_TAG_GL

	SearchKeyword keyword = new SearchKeyword(keyword: gs.name, bioDataId: gs.id, uniqueId: gs.uniqueId,
						  dataCategory: domainKey, displayDataCategory: displayName, dataSource: 'Internal')
	if (!gs.publicFlag) {
	    keyword.ownerAuthUserId = gs.createdByAuthUserId
	}

	SearchKeywordTerm term = new SearchKeywordTerm(keywordTerm: gs.name.toUpperCase(), rank: 1, termLength: gs.name.length())
	if (!gs.publicFlag) {
	    term.ownerAuthUserId = gs.createdByAuthUserId
	}

	keyword.addToTerms term

	keyword
    }
}
