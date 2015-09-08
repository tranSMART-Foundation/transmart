package smartR.plugin

import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import grails.util.Holders
import grails.util.Environment


class SmartRService {

    def grailsApplication = Holders.grailsApplication
    def springSecurityService
    def quartzScheduler
    def i2b2HelperService
    def dataQueryService

    def jobDataMap = [:]

    /**
    *   Get the working directory for the current user and create it if it doesn't exist
    *
    *   @param {str} user: user to get the working directory for
    *   @return {str}: path of the working directory for the current user
    */
    def getWorkingDir(user) {
        def tempDir = Holders.config.RModules.tempFolderDirectory
        tempDir = tempDir.replace('\\', '/')
        if (! tempDir.endsWith('/')) {
            tempDir += '/'
        }
        def workingDir = tempDir + 'SmartR/' + user + '/'
        new File(workingDir).mkdirs()
        return workingDir
    }

    /**
    *   This function calls the appropriate method for data collection of every concept/input box
    *   and writes the collected data to the filesystem afterwards.
    */
    def getData() {
        def lowDimData_cohort1 = [:]
        def lowDimData_cohort2 = [:]
        
        def rIID1 = jobDataMap['result_instance_id1']
        def rIID2 = jobDataMap['result_instance_id2']
        
        def patientIDs_cohort1 = rIID1 ? i2b2HelperService.getSubjectsAsList(rIID1).collect { it.toLong() } : []
        def patientIDs_cohort2 = rIID2 ? i2b2HelperService.getSubjectsAsList(rIID2).collect { it.toLong() } : []

        jobDataMap['conceptBoxes'].each { conceptBox ->
            conceptBox.cohorts.each { cohort ->
                def rIID
                def data
                def highDimFile
                def patientIDs
    
                if (cohort == 1) {
                    rIID = rIID1
                    patientIDs = patientIDs_cohort1
                    data = lowDimData_cohort1
                    highDimFile = jobDataMap['highDimFile_cohort1']
                } else {
                    rIID = rIID2
                    patientIDs = patientIDs_cohort2
                    data = lowDimData_cohort2
                    highDimFile = jobDataMap['highDimFile_cohort2']
                }
                
                if (! rIID || ! patientIDs) {
                    return
                }
    
                if (conceptBox.concepts.size() == 0) {
                    data[conceptBox.name] = [:]
                } else if (conceptBox.type == 'valueicon' || conceptBox.type == 'alphaicon') {
                    data[conceptBox.name] = dataQueryService.getAllData(conceptBox.concepts, patientIDs)
                } else if (conceptBox.type == 'hleaficon') {
                    dataQueryService.exportHighDimData(
                            conceptBox.concepts,
                            patientIDs,
                            rIID as Long,
                            highDimFile)
                } else {
                    throw new IllegalArgumentException()
                }
            }
        }
        new File(jobDataMap['lowDimFile_cohort1']).write(new JsonBuilder(lowDimData_cohort1).toPrettyString())
        new File(jobDataMap['lowDimFile_cohort2']).write(new JsonBuilder(lowDimData_cohort2).toPrettyString())
    }

    /**
    *   Parses JSON string containing the script settings and writes them to the filesystem
    */
    def writeSettings() {
        new File(jobDataMap['settingsFile']).write(new JsonBuilder(jobDataMap['settings']).toPrettyString())
    }

    /**
    *   Gets the directory where all the R scripts are located
    *
    *   @return {str}: path to the script folder
    */
    def getScriptDir() {
        if (Environment.current == Environment.DEVELOPMENT) {
            return org.codehaus.groovy.grails.plugins.GrailsPluginUtils.getPluginDirForName('smart-r').getFile().absolutePath + '/web-app/Scripts/'
        } else {
            return grailsApplication.mainContext.servletContext.getRealPath('/plugins/') + '/smart-r-0.1/Scripts/'
        }
    }

