package jobs.steps

import au.com.bytecode.opencsv.CSVWriter
import jobs.table.Table
import org.transmartproject.core.exceptions.EmptySetException

class SimpleDumpTableResultStep extends AbstractDumpStep {

    Table table

    File temporaryDirectory

    final String statusName = 'Dumping Table Result'

    void execute() {
        try {
            withDefaultCsvWriter { CSVWriter writer ->
                writeHeader writer

                writeMeat writer
            }
        }
        finally {
            table.close()
        }
    }

    protected List<String> getHeaders() {
        table.headers
    }

    protected Iterator getMainRows() {
        table.result.iterator()
    }

    void writeHeader(CSVWriter writer) {
        writer.writeNext(headers as String[])
    }


    void writeMeat(CSVWriter writer) {
        Iterator rows = getMainRows()
        if (!rows.hasNext()) {
            throw new EmptySetException(
		'The result set is empty. Number of patients dropped owing to mismatched data: ' +
		    table.droppedRows)
        }

        rows.each {
            writer.writeNext(it as String[])
        }
    }

    private void withDefaultCsvWriter(Closure constructFile) {
        new File(temporaryDirectory, outputFileName).withWriter { Writer writer ->
			constructFile(new CSVWriter(writer, '\t' as char))
        }
    }
}
