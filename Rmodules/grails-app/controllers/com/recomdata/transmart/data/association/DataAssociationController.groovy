package com.recomdata.transmart.data.association

import grails.converters.JSON

class DataAssociationController {
    
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
        render template: 'dataAssociation', contextPath: pluginContextPath
    }

    def variableSelection(String analysis) {
        render view: '../plugin/' + pluginService.findPluginModuleByModuleName(analysis).formPage
    }

    /**
     * Load required scripts for running analysis
     */
    def loadScripts() {

	List<Map> rows = []

	for (script in scripts) {
	    rows << [path: servletContext.contextPath + pluginContextPath + '/js/' + script, type: 'script']
	}

	for (style in styles) {
	    rows << [path: servletContext.contextPath + pluginContextPath + '/css/' + style, type: 'css']
	}

	render([success: true, totalCount: scripts.size(), files: rows] as JSON)
    }
}
