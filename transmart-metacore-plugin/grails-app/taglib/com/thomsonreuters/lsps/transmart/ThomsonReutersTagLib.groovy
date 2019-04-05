package com.thomsonreuters.lsps.transmart

class ThomsonReutersTagLib {
		
    MetacoreEnrichmentService metacoreEnrichmentService
		
    def metacoreSettingsButton = { attrs, body ->
	out << render(
	    template: '/metacoreEnrichment/metacoreSettingsButton',
	    model: [settingsMode: metacoreEnrichmentService.metacoreSettingsMode(),
		    settings: metacoreEnrichmentService.getMetacoreParams()])
    }

    def metacoreEnrichmentResult = { attrs, body ->
	out << render(
	    template: '/metacoreEnrichment/enrichmentResult',
	    model: [prefix: 'marker_'])
    }
}
