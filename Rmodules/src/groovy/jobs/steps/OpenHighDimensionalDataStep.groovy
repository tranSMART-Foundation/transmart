package jobs.steps

import groovy.util.logging.Slf4j
import jobs.UserParameters
import jobs.misc.AnalysisConstraints
import jobs.misc.Hacks
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.dataconstraints.DataConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection

@Slf4j('logger')
class OpenHighDimensionalDataStep implements Step {

    final String statusName = 'Gathering Data'

    /* in */
    UserParameters params
    HighDimensionDataTypeResource dataTypeResource
    AnalysisConstraints analysisConstraints

    /* out */
    Map<List<String>, TabularResult> results = [:]

    void execute() {
        try {
            List<String> ontologyTerms = extractOntologyTerms()
            extractPatientSets().eachWithIndex { int resultInstanceId, int index ->
 		for (ontologyTerm in ontologyTerms) {
                    String seriesLabel = ontologyTerm.split('\\\\')[-1]
                    List<String> keyList = ['S' + (index + 1), seriesLabel]
//		    logger.info 'execute results[{}] = fetchSubset({},{})', keyList, resultInstanceId, ontologyTerm
                    results[keyList] = fetchSubset(resultInstanceId, ontologyTerm)
                }
            }
        }
        catch(e) {
//	    logger.info 'execute Exception {}', e
	    for (it in results.values()) {
		it.close()
	    }
	    throw e
        }
    }

    private List<String> extractOntologyTerms() {
        analysisConstraints.assayConstraints.remove('ontology_term').collect {
            Hacks.createConceptKeyFrom it.term
        }
    }

    private List<Integer> extractPatientSets() {
        analysisConstraints.assayConstraints.remove('patient_set').grep()
    }

    private TabularResult fetchSubset(int patientSetId, String ontologyTerm) {

//	logger.info 'fetchSubset patientSetId {} ontologyTerm {}', patientSetId, ontologyTerm

        List<DataConstraint> dataConstraints = analysisConstraints['dataConstraints'].collect { String constraintType, values ->
            if (values) {
//		logger.info 'fetchSubset pdataConstraint {} -> {}', constraintType, values
                dataTypeResource.createDataConstraint(values, constraintType)
            }
        }.grep()

        List<AssayConstraint> assayConstraints = analysisConstraints['assayConstraints'].collect { String constraintType, values ->
            if (values) {
//		logger.info 'fetchSubset passayConstraint {} -> {}', constraintType, values
                dataTypeResource.createAssayConstraint(values, constraintType)
            }
        }.grep()

//	logger.info 'fetchSubset add assayConstaint PATIENT_SET_CONSTRAINT rid: {}', patientSetId
        assayConstraints << dataTypeResource.createAssayConstraint(
            AssayConstraint.PATIENT_SET_CONSTRAINT,
            result_instance_id: patientSetId)

//	logger.info 'fetchSubset add assayConstaint ONTOLOGY_TERM_CONSTRAINT concept_key {}', ontologyTerm
        assayConstraints << dataTypeResource.createAssayConstraint(
            AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
            concept_key: ontologyTerm)

        Projection projection = dataTypeResource.createProjection([:], analysisConstraints['projections'][0])

//	logger.info 'fetchSub calling retrieveData'

        dataTypeResource.retrieveData assayConstraints, dataConstraints, projection
    }
}
