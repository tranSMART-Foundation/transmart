package jobs.steps

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import jobs.steps.helpers.ColumnConfigurator
import jobs.table.Table

@CompileStatic
@Slf4j('logger')
class BuildTableResultStep implements Step {

    final String statusName = 'Collecting Data'

    Table table

    List<ColumnConfigurator> configurators

    void execute() {
        try {
            for (ColumnConfigurator cc in configurators) {
                cc.addColumn()
            }

            table.buildTable()
        }
	catch (e) {
            try {
                table.close()
            }
	    catch (e2) {
                logger.error 'Error closing table after exception retrieving results', e2
            }

            throw e
        }
    }
}
