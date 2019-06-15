package com.thomsonreuters.lsps.transmart

import com.thomsonreuters.lsps.transmart.jobs.GwasPlinkAnalysisJob
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.core.io.Resource

@Slf4j('logger')
class GwasPlinkController {

    private static final List<String> scripts = ['gwasPlinkAdd.js'].asImmutable()
    private static final List<String> styles = [].asImmutable()

    def gwasPlinkAnalysisService
    def assetResourceLocator

    def show() {
    }

    def resultOutput() {
        int previewRowsCount = (params.previewRowsCount ?: 10).toInteger()
        [
                previewData     : gwasPlinkAnalysisService.getPreviewData(params.jobName as String, params.previewFileName as String, previewRowsCount),
                zipLink         : '/analysisFiles/' + params.jobName + '/' + gwasPlinkAnalysisService.prepareZippedResult(params.jobName as String, params.analysisName as String),
                previewRowsCount: previewRowsCount
        ]
    }

    def scheduleJob() {
        def result = gwasPlinkAnalysisService.createAnalysisJob(params, 'GWASPlink', GwasPlinkAnalysisJob, false)
        render text: result.toString(), contentType: 'application/json'
    }

    def loadScripts() {
        JSONObject result = new JSONObject()
        JSONArray rows = new JSONArray()

	for (String script in scripts) {
	    Resource assetRes = assetResourceLocator.findAssetForURI(script)
	    logger.info 'loading GwasPlink script {} asset {}', script, assetRes.getPath()
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: 'script']
        }

	for (String style in styles) {
	    Resource assetRes = assetResourceLocator.findAssetForURI(style)
	    logger.info 'loading GwasPlink style {} asset {}', style, assetRes.getPath()
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: 'css']
	}

        result.put('success', true)
        result.put('totalCount', rows.size())
        result.put('files', rows)

        response.setContentType('text/json')
        response.outputStream << result.toString()
    }
}
