package org.transmart.plugin.fractalis

import grails.converters.JSON
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

class FractalisController {

	private static final List<String> scripts = ['fractalis', 'resources/fractal-0.3.2.beta.min'].asImmutable()
	private static final List<String> styles = ['fractalis'].asImmutable()

	@Value('${fractalis.dataSource:}')
	private String dataSource

	@Value('${fractalis.node:}')
	private String node

	@Value('${fractalis.resourceName:}')
	private String resourceName

	def i2b2HelperService

	def index() {}

	/**
	 * Called to get the path to fractalis.js such that the plugin can be loaded in the datasetExplorer
	 */
	def loadScripts() {

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
		String resultInstanceID1 = params.result_instance_id1
		String resultInstanceID2 = params.result_instance_id2

		String subjectIDs1 = ''
		String subjectIDs2 = ''
		if (resultInstanceID1?.trim()) {
			subjectIDs1 = i2b2HelperService.getSubjects(resultInstanceID1)
		}
		if (resultInstanceID2?.trim()) {
			subjectIDs2 = i2b2HelperService.getSubjects(resultInstanceID2)
		}

		render([subjectIDs1: subjectIDs1, subjectIDs2: subjectIDs2] as JSON)
	}

	def settings() {
		Authentication auth = SecurityContextHolder.context.authentication
		if (!auth.respondsTo('getJwtToken')) {
			throw new RuntimeException("Unable to retrieve Auth0 token.")
		}
		render([
			dataSource: dataSource,
			node: node,
			resourceName: resourceName,
			token: auth.getJwtToken()
		] as JSON)
	}
}
