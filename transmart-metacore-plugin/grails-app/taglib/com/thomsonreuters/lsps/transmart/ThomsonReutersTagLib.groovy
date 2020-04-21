package com.thomsonreuters.lsps.transmart

import groovy.util.logging.Slf4j

@Slf4j('logger')
class ThomsonReutersTagLib {
		
    MetacoreEnrichmentService metacoreEnrichmentService
		
    def metacoreSettingsButton = { attrs, body ->
	logger.info 'metacoreSettingsButton with plugin'
	out << render(
	    template: '/metacoreEnrichment/metacoreSettingsButton',
	    model: [settingsMode: metacoreEnrichmentService.metacoreSettingsMode(),
		    settings: metacoreEnrichmentService.getMetacoreParams()],
		    plugin: 'transmart-metacore-plugin')
    }

    def metacoreEnrichmentResult = { attrs, body ->
	logger.info 'metacoreEnrichmentResult with plugin'
	out << render(
	    template: '/metacoreEnrichment/enrichmentResult',
	    model: [prefix: 'marker_'],
	    plugin: 'transmart-metacore-plugin')
    }
}
