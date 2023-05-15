package galaxy.export.plugin

import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.springframework.core.io.Resource

@Slf4j('logger')
class GalaxyExportPluginController {
    def assetResourceLocator

    private static final List<String> scripts = ['galaxyExport.js'].asImmutable()
    private static final List<String> styles = [].asImmutable()
    
    /**
     *   Called to get the path to javascript resources such that the plugin can be loaded in the datasetExplorer
     */
    def loadScripts = {

	List<Map> rows = []

        for (String script in scripts) {
	    Resource assetRes = assetResourceLocator.findAssetForURI(script)
	    logger.info 'GalaxyExport loadScripts script {} at {}', script, assetRes.getPath()
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: 'script']
        }

        for (String style in styles) {
	    Resource assetRes = assetResourceLocator.findAssetForURI(style)
	    logger.info 'GalaxyExport loadScripts style {} at {}', style, assetRes.getPath()
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: 'css']
        }

	render([success: true, totalCount: rows.size(), files: rows] as JSON)
    }
}
