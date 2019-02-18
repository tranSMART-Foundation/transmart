package com.recomdata.transmart.data.association

class AcghFrequencyPlotController {

    RModulesOutputRenderService RModulesOutputRenderService

    def acghFrequencyPlotOutput(String jobName) {
        List<String> imageLinks = []
        RModulesOutputRenderService.initializeAttributes jobName, 'acghFrequencyPlot', imageLinks

        render template: '/plugin/acghFrequencyPlot_out', model: [
	    zipLink: RModulesOutputRenderService.zipLink,
	    imageLinks: imageLinks]
    }

    def imagePath(String jobName) {
        render RModulesOutputRenderService.relativeImageURL + jobName + '/workingDirectory/frequency-plot.png'
    }
}
