package com.recomdata.transmart.data.association

class LineGraphController {

    RModulesOutputRenderService RModulesOutputRenderService

    def lineGraphOutput(String jobName) {
	List<String> imageLinks = []
	RModulesOutputRenderService.initializeAttributes jobName, 'LineGraph', imageLinks

	render template: '/plugin/lineGraph_out', contextPath: pluginContextPath, model: [
	    imageLocations: imageLinks,
	    zipLink: RModulesOutputRenderService.zipLink]
    }
}
