package transmart.mydas

import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.vcf.VcfValues
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import uk.ac.ebi.mydas.exceptions.DataSourceException
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
import uk.ac.ebi.mydas.extendedmodel.DasMethodE
import uk.ac.ebi.mydas.model.DasAnnotatedSegment
import uk.ac.ebi.mydas.model.DasFeature
import uk.ac.ebi.mydas.model.DasFeatureOrientation
import uk.ac.ebi.mydas.model.DasPhase
import uk.ac.ebi.mydas.model.DasType
import org.transmartproject.db.dataquery.highdim.vcf.*

import javax.annotation.PostConstruct

/**
 * Created by j.hudecek on 3-2-14.
 */
class CohortMAFService  extends  VcfServiceAbstract {

    @PostConstruct
    void init() {
        super.init()
        dasTypes = [projectionName];
    }

    @Override
    protected void getSpecificFeatures(RegionRow region, Object assays,  Map<String, String> params, Collection<DasType> dasTypes, Map<String, List<DasFeature>> featuresPerSegment) {
       constructSegmentFeaturesMap([region], getCohortMafFeature, featuresPerSegment)
    }

    private def getCohortMafFeature = { VcfValues val ->
        if (!val.maf || val.maf <= 0) {
            return []
        }

        def linkMap = val.rsId == '.' ? [:]
                : [(new URL("http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=${val.rsId}")): 'NCBI SNP Ref']

        [new DasFeature(
                // feature id - any unique id that represent this feature
                "maf-${val.rsId}",
                // feature label
                'Cohort Minor Allele Frequency',
                // das type
                new DasType('maf', "", "", ""),
                // das method TODO: pls find out what is actually means
                dasMethod,
                // start pos
                val.position.toInteger(),
                // end pos
                val.position.toInteger(),
                // value - this is where Minor Allele Freq (MAF) value is placed
                val.maf,
                DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE,
                DasPhase.PHASE_NOT_APPLICABLE,
                //notes
                ["RefSNP=${val.rsId}",
                        "REF=${val.referenceAllele}",
                        "ALT=${val.alternativeAlleles.join(',')}",
                        "MafAllele=${val.mafAllele}",
                        "AlleleCount=${val.additionalInfo['AC'] ?: NA}",
                        "AlleleFrequency=${val.maf}",
                        "TotalAllele=${val.additionalInfo['AN'] ?: NA}",
                        "VariantClassification=${val.additionalInfo['VC'] ?: NA}",
                        "QualityOfDepth=${val.qualityOfDepth ?: NA}",
                        "GenomicVariantTypes=${val.genomicVariantTypes.join(',')}"]*.toString(),
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
