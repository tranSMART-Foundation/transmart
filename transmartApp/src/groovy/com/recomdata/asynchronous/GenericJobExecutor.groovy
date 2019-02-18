package com.recomdata.asynchronous

import com.recomdata.transmart.asynchronous.job.AsyncJobService
import com.recomdata.transmart.data.export.DataExportService
import com.recomdata.transmart.data.export.exception.DataNotFoundException
import com.recomdata.transmart.data.export.util.FTPUtil
import com.recomdata.transmart.data.export.util.ZipUtil
import grails.util.Holders
import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.RConnection
import org.rosuda.REngine.Rserve.RserveException
import org.springframework.context.ApplicationContext
import org.transmart.authorization.CurrentUserBeanProxyFactory
import org.transmart.spring.QuartzSpringScope

import java.lang.reflect.UndeclaredThrowableException

/**
 * Encompasses the job scheduled by Quartz. When the execute method is called we
 * will travel down a list of predefined methods to prep data
 *
 * @author MMcDuffie
 */
@Slf4j('logger')
class GenericJobExecutor implements Job {

    private static final String lineSeparator = System.getProperty('line.separator')

    ApplicationContext ctx = Holders.applicationContext
    JobResultsService jobResultsService = ctx.jobResultsService
    DataExportService dataExportService = ctx.dataExportService
    AsyncJobService asyncJobService = ctx.asyncJobService
    QuartzSpringScope quartzSpringScope = ctx.quartzSpringScope

    final String tempFolderDirectory = Holders.config.com.recomdata.plugins.tempFolderDirectory

    String jobTmpDirectory
    //This is where all the R scripts get run, intermediate files are created, images are initially saved, etc.
    String jobTmpWorkingDirectory
    String finalOutputFile

    private JobDataMap jobDataMap
    private String jobName
    private String rModulesHost = Holders.config.RModules.host ?: ''
    private int rModulesPort = (Holders.config.RModules.port ?: -1) as int

    File jobInfoFile

    // TODO -- NEED TO BE REVIEWED (f.guitton@imperial.ac.uk)
    private void init() {
        logger.info '{} has been triggered to run ', jobName

        //Get the data map which shows the attributes for our job.

        //Write our attributes to a log file.
        if (logger.debugEnabled) {
            for (String key in jobDataMap.keys) {
                logger.debug '\t {} -> {}', key, jobDataMap[key]
            }
        }

        logger.info 'Data Export Service: {}', dataExportService
    }

    void execute(JobExecutionContext jobExecutionContext) {
        def userInContext = jobExecutionContext.jobDetail.jobDataMap.userInContext

        // put the user in context
        quartzSpringScope[CurrentUserBeanProxyFactory.SUB_BEAN_QUARTZ] = userInContext

        PersistenceContextInterceptor interceptor =  ctx.persistenceInterceptor
        try {
            interceptor.init()
            doExecute jobExecutionContext.jobDetail
        }
        finally {
            // Thread will be reused, need to clear user in context
            quartzSpringScope.clear()
            interceptor.flush()
            interceptor.destroy()
        }
    }

