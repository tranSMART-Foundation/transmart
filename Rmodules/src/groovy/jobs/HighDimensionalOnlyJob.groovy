package jobs

import jobs.steps.AbstractDumpStep
import jobs.steps.OpenHighDimensionalDataStep
import jobs.steps.RCommandsStep
import jobs.steps.Step
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.transmartproject.core.dataquery.highdim.HighDimensionResource

abstract class HighDimensionalOnlyJob extends AbstractAnalysisJob {

    @Autowired
    HighDimensionResource highDimensionResource

    @Autowired
    ApplicationContext appCtx

    protected List<Step> prepareSteps() {

        def openResultSetStep = new OpenHighDimensionalDataStep(
                params: params,
                dataTypeResource: highDimensionResource.getSubResourceForType(analysisConstraints['data_type']),
                analysisConstraints: analysisConstraints)

        List<Step> steps = [openResultSetStep]

        steps << createDumpHighDimensionDataStep { -> openResultSetStep.results }

        steps << new RCommandsStep(
                temporaryDirectory: temporaryDirectory,
                scriptsDirectory: scriptsDirectory,
                rStatements: RStatements,
                studyName: studyName,
                params: params,
                extraParams: [inputFileName: AbstractDumpStep.DEFAULT_OUTPUT_FILE_NAME])

        steps
    }

    protected abstract Step createDumpHighDimensionDataStep(Closure resultsHolder)
}
