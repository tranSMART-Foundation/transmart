import annotation.AmTagItem
import annotation.AmTagItemService
import annotation.AmTagTemplate
import annotation.AmTagTemplateService
import fm.FmFolder
import fm.FmFolderAssociation
import grails.converters.JSON
import groovy.util.logging.Slf4j
import i2b2.OntNodeTag
import org.transmart.biomart.Experiment
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.OntologyTermTag
import org.transmartproject.core.ontology.OntologyTermTagsResource
import org.transmartproject.core.ontology.Study
import transmartapp.OntologyService

import static org.transmartproject.core.ontology.OntologyTerm.VisualAttributes.HIGH_DIMENSIONAL

@Slf4j('logger')
class OntologyController {

    AmTagItemService amTagItemService
    AmTagTemplateService amTagTemplateService
    ConceptsResource conceptsResourceService
    HighDimensionResource highDimensionResourceService
    I2b2HelperService i2b2HelperService
    OntologyService ontologyService
    OntologyTermTagsResource ontologyTermTagsResourceService

    def index() {}

    def showOntTagFilter() {
	List<String> tagtypes = OntNodeTag.executeQuery('''
				SELECT DISTINCT o.tagtype
				FROM OntNodeTag as o
				order by o.tagtype''')
	List<String> tags = OntNodeTag.executeQuery('SELECT DISTINCT o.tag FROM OntNodeTag o order by o.tag')
	render template: 'filter', model: [tagtypes: ['ALL'] + tagtypes, tags: tags]
    }

    def ajaxGetOntTagFilterTerms(String tagtype) {
	logger.trace 'calling search for tagtype:{}', tagtype
	List<String> tags = OntNodeTag.executeQuery('''
				SELECT DISTINCT o.tag
				FROM OntNodeTag o
				WHERE o.tagtype=:tagtype
				order by o.tag''',
						    [tagtype: tagtype])
	render template: 'depSelectTerm', model: [tagtype: tagtype, tags: tags]
    }

    def ajaxOntTagFilter(String tagterm, String ontsearchterm, String tagtype) {
	logger.trace 'called ajaxOntTagFilter; tagterm:{}', tagterm
	render(ontologyService.searchOntology(tagterm, [ontsearchterm], tagtype, 'JSON') as JSON)
    }

    def getInitialSecurity() {
	render(i2b2HelperService.getAccess(i2b2HelperService.getRootPathsWithTokens()) as JSON)
    }

    def sectest(String keys) {
	logger.trace 'KEYS:{}', keys
	List<String> paths = []
	def access
	if (keys) {
	    for (String key in keys.split(',')) {
		paths << i2b2HelperService.keyToPath(key)
            }
	    access = i2b2HelperService.getConceptPathAccessCascadeForUser(paths)
	}
	logger.trace((access as JSON).toString())
    }

    def showConceptDefinition(String conceptKey) {
	Map model = [:]

	OntologyTerm term = conceptsResourceService.getByKey(conceptKey)

        //high dimensional information
        if (term.visualAttributes.contains(HIGH_DIMENSIONAL)) {
	    AssayConstraint dataTypeConstraint = highDimensionResourceService.createAssayConstraint(
                AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
                concept_key: term.key)

	    model.subResourcesAssayMultiMap = highDimensionResourceService.getSubResourcesAssayMultiMap(
		[dataTypeConstraint])
        }

        //browse tab tags
        model.browseStudyInfo = getBrowseStudyInfo(term)

        //ontology term tags
	Map<OntologyTerm, List<OntologyTermTag>> tagsMap = ontologyTermTagsResourceService.getTags([term] as Set, false)
        model.tags = tagsMap?.get(term)

        render template: 'showDefinition', model: model
    }

    private Map getBrowseStudyInfo(OntologyTerm term) {
        Study study = term.study
        if (study?.ontologyTerm != term) {
            return [:]
        }

        Experiment experiment = Experiment.findByAccession(study.id.toUpperCase())
        if (!experiment) {
	    logger.debug 'No experiment entry found for {} study.', study.id
            return [:]
        }

        FmFolder folder = FmFolderAssociation.findByObjectUid(experiment.uniqueId?.uniqueId)?.fmFolder
        if (!folder) {
	    logger.debug 'No fm folder found for {} study.', study.id
            return [:]
        }

        AmTagTemplate amTagTemplate = amTagTemplateService.getTemplate(folder.uniqueId)
        List<AmTagItem> metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate?.id)
	[folder          : folder,
         bioDataObject   : experiment,
	 metaDataTagItems: metaDataTagItems]
    }
}
