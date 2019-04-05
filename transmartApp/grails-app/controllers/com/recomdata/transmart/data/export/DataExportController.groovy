package com.recomdata.transmart.data.export

import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.UtilService
import org.transmartproject.core.exceptions.AccessDeniedException
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.users.User

import java.util.regex.Matcher
import java.util.regex.Pattern

@Slf4j('logger')
class DataExportController {

    User currentUserBean
    @Autowired private DataExportService dataExportService
    @Autowired private ExportService exportService
    @Autowired private ExportMetadataService exportMetadataService
    @Autowired private SecurityService securityService
    @Autowired private UtilService utilService

    def index() {}

    //We need to gather a JSON Object to represent the different data types.
    def getMetaData() {
        List<Long> resultInstanceIds = parseResultInstanceIds()
	checkRightsToExport resultInstanceIds

        render exportMetadataService.getMetaData(
            resultInstanceIds[0],
            resultInstanceIds[1]) as JSON
    }

    def downloadFileExists(String jobName) {
	checkJobAccess jobname

	Map result = [:]

	InputStream inputStream = exportService.downloadFile(jobName)
        if (inputStream) {
            result.fileStatus = true
            inputStream.close()
        }
        else {
            result.fileStatus = false
            result.message = 'Download failed as file could not be found on the server'
        }

	render(result as JSON)
    }

    def downloadFile(String jobname) {
	checkJobAccess jobname

	utilService.sendDownload response, 'application/zip', jobname + '.zip',
	    exportService.downloadFile(params)
    }

    /**
     * Creates the new asynchronous job name.
     * Current methodology is username-jobtype-ID from sequence generator
     */
    def createnewjob() {
	response.contentType = 'text/json'
	response.outputStream << exportService.createExportDataAsyncJob(params).toString()
    }

    /**
     * Runs a data export and is called asynchronously from the datasetexplorer -> Data Export tab.
     */
    def runDataExport() {
	checkRightsToExport parseResultInstanceIds()

	response.contentType = 'text/json'
	response.outputStream << exportService.exportData(params).toString()
    }

    def isCurrentUserAllowedToExport() {
	boolean isAllowed = dataExportService.isUserAllowedToExport(currentUserBean, parseResultInstanceIds())
        render([result: isAllowed] as JSON)
    }

    /**
     * Private method that puts the (Long) values of parameters named 'result_instance_id<n>' in a list.
     * The previous implementation of this method would return an empty list if the key named 'result_instance_id1' was not found.
     * Even if the key named 'result_instance_id2' was present.
     * This caused AssertionException's being thrown by dataExportService.isUserAllowedToExport.
     * Also the checkRightsToExport method would throw an InvalidArgumentsException('No result instance id provided')
     * when trying to export data if subset2 existed, but not subset1
     * Current implementation will in this case (i.e. only key 'result_instance_id2' is present) return a list
     * with the first element equal to null and the second equal to the value of 'result_instance_id2'.
     * Currently the maximum number of subsets that are supported is 2
     */
    private List<Long> parseResultInstanceIds() {
        assert !params.containsKey('result_instance_id3')
        List<Long> result = []
        for (subsetNumber in 1..2) {
	    if (params.containsKey('result_instance_id' + subsetNumber)) {
                result[subsetNumber-1] = params.long('result_instance_id'+subsetNumber)
            }
	}
        result
    }

    private void checkRightsToExport(List<Long> resultInstanceIds) {
        if (!resultInstanceIds) {
            throw new InvalidArgumentsException('No result instance id provided')
        }

	if (!dataExportService.isUserAllowedToExport(currentUserBean, resultInstanceIds)) {
	    throw new AccessDeniedException('User ' + currentUserBean.username +
					    ' has no EXPORT permission on one of the result sets: ' + resultInstanceIds)
        }
    }

    private void checkJobAccess(String jobName) {
	if (securityService.principal().isAdmin()) {
            return
        }

        String jobUsername = extractUserFromJobName(jobName)

	if (jobUsername != securityService.currentUsername()) {
	    logger.warn 'Denying access to job {} because the corresponding username' +
		' ({}) does not match that of the current user', jobName, jobUsername
	    throw new AccessDeniedException("Job $jobName was not started by this user")
        }
    }

    private static String extractUserFromJobName(String jobName) {
        Pattern pattern = ~/(.+)-[a-zA-Z]+-\d+/
        Matcher matcher = pattern.matcher(jobName)

        if (!matcher.matches()) {
            throw new IllegalStateException('Invalid job name')
        }

        matcher.group(1)
    }
}
