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
        resource = highDimensionResourceService.getSubResourceForType 'cohortMAF'
        //TODO Choose correct cvId(3-d parameter) from http://www.ebi.ac.uk/ontology-lookup/browse.do?ontName=SO
        projectionName = 'cohortMAF_values'
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
}
