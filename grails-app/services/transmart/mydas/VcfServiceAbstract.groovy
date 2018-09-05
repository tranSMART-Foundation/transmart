package transmart.mydas

import uk.ac.ebi.mydas.extendedmodel.DasMethodE
import uk.ac.ebi.mydas.model.DasFeature
import uk.ac.ebi.mydas.model.DasType
import org.transmartproject.core.dataquery.highdim.vcf.VcfValues

import javax.annotation.PostConstruct

/**
 *
 * Created by jhudecek on 26-02-14.
 */
abstract class VcfServiceAbstract extends TransmartDasServiceAbstract {

    protected    static final String NA = 'n/a'

    @PostConstruct
    void init() {
        //TODO Choose correct cvId(3-d parameter) from http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=SO
        dasMethod = new DasMethodE('vcf', 'vcf', 'vcf-cv-id')
        version = '0.1'
        resource = highDimensionResourceService.getSubResourceForType 'vcf'
        
        //TODO Choose correct cvId(3-d parameter) from http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=SO
        projectionName = 'cohort'
    }

    @Override
    protected Collection<DasType> getTypes() {
        return null
    }

    protected def constructSegmentFeaturesMap(List<VcfValues> deVariantSubjectDetails, Closure featureCreationClosure, Map<String, List<DasFeature>> featuresPerSegment) {
        deVariantSubjectDetails.each {
            if (!featuresPerSegment[it.chromosome]) {
                featuresPerSegment[it.chromosome] = []
            }

            featuresPerSegment[it.chromosome].addAll(featureCreationClosure(it))
        }
    }
    
    protected List<String> getCommonNotes( VcfValues val ) {
        [
            "RefSNP=" + val.rsId,
            "REF=" + val.cohortInfo.referenceAllele,
            "ALT=" + val.cohortInfo.alternativeAlleles.join(','),
            "MafAllele=" + val.cohortInfo.minorAllele,
            "AlleleFrequency=" + String.format( '%.2f', val.cohortInfo.minorAlleleFrequency ),
            "AlleleCount=" + ( val.cohortInfo.alleleCount ?: NA ),
            "TotalAllele=" + ( val.cohortInfo.totalAlleleCount ?: NA ),
            "GenomicVariantTypes=" + val.cohortInfo.genomicVariantTypes.findAll().join(','),
            
            "VariantClassification=" + ( val.infoFields['VC'] ?: NA ),
            "QualityOfDepth=" + ( val.qualityOfDepth ?: NA ),
            
            "BaseQRankSum=" + ( val.infoFields['BaseQRankSum'] ?: NA ),
            "MQRankSum=" + ( val.infoFields['MQRankSum'] ?: NA ),
            "dbSNPMembership=" + ( val.infoFields['DB'] ?: "No" )
        ]
    }
}