    /**
    *   Sets the job data map, a map containing all necessary parameters to run the job
    *
    *   @param {str} user: username of the account starting the job
    *   @param {str} jobName: name of the job (usually containing username and timestamp)
    *   @param params: several settings from the currently visible .gsp file
    */
    def initJobDataMap(user, jobName, params) {
        def jobDataMap = new JobDataMap()
        def workingDir = getWorkingDir(user)
        def settings = new JsonSlurper().parseText(params.settings)
        def conceptBoxes = new JsonSlurper().parseText(params.conceptBoxes)

        jobDataMap.put('user', user)
        jobDataMap.put('jobName', jobName)
        jobDataMap.put('result_instance_id1', params.result_instance_id1)
        jobDataMap.put('result_instance_id2', params.result_instance_id2)
        jobDataMap.put('script', params.script)
        jobDataMap.put('scriptDir', getScriptDir())
        jobDataMap.put('init', params.init.toBoolean())
        jobDataMap.put('settings', settings)
        jobDataMap.put('workingDir', workingDir)
        jobDataMap.put('conceptBoxes', conceptBoxes)
        jobDataMap.put('lowDimFile_cohort1', workingDir + 'lowDimData_cohort1.json')
        jobDataMap.put('lowDimFile_cohort2', workingDir + 'lowDimData_cohort2.json')
        jobDataMap.put('highDimFile_cohort1', workingDir + 'highDimData_cohort1.tsv')
        jobDataMap.put('highDimFile_cohort2', workingDir + 'highDimData_cohort2.tsv')
        jobDataMap.put('outputFile', workingDir + 'results.json')
        jobDataMap.put('settingsFile', workingDir + 'settings.json')
        jobDataMap.put('errorFile', workingDir + 'error.log')
        this.jobDataMap = jobDataMap
    }

    /**
    *   Removes files that were possibly created on a previous job run.
    *   It's not entirely necessary to do this, but helps a lot with debugging.
    */
    def cleanUp() {
        def lowDimFile_cohort1 = new File(jobDataMap['lowDimFile_cohort1'])
        if (jobDataMap['init'] && lowDimFile_cohort1.exists()) {
            lowDimFile_cohort1.delete()
        }
        def lowDimFile_cohort2 = new File(jobDataMap['lowDimFile_cohort2'])
        if (jobDataMap['init'] && lowDimFile_cohort2.exists()) {
            lowDimFile_cohort2.delete()
        }
        def highDimFile1 = new File(jobDataMap['highDimFile_cohort1'])
        if (jobDataMap['init'] && highDimFile1.exists()) {
            highDimFile1.delete()
        }
        def highDimFile2 = new File(jobDataMap['highDimFile_cohort2'])
        if (jobDataMap['init'] && highDimFile2.exists()) {
            highDimFile2.delete()
        }
        def outputFile = new File(jobDataMap['outputFile'])
        if (outputFile.exists()) {
            outputFile.delete()
        }
        def settingsFile = new File(jobDataMap['settingsFile'])
        if (settingsFile.exists()) {
            settingsFile.delete()
        }
        def errorFile = new File(jobDataMap['errorFile'])
        if (errorFile.exists()) {
            errorFile.delete()
        }
        assert !jobDataMap['init'] || !lowDimFile_cohort1.exists()
        assert !jobDataMap['init'] || !lowDimFile_cohort2.exists()
        assert !jobDataMap['init'] || !highDimFile1.exists()
        assert !jobDataMap['init'] || !highDimFile2.exists()
        assert !outputFile.exists()
        assert !settingsFile.exists()
        assert !errorFile.exists() || errorFile.text == ''
    }

    /**
    *
    *   This method prepares a job for the execution of the analysis scripts and launches it.
    *   When done it will redirect all necessary data to the controller such that they will be send to the browser.
    *
    *   @param params: several settings from the currently visible .gsp file
    *   @return: all data we want to send to the browser such as success state, results and processed db data
    */
    def runJob(params) {
        def user = springSecurityService.getPrincipal().username
        def time = Calendar.instance.time.time
        def jobName = user + '-' + params.script + '-' + time
        initJobDataMap(user, jobName, params)

        // TODO: It'd be nice to have these calls in the job too, but REST API and QUARTZ don't like each other
        cleanUp()
        writeSettings()
        if (jobDataMap['init']) {
            getData()
        }

        def jobDetail = new JobDetail(jobName, 'SmartR', SmartRJobService.class)
        jobDetail.setJobDataMap(jobDataMap)

        def trigger = new SimpleTrigger('triggerNow' + time, 'SmartR', new Date(), null, 0, 0L)
        quartzScheduler.scheduleJob(jobDetail, trigger)

        while (quartzScheduler.getTriggerState('triggerNow' + time, 'SmartR') == Trigger.STATE_NORMAL) {
            sleep(50)
        }

        def errorLog = new File(jobDataMap['errorFile'])
        if (errorLog.exists() && errorLog.text) {
            return [false, 'ERROR: ' + errorLog.text]
        }

        if (! new File(jobDataMap['outputFile']).exists()) {
            return [false, "ERROR: The script didn't generate any output!"]
        }

        def results = new JsonSlurper().parseText(new File(jobDataMap['outputFile']).text)
        return [true, results]
    }
}
