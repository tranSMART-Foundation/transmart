package com.recomdata.transmart.data.export

import au.com.bytecode.opencsv.CSVWriter
import com.google.common.collect.Iterators
import com.google.common.collect.PeekingIterator
import com.recomdata.asynchronous.JobResultsService
import org.transmartproject.core.concept.ConceptKey
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.clinical.ClinicalVariableColumn
import org.transmartproject.core.dataquery.clinical.ComposedVariable
import org.transmartproject.core.dataquery.clinical.PatientRow
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.OntologyTermTag
import org.transmartproject.core.ontology.Study
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.db.clinical.ClinicalDataResourceService
import org.transmartproject.db.ontology.OntologyTermTagsResourceService
import org.transmartproject.db.ontology.StudiesResourceService
import org.transmartproject.db.querytool.QueriesResourceService

import static org.transmartproject.core.dataquery.clinical.ClinicalVariable.NORMALIZED_LEAFS_VARIABLE

class ClinicalExportService {

    static transactional = false

    static final String DATA_FILE_NAME = 'data_clinical.tsv'
    static final String META_FILE_NAME = 'meta.tsv'
    static final String SUBJ_ID_TITLE = 'Subject ID'
    static final char COLUMN_SEPARATOR = '\t' as char
    static final List<String> META_FILE_HEADER = ['Variable', 'Attribute', 'Description'].asImmutable()

    ClinicalDataResourceService clinicalDataResourceService
    ConceptsResource conceptsResourceService
    JobResultsService jobResultsService
    OntologyTermTagsResourceService ontologyTermTagsResourceService
    QueriesResourceService queriesResourceService
    StudiesResourceService studiesResourceService

    List<File> exportClinicalData(String jobName, Long resultInstanceId, List<String> conceptKeys,
	                          File studyDir, boolean exportMetaData = true) {
        if (jobResultsService.isJobCancelled(jobName)) {
            return null
        }

        QueryResult queryResult = queriesResourceService.getQueryResultFromId(resultInstanceId)
        List<ComposedVariable> variables
        if (conceptKeys) {
            variables = createClinicalVariablesForConceptKeys(conceptKeys)
	}
	else {
	    variables = createClinicalVariablesForStudies(getQueriedStudies(queryResult))
        }

	List<File> files = []

        files << exportClinicalDataToFile(queryResult, variables, studyDir, jobName)
        if (exportMetaData) {
	    Set<OntologyTerm> terms = getRelatedOntologyTerms(variables)
	    File tagsFile = exportAllTags(terms, studyDir)
            if (tagsFile) {
                files << tagsFile
            }
        }

        files
    }

    private File exportClinicalDataToFile(QueryResult queryResult, List<ComposedVariable> variables,
	                                  File studyDir, String jobName) {

        TabularResult<ClinicalVariableColumn, PatientRow> tabularResult =
            clinicalDataResourceService.retrieveData(queryResult, variables)

        try {
	    writeToFile tabularResult, variables, studyDir, jobName
	}
	finally {
            tabularResult.close()
        }
    }

    private File writeToFile(TabularResult<ClinicalVariableColumn, PatientRow> tabularResult,
	                     List<ComposedVariable> variables, File studyDir, String jobName) {

	PeekingIterator<PatientRow> peekingIterator = Iterators.peekingIterator(tabularResult.iterator())

        File clinicalDataFile = new File(studyDir, DATA_FILE_NAME)
        clinicalDataFile.withWriter { Writer writer ->
            CSVWriter csvWriter = new CSVWriter(writer, COLUMN_SEPARATOR)

	    PatientRow firstRow = peekingIterator.peek()
	    List<String> headRowList = [SUBJ_ID_TITLE] +
                variables.collectMany { ComposedVariable var ->
                firstRow[var].collect { it.key.label }
            }

            csvWriter.writeNext(headRowList as String[])

            while (peekingIterator.hasNext()) {
                if (jobResultsService.isJobCancelled(jobName)) {
                    return null
                }
		PatientRow row = peekingIterator.next()
		List<String> rowList = [row.patient.inTrialId] +
                    variables.collectMany { ComposedVariable var ->
                    row[var].values()
                }

                csvWriter.writeNext(rowList as String[])
            }
        }

        clinicalDataFile
    }

    File exportAllTags(Set<OntologyTerm> terms, File studyDir) {
	Map<OntologyTerm, List<OntologyTermTag>> tagsMap = ontologyTermTagsResourceService.getTags(terms, true)
        if (tagsMap) {
	    File resultFile = new File(studyDir, META_FILE_NAME)

            resultFile.withWriter { Writer writer ->
                CSVWriter csvWriter = new CSVWriter(writer, COLUMN_SEPARATOR)
                csvWriter.writeNext(META_FILE_HEADER as String[])
                tagsMap.each { OntologyTerm keyTerm, List<OntologyTermTag> valueTags ->
		    for (OntologyTermTag tag in valueTags) {
                        csvWriter.writeNext([keyTerm.fullName, tag.name, tag.description] as String[])
                    }
                }
            }

            resultFile
        }
    }

    private Set<OntologyTerm> getRelatedOntologyTerms(List<ComposedVariable> variables) {
        variables.collect { ComposedVariable variable ->
            conceptsResourceService.getByKey(variable.key.toString())
        } as Set
    }

    private Collection<ComposedVariable> createClinicalVariablesForConceptKeys(Collection<String> conceptKeys) {
        conceptKeys.collectAll {
            clinicalDataResourceService.createClinicalVariable(
                NORMALIZED_LEAFS_VARIABLE,
		concept_path: new ConceptKey(it).conceptFullName.toString())
        }
    }

    private Collection<ComposedVariable> createClinicalVariablesForStudies(Set<Study> queriedStudies) {
        queriedStudies.collect { Study study ->
            clinicalDataResourceService.createClinicalVariable(
                NORMALIZED_LEAFS_VARIABLE,
                concept_path: study.ontologyTerm.fullName)
        }
    }

    private Set<Study> getQueriedStudies(QueryResult queryResult) {
	Set<String> trials = queryResult.patients*.trial as Set
	trials.collect { String trialId -> studiesResourceService.getStudyById(trialId) } as Set
    }
}
