package jobs

import groovy.transform.CompileStatic
import jobs.misc.AnalysisConstraints
import jobs.steps.ParametersFileStep
import jobs.steps.Step
import org.quartz.JobExecutionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.Resource

@CompileStatic
abstract class AbstractAnalysisJob {

    Logger logger = LoggerFactory.getLogger(getClass())

    @Autowired
    UserParameters params

    @Autowired
    AnalysisConstraints analysisConstraints

    @Resource(name = 'jobName')
    String name // The job instance name

    /* manually injected properties
     *********************/

    Closure updateStatus
    Closure setStatusList
    File topTemporaryDirectory
    File scriptsDirectory

    /* TODO: Used to build temporary working directory for R processing phase.
             This is called subset1_<study name>. What about subset 2? Is this
             really needed or an arbitrary directory is enough? Is it required
             due to some interaction with clinical data? */
    String studyName

    /* end manually injected properties
     *************************/

    File temporaryDirectory /* the workingDirectory */

    final void run() {
        validateName()
        setupTemporaryDirectory()

        List<Step> stepList = []

        /* we need the parameters file not just for troubleshooting
         * but also because we need later to read the result instance
         * ids and determine if we should create the zip with the
         * intermediate data */
	stepList << new ParametersFileStep(temporaryDirectory: temporaryDirectory, params: params)

        stepList.addAll prepareSteps()

        // build status list
        setStatusList stepList.collect { Step s -> s.statusName }.grep()

        for (Step step in stepList) {
            if (step.statusName) {
                updateStatus step.statusName
            }

            step.execute()
        }

        updateStatus 'Completed', forwardPath
    }

    protected abstract List<Step> prepareSteps()

    protected abstract List<String> getRStatements()

    protected abstract String getForwardPath()

    private void validateName() {
        if (!(name ==~ /^[0-9A-Za-z-]+$/)) {
            throw new JobExecutionException('Job name mangled')
        }
    }

    private void setupTemporaryDirectory() {
        temporaryDirectory = new File(new File(topTemporaryDirectory, name), 'workingDirectory')
        temporaryDirectory.mkdirs()
    }
}
