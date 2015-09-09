package smartR.plugin

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.codehaus.groovy.grails.web.json.JSONArray
import org.apache.commons.io.FilenameUtils


class SmartRController {

    def smartRService

    /**
    *   Renders the default view
    */
    def index = {
        def dir = smartRService.getScriptDir()
        def scriptList = new File(dir).list().findAll { it != 'Wrapper.R' && it != 'Sample.R' }
        [scriptList: scriptList]
    }

    /**
    *   Renders the actual visualization based on the chosen script and the results computed
    */
    def renderOutputDIV = {
        params.init = params.init == null ? true : params.init // defaults to true
        def (success, results) = smartRService.runScript(params)
        if (! success) {
            render results
        } else {
            render template: "/visualizations/out${FilenameUtils.getBaseName(params.script)}",
                    model: [results: results]
        }
    }

    def updateOutputDIV = {
        params.init = false
        def (success, results) = smartRService.runScript(params)
        def answer = success ? results : [error: [results]]
        render answer as JSON
    }

    def recomputeOutputDIV = {
        params.init = false
        redirect controller: 'SmartR',
                 action: 'renderOutputDIV', 
                 params: params
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
