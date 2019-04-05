import com.recomdata.asynchronous.JobResultsService
import com.recomdata.transmart.asynchronous.job.AsyncJobService
import com.recomdata.transmart.domain.i2b2.AsyncJob
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.transmart.plugin.shared.SecurityService

@Slf4j('logger')
class GenericJobController {

    AsyncJobService asyncJobService
    JobResultsService jobResultsService
    SecurityService securityService

    /**
     * Create the new asynchronous job name.
     * Current methodology is username-jobtype-ID from sequence generator
     */
    def createnewjob(String analysis) {
	String jobStatus = 'Started'

	AsyncJob newJob = new AsyncJob(lastRunOn: new Date())
        newJob.save()

	String jobName = securityService.currentUsername() + '-' + analysis + '-' + newJob.id
        newJob.jobName = jobName
        newJob.jobStatus = jobStatus
        newJob.jobType = analysis
        newJob.save()

        jobResultsService[jobName] = [:]
	asyncJobService.updateStatus jobName, jobStatus

	logger.debug 'Sending {} back to the client', jobName

	render([jobName: jobName, jobStatus: jobStatus] as JSON)
    }
}
