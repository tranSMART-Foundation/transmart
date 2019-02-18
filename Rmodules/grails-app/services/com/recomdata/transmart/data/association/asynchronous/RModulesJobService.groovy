/*************************************************************************   
* Copyright 2008-2012 Janssen Research & Development, LLC.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************/

package com.recomdata.transmart.data.association.asynchronous

import com.recomdata.asynchronous.JobResultsService
import com.recomdata.transmart.util.RUtil
import com.recomdata.transmart.util.ZipService
import grails.util.Holders
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.RConnection
import org.rosuda.REngine.Rserve.RserveException
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext

import java.lang.reflect.UndeclaredThrowableException

@Slf4j('logger')
class RModulesJobService implements Job {

    private static final String lineSeparator = System.getProperty('line.separator')

    static transactional = false
    static scope = 'request'

    private GrailsApplication grailsApplication = Holders.grailsApplication
    private ApplicationContext ctx = grailsApplication.mainContext
    private JobResultsService jobResultsService = ctx.jobResultsService
    private asyncJobService = ctx.asyncJobService
    private dataExportService = ctx.dataExportService
    private ZipService zipService = ctx.zipService

    private String jobTmpDirectory
    //This is where all the R scripts get run, intermediate files are created, images are initially saved, etc.
    private String jobTmpWorkingDirectory
    def finalOutputFile

    private JobDataMap jobDataMap
    private String jobName
    private File jobInfoFile

    @Value('${RModules.host:}')
    private String host

    @Value('${RModules.port:0}')
    private int port

    @Value('${RModules.tempFolderDirectory:}')
    private String tempFolderDirectory

    private void initJob(JobExecutionContext jobExecutionContext) {
	try {
	    JobDetail jobDetail = jobExecutionContext.jobDetail
	    jobName = jobDetail.name
	    jobDataMap = jobDetail.jobDataMap
	    if (!jobName) {
		jobName = jobDataMap.jobName
	    }
	    //Put an entry in our log.
	    logger.info '{} has been triggered to run ', jobName

	    //Write our attributes to a log file.
	    if (logger.debugEnabled) {
		for (key in jobDataMap.keys) {
		    logger.debug '\t{} -> {}', key, jobDataMap[key]
		}
	    }
	}
	catch (e) {
	    throw new Exception('Job Initialization failed!!! Please contact an administrator.', e)
	}
    }

    private void setupTempDirsAndJobFile() {
	try {
	    jobTmpDirectory = (tempFolderDirectory + '/' + jobDataMap.jobName + '/').replace('\\', '\\\\')
	    if (new File(jobTmpDirectory).exists()) {
		logger.warn 'The job folder {} already exists. It is going to be overwritten.', jobTmpDirectory
		FileUtils.deleteDirectory(new File(jobTmpDirectory))
	    }
	    jobTmpWorkingDirectory = jobTmpDirectory + 'workingDirectory'

	    new File(jobTmpWorkingDirectory).mkdirs()

	    //Create a file that will have all the job parameters for debugging purposes.
	    jobInfoFile = new File(jobTmpWorkingDirectory, 'jobInfo.txt')

	    //Write our parameters to the file.
	    jobInfoFile.write 'Parameters' + lineSeparator
	    for (key in jobDataMap.keys) {
		jobInfoFile.append "\t${key} -> ${jobDataMap[key]}" + lineSeparator
	    }
	}
	catch (e) {
	    throw new Exception('Failed to create Temporary Directories and Job Info File, ' +
				'maybe there is not enough space on disk. Please contact an administrator.', e)
	}
    }

    void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
	try {
	    initJob jobExecutionContext
	    setupTempDirsAndJobFile()

	    //TODO identify a different way of fetching the statusList and stepping through them
	    if (updateStatusAndCheckIfJobCancelled(jobName, 'Gathering Data')) {
		return
	    }

	    getData()
	    if (updateStatusAndCheckIfJobCancelled(jobName, 'Running Conversions')) {
		return
	    }
	    
	    runConversions()
	    if (updateStatusAndCheckIfJobCancelled(jobName, 'Running Analysis')) {
		return
	    }

	    runAnalysis()
	    if (updateStatusAndCheckIfJobCancelled(jobName, 'Rendering Output')) {
		return
	    }
	    
	    renderOutput jobExecutionContext.jobDetail
	}
	catch (e) {
	    logger.error 'Exception thrown executing job: {}', e.message, e
	    String errorMsg
	    if (e instanceof UndeclaredThrowableException) {
		errorMsg = e.undeclaredThrowable.message
	    }
	    else {
		errorMsg = e.message
	    }
	    if (!errorMsg?.trim()) {
		errorMsg ="There was an error running your job '$jobName'. Please contact an administrator."
	    }
	    jobResultsService[jobName]['Exception'] = errorMsg
            updateStatusAndCheckIfJobCancelled jobName, 'Error'
	    return
	}

