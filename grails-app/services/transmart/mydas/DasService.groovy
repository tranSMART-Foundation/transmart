package transmart.mydas

import org.transmartproject.core.dataquery.DataQueryResource
import org.transmartproject.core.dataquery.acgh.ChromosomalSegment
import org.transmartproject.core.dataquery.acgh.CopyNumberState
import org.transmartproject.core.dataquery.acgh.RegionResult
import org.transmartproject.core.dataquery.acgh.RegionRow
import org.transmartproject.core.dataquery.acgh.Region
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.constraints.ACGHRegionQuery
import org.transmartproject.core.dataquery.constraints.CommonHighDimensionalQueryConstraints
import org.transmartproject.core.querytool.QueriesResource
import uk.ac.ebi.mydas.exceptions.DataSourceException
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
import uk.ac.ebi.mydas.extendedmodel.DasMethodE
import uk.ac.ebi.mydas.extendedmodel.DasTypeE
import uk.ac.ebi.mydas.model.DasAnnotatedSegment
import uk.ac.ebi.mydas.model.DasEntryPoint
import uk.ac.ebi.mydas.model.DasEntryPointOrientation
import uk.ac.ebi.mydas.model.DasFeature
import uk.ac.ebi.mydas.model.DasFeatureOrientation
import uk.ac.ebi.mydas.model.DasPhase
import uk.ac.ebi.mydas.model.DasType

class DasService {

    static transactional = true

    DataQueryResource dataQueryResourceNoGormService
    QueriesResource queriesResourceService

    String acghVersion = '1.0'
    String acghEntryPointVersion = '1.0'

    def dasTypeToCopyNumberStateMapping = [
            (new DasTypeE('acgh-loss-frequency', null, null, 'acgh-loss-frequency')) : CopyNumberState.LOSS,
            (new DasTypeE('acgh-normal-frequency', null, null, 'acgh-normal-frequency')) : CopyNumberState.NORMAL,
            (new DasTypeE('acgh-gain-frequency', null, null, 'acgh-gain-frequency')): CopyNumberState.GAIN,
            (new DasTypeE('acgh-amp-frequency', null, null, 'acgh-amp-frequency')): CopyNumberState.AMPLIFICATION,
            (new DasTypeE('acgh-inv-frequency', null, null, 'acgh-inv-frequency')): CopyNumberState.INVALID
    ]

    def copyNumberStateToDasTypeMapping = dasTypeToCopyNumberStateMapping.collectEntries { [(it.value): it.key] }

    Collection<DasType> acghDasTypes = dasTypeToCopyNumberStateMapping.keySet()

    //TODO Choose correct cvId(3-d parameter) from http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=SO
    private def acghDasMethod = new DasMethodE('acgh', 'acgh', 'acgh-cv-id')

    List<DasAnnotatedSegment> getAcghFeatures(Long resultInstanceId,
                                              Collection<String> segmentIds = [],
                                              Integer maxbins = null,
                                              uk.ac.ebi.mydas.model.Range range = null,
                                              Collection<DasType> dasTypes = acghDasTypes) throws UnimplementedFeatureException, DataSourceException {
        def query = getACGHRegionQuery(resultInstanceId, segmentIds, range)
        RegionResult regionResult = dataQueryResourceNoGormService.runACGHRegionQuery(query, null)
        def assays = regionResult.indicesList
        Map<String, List<DasFeature>> featuresPerSegment = [:]
        try {
            for (RegionRow row: regionResult.rows) {
                def region = row.region

                if(!featuresPerSegment[region.chromosome]) featuresPerSegment[region.chromosome] = []
                def countPerDasType = countAcghDasTypesForRow(row, assays, dasTypes)
                countPerDasType.each { typeCountEntry ->
                    def freq = typeCountEntry.value / (double) assays.size()

                    featuresPerSegment[region.chromosome] << new DasFeature(
                            //featureId
                            "${typeCountEntry.key.id}-${region.id}",
                            //featureLabel
                            "${typeCountEntry.key.id}-${region.id}",
                            //type
                            typeCountEntry.key,
                            //method
                            acghDasMethod,
                            //startCoordinate
                            region.start.intValue(),
                            //endCoordinate
                            region.end.intValue(),
                            //score
                            freq.doubleValue(),
                            //orientation
                            DasFeatureOrientation.ORIENTATION_ANTISENSE_STRAND,
                            //phase
                            DasPhase.PHASE_NOT_APPLICABLE,
                            //notes
                            [],
                            //links
                            [:],
                            //targets
                            [],
                            //parents
                            [],
                            //parts
                            [])

                }
            }
        }  finally {
            regionResult.close()
        }

        def segments = segmentIds.collect{ new DasAnnotatedSegment(it , range?.getFrom() ?: null , range?.getTo() ?: null , acghVersion, it, featuresPerSegment[it] ?: []) }

        reduceBins(segments, maxbins)
    }

    List<DasEntryPoint> getAcghEntryPoints(Long resultInstanceId) {
        def query = getACGHRegionQuery(resultInstanceId)
        List<ChromosomalSegment> regions = dataQueryResourceNoGormService.getChromosomalSegments(query)
        regions.collect {
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

    private List<CopyNumberState> convertToCopyNumberState(Collection<DasType> dasTypes) {
        dasTypes.collect{ dasTypeToCopyNumberStateMapping[it] }
    }

    private List<CopyNumberState> convertToDasType(Collection<CopyNumberState> states) {
        states.collect{ dasTypeToCopyNumberStateMapping[it] }
    }

    private def countAcghCopyNumberStatesForRow(RegionRow row, Collection<Assay> assays, Collection<CopyNumberState> states) {
        //TODO Use countBy
        states.collectEntries {
            [(it): assays.count { assay ->
                it == row.getRegionDataForAssay(assay).copyNumberState
            }]
        }
    }

    private def countAcghDasTypesForRow(RegionRow row, Collection<Assay> assays, Collection<DasType> dasTypes) {
        Map<CopyNumberState, Integer> acghCopyNumberStatesForRowMap = countAcghCopyNumberStatesForRow(row, assays, convertToCopyNumberState(dasTypes))
        acghCopyNumberStatesForRowMap.collectEntries {
            [(copyNumberStateToDasTypeMapping[it.key]): it.value]
        }
    }

    private List<DasAnnotatedSegment> reduceBins(List<DasAnnotatedSegment> segments, Integer maxbins) {
        if(maxbins) {
            //TODO Reduce to maxbins
            segments
        } else segments
    }

    private ACGHRegionQuery getACGHRegionQuery(Long resultInstanceId, Collection<String> segmentIds = [], uk.ac.ebi.mydas.model.Range range = null) {
        def segments = segmentIds.collect {
            def chromosomeSegment = new ChromosomalSegment(chromosome: it)
            if(range) {
                chromosomeSegment.start = range.from
                chromosomeSegment.end = range.to
            }
            chromosomeSegment
        }

        new ACGHRegionQuery(
                common: new CommonHighDimensionalQueryConstraints(
                        patientQueryResult: queriesResourceService.getQueryResultFromId(resultInstanceId)
                ),
                segments: segments
        )
    }

}
