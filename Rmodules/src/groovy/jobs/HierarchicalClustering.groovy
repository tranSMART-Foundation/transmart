package jobs

import groovy.transform.CompileStatic
import jobs.steps.Step
import jobs.steps.ValueGroupDumpDataStep
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Scope('job')
class HierarchicalClustering extends HighDimensionalOnlyJob {

    @Override
    protected Step createDumpHighDimensionDataStep(Closure resultsHolder) {
        new ValueGroupDumpDataStep(
                temporaryDirectory: temporaryDirectory,
                resultsHolder: resultsHolder,
                params: params)
    }

    @Override
    protected List<String> getRStatements() {
        String source = 'source(\'$pluginDirectory/Heatmap/HClusteredHeatmapLoader.R\')'

        String createHeatmap = '''HClusteredHeatmap.loader(
                            input.filename = '$inputFileName',
                            aggregate.probes = '$divIndependentVariableprobesAggregation' == 'true',
                            cluster.by.rows = '$doClusterRows' == 'true',
                            cluster.by.columns = '$doClusterColumns' == 'true',
                            ${ txtMaxDrawNumber ? ", maxDrawNumber  = as.integer('$txtMaxDrawNumber')" : ''},
                            ${ txtPixelsPerCell ? ", pxPerCell  = as.integer('$txtPixelsPerCell')" : ''},
                            calculateZscore = '$calculateZscore'
                            )'''

        [ source, createHeatmap ]
    }

    @Override
    protected String getForwardPath() {
        "/RHClust/heatmapOut?jobName=${name}"
    }
}
