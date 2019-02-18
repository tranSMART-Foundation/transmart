package com.recomdata.transmart.data.association

import grails.converters.JSON
import jobs.misc.AnalysisConstraints
import jobs.misc.Hacks
//import jobs.steps.OpenHighDimensionalDataStep
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.acgh.CopyNumberState
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.dataconstraints.DataConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection

class GeneprintController {

    static final Map<String, String> projectionLookup = [
        mrna:     'zscore',
        acgh:     'acgh_values',
        protein:  'zscore',
        vcf:      Projection.ALL_DATA_PROJECTION
    ].asImmutable()

    List<Map> geneprintEntries = []

    @Autowired
    HighDimensionResource highDimensionResource

    def geneprintOut() {
        render template: '/plugin/geneprint_out', contextPath:pluginContextPath
    }

    def fetchGeneData() {
        // Debugging: generate all possible combinations of data
        //generateAllCombinations()
        //render (geneprintEntries as JSON)
        //return

        AnalysisConstraints analysisConstraints = RModulesController.createAnalysisConstraints(params)
        double mrnaThreshold = Double.parseDouble(analysisConstraints.mrnaThreshold)
        double proteinThreshold = Double.parseDouble(analysisConstraints.proteinThreshold)

        List<String> ontologyTerms = extractOntologyTerms(analysisConstraints)
        List<Integer> searchKeywordIds = extractSearchKeywordIds(analysisConstraints)
        TabularResult tabularResult

        Map<String, HighDimensionDataTypeResource> highDimDataTypeResourceCache = [:]
        for (ontologyTerm in ontologyTerms) {
            highDimDataTypeResourceCache[ontologyTerm] = getHighDimDataTypeResourceFromConcept(ontologyTerm)
        }

        try {
            for (resultInstanceId in extractPatientSets(analysisConstraints)) {
                for(searchKeywordId in searchKeywordIds) {
                    Map searchKeyword = [symbol: null]
                    highDimDataTypeResourceCache.each { String ontologyTerm, HighDimensionDataTypeResource dataTypeResource ->
                        tabularResult = fetchData(resultInstanceId, searchKeywordId, ontologyTerm,
                                dataTypeResource, analysisConstraints)
                        processResult tabularResult, searchKeyword, dataTypeResource.dataTypeName,
			    mrnaThreshold, proteinThreshold
                        tabularResult.close()
                    }
                }
            }
        }
        catch(e) {
            tabularResult?.close()
            throw e
        }

        render (geneprintEntries as JSON)
    }

    def fetchClinicalAttributes() {
        render '[]'
    }

    private void processResult(TabularResult tabularResult, searchKeyword, String dataType, double mrnaThreshold, double proteinThreshold) {
        List<AssayColumn> assayList = tabularResult.indicesList
        for (DataRow row in tabularResult) {
            if (searchKeyword.symbol == null) {
                searchKeyword.symbol = row.bioMarker
            }
            for (AssayColumn assayColumn in assayList) {

                Map geneprintEntry = getOrCreateGeneprintEntry(searchKeyword.symbol, assayColumn.patientInTrialId)

                def value = row[assayColumn]
                if (value == null) {
                    return
                }

                switch (dataType) {
                case 'mrna':
                    processMrna value, geneprintEntry, mrnaThreshold
                    break
                case 'acgh':
                    processAcgh value.copyNumberState, geneprintEntry
                    break
                case 'protein':
                    processProtein value, geneprintEntry, proteinThreshold
                    break
                case 'vcf':
                    processVcf value.reference, geneprintEntry
                    break
                }
            }
        }
    }

    private void processMrna(double value, Map geneprintEntry, double mrnaThreshold) {
        if (value > mrnaThreshold) {
            geneprintEntry.mrna = 'UPREGULATED'
        }
        if (value < -mrnaThreshold) {
            geneprintEntry.mrna = 'DOWNREGULATED'
        }
    }

    private void processAcgh(CopyNumberState value, Map geneprintEntry) {
        switch (value) {
        case CopyNumberState.LOSS:
            geneprintEntry.cna = 'LOSS'
            break
        case CopyNumberState.NORMAL:
            geneprintEntry.cna = 'DIPLOID'
            break
        case CopyNumberState.GAIN:
            geneprintEntry.cna = 'GAINED'
            break
        case CopyNumberState.AMPLIFICATION:
            geneprintEntry.cna = 'AMPLIFIED'
            break
        }
    }

