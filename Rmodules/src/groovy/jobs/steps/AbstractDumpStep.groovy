package jobs.steps

import groovy.transform.CompileStatic

@CompileStatic
abstract class AbstractDumpStep implements Step {

    static final String DEFAULT_OUTPUT_FILE_NAME = 'outputfile.txt'

    String outputFileName = DEFAULT_OUTPUT_FILE_NAME
}
