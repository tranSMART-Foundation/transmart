package org.transmart.plugin.fractalis

import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.core.io.Resource

@Slf4j('logger')
class FractalisController {
    def assetResourceLocator

    private static final List<String> scripts = ['fractalis.js'].asImmutable()
    private static final List<String> styles = ['fractalis.css'].asImmutable()

    @Value('${fractalis.dataSource:}')
    private String dataSource

    @Value('${fractalis.node:}')
    private String node

    @Value('${fractalis.resourceName:}')
    private String resourceName

    @Value('${fractalis.version:1.3.0}')
    private String version

    def i2b2HelperService

    def index() {}

    /**
     * Called to get the path to fractalis.js such that the plugin can be loaded in the datasetExplorer
     */
    def loadScripts() {

	List rows = []
	Resource assetRes

	// for all js files
	for (String script in scripts) {
	    assetRes = assetResourceLocator.findAssetForURI(script)
	    logger.info 'Fractalis loadScripts script {} at {}', script, assetRes.getPath()
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: "script"]
	}

	// fractalis script for required version
	assetRes = assetResourceLocator.findAssetForURI("resources/fractalis-${version}.js")
	logger.info 'Fractalis loadScripts version {} script {} at {}',
	    version, "resources/fractalis-${version}.js", assetRes.getPath()
	rows << [path: servletContext.contextPath + assetRes.getPath(), type: "script"]

	// for all css files
	for (String style in styles) {
	    assetRes = assetResourceLocator.findAssetForURI(style)
	    logger.info 'Fractalis loadScripts style {} at {}', style, assetRes.getPath()
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: "css"]
	}

	render([success: true, totalCount: rows.size(), files: rows] as JSON)
    }

    def state() {
	[url: request.scheme + '://' + request.serverName + ':' +
	 request.serverPort + request.contextPath + '/datasetExplorer']
    }

    def patients() {
	String resultInstanceID1 = request.JSON['result_instance_id1']
	String resultInstanceID2 = request.JSON['result_instance_id2']

	String subjectIDs1 = ''
	String subjectIDs2 = ''

	logger.debug 'settings request.JSON {}', request.JSON
	if (resultInstanceID1?.trim()) {
	    subjectIDs1 = i2b2HelperService.getSubjects(resultInstanceID1)
	    logger.debug 'patients subset1 {}: {}', resultInstanceID1, subjectIDs1
	}
	if (resultInstanceID2?.trim()) {
	    subjectIDs2 = i2b2HelperService.getSubjects(resultInstanceID2)
	    logger.debug 'patients subset1 {}: {}', resultInstanceID2, subjectIDs2
	}

	render([subjectIDs1: subjectIDs1, subjectIDs2: subjectIDs2] as JSON)
    }

    def settings() {
	Authentication auth = SecurityContextHolder.context.authentication
	logger.debug 'settings dataSource {} node {} resourceName {} token {}', dataSource, node, resourceName, auth.getJwtToken()
	if (!auth.respondsTo('getJwtToken')) {
	    throw new RuntimeException('Unable to retrieve Auth0 token.')
	}
	render([
	    dataSource: dataSource,
	    node: node,
	    resourceName: resourceName,
	    token: auth.getJwtToken()
	] as JSON)
    }
}
