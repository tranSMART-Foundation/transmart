package jobs.steps

import groovy.transform.CompileStatic
import jobs.steps.helpers.ColumnConfigurator

@CompileStatic
class OpenResultSetsWithConfiguratorsStep implements Step {

    List<ColumnConfigurator> configurators

    final String statusName = 'Open Result Sets'

    void execute() {
        configurators*.addColumn()
    }
}
