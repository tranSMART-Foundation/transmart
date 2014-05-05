package transmart.mydas

import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.vcf.VcfValues
import uk.ac.ebi.mydas.model.DasFeature
import uk.ac.ebi.mydas.model.DasFeatureOrientation
import uk.ac.ebi.mydas.model.DasPhase
import uk.ac.ebi.mydas.model.DasType

import javax.annotation.PostConstruct

/**
 * Created by j.hudecek on 5-3-14.
 */
class SummaryMAFService extends  VcfServiceAbstract {

    @PostConstruct
    void init() {
        super.init()
        dasTypes = [projectionName];
    }

    @Override
    protected void getSpecificFeatures(RegionRow region, Object assays,  Map<String, String> params, Collection<DasType> dasTypes, Map<String, List<DasFeature>> featuresPerSegment) {
        constructSegmentFeaturesMap([region], getSummaryMafFeature, featuresPerSegment)
    }

    private def getSummaryMafFeature = { VcfValues val ->
        def maf = val?.cohortInfo?.minorAlleleFrequency
        if (!maf || maf <= 0) {
            return []
        }

        def linkMap = val.rsId == '.' ? [:]
                : [(new URL("http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=${val.rsId}")): 'NCBI SNP Ref']

        [new DasFeature(
                // feature id - any unique id that represent this feature
                "smaf-${val.rsId}",
                // feature label
                'Minor Allele Frequency',
                // das type
                new DasType('smaf', "", "", ""),
                // das method TODO: pls find out what is actually means
                dasMethod,
                // start pos
                val.position.toInteger(),
                // end pos
                val.position.toInteger(),
                // value - this is where Minor Allele Freq (MAF) value is placed
                (val.infoFields['AF'] ?: '0') as double,
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
