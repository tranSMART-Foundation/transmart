package com.recomdata.transmart.asynchronous.job

import com.recomdata.asynchronous.JobResultsService
import com.recomdata.transmart.data.export.DataExportService
import com.recomdata.transmart.domain.i2b2.AsyncJob
import grails.transaction.Transactional
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import org.json.JSONArray
import org.json.JSONObject
import org.quartz.JobKey
import org.quartz.Scheduler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Propagation
import org.springframework.util.Assert
import org.transmart.plugin.shared.SecurityService
import org.transmartproject.core.users.User

@Slf4j('logger')
class AsyncJobService {

    @Autowired private DataExportService dataExportService
    @Autowired private JobResultsService jobResultsService
    @Autowired private Scheduler quartzScheduler
    @Autowired private SecurityService securityService

    /**
     * The jobs to show in the jobs tab.
     */
    JSONObject getjobs(String jobType = null) {
        JSONObject result = new JSONObject()
        JSONArray rows = new JSONArray()

	String userName = securityService.currentUsername()
	List<AsyncJob> jobResults = AsyncJob.createCriteria().list {
	    like 'jobName', userName + '%'
	    if (jobType) {
		eq 'jobType', jobType
            }
            else {
                or {
		    ne 'jobType', 'DataExport'
		    isNull 'jobType'
                }
            }
	    ge 'lastRunOn', new Date() - 7
	    order 'id', 'desc'
        }

	for (AsyncJob jobResult in jobResults) {
	    rows.put([name: jobResult.jobName, type: jobResult.jobType, status: jobResult.jobStatus, runTime: jobResult.jobStatusTime,
		      startDate: jobResult.lastRunOn, viewerURL: jobResult.viewerURL, altViewerURL: jobResult.altViewerURL])
	}

	result.put 'success', true
	result.put 'totalCount', jobResults.size()
	result.put 'jobs', rows

	result
    }

    /**
     * get job info by job name
     */
    JSONObject getjobbyname(String jobName = '') {

        JSONObject result = new JSONObject()
        JSONArray rows = new JSONArray()
	List<AsyncJob> jobResults = null
	if (jobName) {
	    jobResults = AsyncJob.findAllByJobNameLike('%' + jobName + '%')
	    for (jobResult in jobResults) {
		rows.put(name: jobResult.jobName, status: jobResult.jobStatus, runTime: jobResult.runTime,
			 startDate: jobResult.lastRunOn, viewerURL: jobResult.viewerURL,
			 altViewerURL: jobResult.altViewerURL,
			 jobInputsJson: new JSONObject(jobResult.jobInputsJson ?: '{}'))
            }
        }

	result.put 'success', true
	result.put 'totalCount', jobResults.size()
	result.put 'jobs', rows

	result
    }

    /**
     * Retrieve the job results (HTML) stored in the JOB_RESULTS field for Haploview and Survival Analysis.
     */
    JSONObject getjobresults(String jobName) {
        JSONObject result = new JSONObject()
	result.put 'jobResults', AsyncJob.findByJobName(jobName).results
	result
    }

