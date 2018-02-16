package org.transmart.plugin.fractalis

import grails.converters.JSON
import org.json.JSONArray
import org.json.JSONObject

class FractalisController {

	def i2b2HelperService

	def index() {}

	/**
	 *   Called to get the path to fractalis.js such that the plugin can be loaded in the datasetExplorer
	 */
	def loadScripts = {

		// list of required javascript files
		def scripts = [
				servletContext.contextPath + pluginContextPath + '/js/fractalis.js',
				servletContext.contextPath + pluginContextPath + '/js/resources/fractal-0.2.0.min.js'
		]
		// list of required css files
		def styles = [
                servletContext.contextPath + pluginContextPath + '/css/fractalis.css'
		]

		JSONObject result = new JSONObject()
		JSONArray rows = new JSONArray()

		// for all js files
		for (file in scripts) {
			def m = [:]
			m["path"] = file.toString()
			m["type"] = "script"
			rows.put(m)
		}

		// for all css files
		for (file in styles) {
			def n = [:]
			n["path"] = file.toString()
			n["type"] = "css"
			rows.put(n)
		}

		result.put("success", true)
		result.put("totalCount", scripts.size())
		result.put("files", rows)

		render result.toString()
	}

    def state() {
        [url: request.scheme + '://' + request.serverName + ':' +
                request.serverPort + request.contextPath + '/datasetExplorer']
    }

	def patients() {
		def resultInstanceID1 = request.getParameter('result_instance_id1')
		def resultInstanceID2 = request.getParameter('result_instance_id2')

        def subjectIDs1 = ''
        def subjectIDs2 = ''
		if (resultInstanceID1 != null && resultInstanceID1.trim().length() != 0) {
            subjectIDs1 = i2b2HelperService.getSubjects(resultInstanceID1)
		}
		if (resultInstanceID2 != null && resultInstanceID2.trim().length() != 0) {
			subjectIDs2 = i2b2HelperService.getSubjects(resultInstanceID2)
		}

        def response = ['subjectIDs1': subjectIDs1, 'subjectIDs2': subjectIDs2]
		render response as JSON
	}
}
