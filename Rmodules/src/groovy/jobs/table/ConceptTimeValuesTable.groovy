package jobs.table

import jobs.misc.Hacks
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.ontology.OntologyTerm

/**
 * @author carlos
 */
@Component
@Scope('job')
class ConceptTimeValuesTable {

    @Autowired
    ConceptsResource conceptsResource

    List<String> conceptPaths

    @Lazy
    Map<String,Map> resultMap = computeMap()

    /**
     * @return map of concept_fullname -> series_meta map, or null if not enabled or metadata not applicable
     */
    private Map<String, Map> computeMap() {

        //get all the OntologyTerms for the concepts
        Set<OntologyTerm> terms = conceptPaths.collect { conceptsResource.getByKey(getConceptKey(it)) }

        //get all the SeriesMeta mapped by concept name
        Map<String, Map> nameToSeriesMeta = terms.collectEntries {[it.fullName, it.metadata?.seriesMeta as Map]}

        if (nameToSeriesMeta) {
            String firstUnit = nameToSeriesMeta.values().first()?.unit?.toString()

            //if all the units are the same and not null, and with numerical values
            if (firstUnit != null && nameToSeriesMeta.values().every { it?.value?.isInteger() && firstUnit == it?.unit?.toString() }) {
                return nameToSeriesMeta
            }
        }
    }

    static String getConceptKey(String path) {
        Hacks.createConceptKeyFrom path
    }

}