    private void processProtein(double value, Map geneprintEntry, double proteinThreshold) {
        if (value > proteinThreshold) {
            geneprintEntry.rppa = 'UPREGULATED'
        }
        if (value < -proteinThreshold) {
            geneprintEntry.rppa = 'DOWNREGULATED'
        }
    }

    private void processVcf(boolean isReference, Map geneprintEntry) {
        if (!isReference) {
            // Any non-undefined value will flag it as a mutation
            // TODO: differentiate between types of mutation?
            geneprintEntry.mutation = '1'
        }
    }

    private void generateAllCombinations() {
        def mappings = [
                [key: 'mrna',
                 values: ['', 'UPREGULATED', 'DOWNREGULATED']],
                [key: 'cna',
                 values: ['', 'LOSS', 'DIPLOID', 'GAINED', 'AMPLIFIED']],
                [key: 'rppa',
                 values: ['', 'UPREGULATED', 'DOWNREGULATED']],
                [key: 'mutation',
                 values: ['', '1']]]

        int nPermutations = mappings.inject(1) { acc, val -> acc * val.values.size() }
        for (i in 0..nPermutations-1) {
            def entry = [gene: 'TEST', sample: ''+i]
            geneprintEntries << entry

            // Add each type of mapping that has a value.
            // The values are permutated based on the index.
            int d = 1
            mappings.each { mapping ->
                int n = mapping.values.size()
                def value = mapping.values[(i/d).intValue() % n]
                if (value != '') {
                    entry[mapping.key] = value
                }
                d *= n
            }
        }
    }

    private Map getOrCreateGeneprintEntry(String geneSymbol, String sampleId) {
        Map entry = geneprintEntries.find( {
            it.gene == geneSymbol && it.sample == sampleId
        })
        if (entry != null) {
            return entry
        }
        entry = [ gene: geneSymbol, sample: sampleId ]
        geneprintEntries << entry
        return entry
    }

    private HighDimensionDataTypeResource getHighDimDataTypeResourceFromConcept(String conceptKey) {
        List<AssayConstraint> constraints = []

        constraints << highDimensionResource.createAssayConstraint(
                AssayConstraint.DISJUNCTION_CONSTRAINT,
                subconstraints: [(AssayConstraint.ONTOLOGY_TERM_CONSTRAINT): [concept_key: conceptKey]])

	highDimensionResource.getSubResourcesAssayMultiMap(constraints).keySet()[0]
    }

    private List<String> extractOntologyTerms(AnalysisConstraints analysisConstraints) {
        analysisConstraints.assayConstraints.remove('ontology_term').collect {
            Hacks.createConceptKeyFrom it.term
        }
    }

    private List<Integer> extractSearchKeywordIds(AnalysisConstraints analysisConstraints) {
        analysisConstraints.dataConstraints.remove('search_keyword_ids').keyword_ids
    }

    private List<Integer> extractPatientSets(AnalysisConstraints analysisConstraints) {
        analysisConstraints.assayConstraints.remove('patient_set').grep()
    }

    private TabularResult fetchData(Integer patientSetId, String searchKeywordId, String ontologyTerm,
                                    HighDimensionDataTypeResource dataTypeResource,
				    AnalysisConstraints analysisConstraints) {

        List<DataConstraint> dataConstraints = analysisConstraints.dataConstraints.collect { String constraintType, values ->
                    if (values) {
                        dataTypeResource.createDataConstraint(values, constraintType)
                    }
                }.grep()

        dataConstraints << dataTypeResource.createDataConstraint(
	    DataConstraint.SEARCH_KEYWORD_IDS_CONSTRAINT, keyword_ids: [searchKeywordId])

        List<AssayConstraint> assayConstraints = analysisConstraints.assayConstraints.collect { String constraintType, values ->
                    if (values) {
                        dataTypeResource.createAssayConstraint(values, constraintType)
                    }
                }.grep()

        assayConstraints << dataTypeResource.createAssayConstraint(
                        AssayConstraint.PATIENT_SET_CONSTRAINT, result_instance_id: patientSetId)

        assayConstraints << dataTypeResource.createAssayConstraint(
                        AssayConstraint.ONTOLOGY_TERM_CONSTRAINT, concept_key: ontologyTerm)

        Projection projection = dataTypeResource.createProjection([:], projectionLookup[dataTypeResource.dataTypeName])

        dataTypeResource.retrieveData assayConstraints, dataConstraints, projection
    }

}

