package jobs

import groovy.transform.CompileStatic
import jobs.steps.BuildTableResultStep
import jobs.steps.MultiRowAsGroupDumpTableResultsStep
import jobs.steps.RCommandsStep
import jobs.steps.Step
import jobs.steps.helpers.CensorColumnConfigurator
import jobs.steps.helpers.NumericColumnConfigurator
import jobs.steps.helpers.OptionalBinningColumnConfigurator
import jobs.steps.helpers.SimpleAddColumnConfigurator
import jobs.table.MissingValueAction
import jobs.table.Table
import jobs.table.columns.PrimaryKeyColumn
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.transmartproject.core.dataquery.highdim.projections.Projection

import static jobs.steps.AbstractDumpStep.DEFAULT_OUTPUT_FILE_NAME

/**
 * @author carlos
 */
@CompileStatic
@Component
@Scope('job')
class SurvivalAnalysis extends AbstractAnalysisJob implements InitializingBean {

    @Autowired
    SimpleAddColumnConfigurator primaryKeyColumnConfigurator

    @Autowired
    NumericColumnConfigurator timeVariableConfigurator

    @Autowired
    @Qualifier('general')
    OptionalBinningColumnConfigurator categoryVariableConfigurator

    @Autowired
    CensorColumnConfigurator censoringVariableConfigurator

    @Autowired
    Table table

    void afterPropertiesSet() throws Exception {
        primaryKeyColumnConfigurator.column = new PrimaryKeyColumn(header: 'PATIENT_NUM')

        configureTimeVariableConfigurator()
        configureCategoryVariableConfigurator()
        configureCensoringVariableConfigurator()
    }

    void configureTimeVariableConfigurator() {
        timeVariableConfigurator.header = 'TIME'
        timeVariableConfigurator.setKeys('time')
        timeVariableConfigurator.alwaysClinical = true
    }

    void configureCategoryVariableConfigurator() {
        categoryVariableConfigurator.required = false
        categoryVariableConfigurator.header             = 'CATEGORY'
        categoryVariableConfigurator.projection         = Projection.LOG_INTENSITY_PROJECTION
        categoryVariableConfigurator.multiRow           = true

        categoryVariableConfigurator.setKeys('dependent')
        categoryVariableConfigurator.binningConfigurator.setKeys('')
        categoryVariableConfigurator.keyForConceptPaths = 'categoryVariable'

        def missingValueAction = categoryVariableConfigurator.conceptPaths ?
                new MissingValueAction.DropRowMissingValueAction() :
                new MissingValueAction.ConstantReplacementMissingValueAction(replacement: 'STUDY')

        categoryVariableConfigurator.missingValueAction = missingValueAction
        categoryVariableConfigurator.binningConfigurator.missingValueAction = missingValueAction
    }

    void configureCensoringVariableConfigurator() {
        censoringVariableConfigurator.header             = 'CENSOR'
        censoringVariableConfigurator.keyForConceptPaths = 'censoringVariable'
    }

    protected List<Step> prepareSteps() {
        List<Step> steps = []

        steps << new BuildTableResultStep(
                table:         table,
                configurators: [primaryKeyColumnConfigurator,
                        timeVariableConfigurator,
                        censoringVariableConfigurator,
                        categoryVariableConfigurator,
                ])

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

    @Override
    protected List<String> getRStatements() {
        [
            '''source('$pluginDirectory/Survival/CoxRegressionLoader.r')''',
            '''CoxRegression.loader(
                input.filename      = '$inputFileName')''',
            '''source('$pluginDirectory/Survival/SurvivalCurveLoader.r')''',
            '''SurvivalCurve.loader(
                input.filename      = '$inputFileName',
                concept.time        = '$timeVariable')''',
        ]
    }

    @Override
    protected String getForwardPath() {
        "/survivalAnalysis/survivalAnalysisOutput?jobName=$name"
    }

}
