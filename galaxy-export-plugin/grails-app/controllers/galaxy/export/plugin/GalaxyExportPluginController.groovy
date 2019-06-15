package galaxy.export.plugin

import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
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

        JSONObject result = new JSONObject()
        JSONArray rows = new JSONArray()

        for (String script in scripts) {
	    Resource assetRes = assetResourceLocator.findAssetForURI(script)
	    logger.info 'loading GalaxyExport script {} asset{}', script, assetRes.getPath()
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: 'script']
        }

        for (String style in styles) {
	    Resource assetRes = assetResourceLocator.findAssetForURI(style)
	    logger.info 'loading GalaxyExport style {} asset {}', style, assetRes.getPath()
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: 'css']
        }

        result.put('success', true)
        result.put('totalCount', scripts.size())
        result.put('files', rows)

        render result as JSON
    }
}
