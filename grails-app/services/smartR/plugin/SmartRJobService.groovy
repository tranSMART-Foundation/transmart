package smartR.plugin

import org.quartz.Job
import org.quartz.InterruptableJob
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.UnableToInterruptJobException
import org.apache.commons.io.FilenameUtils


class SmartRJobService implements InterruptableJob {

    def interrupted = false

    /**
    *   Executes a the script and makes sure that it terminates (eventually by force)
    *
    *   @param jobDataMap: contains all information for the job execution
    *   @return {int}: the return value of the executed script
    */
    def runScript(jobDataMap) {
    	def interpreterMap = [R: 'Rscript', r: 'Rscript', py: 'python']
        def interpreter = interpreterMap[FilenameUtils.getExtension(jobDataMap['script'])]
        def procBuilder = new ProcessBuilder(
            interpreter,
            jobDataMap['scriptDir'] + 'Wrapper.R',
            jobDataMap['scriptDir'] + jobDataMap['script'],
            jobDataMap['lowDimFile'],
            jobDataMap['highDimFile_cohort1'],
            jobDataMap['highDimFile_cohort2'],
            jobDataMap['settingsFile'],
            jobDataMap['outputFile'],
            jobDataMap['errorFile'])
        def errorLog = new File(jobDataMap['errorFile'])
        procBuilder.redirectErrorStream(true)
        procBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(errorLog))
        def proc = procBuilder.start()
        Thread.start() {
            sleep(1000 * 60 * 15) // 15 min before destruction
            try {
                proc.exitValue()
            } catch (IllegalThreadStateException) {
                proc.destroy()
            }
        }
        return proc.waitFor()
    }

    /**
    *   Method called when job is started
    *
    *   @param {JobExectionContext} context: contains the context of the job (i.e. the job data map)
    */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info('Starting job...')
            def jobDetail = context.getJobDetail()
            def jobName = jobDetail.getName()
            def jobDataMap = jobDetail.getJobDataMap()
            def exitValue = runScript(jobDataMap)
            if (exitValue != 0) {
                log.error('Job failed to execute!')
            } else {
                log.info('Job successfully executed!')
            }
        } catch (Exception e) {
            log.error('Failed to launch job!')
            throw new JobExecutionException(e)
        }
	}

    /**
    *   Method is called when job shall be interrupted. Note: this doesn't interrupt the job automatically!
    *   (Not used in the current implementation)
    */
    @Override
    public void interrupt() throws UnableToInterruptJobException {
        this.interrupted = true;
    }
}
