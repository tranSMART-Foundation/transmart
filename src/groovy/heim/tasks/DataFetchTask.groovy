package heim.tasks

import au.com.bytecode.opencsv.CSVWriter
import com.google.common.base.Charsets
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Iterators
import heim.rserve.RServeSession
import heim.rserve.RUtil
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor
import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.RConnection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.transmartproject.core.concept.ConceptKey
import org.transmartproject.core.dataquery.DataColumn
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.clinical.ClinicalDataResource
import org.transmartproject.core.dataquery.clinical.ClinicalVariable
import org.transmartproject.core.dataquery.clinical.ClinicalVariableColumn
import org.transmartproject.core.dataquery.clinical.PatientRow
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.BioMarkerDataRow
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.querytool.QueriesResource

import static org.transmartproject.core.dataquery.highdim.projections.Projection.DEFAULT_REAL_PROJECTION
import static org.transmartproject.core.ontology.OntologyTerm.VisualAttributes.HIGH_DIMENSIONAL

@Component
@Scope('prototype')
class DataFetchTask extends AbstractTask {

    private static final char SEPARATOR = "\t" as char

    ConceptKey conceptKey

    String dataType

    Map<String, Map> assayConstraints

    Map<String, Map> dataConstraints

    String projection

    String label

    Long resultInstanceId

    @Autowired
    private ConceptsResource conceptsResource

    @Autowired
    private HighDimensionResource highDimensionResource

    @Autowired
    private ClinicalDataResource clinicalDataResource

    @Autowired
    private QueriesResource queriesResource

    @Autowired
    private RServeSession rServeSession // session scoped

    @Autowired
    private PersistenceContextInterceptor interceptor

    private TabularResult<?, ?> tabularResult

    @Override
    TaskResult call() throws Exception {
        interceptor.init()
        try {
            def ret = doCall()
            interceptor.flush()
            ret
        } finally {
            interceptor.destroy()
        }
    }

    private TaskResult doCall() throws Exception {
        def concept = conceptsResource.getByKey(conceptKey.toString())

        if (HIGH_DIMENSIONAL in concept.visualAttributes) {
            tabularResult = createHighDimensionalResult()
        } else {
            tabularResult = createClinicalDataResult()
        }

        String fileName = writeTabularResult()
        List<String> currentLabels = loadFile(fileName)
        new TaskResult(
                successful: true,
                artifacts: ImmutableMap.of('currentLabels', currentLabels),
        )
    }

    private String /* filename */ writeTabularResult() {
        String filename = UUID.randomUUID().toString()
        rServeSession.doWithRConnection { RConnection conn ->
            OutputStream os = conn.createFile(filename)
            Writer writer = new OutputStreamWriter(os, Charsets.UTF_8)

            def csvWriter = new CSVWriter(writer, SEPARATOR)

            try {
                Iterator<? extends DataRow> it =
                        Iterators.peekingIterator(tabularResult.iterator())
                boolean isBioMarker = it.peek().hasProperty('bioMarker')
                writeHeader(csvWriter, isBioMarker, tabularResult.indicesList)
                it.each { DataRow row ->
                    if (Thread.interrupted()) {
                        throw new InterruptedException(
                                'Thread was interrupted while dumping the ' +
                                        'TabularResult into a file')
                    }
                    writeLine csvWriter, isBioMarker, row
                }
            } finally {
                csvWriter.close()
            }
        }

        filename
    }

    private List<String> /* current labels */ loadFile(String filename) {
        def escapedFilename = RUtil.escapeRStringContent(filename)
        def escapedLabel = RUtil.escapeRStringContent(label)

        List<String> commands = [
                "if (!exists('loaded_variables')) { loaded_variables <- list() }",
                """
                loaded_variables <- c(
                        loaded_variables,
                        list('$escapedLabel' = read.csv(
                               '$escapedFilename', sep = "\t", header = TRUE)));
                names(loaded_variables)""",
        ]
        REXP rexp = rServeSession.doWithRConnection { RConnection conn ->
            RUtil.runRCommand conn, commands[0]
            RUtil.runRCommand conn, commands[1] /* return value */
        }
        rexp.asNativeJavaObject() as List
    }

    private static void writeHeader(CSVWriter writer,
                                    boolean isBioMarker,
                                    List<? extends DataColumn> columns) {
        List line = ['Row Label']
        if (isBioMarker) {
            line += 'Bio marker'
        }
        line += columns*.label

        writer.writeNext(line as String[])
    }

    private static void writeLine(CSVWriter writer, boolean isBioMarker, DataRow row) {
        List line = [row.label]
        if (isBioMarker) {
            line += ((BioMarkerDataRow) row).bioMarker
        }
        row.iterator().each {
            line += it
        }

        writer.writeNext(line as String[])
    }

    private TabularResult<AssayColumn, ?> createHighDimensionalResult() {
        if (!dataType) {
            throw new InvalidArgumentsException("High dimensional node, " +
                    "data type should have been given")
        }

        def subResource =
                highDimensionResource.getSubResourceForType(dataType)

        def builtAssayConstraints = [
                subResource.createAssayConstraint(
                        AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
                        concept_key: conceptKey.toString()),
        ]
        if (resultInstanceId) {
            builtAssayConstraints << subResource.createAssayConstraint(
                    AssayConstraint.PATIENT_SET_CONSTRAINT,
                    result_instance_id: resultInstanceId)
        }
        if (assayConstraints) {
            builtAssayConstraints +=
                    assayConstraints.collect { k, v ->
                        subResource.createAssayConstraint(v, k)
                    }
        }

        def builtDataConstraints = []
        if (dataConstraints) {
            builtDataConstraints +=
                    dataConstraints.collect { k, v ->
                        subResource.createDataConstraint(v, k)
                    }
        }

        def builtProjection = projection ?
                subResource.createProjection(projection) :
                subResource.createProjection(DEFAULT_REAL_PROJECTION)

        subResource.retrieveData(
                builtAssayConstraints,
                builtDataConstraints,
                builtProjection)
    }

    private TabularResult<ClinicalVariableColumn, PatientRow> createClinicalDataResult() {
        if (dataType) {
            throw new InvalidArgumentsException("Provided data type $dataType" +
                    ", but the concept key $conceptKey doesn't point ot a " +
                    "high dimensional node")
        }

        def clinicalVariable = clinicalDataResource.createClinicalVariable(
                ClinicalVariable.NORMALIZED_LEAFS_VARIABLE,
                concept_path: conceptKey.conceptFullName.toString())


        def queryResult = queriesResource.getQueryResultFromId(resultInstanceId)
        clinicalDataResource.retrieveData(
                queryResult,
                [clinicalVariable])
    }

    @Override
    void close() throws Exception {
        tabularResult?.close()
    }
}
