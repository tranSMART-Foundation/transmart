package com.recomdata.transmart.data.association

class aCGHSurvivalAnalysisController {

    RModulesOutputRenderService RModulesOutputRenderService

    def index() {}

    def aCGHSurvivalAnalysisOutput(String jobName) {
        List<String> imageLinks = []
	RModulesOutputRenderService.initializeAttributes jobName, 'aCGHSurvivalAnalysis', imageLinks

	render template: '/plugin/aCGHSurvivalAnalysis_out'
    }
}
