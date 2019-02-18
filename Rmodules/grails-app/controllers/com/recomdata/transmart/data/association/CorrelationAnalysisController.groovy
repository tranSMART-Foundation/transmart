/*************************************************************************
* Copyright 2008-2012 Janssen Research & Development, LLC.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************/

package com.recomdata.transmart.data.association

class CorrelationAnalysisController {

    RModulesOutputRenderService RModulesOutputRenderService
	
    def index() {}
	
    def correlationAnalysisOutput(String jobName) {
		
	List<String> imageLinks = []
	RModulesOutputRenderService.initializeAttributes(jobName, 'Correlation', imageLinks)

	String correlationData = RModulesOutputRenderService.fileParseLoop(
	    new File(RModulesOutputRenderService.tempDirectory), /.*Correlation.*\.txt/,
	    /.*Correlation(.*)\.txt/, parseCorrelationFile)

	render template: '/plugin/correlationAnalysis_out', contextPath: pluginContextPath, model: [
	    correlationData: correlationData,
	    imageLocations : imageLinks,
	    zipLink        : RModulesOutputRenderService.zipLink]
    }
		
    private Closure parseCorrelationFile = { String inStr ->

	StringBuilder sb = new StringBuilder('<table class="AnalysisResults">')

	int lineCounter = 0
	String[] variables

	for (String line in inStr.readLines()) {
	    //The first line has a list of the variables.
	    if (lineCounter == 0) {
		//The top left most cell is blank.
		sb << '<tr>'
		sb << '<td class="blankCell">&nbsp</td>'
		//Write the variable names across the top.
		variables = line.split()
		for (currentVar in variables) {
		    String printableHeading = currentVar.replace('.', ' ')
		    sb << '<th>' << printableHeading << '</th>'
		}
		sb << '</tr>'
	    }
	    else {
		//The other lines have spaces separating the correlation values.
		sb << '<tr>'
		String printableHeading = variables[lineCounter - 1].replace('.', ' ')
		//Start with the variable name for this row.
		sb << '<th>' << printableHeading << '</th>'

		for (currentValue in line.split()) {
		    sb << '<td>' << currentValue << '</td>'
		}
		sb << '</tr>'
	    }
	    lineCounter++
	}
	sb << '</table>'
	sb
    }
}
