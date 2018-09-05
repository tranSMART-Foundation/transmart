package transmart.mydas

import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.querytool.QueriesResource
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import uk.ac.ebi.mydas.extendedmodel.DasMethodE
import uk.ac.ebi.mydas.model.*
import uk.ac.ebi.mydas.exceptions.DataSourceException
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.dataconstraints.DataConstraint

abstract class TransmartDasServiceAbstract {

    abstract protected void getSpecificFeatures(RegionRow region, assays, Map<String, String> params, Collection<DasType> dasTypes, Map<String, List<DasFeature>> featuresPerSegment);

    HighDimensionResource highDimensionResourceService
    HighDimensionDataTypeResource resource

    QueriesResource queriesResourceService
    ConceptsResource conceptsResourceService

    String version = '1.0'
    DasMethodE dasMethod
    String projectionName

    protected Collection<DasType> dasTypes

    List<DasEntryPoint> getEntryPoints(Long resultInstanceId) {
        def query = getRegionQuery(resultInstanceId)
        TabularResult<AssayColumn, RegionRow> regions = resource.retrieveData(*query)
        regions.rows.collect {
            new DasEntryPoint(
                    //segmentId
                    it.chromosome,
                    //startCoordinate
                    it.start.intValue(),
                    //stopCoordinate
                    it.end.intValue(),
                    //type
                    '',
                    //version
                    '',
                    //orientation
                    null,
                    //description
                    '',
                    //hasSubparts
                    false
            )
        }
    }

    List<DasAnnotatedSegment> getFeatures(Long resultInstanceId,
                                          String conceptKey,
                                          Collection<String> segmentIds = [],
                                          Integer maxbins = null,
                                          uk.ac.ebi.mydas.model.Range range = null,
                                          Map<String, String> params = null,
                                          Collection<DasType> dasTypes = dasTypes) throws UnimplementedFeatureException, DataSourceException {
        def query = getRegionQuery(resultInstanceId, conceptKey, segmentIds, range)
        TabularResult<AssayColumn, RegionRow> regionResult = resource.retrieveData(*query)
        def assays = regionResult.indicesList
        Map<String, List<DasFeature>> featuresPerSegment = [:]
        try {
            for (RegionRow region : regionResult.rows) {

                if(!featuresPerSegment[region.chromosome]) featuresPerSegment[region.chromosome] = []
                getSpecificFeatures(region, assays, params, dasTypes, featuresPerSegment)
            }
        }  finally {
            regionResult.close()
        }

        def segments = segmentIds.collect{ new DasAnnotatedSegment(it , range?.getFrom() ?: null , range?.getTo() ?: null , version, it, featuresPerSegment[it] ?: []) }

        reduceBins(segments, maxbins)
    }



    protected List getRegionQuery(Long resultInstanceId,
                                String conceptKey,
                                Collection<String> segmentIds = [],
                                uk.ac.ebi.mydas.model.Range range = null) {

        List assayConstraints = [
                resource.createAssayConstraint(AssayConstraint.PATIENT_SET_CONSTRAINT,
                        result_instance_id: resultInstanceId)]

        if (conceptKey) {
            assayConstraints.add(resource.createAssayConstraint(
                    AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
                    concept_key: conceptKey))
        }

        List dataConstraints = [
                resource.createDataConstraint(
                        DataConstraint.DISJUNCTION_CONSTRAINT,
                        subconstraints: [
                                (DataConstraint.CHROMOSOME_SEGMENT_CONSTRAINT): segmentIds.collect {
                                    def ret = [ chromosome: it ]
                                    if (range) {
                                        ret.start = range.from
                                        ret.end = range.to
                                    }
                                    ret
                                }
                        ]
                )
        ]

        def projection = resource.createProjection [:], projectionName

        [ assayConstraints, dataConstraints, projection ]
    }


    private List<DasAnnotatedSegment> reduceBins(List<DasAnnotatedSegment> segments, Integer maxbins) {
        if(maxbins) {
            //TODO Reduce to maxbins
            segments
        } else segments
    }

}