    private void doExecute(JobDetail jobDetail) {
        //Gather the jobs info.
        jobName = jobDetail.name
        jobDataMap = jobDetail.jobDataMap

        init()

        //Initialize the jobTmpDirectory which will be used during bundling in ZipUtil
        jobTmpDirectory = escapeSlash(tempFolderDirectory + File.separatorChar + jobName + File.separatorChar)
	File tempDir = new File(jobTmpDirectory)
	if (tempDir.exists()) {
            logger.warn 'The job folder {} already exists. It is going to be overwritten.', jobTmpDirectory
            FileUtils.deleteDirectory tempDir
        }
        jobTmpWorkingDirectory = jobTmpDirectory + 'workingDirectory'

        //Try to make the working directory.
        new File(jobTmpWorkingDirectory).mkdirs()

        //Create a file that will have all the job parameters for debugging purposes.
        jobInfoFile = new File(jobTmpWorkingDirectory, 'jobInfo.txt')

        //Write our parameters to the file.
        jobInfoFile.write 'Parameters' + lineSeparator
        for (String key in jobDataMap.keys) {
            jobInfoFile.append '\t' + key + ' -> ' + jobDataMap[key] + lineSeparator
        }

        //JobResult[] jresult
        String sResult
        try {
            //TODO: Possibly abstract this our so the Quartz job doesn't have all this nonsense.
            updateStatus 'Gathering Data'

            if (isJobCancelled()) {
		return
	    }

            getData()

            updateStatus 'Running Conversions'

            if (isJobCancelled()) {
		return
	    }
	    
            runConversions()

            updateStatus  'Running Analysis'

            if (isJobCancelled()) {
		return
	    }
	    
            runAnalysis()

            updateStatus  'Rendering Output'

            if (isJobCancelled()) {
		return
	    }
	    
            renderOutput()
        }
        catch (DataNotFoundException e) {
            logger.error 'DAO exception thrown executing job: {}', e.message, e
            jobResultsService[jobName].Exception = e.message
            return
        }
        catch (RserveException e) {
            logger.error 'RserveException thrown executing job: {}', e.message, e
            jobResultsService[jobName].Exception =
		'There was an error running the R script for your job. Please contact an administrator.'
            return
        }
        catch (e) {
            logger.error 'Exception thrown executing job: {}', e.message, e
            String errorMsg
            if (e instanceof UndeclaredThrowableException) {
                errorMsg = ((UndeclaredThrowableException) e)?.undeclaredThrowable?.message
            }
            else {
                errorMsg = e.message
            }
            if (!errorMsg?.trim()) {
                errorMsg = 'There was an error running your job "' + jobName + '". Please contact an administrator.'
            }
            jobResultsService[jobName].Exception = errorMsg
            return
        }
        finally {
            if (jobResultsService[jobName].Exception) {
                asyncJobService.updateStatus jobName, 'Error', null, null, jobResultsService[jobName].Exception
            }
        }

        updateStatus 'Completed'
    }

    private void getData() {
        jobDataMap.jobTmpDirectory = jobTmpDirectory
	dataExportService.exportData jobDataMap
    }

    private void runConversions() {
        //Get the data based on the job configuration.
        for (currentStep in jobDataMap.conversionSteps) {
	    if (currentStep.key == 'R') {
                    //Call a function to process our R commands.
                    runRCommandList currentStep.value
            }
        }
    }

    private void runAnalysis() {
        //Get the data based on the job configuration.
	for (currentStep in jobDataMap.analysisSteps) {
            switch (currentStep.key) {
                case 'bundle':
                    // Access the ZipUtil in a static way
                    String zipFileLoc = new File(jobTmpDirectory).parent + File.separatorChar
                    finalOutputFile = ZipUtil.zipFolder(jobTmpDirectory, zipFileLoc + jobDataMap.jobName + '.zip')
                    try {
                        File outputFile = new File(zipFileLoc, finalOutputFile)
                        if (outputFile.isFile()) {
                            String remoteFilePath = FTPUtil.uploadFile(true, outputFile)
                            if (remoteFilePath) {
                                //Since File has been uploaded to the FTP server, we can delete the
                                //ZIP file and the folder which has been zipped

                                //Delete the output Folder
                                String outputFolder = null
                                int index = outputFile.name.lastIndexOf('.')
                                if (index > 0 && index <= outputFile.name.length() - 2) {
                                    outputFolder = outputFile.name.substring(0, index)
                                }
                                File outputDir = new File(zipFileLoc, outputFolder)
                                if (outputDir.isDirectory()) {
                                    outputDir.deleteDir()
                                }

                                //Delete the ZIP file
                                outputFile.delete()
                            }
                        }
                    }
                    catch (e) {
                        logger.error 'Failed to FTP PUT the ZIP file: {}', e.message
                    }

                    break
                case 'R':
                    //Call a function to process our R commands.
                    runRCommandListcurrentStep.value
                    break
            }
        }
    }

