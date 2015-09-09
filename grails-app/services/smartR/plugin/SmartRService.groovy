package smartR.plugin

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
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
    *   This function calls the appropriate method for data collection of every concept/input box
    *   and writes the collected data to the filesystem afterwards.
    */
    def getData() {
        def data_cohort1 = [:]
        def data_cohort2 = [:]
        
        def rIID1 = parameterMap['result_instance_id1']
        def rIID2 = parameterMap['result_instance_id2']
        
        def patientIDs_cohort1 = rIID1 ? i2b2HelperService.getSubjectsAsList(rIID1).collect { it.toLong() } : []
        def patientIDs_cohort2 = rIID2 ? i2b2HelperService.getSubjectsAsList(rIID2).collect { it.toLong() } : []

        parameterMap['conceptBoxes'].each { conceptBox ->
            conceptBox.cohorts.each { cohort ->
                def rIID
                def data
                def patientIDs
    
                if (cohort == 1) {
                    rIID = rIID1
                    patientIDs = patientIDs_cohort1
                    data = data_cohort1
                } else {
                    rIID = rIID2
                    patientIDs = patientIDs_cohort2
                    data = data_cohort2
                }
                
                if (! rIID || ! patientIDs) {
                    return
                }
    
                if (conceptBox.concepts.size() == 0) {
                    data[conceptBox.name] = [:]
                } else if (conceptBox.type == 'valueicon' || conceptBox.type == 'alphaicon') {
                    data[conceptBox.name] = dataQueryService.getAllData(conceptBox.concepts, patientIDs)
                } else if (conceptBox.type == 'hleaficon') {
                    def rawData = dataQueryService.exportHighDimData(
                            conceptBox.concepts,
                            patientIDs,
                            rIID as Long)
                     data[conceptBox.name] = rawData
                } else {
                    throw new IllegalArgumentException()
                }
            }
        }

        parameterMap['data_cohort1'] = JsonOutput.toJson(data_cohort1)
        parameterMap['data_cohort2'] = JsonOutput.toJson(data_cohort2)
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
        def init = params.init.toBoolean()
        def user = springSecurityService.getPrincipal().username
        def conceptBoxes = new JsonSlurper().parseText(params.conceptBoxes)
        parameterMap['init'] = init
        parameterMap['user'] = user
        parameterMap['script'] = params.script
        parameterMap['scriptDir'] = getScriptDir()
        parameterMap['result_instance_id1'] = params.result_instance_id1
        parameterMap['result_instance_id2'] = params.result_instance_id2
        parameterMap['settings'] = params.settings
        parameterMap['conceptBoxes'] = conceptBoxes
    }

    def runScript(params) {
        initParameterMap(params)

        if (parameterMap['init']) {
            getData()
        }

        return scriptExecutorService.run(parameterMap)
    }
}
