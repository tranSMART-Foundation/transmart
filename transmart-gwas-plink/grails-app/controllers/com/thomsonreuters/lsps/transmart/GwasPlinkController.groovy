package com.thomsonreuters.lsps.transmart

import com.thomsonreuters.lsps.transmart.jobs.GwasPlinkAnalysisJob
import grails.converters.JSON
import groovy.util.logging.Slf4j
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
	List<Map> rows = []

	for (String script in scripts) {
	    Resource assetRes = assetResourceLocator.findAssetForURI(script)
	    logger.info 'GwasPlink loadScripts script {} at {}', script, assetRes.getPath()
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: 'script']
        }

	for (String style in styles) {
	    Resource assetRes = assetResourceLocator.findAssetForURI(style)
	    logger.info 'GwasPlink loadScripts style {} at {}', style, assetRes.getPath()
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: 'css']
	}

	render([success: true, totalCount: rows.size(), files: rows] as JSON)
    }
}
