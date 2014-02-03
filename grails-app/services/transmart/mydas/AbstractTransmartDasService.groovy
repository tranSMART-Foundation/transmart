package transmart.mydas

import org.transmartproject.core.dataquery.acgh.ChromosomalSegment
import org.transmartproject.core.dataquery.constraints.CommonHighDimensionalQueryConstraints
import org.transmartproject.core.dataquery.constraints.HighDimensionalQuery
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.querytool.QueriesResource

abstract class AbstractTransmartDasService {

    QueriesResource queriesResourceService
    ConceptsResource conceptsResourceService

    protected HighDimensionalQuery createHighDimensionalQuery(Long resultInstanceId, String conceptKey = null, Collection<String> segmentIds = [], uk.ac.ebi.mydas.model.Range range = null) {
        def segments = segmentIds.collect {
            def chromosomeSegment = new ChromosomalSegment(chromosome: it)
            if (range) {
                chromosomeSegment.start = range.from
                chromosomeSegment.end = range.to
            }
            chromosomeSegment
        }

        new HighDimensionalQuery(
                common: new CommonHighDimensionalQueryConstraints(
                        patientQueryResult: queriesResourceService.getQueryResultFromId(resultInstanceId)
                ),
                segments: segments,
                term: conceptKey ? conceptsResourceService.getByKey(conceptKey) : null
        )
    }

}
