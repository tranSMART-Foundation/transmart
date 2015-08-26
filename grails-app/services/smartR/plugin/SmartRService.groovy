package smartR.plugin

import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import grails.util.Holders
import groovy.util.FileNameFinder


class SmartRService {

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
        if (tempDir[-1] != '/') {
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
        def data = [:]
        def rIID1 = jobDataMap['result_instance_id1']
        def rIID2 = jobDataMap['result_instance_id2']
        def cohort1 = rIID1 ? i2b2HelperService.getSubjectsAsList(rIID1).collect { it.toLong() } : []
        def cohort2 = rIID2 ? i2b2HelperService.getSubjectsAsList(rIID2).collect { it.toLong() } : []
        def cohorts = [cohort1, cohort2]

        jobDataMap['conceptBoxes'].each { conceptBox ->
            def name = conceptBox.name
            def cohort = conceptBox.cohort
            def type = conceptBox.type
            def concepts = conceptBox.concepts

            def rIID
            def highDimFile
            def patientIDs

            if (cohort == 1) {
                rIID = rIID1
                patientIDs = cohorts[0]
                highDimFile = jobDataMap['highDimFile_cohort1']
            } else {
                rIID = rIID2
                patientIDs = cohorts[1]
                highDimFile = jobDataMap['highDimFile_cohort2']
            }

            if (type == 'valueicon' || type == 'alphaicon') {
                data[name] = dataQueryService.getAllData(concepts, patientIDs)
            } else if (type == 'hleaficon') {
                def tsvFiles = dataQueryService.exportHighDimData(
                        concepts,
                        rIID as Long,
                        new File(jobDataMap['workingDir']),
                        'TSV',
                        'mrna')
                assert tsvFiles.size() == 1
                assert new File(tsvFiles[0]).renameTo(new File(highDimFile))
            } else if (type == 'null') {
                data[name] = [:]
            }
            else {
                throw new IllegalArgumentException()
            }
        }
        new File(jobDataMap['lowDimFile']).write(new JsonBuilder(data).toPrettyString())
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
        return org.codehaus.groovy.grails.plugins.GrailsPluginUtils.getPluginDirForName('smart-r').getFile().absolutePath + '/web-app/Scripts/'
    }

    /**
    *   Sets the job data map, a map containing all neccessary parameters to run the job
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
        jobDataMap.put('lowDimFile', workingDir + 'data.json') // both cohorts are saved in the same file, unlike highdim
        jobDataMap.put('highDimFile_cohort1', workingDir + 'highdim_cohort1.tsv')
        jobDataMap.put('highDimFile_cohort2', workingDir + 'highdim_cohort2.tsv')
        jobDataMap.put('outputFile', workingDir + 'results.json')
        jobDataMap.put('settingsFile', workingDir + 'settings.json')
        jobDataMap.put('errorFile', workingDir + 'error.log')
        this.jobDataMap = jobDataMap
    }

    /**
    *   Removes files that were possibly created on a previous job run.
    *   It's not entirely neccessary to do this, but helps a lot with debugging.
    */
    def cleanUp() {
        def lowDimFile = new File(jobDataMap['lowDimFile'])
        if (jobDataMap['init'] && lowDimFile.exists()) {
            lowDimFile.delete()
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
        assert !jobDataMap['init'] || !lowDimFile.exists()
        assert !jobDataMap['init'] || !highDimFile1.exists()
        assert !jobDataMap['init'] || !highDimFile2.exists()
        assert !outputFile.exists()
        assert !settingsFile.exists()
        assert !errorFile.exists() || errorFile.text == ''
    }

    /**
    *
    *   This method prepares a job for the execution of the analysis scripts and launches it.
    *   When done it will redirect all neccessary data to the controller such that they will be send to the browser.
    *
    *   @param params: several settings from the currently visible .gsp file
    *   @return: all data we want to send to the browser such as success state, results and proccessed db data
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
