package smartR.plugin

import grails.converters.JSON
import groovy.json.JsonBuilder
import org.apache.commons.io.FilenameUtils
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject


class SmartRController {

    def smartRService

    /**
    *   Renders the default view
    */
    def index = {
        [scriptList: smartRService.getScriptList()]
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
            render template: "/heimVisualizations/out${FilenameUtils.getBaseName(params.script)}",
                    model: [results: results]
        }
    }

    def updateOutputDIV = {
        params.init = false
        def (success, results) = smartRService.runScript(params)
        if (! success) {
            render new JsonBuilder([error: results]).toString()
        } else {
            render results
        }
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
            render template: "/heim/in${FilenameUtils.getBaseName(params.script)}"
        }
    }

    def renderLoadingScreen = {
        render template: "/visualizations/outLoading"
    }

    /**
    *   Called to get the path to smartR.js such that the plugin can be loaded in the datasetExplorer
    */
    def loadScripts = {

        // list of required javascripts
        def scripts = [servletContext.contextPath + pluginContextPath + '/js/smartR/smartR.js']

        // list of required css
        def styles = [servletContext.contextPath+pluginContextPath+'/css/smartR.css']

        JSONObject result = new JSONObject()
        JSONArray rows = new JSONArray()

        // for all js files
        for (file in scripts) {
            def m = [:]
            m["path"] = file.toString()
            m["type"] = "script"
            rows.put(m);
        }

        // for all css files
        for (file in styles) {
            def n = [:]
            n["path"] = file.toString()
            n["type"] = "css"
            rows.put(n);
        }

        result.put("success", true)
        result.put("totalCount", scripts.size())
        result.put("files", rows)

        render result as JSON;
    }
}
