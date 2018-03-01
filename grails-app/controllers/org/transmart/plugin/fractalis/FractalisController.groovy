package org.transmart.plugin.fractalis

import grails.converters.JSON

class FractalisController {

	def i2b2HelperService

	def index() {}

	/**
	 *   Called to get the path to fractalis.js such that the plugin can be loaded in the datasetExplorer
	 */
	def loadScripts() {

		def scripts = ['fractalis', 'resources/fractal-0.2.0.min']
		def styles = ['fractalis']

		List rows = []

		// for all js files
		for (file in scripts) {
			rows << [path: servletContext.contextPath + pluginContextPath + '/js/' + file + '.js', type: "script"]
		}

		// for all css files
		for (file in styles) {
			rows << [path: servletContext.contextPath + pluginContextPath + '/css/' + file + '.css', type: "css"]
		}

		render([success: true, totalCount: scripts.size(), files: rows] as JSON)
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
