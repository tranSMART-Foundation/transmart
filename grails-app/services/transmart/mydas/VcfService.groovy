package transmart.mydas

import org.transmartproject.core.dataquery.DataQueryResource
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException
import uk.ac.ebi.mydas.extendedmodel.DasMethodE
import uk.ac.ebi.mydas.model.DasAnnotatedSegment
import uk.ac.ebi.mydas.model.DasFeature
import uk.ac.ebi.mydas.model.DasFeatureOrientation
import uk.ac.ebi.mydas.model.DasPhase
import uk.ac.ebi.mydas.model.DasType
import uk.ac.ebi.mydas.model.Range
import org.transmartproject.core.dataquery.vcf.VcfValues

/**
 *
 * Created by rnugraha on 26-09-13.
 */
class VcfService extends AbstractTransmartDasService {

    //TODO Choose correct cvId(3-d parameter) from http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=SO
    private def vcfMethod = new DasMethodE('vcf', 'vcf', 'vcf-cv-id')
    private static final String NA = 'n/a'
    String vcfVersion = '0.1'

    DataQueryResource dataQueryResourceNoGormService

    /**
     * Retrieve features
     * @return
     * @throws UnimplementedFeatureException
     */
    List<DasAnnotatedSegment> getCohortMAF(Long resultInstanceId, String conceptKey,
                                             Collection<String> segmentIds = [],
                                             Integer maxbins = null,
                                             Range range = null ) throws UnimplementedFeatureException {

        def query = createHighDimensionalQuery(resultInstanceId, conceptKey, segmentIds, range)
        def deVariantSubjectDetails =  dataQueryResourceNoGormService.getCohortMaf(query)
        def featuresPerSegment = constructSegmentFeaturesMap(deVariantSubjectDetails, getCohortMafFeature)
        segmentIds.collect { new DasAnnotatedSegment(it, range?.getFrom(), range?.getTo(), vcfVersion, it, featuresPerSegment[it] ?: []) }
    }

    /**
     * Retrieve summary level of Minor Alele Frequency
     * @param segmentIds
     * @param maxbins
     * @param range
     * @return
     */
    List<DasAnnotatedSegment> getSummaryMAF(long resultInstanceId, String conceptKey, Collection<String> segmentIds = [],
                                            Integer maxbins = null,
                                            Range range = null) {
        def query = createHighDimensionalQuery(resultInstanceId, conceptKey, segmentIds, range)
        def deVariantSubjectDetails =  dataQueryResourceNoGormService.getSummaryMaf(query)
        def featuresPerSegment = constructSegmentFeaturesMap(deVariantSubjectDetails, getSummaryMafFeature)
        segmentIds.collect { new DasAnnotatedSegment(it, range?.getFrom(), range?.getTo(), vcfVersion, it, featuresPerSegment[it] ?: []) }
    }

    List<DasAnnotatedSegment> getQualityByDepth(long resultInstanceId, String conceptKey, Collection<String> segmentIds = [],
                                            Integer maxbins = null,
                                            Range range = null) {
        def query = createHighDimensionalQuery(resultInstanceId, conceptKey, segmentIds, range)
        def deVariantSubjectDetails =  dataQueryResourceNoGormService.getSummaryMaf(query)
        def featuresPerSegment = constructSegmentFeaturesMap(deVariantSubjectDetails, getQDFeature)
        segmentIds.collect { new DasAnnotatedSegment(it, range?.getFrom(), range?.getTo(), vcfVersion, it, featuresPerSegment[it] ?: []) }
    }

    List<DasAnnotatedSegment> getGenomicVariants(long resultInstanceId, String conceptKey,
                                                 Collection<String> segmentIds = [],
                                                 Integer maxbins = null,
                                                 Range range = null) {

        def query = createHighDimensionalQuery(resultInstanceId, conceptKey, segmentIds, range)
        def deVariantSubjectDetails =  dataQueryResourceNoGormService.getCohortMaf(query)
        def featuresPerSegment = constructSegmentFeaturesMap(deVariantSubjectDetails, getGenomicTypeFeature)
        segmentIds.collect { new DasAnnotatedSegment(it, range?.getFrom(), range?.getTo(), vcfVersion, it, featuresPerSegment[it] ?: []) }
    }

