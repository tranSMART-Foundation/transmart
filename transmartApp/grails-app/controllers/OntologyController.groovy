import annotation.AmTagItem
import annotation.AmTagTemplate
import fm.FmFolder
import fm.FmFolderAssociation
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.transmart.biomart.Experiment
import org.transmart.searchapp.AuthUser
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.Platform
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.OntologyTermTagsResource
import org.transmartproject.core.ontology.Study

import static org.transmartproject.core.ontology.OntologyTerm.VisualAttributes.HIGH_DIMENSIONAL

@Slf4j('logger')
class OntologyController {

    def index = {}
    def i2b2HelperService
    def springSecurityService
    def ontologyService
    def amTagTemplateService
    def amTagItemService
    ConceptsResource conceptsResourceService
    OntologyTermTagsResource ontologyTermTagsResourceService
    HighDimensionResource highDimensionResourceService

    def showOntTagFilter = {
        def tagtypesc = []
        tagtypesc.add("ALL")
        def tagtypes = i2b2.OntNodeTag.executeQuery("SELECT DISTINCT o.tagtype FROM i2b2.OntNodeTag as o order by o.tagtype")
        tagtypesc.addAll(tagtypes)
        def tags = i2b2.OntNodeTag.executeQuery("SELECT DISTINCT o.tag FROM i2b2.OntNodeTag o order by o.tag")
        /*WHERE o.tagtype='"+tagtypesc[0]+"'*/
        logger.trace "${tags as JSON}"
        render(template: 'filter', model: [tagtypes: tagtypesc, tags: tags])
    }

    def ajaxGetOntTagFilterTerms = {
        def tagtype = params.tagtype
        logger.trace("calling search for tagtype:" + tagtype)
        def tags = i2b2.OntNodeTag.executeQuery("SELECT DISTINCT o.tag FROM i2b2.OntNodeTag o WHERE o.tagtype='" + tagtype + "' order by o.tag")
        logger.trace "${tags as JSON}"
        render(template: 'depSelectTerm', model: [tagtype: tagtype, tags: tags])
    }

    def ajaxOntTagFilter =
            {
                logger.trace("called ajaxOntTagFilter")
                logger.trace("tagterm:" + params.tagterm)
                def tagterm = params.tagterm
                def ontsearchterm = params.ontsearchterm
                def tagtype = params.tagtype
                def result = ontologyService.searchOntology(tagterm, [ontsearchterm], tagtype, 'JSON')
                render result as JSON
            }


    def getInitialSecurity =
            {
                def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
                def result = i2b2HelperService.getAccess(i2b2HelperService.getRootPathsWithTokens(), user);
                render result as JSON
            }
    def sectest =
            {
                logger.trace("KEYS:" + params.keys)
                def keys = params.keys.toString().split(",");
                def paths = [];
                def access;
                if (params.keys != "") {
                    keys.each { key ->
                        logger.debug("in LOOP")
                        paths.add(i2b2HelperService.keyToPath(key))
                    }
                    def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)


                    access = i2b2HelperService.getConceptPathAccessCascadeForUser(paths, user)
                }
                logger.trace(access as JSON)
            }

    def showConceptDefinition = {
        def model = [:]

        OntologyTerm term = conceptsResourceService.getByKey(params.conceptKey)

        //high dimensional information
        if (term.visualAttributes.contains(HIGH_DIMENSIONAL)) {
            def dataTypeConstraint = highDimensionResourceService.createAssayConstraint(
                    AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
                    concept_key: term.key)

            model.subResourcesAssayMultiMap = highDimensionResourceService
                    .getSubResourcesAssayMultiMap([dataTypeConstraint])
        }

        //browse tab tags
        model.browseStudyInfo = getBrowseStudyInfo(term)

        //ontology term tags
        def tagsMap = ontologyTermTagsResourceService.getTags([ term ] as Set, false)
        model.tags = tagsMap?.get(term)

        render template: 'showDefinition', model: model
    }

    private def getBrowseStudyInfo = { OntologyTerm term ->
        Study study = term.study
        if (study?.ontologyTerm != term) {
            return [:]
        }

        Experiment experiment = Experiment.findByAccession(study.id.toUpperCase())
        if (!experiment) {
            logger.debug("No experiment entry found for ${study.id} study.")
            return [:]
        }

        FmFolder folder = FmFolderAssociation.findByObjectUid(experiment.uniqueId?.uniqueId)?.fmFolder
        if (!folder) {
            logger.debug("No fm folder found for ${study.id} study.")
            return [:]
        }

        AmTagTemplate amTagTemplate = amTagTemplateService.getTemplate(folder.uniqueId)
        List<AmTagItem> metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate?.id)
        [
                folder          : folder,
                bioDataObject   : experiment,
                metaDataTagItems: metaDataTagItems
        ]
    }

}
