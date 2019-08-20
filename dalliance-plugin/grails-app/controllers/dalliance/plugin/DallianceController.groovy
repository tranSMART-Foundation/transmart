package dalliance.plugin

import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.springframework.core.io.Resource

@Slf4j('logger')
class DallianceController {
    def assetResourceLocator

    private static final List<String> scripts = ['build/dalliance-all.js','head/dalliance.js'].asImmutable()

    private static final List<String> styles = ['dalliance.css', 'dalliance-scoped.css'].asImmutable()

    def index() {
        render (view: 'main')
    }

    def loadScripts = {
	List<Map> rows = []

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

	render([success: true, totalCount: rows.size(), files: rows] as JSON)
   }
}
