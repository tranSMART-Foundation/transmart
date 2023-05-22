package jobs

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import jobs.steps.Step
import jobs.steps.ValueGroupDumpDataStep
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Scope('job')
@Slf4j('logger')
class Heatmap extends HighDimensionalOnlyJob {

    @Override
    protected Step createDumpHighDimensionDataStep(Closure resultsHolder) {

//	logger.info 'createDumpHighDimensionDataStep temporaryDirectory {}', temporaryDirectory
//	logger.info 'createDumpHighDimensionDataStep params {}', params

        new ValueGroupDumpDataStep(
                temporaryDirectory: temporaryDirectory,
                resultsHolder: resultsHolder,
                params: params)
    }

    @Override
    protected List<String> getRStatements() {
        String source = 'source(\'$pluginDirectory/Heatmap/HeatmapLoader.R\')'

        String createHeatmap = '''Heatmap.loader(
                            input.filename = '$inputFileName',
                            aggregate.probes = '$divIndependentVariableprobesAggregation' == 'true'
                            ${ txtMaxDrawNumber ? ", maxDrawNumber  = as.integer('$txtMaxDrawNumber')" : ''},
                            ${ txtPixelsPerCell ? ", pxPerCell  = as.integer('$txtPixelsPerCell')" : ''},
                            calculateZscore = '$calculateZscore'
                            )'''
//	logger.info 'getRStatements source {}', source
//	logger.info 'getRStatements createHeatmap {}', createHeatmap

        [ source, createHeatmap ]
    }

    @Override
    protected String getForwardPath() {
//	logger.info 'getForwardPath /RHeatmap/heatmapOut?jobName{}', name
        "/RHeatmap/heatmapOut?jobName=${name}"
    }
}