    private void renderOutput() {
        //Get the data based on the job configuration.
        for (currentStep in jobDataMap.renderSteps) {
            switch (currentStep.key) {
                case 'FILELINK':
                    //Add the result file link to the job.
                    jobResultsService[jobName].resultType = 'DataExport'
                    jobResultsService[jobName].ViewerURL = finalOutputFile
                    asyncJobService.updateStatus jobName, 'Rendering Output', finalOutputFile, null, null
                    break
                case 'GSP':
                    //Add the link to the output URL to the jobs object. We get the base URL from the job parameters.
                    jobResultsService[jobName].ViewerURL = currentStep.value + '?jobName=' + jobName
                    break
            }
        }
    }

    private void runRCommandList(stepList) {

        //We need to get the study ID for this study so we can know the path to the clinical output file.
        def studies = jobDataMap.studyAccessions

        //String representing rOutput Directory.
        String rOutputDirectory = jobTmpWorkingDirectory

        //Make sure an rOutputFiles folder exists in our job directory.
        new File(rOutputDirectory).mkdir()

        //Establish a connection to R Server.
        RConnection c = new RConnection(rModulesHost, rModulesPort)

        //Set the working directory to be our temporary location.
        String workingDirectoryCommand = escapeSlash("setwd('${rOutputDirectory}')")

        logger.debug 'Attempting following R Command : {}', workingDirectoryCommand

        //Run the R command to set the working directory to our temp directory.
        c.eval workingDirectoryCommand 

        //For each R step there is a list of commands.
	for (String currentCommand in stepList) {

            //Need to escape backslashes for R commands.
            String reformattedCommand = escapeSlash(currentCommand)

            //Replace the working directory flag if it exists in the string.
            reformattedCommand = reformattedCommand.replace('||TEMPFOLDERDIRECTORY||',
							    jobTmpDirectory + 'subset1_' + studies[0] + escapeSlash(File.separator))

            //We need to loop through the variable map and do string replacements on the R command.
            for (Map.Entry variableItem in jobDataMap.variableMap) {

                //Try and grab the variable from the Job Data Map. These were fed in from the HTML form.
                def valueFromForm = jobDataMap[variableItem.value]

                //Clean up the variable if it was found in the form.
                if (valueFromForm) {
                    valueFromForm = escapeSlash(valueFromForm).trim()
                }
                else {
                    valueFromForm = ''
                }

                reformattedCommand = reformattedCommand.replace(variableItem.key, valueFromForm)
            }

            logger.debug 'Attempting following R Command : {}', reformattedCommand

            REXP r = c.parseAndEval('try(' + reformattedCommand + ',silent=TRUE)')
            if (r.inherits('try-error')) {
                String rError = r.asString()

                RserveException newError

                //If it is a friendly error, use that, otherwise throw the default message.
                if (rError ==~ /.*\|\|FRIENDLY\|\|.*/) {
                    newError = new RserveException(c, rError.replaceFirst(/.*\|\|FRIENDLY\|\|/, ''))
                }
                else {
                    logger.error 'RserveException thrown executing job: {}', rError
                newError = new RserveException(c, 'There was an error running the R script for your job. Please contact an administrator.')
                }

                c.close()

                throw newError
            }
        }

        c.close()
    }
    
    /**
     * Helper to update the status of the job and log it
     *
     * @param status - the new status
     */
    private void updateStatus(String status) {
        jobResultsService[jobName].Status = status
        logger.debug status
        asyncJobService.updateStatus jobName, status
    }

    private boolean isJobCancelled() {
        //if no job has been submitted, it cannot be cancelled
        if (! jobName) {
	    return false
	}

        if (jobResultsService[jobName].Status == 'Cancelled') {
            logger.warn '{} has been cancelled', jobName
            true
        }
	else {
	    false
	}
    }

    private String escapeSlash(String s) {
	s.replace('\\', '\\\\')
    }
}
