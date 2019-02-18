package com.recomdata.transmart.asynchronous.job

import com.recomdata.asynchronous.JobResultsService
import com.recomdata.genepattern.JobStatus
import com.recomdata.genepattern.WorkflowStatus
import grails.converters.JSON
import grails.gsp.PageRenderer
import org.json.JSONObject
import org.transmart.audit.AuditLogService
import org.transmart.audit.StudyIdService
import org.transmartproject.core.log.AccessLogEntryResource
import org.transmartproject.core.users.User

class AsyncJobController {

    AccessLogEntryResource accessLogService
    AsyncJobService asyncJobService
    AuditLogService auditLogService
    PageRenderer groovyPageRenderer
    JobResultsService jobResultsService
    StudyIdService studyIdService
    User currentUserBean

    /**
     * Jobs to show in the jobs tab
     */
    def getjobs(String jobType) {
        response.contentType = 'text/json'
        response.outputStream << asyncJobService.getjobs(jobType)?.toString()
    }

    /**
     * get job stats by name
     */
    def getjobbyname(String jobName) {
	response.contentType = 'text/json'
	response.outputStream << asyncJobService.getjobbyname(jobName)?.toString()
    }

    /**
     * Retrieve the job results (HTML) stored in the JOB_RESULTS field for Haploview and Survival Analysis.
     */
    def getjobresults(String jobName) {
	response.contentType = 'text/json'
	response.outputStream << asyncJobService.getjobresults(jobName)?.toString()
    }

    def createnewjob() {
	String studies = getStudyIds()
	String workflow = getWorkflow()

        JSONObject result = asyncJobService.createnewjob(params)

        auditLogService.report 'Run advanced workflow', request,
                               user: currentUserBean,
                               study: studies,
                               jobname: result.jobName,
                               workflow: workflow

        response.contentType = 'text/json' 
        response.outputStream << result?.toString()
    }

    /**
     * Cancel a running job.
     */
    def canceljob(String jobName, String group) {
        response.contentType = 'text/json'
        response.outputStream << asyncJobService.canceljob(jobName, group)?.toString()
    }

    /**
     * Repeatedly called by datasetExplorer.js to get the job status and results
     */
    def checkJobStatus(String jobName, String jobType) {
        JSONObject result = asyncJobService.checkJobStatus(jobName, jobType)

        def statusIndexExists = result.get('statusIndexExists')
        if (statusIndexExists) {
            def statusIndex = result.get('statusIndex')
            String statusHtml = groovyPageRenderer.render(template: '/genePattern/jobStatusList', model: [
		jobStatuses: jobResultsService[jobName]['StatusList'],
		statusIndex: statusIndex])
            result.put 'jobStatusHTML', statusHtml
            result.remove 'statusIndex'
            result.remove 'statusIndexExists'
        }

        response.contentType = 'text/json'
        response.outputStream << result?.toString()
    }

    /**
     * Shows the job status window
     */
    def showJobStatus() {
        render view: '/genePattern/workflowStatus'
    }

    /**
     * for snp viewer and igv
     */
    def showWorkflowStatus() {
        WorkflowStatus wfstatus = sessionWorkflowstatus()
        if (!wfstatus) {
            wfstatus = new WorkflowStatus()
            wfstatus.setCurrentJobStatus new JobStatus(name: 'initializing Workflow', status: 'R')
            session.workflowstatus = wfstatus
        }

        render view: '/genePattern/workflowStatus'
    }

    def checkWorkflowStatus() {
        WorkflowStatus wfstatus = sessionWorkflowstatus()

        JSONObject result = wfstatus.result ?: new JSONObject()

	String statusHtml = groovyPageRenderer.render(template: '/genePattern/jobStatus', model: [wfstatus: wfstatus])
	result.put 'statusHTML', statusHtml

        if (wfstatus.isCompleted()) {
            result.put 'wfstatus', 'completed'
            wfstatus.rpCount++
            result.put 'rpCount', wfstatus.rpCount
        }
        else {
            result.put 'wfstatus', 'running'
        }
        render result.toString()
    }

    def cancelJob() {
	WorkflowStatus wfstatus = sessionWorkflowstatus()
	wfstatus.setCancelled()
	render(wfstatus.jobStatusList as JSON)
    }

    private String getWorkflow() {
        if (params.jobType) {
            return params.jobType
	}

        if (params.analysisConstraints?.job_type) {
            return params.analysisConstraints.job_type
	}

        'unknownWorkflow'
    }

    private String getStudyIds() {

        Set<String> studyIds = []

        if (params.analysisConstraints) {
            def jsonAnalysisConstraints = JSON.parse(params.analysisConstraints)
            if (jsonAnalysisConstraints.assayConstraints?.patient_set != null) {
                studyIds << studyIdService.getStudyIdsForQueries(jsonAnalysisConstraints.assayConstraints.patient_set)
	    }
        }

        // for concept paths we have to make sure they start with \\top node
        // note the string is escaped so we are adding a double backslash at the start
	String conceptKey
	String conceptTable
        if (params.independentVariable) {
            conceptKey = params.independentVariable.split('\\|')[0]
            conceptTable = conceptKey.split('\\\\')[1]
            studyIds << studyIdService.getStudyIdForConceptKey('\\\\' + conceptTable + conceptKey)
        }

        if (params.dependentVariable) {
            conceptKey = params.dependentVariable.split('\\|')[0]
            conceptTable = conceptKey.split('\\\\')[1]
            studyIds << studyIdService.getStudyIdForConceptKey('\\\\' + conceptTable + conceptKey)
        }

        if (params.variablesConceptPaths) {
            conceptKey = params.variablesConceptPaths.split('\\|')[0]
            conceptTable = conceptKey.split('\\\\')[1]
            studyIds << studyIdService.getStudyIdForConceptKey('\\\\' + conceptTable + conceptKey)
        }

        studyIds.sort().join ','
    }

    private WorkflowStatus sessionWorkflowstatus() {
	session.workflowstatus
    }
}
