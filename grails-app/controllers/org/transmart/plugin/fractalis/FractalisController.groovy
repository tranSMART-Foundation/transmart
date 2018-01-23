package org.transmart.plugin.fractalis

import org.json.JSONArray
import org.json.JSONObject

class FractalisController {

	def index() {}

	/**
	 *   Called to get the path to fractalis.js such that the plugin can be loaded in the datasetExplorer
	 */
	def loadScripts = {

		// list of required javascript files
		def scripts = [
				servletContext.contextPath + pluginContextPath + '/js/fractalis.js',
				servletContext.contextPath + pluginContextPath + '/js/resources/fractal-0.1.9.min.js'
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
}
