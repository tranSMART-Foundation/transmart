package transmart.mydas

import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.vcf.VcfValues
import uk.ac.ebi.mydas.model.DasFeature
import uk.ac.ebi.mydas.model.DasFeatureOrientation
import uk.ac.ebi.mydas.model.DasPhase
import uk.ac.ebi.mydas.model.DasType

import javax.annotation.PostConstruct

/**
 * Created by j.hudecek on 18-3-14.
 */
class VcfInfoService  extends  VcfServiceAbstract {

    @PostConstruct
    void init() {
        super.init()
        dasTypes = [projectionName];
    }

    @Override
    protected void getSpecificFeatures(RegionRow region, Object assays,  Map<String, String> params, Collection<DasType> dasTypes, Map<String, List<DasFeature>> featuresPerSegment) {
        if (!featuresPerSegment[region.chromosome]) {
            featuresPerSegment[region.chromosome] = []
        }

        featuresPerSegment[region.chromosome].addAll(getInfoAndFeature(region, params['infoField']))
    }

    private def getInfoAndFeature = { VcfValues val, String infoField ->
        def infoFieldValue = val.infoFields[infoField]
        if (null == infoFieldValue) {
            return []
        }

        def linkMap = val.rsId == '.' ? [:]
                : [(new URL("http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=${val.rsId}")): 'NCBI SNP Ref']

        [new DasFeature(
                // feature id - any unique id that represent this feature
                "vcfInfo-${val.position}",
                // feature label
                'VCF Info Field',
                // das type
                new DasType('vcfInfo', "", "", ""),
                // das method TODO: pls find out what is actually means
                dasMethod,
                // start pos
                val.position.toInteger(),
                // end pos
                val.position.toInteger(),
                // value - this is where we place the value from the info field
                infoFieldValue as double,
                DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE,
                DasPhase.PHASE_NOT_APPLICABLE,
                //notes
                getCommonNotes(val),
                //links
                linkMap,
                //targets
                [],
                //parents
                [],
                //parts
                []
        )]
    }
}