    private def constructSegmentFeaturesMap(List<VcfValues> deVariantSubjectDetails, Closure featureCreationClosure) {
        Map<String, List<DasFeature>> featuresPerSegment = [:]

        deVariantSubjectDetails.each {
            if (!featuresPerSegment[it.chromosome]) {
                featuresPerSegment[it.chromosome] = []
            }

            featuresPerSegment[it.chromosome].addAll(featureCreationClosure(it))
        }

        featuresPerSegment
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
                        vcfMethod,
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
                        "AlleleFrequency=${val.additionalInfo['AF'] ?: NA}",
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

    private def getSummaryMafFeature = { VcfValues val ->
        if (!val.maf || val.maf <= 0) {
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
                vcfMethod,
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
                        "AlleleCount=${val.additionalInfo['AC'] ?: NA}",
                        "AlleleFrequency=${val.additionalInfo['AF'] ?: NA}",
                        "TotalAllele=${val.additionalInfo['AN'] ?: NA}",
                        "BaseQRankSum=${val.additionalInfo['BaseQRankSum'] ?: NA}",
                        "MQRankSum=${val.additionalInfo['MQRankSum'] ?: NA}",
                        "dbSNPMembership=${val.additionalInfo['DB'] ?: 'No'}",
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

    private def getQDFeature = { VcfValues val ->

        def linkMap = val.rsId == '.' ? [:]
        : [(new URL("http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=${val.rsId}")): 'NCBI SNP Ref']

        [new DasFeature(
                // feature id - any unique id that represent this feature
                "qd-${val.rsId}",
                // feature label
                'Quality of Depth',
                // das type
                new DasType('qd', "", "", ""),
                // das method TODO: pls find out what is actually means
                vcfMethod,
                // start pos
                val.position.toInteger(),
                // end pos
                val.position.toInteger(),
                // value - this is where Minor Allele Freq (MAF) value is placed
                val.qualityOfDepth,
                DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE,
                DasPhase.PHASE_NOT_APPLICABLE,
                //notes
                ["RefSNP=${val.rsId}",
                        "REF=${val.referenceAllele}",
                        "ALT=${val.alternativeAlleles.join(',')}",
                        "AlleleCount=${val.additionalInfo['AC'] ?: NA}",
                        "AlleleFrequency=${val.additionalInfo['AF'] ?: NA}",
                        "TotalAllele=${val.additionalInfo['AN'] ?: NA}",
                        "BaseQRankSum=${val.additionalInfo['BaseQRankSum'] ?: NA}",
                        "MQRankSum=${val.additionalInfo['MQRankSum'] ?: NA}",
                        "dbSNPMembership=${val.additionalInfo['DB'] ?: 'No'}",
                        "VariantClassification=${val.additionalInfo['VC'] ?: NA}",
                        "MAF=${val.maf ? String.format('%.2f', val.maf) : NA}",
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

    private def getGenomicTypeFeature = { VcfValues val ->

        def linkMap = val.rsId == '.' ? [:]
        : [(new URL("http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?rs=${val.rsId}")): 'NCBI SNP Ref']

        def results = []
        val.genomicVariantTypes.eachWithIndex { genomicVariantType, indx ->
            results << new DasFeature(
                    // feature id - any unique id that represent this feature
                    "gv-${val.rsId}-$genomicVariantType",
                    // feature label
                    'Genomic Variant Type',
                    // das type
                    new DasType(genomicVariantType.toString(), "", "", ""),
                    // das method TODO: pls find out what is actually means
                    vcfMethod,
                    // start pos
                    val.position.toInteger(),
                    // end pos
                    val.position.toInteger(),
                    // value - this is where Minor Allele Freq (MAF) value is placed
                    null,
                    DasFeatureOrientation.ORIENTATION_NOT_APPLICABLE,
                    DasPhase.PHASE_NOT_APPLICABLE,
                    //notes
                    ["RefSNP=${val.rsId}",
                            "REF=${val.referenceAllele}",
                            "ALT=${val.alternativeAlleles.join(',')}",
                            "CurrentALT=${val.alternativeAlleles[indx]}",
                            "AlleleCount=${val.additionalInfo['AC'] ?: NA}",
                            "AlleleFrequency=${val.additionalInfo['AF'] ?: NA}",
                            "TotalAllele=${val.additionalInfo['AN'] ?: NA}",
                            "VariantClassification=${val.additionalInfo['VC'] ?: NA}",
                            "MAF=${val.maf ? String.format('%.2f', val.maf) : NA}",
                            "QualityOfDepth=${val.qualityOfDepth ?: NA}"]*.toString(),
                    //links
                    linkMap,
                        //targets
                        [],
                        //parents
                        [],
                        //parts
                        []
                )
        }

        results
    }

}
