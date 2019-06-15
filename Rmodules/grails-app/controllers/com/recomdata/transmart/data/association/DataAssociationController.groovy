package com.recomdata.transmart.data.association

import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.springframework.core.io.Resource

@Slf4j('logger')
class DataAssociationController {
    def assetResourceLocator

    private static final List<String> scripts = [
	'FormValidator.js',
	'HighDimensionalData.js',
	'RmodulesView.js',
	'dataAssociation.js',
	'PDFGenerator.js',
	'ext/tsmart-overrides.js',
	'ext/tsmart-generic.js',
	'plugin/IC50.js'].asImmutable()
    
    //TODO: requires images: servletContext.contextPath+pluginContextPath+'/css/jquery-ui-1.10.3.custom.css']
    private static final List<String> styles = ['rmodules.css', 'jquery.qtip.min.css'].asImmutable()

    def pluginService

    /**
     * Load the initial DataAssociation page.
     */
    def defaultPage() {
	logger.info 'defaultPage dataAssociation contextPath {}', pluginContextPath
        render template: 'dataAssociation', contextPath: pluginContextPath
    }

    def variableSelection(String analysis) {
	logger.info 'variableSelection view analysis {} ../plugin/ + {}', analysis, pluginService.findPluginModuleByModuleName(analysis).formPage
        render view: servletContext.contextPath + pluginService.findPluginModuleByModuleName(analysis).formPage
    }

    /**
     * Load required scripts for running analysis
     */
    def loadScripts() {

	List<Map> rows = []
	logger.info 'also reporting servletContext.contextPath{}', servletContext.contextPath
	logger.info 'also reporting defaultPage dataAssociation contextPath {}', pluginContextPath
	logger.info 'also reporting variableSelection view analysis {} ../plugin/ + {}', 'PCA', pluginService.findPluginModuleByModuleName('PCA')
	for (String script in scripts) {
	    Resource assetRes = assetResourceLocator.findAssetForURI(script)
//	    Resource resourceRes = assetResourceLocator.findResourceForURI(script)
	    logger.info 'loading DataAssociation script {} asset {}', script, assetRes.getPath()
//	    rows << [path: servletContext.contextPath + pluginContextPath + '/assets/' + script, type: 'script']
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: 'script']
	}

	for (String style in styles) {	
	    Resource assetRes = assetResourceLocator.findAssetForURI(style)
	    logger.info 'loading DataAssociation style {} asset {}', style, assetRes.getPath()
//	    rows << [path: servletContext.contextPath + pluginContextPath + '/assets/' + style, type: 'css']
	    rows << [path: servletContext.contextPath + assetRes.getPath(), type: 'css']
	}

	render([success: true, totalCount: scripts.size(), files: rows] as JSON)
    }
}