    /**
     * Creates the new asynchronous job name.
     * Current methodology is username-jobtype-ID from sequence generator
     */
    @Transactional
    JSONObject createnewjob(Map params) {
	String userName = securityService.currentUsername()
	String jobStatus = 'Started'

	AsyncJob newJob = new AsyncJob(lastRunOn: new Date())
        newJob.save()

	String jobName = params.jobName
	if (!jobName) {
	    StringBuilder sb = new StringBuilder(userName)
	    sb << '-'
	    if (params.jobType) {
		sb << params.jobType
	    }
	    sb << '-' << newJob.id
	    jobName = sb.toString()
        }
        newJob.jobName = jobName
	newJob.jobType = params.jobType
        newJob.jobStatus = jobStatus
        newJob.jobInputsJson = new JSONObject(params).toString()
        newJob.save()

        jobResultsService[jobName] = [:]
	updateStatus jobName, jobStatus

	logger.debug 'Sending {} back to the client', jobName
        JSONObject result = new JSONObject()
	result.put 'jobName', jobName
	result.put 'jobStatus', jobStatus

	result
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def updateJobInputs(final String jobName, final Map params) {
        assert jobName
        assert params

	AsyncJob job = AsyncJob.findByJobName(jobName)
	assert "${jobName} job is not found.", job

        job.jobInputsJson = new JSONObject(params).toString()
        job.save(flush: true)
    }

    /**
     * Cancel a running job
     */
    JSONObject canceljob(String jobName, String group = null) {
	String jobStatus = 'Cancelled'
	logger.debug 'Attempting to delete {} from the Quartz scheduler', jobName
	boolean result = quartzScheduler.deleteJob(new JobKey(jobName, group))
	logger.debug 'Deletion attempt successful? {}', result

	updateStatus jobName, jobStatus

	new JSONObject(jobName: jobName)
    }

    /**
     * Repeatedly called by datasetExplorer.js to get the job status and results
     */
    JSONObject checkJobStatus(String jobName, String jobType = null) {
        JSONObject result = new JSONObject()
	if (!jobType) {
	    jobType = jobName.split('-')[1]
        }

	Map jobData = jobResultsService[jobName]

	String jobStatus = jobData.Status
        def statusIndex = null
	if (jobData.StatusList != null) {
	    statusIndex = jobData.StatusList.indexOf(jobStatus)
        }
	String jobException = jobData.Exception
	String viewerUrl = jobData.ViewerURL
	String altViewerUrl = jobData.AltViewerURL
	String jobResults = jobData.Results
	String errorType = ''
	if (viewerUrl != null) {
	    def jobResultType = jobData.resultType
	    if (jobResultType != null) {
		result.put 'resultType', jobResultType
	    }
	    logger.debug '{} is being sent to the client', viewerUrl
	    result.put 'jobViewerURL', viewerUrl
	    if (altViewerUrl != null) {
		logger.debug '{} for Comparative Marker Selection', altViewerUrl
		result.put 'jobAltViewerURL', altViewerUrl
            }
            jobStatus = 'Completed'
        }
        else if (jobResults != null) {
	    result.put 'jobResults', jobResults
	    result.put 'resultType', jobType
            jobStatus = 'Completed'
        }
	else if (jobException) {
	    logger.warn 'An exception was thrown, passing this back to the user', jobException
	    result.put 'jobException', jobException
            jobStatus = 'Error'
            errorType = 'data'
        }
        if (statusIndex != null) {
	    result.put 'statusIndexExists', true
	    result.put 'statusIndex', statusIndex
        }
        else {
	    result.put 'statusIndexExists', false
        }

	updateStatus jobName, jobStatus, viewerUrl, altViewerUrl, jobResults

	result.put 'jobStatus', jobStatus
	result.put 'errorType', errorType
	result.put 'jobName', jobName

	result
    }

    /**
     * Helper to update the status of the job and log it
     *
     * @param jobName - the unique job name
     * @param status - the new status
     * @param viewerUrl - optional, store the viewer URL if the job is completed
     * @param altViewerUrl - optional, store the alternate viewer URL for CMS heatmaps
     * @param results - optional, store the results from survival analysis, haploview, etc.
     *
     * @return true if the job was cancelled
     */
    @Transactional
    boolean updateStatus(String jobName, String status, String viewerUrl = null,
	                 String altViewerUrl = null, String results = null) {
	boolean cancelled = false
	String jobId = jobName.split('-')[-1]

	Map jobData = jobResultsService[jobName]
	if (jobData.Status == 'Cancelled') {
	    logger.warn '{} has been cancelled', jobName
	    cancelled = true
        }
        else {
	    jobData.Status = status
        }
        //If the job isn't already cancelled, update the job info.
	if (!cancelled) {
	    AsyncJob asyncJob = AsyncJob.get(jobId)
            asyncJob.jobStatus = status
	    if (viewerUrl) {
		asyncJob.viewerURL = viewerUrl
	    }
	    if (altViewerUrl && asyncJob.altViewerURL != null) {
		asyncJob.altViewerURL = altViewerUrl
	    }
	    if (results) {
		asyncJob.results = results
	    }
	    jobData.ViewerURL = viewerUrl
            //We need to flush so that the value doesn't overwrite cancelled when the controller finishes.
            asyncJob.save(flush: true)
        }

	cancelled
    }

    boolean isUserAllowedToExportResults(User user, String jobName) {
	Assert.notNull user
	Assert.hasLength jobName

	AsyncJob job = AsyncJob.findByJobName(jobName)
	Assert.notNull job, jobName + ' is not found.'

        def jobInputsJsonObj = new JsonSlurper().parseText(job.jobInputsJson)

        List<Long> resultInstanceIds = []
        int subsetNumber = 1
        while (jobInputsJsonObj['result_instance_id' + subsetNumber]) {
            resultInstanceIds << (jobInputsJsonObj['result_instance_id' + subsetNumber] as Long)
	    subsetNumber++
	}

	dataExportService.isUserAllowedToExport user, resultInstanceIds
    }
}
