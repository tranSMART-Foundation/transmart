package jobs.steps

import au.com.bytecode.opencsv.CSVWriter
import groovy.transform.CompileStatic
import jobs.table.ConceptTimeValuesTable

/**
 * @author carlos
 */
@CompileStatic
class BuildConceptTimeValuesStep implements Step {

    ConceptTimeValuesTable table

    String[] header

    File outputFile

    String getStatusName() {
        'Creating concept time values table'
    }

    void execute() {

        //makes sure the file is not there
        outputFile.delete()

        Map<String,Map> map = table.resultMap
        if (map != null) {
            writeToFile map
        }
    }

    private void writeToFile(Map<String, Map> map) {

        outputFile.withWriter { Writer writer ->
            CSVWriter csvWriter = new CSVWriter(writer, '\t' as char)
            csvWriter.writeNext header

            for (Map.Entry<String, Map> entry in map.entrySet()) {
		csvWriter.writeNext([entry.key, entry.value.value] as String[])
            }
        }
    }
}
