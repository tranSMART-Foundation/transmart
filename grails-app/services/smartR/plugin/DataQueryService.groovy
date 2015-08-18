package smartR.plugin

import org.transmartproject.rest.marshallers.ObservationWrapper
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.dataquery.clinical.*
import org.transmartproject.core.dataquery.TabularResult


class DataQueryService {

    def studiesResourceService
    def conceptsResourceService
    def clinicalDataResourceService
    def highDimExportService

    /**
    *   This method can be considered the main method of this class
    *   It will collect all possible low dimensional data for every given concept.
    *   Also it does a filtering based on the given patient ids.
    */
    def getAllData(conceptKeys, patientIDs) {
        def data = []
        conceptKeys.each { conceptKey ->
            def concept = conceptsResourceService.getByKey(conceptKey)
            def study = studiesResourceService.getStudyById(concept.studyId)

            def observations
            def wrappedObservations
            try {
                observations = clinicalDataResourceService.retrieveData(
                    concept.patients.toSet(),
                    [createClinicalVariable(concept)])
                wrappedObservations = wrapObservations(observations)
            } finally {
                observations.close()
            }

            def values = wrappedObservations
                .findAll { it.subject.id in patientIDs }
                .collect { it.value }
            def ids = concept.patients
                .findAll { it.id in patientIDs }
                .collect { it.id }

            assert values.size() == ids.size()

            [values, ids].transpose().each { value, id ->
                data << [patientID: id, concept: conceptKey, value: value]
            }
        }
        return data
    }

    def getHighDimData(conceptPaths, resultInstanceId, studyDir, format, dataType, jobName) {
        def map = [:]
        map.conceptPaths = conceptPaths
        map.resultInstanceId = resultInstanceId
        map.studyDir = studyDir
        map.format = format
        map.dataType = dataType
        map.jobName = jobName
        highDimExportService.exportHighDimData(map)
    }

    private ClinicalVariable createClinicalVariable(OntologyTerm term) {
        clinicalDataResourceService.createClinicalVariable(
                ClinicalVariable.NORMALIZED_LEAFS_VARIABLE,
                concept_path: term.fullName)
    }

    private static List<ObservationWrapper> wrapObservations(
        TabularResult<ClinicalVariable, PatientRow> tabularResult) {
        List<ObservationWrapper> observations = []
        def variableColumns = tabularResult.getIndicesList()
        tabularResult.getRows().each { row ->
            variableColumns.each { ClinicalVariableColumn topVar ->
                def value = row.getAt(topVar)

                if (value instanceof Map) {
                    value.each { ClinicalVariableColumn var, Object obj ->
                        observations << new ObservationWrapper(
                                subject: row.patient,
                                label: var.label,
                                value: obj)
                    }
                } else {
                    observations << new ObservationWrapper(
                            subject: row.patient,
                            label: topVar.label,
                            value: value)
                }
            }
        }
        return observations
    }
}
