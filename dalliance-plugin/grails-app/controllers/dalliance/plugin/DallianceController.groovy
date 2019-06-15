package dalliance.plugin

import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.core.io.Resource

@Slf4j('logger')
class DallianceController {
    def assetResourceLocator

    private static final List<String> scripts = ['dalliance-all.js','dalliance.js'].asImmutable()

    private static final List<String> styles = [].asImmutable()

    def index() {
        render (view: 'main')
    }

    def loadScripts = {

        JSONObject result = new JSONObject()
        JSONArray rows = new JSONArray()

        for (String script in scripts) {
	    Resource assetRes = assetResourceLocator.findAssetForURI(script)
	    logger.info 'loading Dalliance script {} asset {}', script, assetRes.getPath()
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: 'script']
        }

 	for (String style in styles) {
	    Resource assetRes = assetResourceLocator.findAssetForURI(style)
	    logger.info 'loading Dalliance style {} asset {}', style, assetRes.getPath()
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: 'css']
	}

	result.put('success', true)
        result.put('totalCount', scripts.size())
        result.put('files', rows)

        response.setContentType('text/json')
        response.outputStream << result.toString()
    }
}
