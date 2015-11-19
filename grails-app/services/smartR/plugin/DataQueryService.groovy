package smartR.plugin

import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.dataquery.clinical.*
import org.transmartproject.core.dataquery.TabularResult
import groovy.sql.Sql

class DataQueryService {

    def studiesResourceService
    def conceptsResourceService
    def clinicalDataResourceService
    def dataSource
    def i2b2HelperService

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

    def exportHighDimData(conceptKeys, patientIDs, resultInstanceId) {
        def data = [PATIENTID: [], VALUE: [], PROBE: [], GENESYMBOL: []]
        def query = 
        """
        SELECT
            ssm.patient_id,
            ma.probe_id,
            ma.gene_symbol,
            smd.raw_intensity
        FROM
            deapp.de_subject_microarray_data smd
            INNER JOIN
                deapp.de_subject_sample_mapping ssm
            ON
                smd.assay_id = ssm.assay_id
            AND
                ssm.patient_id in (${patientIDs.collect { '?' }.join(',')})
            INNER JOIN
                deapp.de_mrna_annotation ma
            ON 
                smd.probeset_id = ma.probeset_id
            INNER JOIN
                deapp.de_gpl_info gi
            ON
                ssm.gpl_id = gi.platform
        WHERE
            ssm.concept_code = ?
        AND
            gi.marker_type = 'Gene Expression'
        """
        assert conceptKeys.size() == 1 // for now we support only one HDD node per concept 
        def conceptCode = i2b2HelperService.getConceptCodeFromKey(conceptKeys[0])
        def params = patientIDs
        params << conceptCode
        
        def sql = new Sql(dataSource.connection)
        try {
            sql.eachRow(query, params, { row ->
                data.PATIENTID << row.patient_id
                data.VALUE << row.raw_intensity
                data.PROBE << row.probe_id
                data.GENESYMBOL << row.gene_symbol
            })
        } finally {
            sql.close()
        }
        return data
    }

    private ClinicalVariable createClinicalVariable(OntologyTerm term) {
        clinicalDataResourceService.createClinicalVariable(
                ClinicalVariable.NORMALIZED_LEAFS_VARIABLE,
                concept_path: term.fullName)
    }

    private static List<Map> wrapObservations(
        TabularResult<ClinicalVariable, PatientRow> tabularResult) {
        List<Map> observations = []
        def variableColumns = tabularResult.getIndicesList()
        tabularResult.getRows().each { row ->
            variableColumns.each { ClinicalVariableColumn topVar ->
                def value = row.getAt(topVar)

                if (value instanceof Map) {
                    value.each { ClinicalVariableColumn var, Object obj ->
                        observations << [
                                subject: row.patient,
                                label: var.label,
                                value: obj
                                ]
                    }
                } else {
                    observations << [
                            subject: row.patient,
                            label: topVar.label,
                            value: value
                    ]
                }
            }
        }
        return observations
    }
}