	//Marking the status as complete makes the
	updateStatusAndCheckIfJobCancelled jobName, 'Completed'
    }

    private void getData() {
	jobDataMap.jobTmpDirectory = jobTmpDirectory
	logger.debug 'RModulesJobService getData directory {}', jobTmpDirectory
	dataExportService.exportData jobDataMap
    }

    private void runConversions() {
	try {
	    //Get the data based on the job configuration.
	    for (currentStep in jobDataMap.conversionSteps) {
		if (currentStep.key == 'R') {
		    //Call a function to process our R commands.
		    runRCommandList(currentStep.value)
		}
	    }
	}
	catch (e) {
	    throw new Exception('Job Failed while running Conversions. '+e.message, e)
	}
    }

    private void runAnalysis() {
	try {
	    for (currentStep in jobDataMap.analysisSteps) {
		switch (currentStep.key) {
		    case 'bundle':
			String zipFileLoc = new File(jobTmpDirectory).parent
			finalOutputFile = zipService.zipFolder(jobTmpDirectory, zipFileLoc + jobDataMap.jobName + '.zip')
			try {
			    File outputFile = new File(zipFileLoc, finalOutputFile)
			    if (outputFile.isFile()) {

				//TODO replace FTPUtil with FTPService from core
				String remoteFilePath = FTPUtil.uploadFile(true, outputFile)
				if (remoteFilePath) {
				    //Since File has been uploaded to the FTP server, we can delete the
				    //ZIP file and the folder which has been zipped

				    //Delete the output Folder
				    String outputFolder = null
				    int index = outputFile.name.lastIndexOf('.')
				    if (index > 0 && index <= outputFile.name.length() - 2 ) {
					outputFolder = outputFile.name.substring(0, index)
				    }
				    File outputDir = new File(zipFileLoc, outputFolder)
				    if (outputDir.isDirectory()) {
					outputDir.deleteDir()
				    }

				    outputFile.delete()
				}
			    }
			}
			catch (e) {
			    logger.error 'Failed to FTP PUT the ZIP file', e
			}
			break
		    case 'R':
			//Call a function to process our R commands.
			runRCommandList currentStep.value
			break
		}
	    }
	}
	catch (e) {
	    throw new Exception('Job Failed while running Analysis. '+e.message, e)
	}
    }

    private void renderOutput(jobDetail) {
	try {
	    for (currentStep in jobDataMap.renderSteps) {
		switch (currentStep.key) {
		    case 'FILELINK':
			String jobName = jobDetail.name
			jobResultsService[jobName]['resultType'] = 'DataExport'
			jobResultsService[jobName]['ViewerURL'] = finalOutputFile
			break

		    case 'GSP':
			String jobName = jobDetail.name
			//Add the link to the output URL to the jobs object. We get the base URL from the job parameters.
			jobResultsService[jobName]['ViewerURL'] = currentStep.value + '?jobName=' + jobName
			break
		}
	    }
	}
	catch (e) {
	    throw new Exception('Job Failed while rendering Output. ', e)
	}
    }

    private void runRCommandList(stepList) {

	//We need to get the study ID for this study so we can know the path to the clinical output file.
	def studies = jobDataMap.studyAccessions

	String rOutputDirectory = jobTmpWorkingDirectory
	new File(rOutputDirectory).mkdir()

	RConnection c = new RConnection(host, port)
	c.stringEncoding = 'utf8'

        //Set the working directory to be our temporary location.
        String workingDirectoryCommand = "setwd('" + RUtil.escapeRStringContent(rOutputDirectory) + "')"

	logger.info 'Attempting following R Command : {}', workingDirectoryCommand

	//Run the R command to set the working directory to our temp directory.
	c.eval workingDirectoryCommand

	//For each R step there is a list of commands.
	for (String currentCommand in stepList) {

	    //Replace the working directory flag if it exists in the string.
	    String reformattedCommand = currentCommand.replace('||PLUGINSCRIPTDIRECTORY||',
							RUtil.escapeRStringContent(grailsApplication.config.RModules.pluginScriptDirectory))
	    reformattedCommand = reformattedCommand.replace('||TEMPFOLDERDIRECTORY||',
							    RUtil.escapeRStringContent(jobTmpDirectory + 'subset1_' + studies[0] + File.separator))
	    reformattedCommand = reformattedCommand.replace('||TOPLEVELDIRECTORY||',
							    RUtil.escapeRStringContent(jobTmpDirectory))

	    for (variableItem in jobDataMap.variableMap) {
                //Try and grab the variable from the Job Data Map. These were fed in from the HTML form.
                String valueFromForm = jobDataMap[variableItem.value]
                valueFromForm = RUtil.escapeRStringContent(valueFromForm ? valueFromForm.trim() : '')
                reformattedCommand = reformattedCommand.replace(variableItem.key, valueFromForm)
	    }

	    logger.info 'Attempting following R Command : {}', reformattedCommand

	    REXP r = c.parseAndEval('try(' + reformattedCommand + ',silent=TRUE)')

	    if (r.inherits('try-error')) {
		String rError = r.asString()
		RserveException newError

		//If it is a friendly error, use that, otherwise throw the default message.
		if(rError ==~ /(?ms).*\|\|FRIENDLY\|\|.*/) {
		    rError = rError.replaceFirst(/(?ms).*\|\|FRIENDLY\|\|/,'')
		    newError = new RserveException(c,rError)
		}
		else {
		    logger.error 'RserveException thrown executing job: {}',rError
		    newError = new RserveException(c,'There was an error running the R script for your job. Please contact an administrator.')
		}

		throw newError
	    }
	}

        c.close()
    }

    /**
     * Helper to update the status of the job and log it and check if the job was Cancelled
     *
     * @param jobName - the unique job name
     * @param status - the new status
     */
    private boolean updateStatusAndCheckIfJobCancelled(String jobName, String status) {
	if (status) {
	    jobResultsService[jobName]['Status'] = status
	}

	asyncJobService.updateStatus(jobName, status,
				     jobResultsService[jobName]['ViewerURL'],
				     jobResultsService[jobName]['AltViewerURL'],
				     jobResultsService[jobName]['Results'])

	boolean jobCancelled = jobResultsService[jobName]['Status'] == 'Cancelled'
	if (jobCancelled) {
	    logger.warn '{} has been cancelled', jobName
	}

	jobCancelled
    }
}
