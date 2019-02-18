package jobs.steps

import au.com.bytecode.opencsv.CSVWriter
import jobs.UserParameters
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn

abstract class AbstractDumpHighDimensionalDataStep extends AbstractDumpStep {

    final String statusName = 'Dumping high dimensional data'

    /* true if computeCsvRow is to be called once per (row, column),
       false to called only once per row */
    boolean callPerColumn = true

    File temporaryDirectory
    Closure<Map<List<String>, TabularResult>> resultsHolder
    UserParameters params

    Map<List<String>, TabularResult> getResults() {
        resultsHolder()
    }

    void execute() {
        try {
            writeDefaultCsv results, csvHeader
        }
        finally {
            results.values().each { it?.close() }
        }
    }

    protected abstract computeCsvRow(String subsetName, String seriesName, DataRow row, AssayColumn column, cell)

    abstract List<String> getCsvHeader()

    protected String getRowKey(String subsetName, String seriesName, String patientId) {
        if (params.doGroupBySubject == 'true') {
            return [subsetName, patientId, seriesName].join('_')
        }
	else {
            return [subsetName, seriesName, patientId].join('_')
	}
    }

    private void withDefaultCsvWriter(Closure constructFile) {
        File output = new File(temporaryDirectory, outputFileName)
        output.createNewFile()
        output.withWriter { Writer writer ->
            constructFile(new CSVWriter(writer, '\t' as char))
        }
    }

    /* nextRow is a closure with this signature:
     * (String subsetName, DataRow row, Long rowNumber, AssayColumn column, Object cell) -> List<Object> csv row
     */
    private void writeDefaultCsv(Map<List<String>, TabularResult<AssayColumn, DataRow<AssayColumn, Object>>> results,
                                 List<String> header) {

        withDefaultCsvWriter { CSVWriter csvWriter ->
            csvWriter.writeNext(header as String[])

            for (key in results.keySet()) {
                doSubset key, csvWriter
            }
        }
    }

    private void doSubset(List<String> resultsKey, CSVWriter csvWriter) {

        TabularResult tabularResult = results[resultsKey]
        if (!tabularResult) {
            return
        }

        String subsetName = resultsKey[0]
        String seriesName = resultsKey[1]

        List<AssayColumn> assayColumns = tabularResult.indicesList

        for (DataRow row in tabularResult) {
            if (callPerColumn) {
                for(AssayColumn assay in assayColumns) {
                    if (row[assay] != null) {
			csvWriter.writeNext(computeCsvRow(subsetName, seriesName, row, assay, row[assay]) as String[])
                    }
		}
	    }
	    else {
		csvWriter.writeNext(computeCsvRow(subsetName, seriesName, row, null, null) as String[])
            }
        }
    }
}
