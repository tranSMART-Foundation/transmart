package jobs

import groovy.transform.CompileStatic
import jobs.steps.BuildTableResultStep
import jobs.steps.MultiRowAsGroupDumpTableResultsStep
import jobs.steps.RCommandsStep
import jobs.steps.Step
import jobs.steps.helpers.BinningColumnConfigurator
import jobs.steps.helpers.ColumnConfigurator
import jobs.steps.helpers.OptionalBinningColumnConfigurator
import jobs.steps.helpers.SimpleAddColumnConfigurator
import jobs.table.Table
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.highdim.projections.Projection

import static jobs.steps.AbstractDumpStep.DEFAULT_OUTPUT_FILE_NAME

@CompileStatic
abstract class CategoricalOrBinnedJob extends AbstractAnalysisJob implements InitializingBean {

    @Autowired
    SimpleAddColumnConfigurator primaryKeyColumnConfigurator

    abstract ColumnConfigurator getIndependentVariableConfigurator()

    abstract ColumnConfigurator getDependentVariableConfigurator()

    @Autowired
    Table table

    protected List<Step> prepareSteps() {
        List<Step> steps = []

        steps << new BuildTableResultStep(
                table:         table,
                configurators: [primaryKeyColumnConfigurator,
                        independentVariableConfigurator,
                        dependentVariableConfigurator,])

        steps << new MultiRowAsGroupDumpTableResultsStep(
                table: table,
                temporaryDirectory: temporaryDirectory,
                outputFileName: DEFAULT_OUTPUT_FILE_NAME)

        steps << new RCommandsStep(
                temporaryDirectory: temporaryDirectory,
                scriptsDirectory: scriptsDirectory,
                rStatements: RStatements,
                studyName: studyName,
                params: params,
                extraParams: [inputFileName: DEFAULT_OUTPUT_FILE_NAME])

        steps
    }

    protected void configureConfigurator(OptionalBinningColumnConfigurator configurator, String keyBinPart,
                                         String keyVariablePart, String header = null) {
        if (header != null) {
            configurator.header = header
        }
        configurator.projection            = Projection.LOG_INTENSITY_PROJECTION

        configurator.multiRow              = true

	String cap = keyVariablePart.capitalize()
        configurator.keyForConceptPaths    = keyVariablePart + 'Variable'
        configurator.keyForDataType        = 'div' + cap + 'VariableType'
        configurator.keyForSearchKeywordId = 'div' + cap + 'VariablePathway'

	cap = keyBinPart.capitalize()
        BinningColumnConfigurator binningColumnConfigurator = configurator.binningConfigurator
        binningColumnConfigurator.keyForDoBinning       = 'binning' + cap
        binningColumnConfigurator.keyForManualBinning   = 'manualBinning' + cap
        binningColumnConfigurator.keyForNumberOfBins    = 'numberOfBins' + cap
        binningColumnConfigurator.keyForBinDistribution = 'binDistribution' + cap
        binningColumnConfigurator.keyForBinRanges       = 'binRanges' + cap
        binningColumnConfigurator.keyForVariableType    = 'variableType' + cap
    }
}
