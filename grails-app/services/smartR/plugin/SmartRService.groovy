package smartR.plugin

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import grails.util.Holders
import grails.util.Environment


class SmartRService {

    def DEBUG = Environment.current == Environment.DEVELOPMENT
    def DEBUG_TMP_DIR = '/tmp/'

    def grailsApplication = Holders.grailsApplication
    def springSecurityService
    def i2b2HelperService
    def dataQueryService
    def scriptExecutorService

    def queryData(parameterMap) {
        def data_cohort1 = [:]
        def data_cohort2 = [:]
        
        def rIID1 = parameterMap['result_instance_id1'].toString()
        def rIID2 = parameterMap['result_instance_id2'].toString()

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

        parameterMap['data_cohort1'] = new JsonBuilder(data_cohort1).toString()
        parameterMap['data_cohort2'] = new JsonBuilder(data_cohort2).toString()

        if (DEBUG) {
            new File(DEBUG_TMP_DIR + 'data1.json').write(parameterMap['data_cohort1'])
            new File(DEBUG_TMP_DIR + 'data2.json').write(parameterMap['data_cohort2'])
        }

        return parameterMap
    }

    /**
    *   Gets the directory where all the R scripts are located
    *
    *   @return {str}: path to the script folder
    */
    def getWebAppFolder() {
        if (Environment.current == Environment.DEVELOPMENT) {
            return org.codehaus.groovy.grails.plugins.GrailsPluginUtils
                .getPluginDirForName('smart-r')
                .getFile()
                .absolutePath + '/web-app/'
        } else {
            return grailsApplication
                .mainContext
                .servletContext
                .getRealPath('/plugins/') + '/smart-r-0.1/'
        }
    }

    def createParameterMap(params) {
        def parameterMap = [:]
        parameterMap['init'] = params.init.toBoolean()
        parameterMap['script'] = params.script
        parameterMap['scriptDir'] = getWebAppFolder() + '/Scripts/'
        parameterMap['result_instance_id1'] = params.result_instance_id1
        parameterMap['result_instance_id2'] = params.result_instance_id2
        parameterMap['settings'] = params.settings
        parameterMap['conceptBoxes'] = new JsonSlurper().parseText(params.conceptBoxes)
        parameterMap['cookieID'] = params.cookieID
        parameterMap['DEBUG'] = DEBUG
        return parameterMap
    }

    def runScript(params) {
        def parameterMap = createParameterMap(params)

        if (parameterMap['init']) {
            parameterMap = queryData(parameterMap)
        }

        return scriptExecutorService.run(parameterMap)
    }
}
