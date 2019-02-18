/*************************************************************************
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License')
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************/

package com.recomdata.transmart.data.association

import org.apache.commons.io.FileUtils

class LogisticRegressionController {

    RModulesOutputRenderService RModulesOutputRenderService
	
    def logisticRegressionOutput(String jobName) {
	List<String> imageLinks = []
	RModulesOutputRenderService.initializeAttributes jobName, 'LogisticRegression', imageLinks

	String logRegSummaryFileName = 'LOGREGSummary.txt'

	File tempDirectory = new File(RModulesOutputRenderService.tempDirectory)

	//TODO move FileUtils to Core
	FileUtils.copyFile new File(tempDirectory, logRegSummaryFileName),
	    new File(tempDirectory, jobName + '/' + logRegSummaryFileName)

	String logRegData = RModulesOutputRenderService.fileParseLoop(tempDirectory,
								      /.*LOGREG_RESULTS.*\.txt/,
								      /.*LOGREG_RESULTS(.*)\.txt/, parseLOGREGStr)

	render template: '/plugin/logisticRegression_out', contextPath: pluginContextPath, model: [
	    imageLocations: imageLinks,
	    LOGREGData    : logRegData,
	    LOGREGSummary : RModulesOutputRenderService.imageURL + jobName + '/' + logRegSummaryFileName,
	    zipLocation   : RModulesOutputRenderService.zipLink,
	    rVersionInfo  : RModulesOutputRenderService.parseVersionFile()]
    }

    private Closure parseLOGREGStr = { String statsInStr ->

	StringBuilder sb = new StringBuilder()

	boolean firstGroup = true
	boolean groupedData = false
	boolean pvalueExtraction = false

	//################################
	//Summary table.
	//################################
	sb << '<span class="AnalysisHeader">Logistic Regression Result</span><br /><br />'

	for (String line in statsInStr.readLines()) {
	    //If we find the name line, we can add it to the buffer.
	    if (line.contains('name=')) {

		//If this isn't our first name field, we need to end the previous HTML table.
		if (!firstGroup) {
		    sb << '</table><br /><br />'
		}

		sb << '<table class="AnalysisResults">'
		sb << '<tr><th>Group</th><td>' << extract(line, 'name=') << '</td></tr>'

		groupedData = true
		firstGroup = false
	    }

	    //Set the boolean indicating the next few lines are pvalue text.
	    if (line.contains('||PVALUES||')) {
		pvalueExtraction = true
	    }

	    if (line.contains('||END||')) {
		sb << '</table><br /><br />'
		pvalueExtraction = false
	    }

	    //If we hit the p value extraction marker, pull the values out.
	    // Coefficients
	    if (line.contains('I.p=') && pvalueExtraction) {
		if (!groupedData) {
		    sb << '<table class="AnalysisRespvalueults" style="table-layout: fixed; width: 60%">'
		}
		sb << '<tr><th>Model</th><th colspan=2>binomial generalized linear model</th>'
		sb << '<th colspan=2>glm(Outcome~Independent)</th></tr>'
		sb << '<tr><th colspan=5>Coefficients</th></tr>'
		sb << '<tr><th></th><th>p-Value</th><th>Estimate</th>'
		sb << '<th>Z Value</th><th>Standard Error</th></tr>'
		sb << '<tr><th>Intercept</th>'
		sb << '<td>' << extract(line, '=') << '</td>'
	    }

	    if (line.contains('I.est=') && pvalueExtraction) {
		sb << '<td>' << extract(line, '=') << '</td>'
	    }

	    if (line.contains('I.zvalue=') && pvalueExtraction) {
		sb << '<td>' << extract(line, '=') << '</td>'
	    }

	    if (line.contains('I.std=') && pvalueExtraction) {
		sb << '<td>' << extract(line, '=') << '</td>'
		sb << '</tr>'
	    }

	    if (line.contains('Y.p=') && pvalueExtraction) {
		sb << '<tr><th>Y</th>'
		sb << '<td>' << extract(line, '=') << '</td>'
	    }

	    if (line.contains('Y.est=') && pvalueExtraction) {
		sb << '<td>' << extract(line, '=') << '</td>'
	    }

	    if (line.contains('Y.zvalue=') && pvalueExtraction) {
		sb << '<td>' << extract(line, '=') << '</td>'
	    }

	    if (line.contains('Y.std=') && pvalueExtraction) {
		sb << '<td>' << extract(line, '=') << '</td>'
		sb << '</tr>'
	    }

	    if (line.contains('X.p=') && pvalueExtraction) {
		sb << '<tr><th>X</th>'
		sb << '<td>' << extract(line, '=') << '</td>'
	    }

	    if (line.contains('X.est=') && pvalueExtraction) {
		sb << '<td>' << extract(line, '=') << '</td>'
	    }

	    if (line.contains('X.zvalue=') && pvalueExtraction) {
		sb << '<td>' << extract(line, '=') << '</td>'
	    }

	    if (line.contains('X.std=') && pvalueExtraction) {
		sb << '<td>' << extract(line, '=') << '</td>'
		sb << '</tr>'
	    }

	    // Deviance Residuals
	    if (line.contains('deviance.resid.min=') && pvalueExtraction) {
		sb << '<tr ><th colspan=5>Deviance Residuals</th></tr>'
		sb << '<tr><th>Minimum</th><th>1st Quartile</th>'
		sb << '<th>Median</th><th>3rd Quartile</th><th>Maximum</th></tr>'
		sb << '<tr><td>' << extract(line, '=') << '</td>'
	    }

	    if (line.contains('deviance.resid.1Q=') && pvalueExtraction) {
		sb << '<td>' << extract(line, '=') << '</td>'
	    }

	    if (line.contains('deviance.resid.med=') && pvalueExtraction) {
		sb << '<td>' << extract(line, '=') << '</td>'
	    }

	    if (line.contains('deviance.resid.3Q=') && pvalueExtraction) {
		sb << '<td>' << extract(line, '=') << '</td>'
	    }

	    if (line.contains('deviance.resid.max=') && pvalueExtraction) {
		sb << '<td>' << extract(line, '=') << '</td>'
		sb << '</tr>'
	    }

	    // Deviance Residuals & degree of freedom
	    if (line.contains('null.resid=:') && pvalueExtraction) {
		String[] parts = line.substring(line.indexOf('=:') + 1).trim().split(':')
		sb << '<tr><td colspan=5><b>Null deviance:</b>'
		sb << parts[1] << ' on ' << parts[2] << ' degrees of freedom</td></tr>'
	    }

	    if (line.contains('deviance.resid=:') && pvalueExtraction) {
		String[] parts = line.substring(line.indexOf('=:') + 1).trim().split(':')
		sb << '<tr><td colspan=5><b>Residual deviance:</b>'
		sb << parts[1] << ' on ' << parts[2] << ' degrees of freedom</td></tr>'
	    }

	    if (line.contains('overall.model.pvalue=') && pvalueExtraction) {
		sb << '<tr><td colspan=5><b>Overall Model p-Value:</b>' << extract(line, '=') << '</td></tr>'
	    }
	}

	sb
    }

    private String extract(String s, String search) {
	s.substring(s.indexOf(search) + search.length()).trim()
    }
}
