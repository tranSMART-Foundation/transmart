package org.transmartproject.utils

import au.com.bytecode.opencsv.CSVReader
import groovy.transform.CompileStatic

/**
 * @author ruslan
 */
@CompileStatic
class FileUtils {

    private static final int TO_END = -1

    /**
     * Parses character separated values file (csv, tsv,...) and return back table object.
     * Table object contains page of parsed row objects and number of all available rows to be used for pagination.
     * It's expected that csv file contains title row as first row. Columns titles become field names of row object while parsing.
     *
     * @param args
     *  <p>args.separator - character that is used for separation columns in row in csv file. By default it's comma.</p>
     *  <p>args.fields - list detect which columns/fields should be present in result object. All columns are included by default.</p>
     *  <p>args.sort - field to sort by. If not specified it' be order of rows in file.</p>
     *  <p>args.dir - direction of sorting. Possible values: <code>'ASC'</code> (default), <code>'DESC'</code></p>
     *  <p>args.numberFields - field to be considered with numeric content.</p>
     *  <p>args.start - From which row to start result page. By default it's <code>0</code></p>
     *  <p>args.limit - Number of rows to include to result page. By default all rows to the end will be included.</p>
     * @param file the file which contains character separated values to parse
     * @return
     * <code><pre>
     * [
     *  totalCount: 100,
     *  result: [
     *      [title1: value1],
     *      [title2: value2],
     *      ...
     *  ]
     * ]
     * </pre></code>
     */
    static Map parseTable(Map args, File file) {

        Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), 'UTF-8'))
	CSVReader csvReader = new CSVReader(reader, (args.separator ?: ',') as Character)

	List<String[]> rows = []
        try {
            rows = csvReader.readAll()
        }
	finally {
            csvReader.close()
        }

	List<Map> resultRows = []

        if (rows) {
	    Set<String> fields = (Set) args.fields
	    Set<String> numberFields = (Set) args.numberFields

	    List<String> headerRow = rows.remove(0) as List
	    List<String> useFields = []
	    List<Integer> usePositions = []
            headerRow.eachWithIndex {String entry, int i ->
                if (!fields || fields.contains(entry)) {
                    useFields << entry
                    usePositions << i
                }
            }
            int sortByPosition = args.sort ? headerRow.indexOf(args.sort) : -1
            if (sortByPosition >= 0) {
                int dirMultiplier = args.dir ==~ /(?i)DESC/ ? -1 : 1
                boolean isNumberSort = numberFields?.contains(args.sort) ?: false
		rows.sort { String[] row1, String[] row2 ->
		    if (isNumberSort) {
			row1[sortByPosition].toDouble().compareTo(row2[sortByPosition].toDouble()) * dirMultiplier
		    }
		    else {
			row1[sortByPosition].compareTo(row2[sortByPosition]) * dirMultiplier
		    }
                }
            }
            int start = ((Integer) args.start) > 0 ? (int) args.start : 0
            int end = ((Integer) args.limit) >= 0 ? (int) args.limit + start - 1 : TO_END
            if (end >= rows.size()) {
                end = TO_END
            }

            if ((end == TO_END || start <= end) && start < rows.size()) {
                for (String[] row in rows[start..end]) {
                    Map rowMap = [:]
                    List<String> useValues = row.getAt(usePositions)
                    useFields.eachWithIndex {String entry, int i ->
			String value = useValues[i]
                        rowMap[entry] = numberFields?.contains(entry) ? value.toDouble() : value
                    }
                    resultRows << rowMap
                }
            }
        }

        [totalCount: rows.size(), result: resultRows]
    }
}
