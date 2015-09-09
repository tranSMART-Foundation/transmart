package smartR.plugin

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import grails.util.Holders
import grails.util.Environment


class SmartRService {

    def grailsApplication = Holders.grailsApplication
    def springSecurityService
    def quartzScheduler
    def i2b2HelperService
    def dataQueryService
    def scriptExecutorService

    def parameterMap = [:]

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
        def workingDir = "${tempDir}SmartR/${user}/"
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
        
        def rIID1 = parameterMap['result_instance_id1']
        def rIID2 = parameterMap['result_instance_id2']
        
        def patientIDs_cohort1 = rIID1 ? i2b2HelperService.getSubjectsAsList(rIID1).collect { it.toLong() } : []
        def patientIDs_cohort2 = rIID2 ? i2b2HelperService.getSubjectsAsList(rIID2).collect { it.toLong() } : []

        parameterMap['conceptBoxes'].each { conceptBox ->
            conceptBox.cohorts.each { cohort ->
                def rIID
                def data
                def highDimFile
                def patientIDs
    
                if (cohort == 1) {
                    rIID = rIID1
                    patientIDs = patientIDs_cohort1
                    data = lowDimData_cohort1
                    highDimFile = parameterMap['highDimFile_cohort1']
                } else {
                    rIID = rIID2
                    patientIDs = patientIDs_cohort2
                    data = lowDimData_cohort2
                    highDimFile = parameterMap['highDimFile_cohort2']
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
        new File(parameterMap['lowDimFile_cohort1'])
            .write(new JsonBuilder(lowDimData_cohort1).toPrettyString())
        new File(parameterMap['lowDimFile_cohort2'])
            .write(new JsonBuilder(lowDimData_cohort2).toPrettyString())
    }

    /**
    *   Gets the directory where all the R scripts are located
    *
    *   @return {str}: path to the script folder
    */
    def getScriptDir() {
        if (Environment.current == Environment.DEVELOPMENT) {
            return org.codehaus.groovy.grails.plugins.GrailsPluginUtils
                .getPluginDirForName('smart-r')
                .getFile()
                .absolutePath + '/web-app/Scripts/'
        } else {
            return grailsApplication
                .mainContext
                .servletContext
                .getRealPath('/plugins/') + '/smart-r-0.1/Scripts/'
        }
    }

    def initParameterMap(params) {
        def parameterMap = [:]
        def user = springSecurityService.getPrincipal().username
        def workingDir = getWorkingDir(user)
        def conceptBoxes = new JsonSlurper().parseText(params.conceptBoxes)
        parameterMap.put('user', user)
        parameterMap.put('result_instance_id1', params.result_instance_id1)
        parameterMap.put('result_instance_id2', params.result_instance_id2)
        parameterMap.put('script', params.script)
        parameterMap.put('scriptDir', getScriptDir())
        parameterMap.put('init', params.init.toBoolean())
        parameterMap.put('settings', params.settings)
        parameterMap.put('workingDir', workingDir)
        parameterMap.put('conceptBoxes', conceptBoxes)
        parameterMap.put('lowDimFile_cohort1', workingDir + 'lowDimData_cohort1.json')
        parameterMap.put('lowDimFile_cohort2', workingDir + 'lowDimData_cohort2.json')
        parameterMap.put('highDimFile_cohort1', workingDir + 'highDimData_cohort1.tsv')
        parameterMap.put('highDimFile_cohort2', workingDir + 'highDimData_cohort2.tsv')
        this.parameterMap = parameterMap
    }

    def cleanUp() {
        def lowDimFile_cohort1 = new File(parameterMap['lowDimFile_cohort1'])
        if (parameterMap['init'] && lowDimFile_cohort1.exists()) {
            lowDimFile_cohort1.delete()
        }
        def lowDimFile_cohort2 = new File(parameterMap['lowDimFile_cohort2'])
        if (parameterMap['init'] && lowDimFile_cohort2.exists()) {
            lowDimFile_cohort2.delete()
        }
        def highDimFile_cohort1 = new File(parameterMap['highDimFile_cohort1'])
        if (parameterMap['init'] && highDimFile_cohort1.exists()) {
            highDimFile_cohort1.delete()
        }
        def highDimFile_cohort2 = new File(parameterMap['highDimFile_cohort2'])
        if (parameterMap['init'] && highDimFile_cohort2.exists()) {
            highDimFile_cohort2.delete()
        }
        assert !parameterMap['init'] || !lowDimFile_cohort1.exists()
        assert !parameterMap['init'] || !lowDimFile_cohort2.exists()
        assert !parameterMap['init'] || !highDimFile_cohort1.exists()
        assert !parameterMap['init'] || !highDimFile_cohort2.exists()
    }


    def runScript(params) {
        initParameterMap(params)

        cleanUp()

        if (parameterMap['init']) {
            getData()
        }

        return scriptExecutorService.run(parameterMap)
    }
}
