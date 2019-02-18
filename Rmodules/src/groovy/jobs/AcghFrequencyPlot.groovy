package jobs

import groovy.transform.CompileStatic
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Scope('job')
class AcghFrequencyPlot extends AcghAnalysisJob {

    @Override
    protected List<String> getRStatements() {
        ['''source('$pluginDirectory/aCGH/acgh-frequency-plot.R')''',
                '''acgh.frequency.plot(column = 'group')''']
    }

    @Override
    protected String getForwardPath() {
        "/AcghFrequencyPlot/acghFrequencyPlotOutput?jobName=${name}"
    }
}
