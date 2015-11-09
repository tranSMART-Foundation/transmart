package smartR.plugin

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.json.JSONArray
import groovy.json.JsonBuilder
import org.apache.commons.io.FilenameUtils


class SmartRController {

    def smartRService
    def scriptExecutorService

    def index = {
        def dir = smartRService.getWebAppFolder() + '/Scripts/'
        def scriptList = new File(dir).list().findAll { it != 'Wrapper.R' && it != 'Sample.R' }
        [scriptList: scriptList]
    }

    def computeResults = {
        params.init = params.init == null ? true : params.init // defaults to true
        smartRService.runScript(params)
        render ''
    }

    def reComputeResults = {
        params.init = false
        redirect controller: 'SmartR',
                 action: 'computeResults', 
                 params: params
    }

    // For handling results yourself
    def renderResults = {
        params.init = false
        def (success, results) = scriptExecutorService.getResults(params.cookieID)
        if (! success) {
            render new JsonBuilder([error: results]).toString()
        } else {
            render results
        }
    }

    // For (re)drawing the whole visualization
    def renderResultsInTemplate = {
        def (success, results) = scriptExecutorService.getResults(params.cookieID)
        if (! success) {
            render results
        } else {
            render template: "/visualizations/out${FilenameUtils.getBaseName(params.script)}",
                    model: [results: results]
        }       
    }
    
    /**
    *   Renders the input form for initial script parameters
    */
    def renderInputDIV = {
        if (! params.script) {
            render 'Please select a script to execute.'
        } else {
            render template: "/smartR/in${FilenameUtils.getBaseName(params.script)}"
        }
    }

    def renderLoadingScreen = {
        render template: "/visualizations/outLoading"
    }


    /**
    *   Called to get the path to smartR.js such that the plugin can be loaded in the datasetExplorer
    */
    def loadScripts = {
        JSONObject result = new JSONObject()
        JSONObject script = new JSONObject()
        script.put("path", "${servletContext.contextPath}${pluginContextPath}/js/smartR/smartR.js" as String)
        script.put("type", "script")
        result.put("success", true)
        result.put("files", new JSONArray() << script)
        render result as JSON;
    }
}
