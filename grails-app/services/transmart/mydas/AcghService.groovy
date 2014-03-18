package transmart.mydas

import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.acgh.AcghValues
import org.transmartproject.core.dataquery.highdim.acgh.CopyNumberState
import org.transmartproject.core.dataquery.highdim.dataconstraints.DataConstraint
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import uk.ac.ebi.mydas.extendedmodel.DasMethodE
import uk.ac.ebi.mydas.extendedmodel.DasTypeE
import uk.ac.ebi.mydas.model.*

import javax.annotation.PostConstruct

class AcghService extends TransmartDasServiceAbstract {

    static transactional = true

    @PostConstruct
    void init() {
        resource = highDimensionResourceService.getSubResourceForType 'acgh'
        //TODO Choose correct cvId(3-d parameter) from http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=SO
        dasMethod = new DasMethodE('acgh', 'acgh', 'acgh-cv-id')
        projectionName = 'acgh_values'
        dasTypes = dasTypeToCopyNumberStateMapping.keySet();

    }

    String acghEntryPointVersion = '1.0'

    def dasTypeToCopyNumberStateMapping = [
            (new DasTypeE('acgh-loss-frequency', null, null, 'acgh-loss-frequency')) : CopyNumberState.LOSS,
            (new DasTypeE('acgh-normal-frequency', null, null, 'acgh-normal-frequency')) : CopyNumberState.NORMAL,
            (new DasTypeE('acgh-gain-frequency', null, null, 'acgh-gain-frequency')): CopyNumberState.GAIN,
            (new DasTypeE('acgh-amp-frequency', null, null, 'acgh-amp-frequency')): CopyNumberState.AMPLIFICATION,
            (new DasTypeE('acgh-inv-frequency', null, null, 'acgh-inv-frequency')): CopyNumberState.INVALID
    ]

    def copyNumberStateToDasTypeMapping = dasTypeToCopyNumberStateMapping.collectEntries { [(it.value): it.key] }



    @Override
    protected void getSpecificFeatures(RegionRow region, assays,  Map<String, String> params, Collection<DasType> dasTypes, Map<String, List<DasFeature>> featuresPerSegment) {
        def countPerDasType = countAcghDasTypesForRow(region, assays, dasTypes)
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
                    dasMethod,
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


    private List<CopyNumberState> convertToCopyNumberState(Collection<DasType> dasTypes) {
        dasTypes.collect{ dasTypeToCopyNumberStateMapping[it] }
    }

    private def countAcghCopyNumberStatesForRow(RegionRow<AcghValues> row, Collection<Assay> assays, Collection<CopyNumberState> states) {
        //TODO Use countBy
        states.collectEntries {
            [(it): assays.count { Assay assay ->
                it == row.getAt(assay).copyNumberState
            }]
        }
    }

    private def countAcghDasTypesForRow(RegionRow row, Collection<Assay> assays, Collection<DasType> dasTypes) {
        Map<CopyNumberState, Integer> acghCopyNumberStatesForRowMap =
            countAcghCopyNumberStatesForRow(row, assays, convertToCopyNumberState(dasTypes))
        acghCopyNumberStatesForRowMap.collectEntries {
            [(copyNumberStateToDasTypeMapping[it.key]): it.value]
        }
    }
}



