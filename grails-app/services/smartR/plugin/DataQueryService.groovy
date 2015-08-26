package smartR.plugin

import org.transmartproject.rest.marshallers.ObservationWrapper
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.dataquery.clinical.*
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection


class DataQueryService {

    def studiesResourceService
    def conceptsResourceService
    def clinicalDataResourceService
    def highDimensionResourceService
    def highDimExporterRegistry

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

    def exportHighDimData(conceptKeys, resultInstanceId, studyDir, format, dataType) {
        def fileNames = []
        conceptKeys.eachWithIndex { conceptKey, idx ->
            def file = exportForSingleNode(
                    conceptKey,
                    resultInstanceId,
                    studyDir,
                    format,
                    dataType,
                    idx)

            if (file) {
                fileNames << file.absolutePath
            }
        }

        return fileNames
    }

    private File exportForSingleNode(String conceptKey, Long resultInstanceId, File studyDir, String format, String dataType, Integer index) {

        HighDimensionDataTypeResource dataTypeResource = highDimensionResourceService.getSubResourceForType(dataType)

        def assayConstraints = []

        assayConstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.PATIENT_SET_CONSTRAINT,
                result_instance_id: resultInstanceId)

        assayConstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
                concept_key: conceptKey)

        // Setup class to export the data
        def exporter = highDimExporterRegistry.getExporterForFormat(format)
        Projection projection = dataTypeResource.createProjection(exporter.projection)

        // Retrieve the data itself
        TabularResult<AssayColumn, DataRow<Map<String, String>>> tabularResult =
                dataTypeResource.retrieveData(assayConstraints, [], projection)

        File outputFile = new File(studyDir,
                "${dataType}_${makeFileNameFromConceptPath(conceptKey)}_${index}.${format.toLowerCase()}")

        try {
            outputFile.withOutputStream { outputStream ->
                exporter.export tabularResult, projection, outputStream, { false }
            }
        } catch (RuntimeException e) {
            log.error('Data export to the file has thrown an exception', e)
        } finally {
            tabularResult.close()
        }

        outputFile
    }

    private String makeFileNameFromConceptPath(String conceptPath) {
        conceptPath
                .split('\\\\')
                .reverse()[0..1]
                .join('_')
                .replaceAll('[\\W_]+', '_')
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
