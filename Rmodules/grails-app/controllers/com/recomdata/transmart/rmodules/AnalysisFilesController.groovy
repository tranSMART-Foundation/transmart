package com.recomdata.transmart.rmodules

import com.recomdata.transmart.data.association.RModulesOutputRenderService
import groovy.util.logging.Slf4j
import org.springframework.security.core.GrantedAuthority
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.security.Roles
import org.transmartproject.core.exceptions.InvalidRequestException
import sendfile.SendFileService

import java.util.regex.Matcher
import java.util.regex.Pattern

@Slf4j('logger')
class AnalysisFilesController {

    RModulesOutputRenderService RModulesOutputRenderService
    SecurityService securityService
    SendFileService sendFileService

    def download(String analysisName, String path) {

        if (!checkPermissions(analysisName)) {
            render status: 403
            return
        }

        File analysisDirectory = new File(jobsDirectory, analysisName)
        if (analysisDirectory.parentFile != jobsDirectory) {
            // just some sanity checking... should always happen
            logger.error 'Unexpected analysis directory: {}', analysisDirectory
            render status: 404
            return
        }

	if (!analysisDirectory.exists()) {
            logger.warn 'Could not find directory for job {}: {}', analysisName, analysisDirectory
            render status: 404
            return
        }

        if (!analysisDirectory.isDirectory()) {
            logger.error 'Analysis directory is surprisingly not a directory: {}', analysisDirectory
            render status: 404
            return
        }

        // Only expose files under the analysis directory
        File targetFile = new File(analysisDirectory, path)
        //canonical path does not end with separator
        if (!targetFile.canonicalPath.startsWith(analysisDirectory.canonicalPath + File.separator)) {
            logger.warn 'Request for {}, but it is not under {}', targetFile, analysisDirectory
            render status: 404
            return
        }

        if (!targetFile.isFile()) {
            logger.warn 'Request for {}, but such file does not exist', targetFile
            render status: 404
            return
        }

        sendFileService.sendFile servletContext, request, response, targetFile
    }

    private File getJobsDirectory() {
        new File(RModulesOutputRenderService.tempFolderDirectory)
    }

    private boolean checkPermissions(String jobName) {
        String userNameFromJobName = extractUserFromJobName(jobName)

	String currentUsername = securityService.currentUsername()
	if (!currentUsername) {
	    logger.error 'Could not determine current logged in user\'s name'
	    return false
	}

        if (userNameFromJobName == currentUsername || securityService.principal().isAdmin()) {
            return true
        }

        logger.warn 'User {} has no access for job {}; refusing request for job {}',
		     currentUsername, jobName, jobName
        false
    }

    private String extractUserFromJobName(String jobName) {
        Pattern pattern = ~/(.+)-[a-zA-Z]+-\d+/
        Matcher matcher = pattern.matcher(jobName)

        if (!matcher.matches()) {
            //should never happen due to url mapping
            throw new InvalidRequestException('Invalid job name')
        }

        matcher.group(1)
    }
}
