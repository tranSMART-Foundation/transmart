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

import groovy.util.logging.Slf4j
import java.util.regex.Matcher

@Slf4j('logger')
class SurvivalAnalysisController {
	
    RModulesOutputRenderService RModulesOutputRenderService

    def survivalAnalysisOutput(String jobName) {
	List<String> imageLinks = []
	RModulesOutputRenderService.initializeAttributes jobName, 'SurvivalCurve', imageLinks

	File tempDirectory = new File(RModulesOutputRenderService.tempDirectory)

	//Parse the output files.
	String legendText = RModulesOutputRenderService.fileParseLoop(tempDirectory,
								      /.*legend.*\.txt/, /.*legend(.*)\.txt/, parseLegendTable)

	//Parse the output files. If cox data isn't there, send an HTML message indicating that instead.
	String coxData = RModulesOutputRenderService.fileParseLoop(tempDirectory,
								   /.*CoxRegression_result.*\.txt/,
								   /.*CoxRegression_result(.*)\.txt/, parseCoxRegressionStr)

	if (!coxData) {
	    coxData = 'No Cox Data available for the given analysis.'
	}

	String survivalData = RModulesOutputRenderService.fileParseLoop(tempDirectory,
									/.*SurvivalCurve.*FitSummary.*\.txt/,
									/.*SurvivalCurve(.*)FitSummary\.txt/, parseSurvivalCurveSummary)

	render template: '/plugin/survivalAnalysis_out', contextPath: pluginContextPath, model: [
	    legendText   : legendText,
	    imageLocation: imageLinks,
	    coxData      : coxData,
	    survivalData : survivalData,
	    zipLink      : RModulesOutputRenderService.zipLink]
    }

    private Closure parseCoxRegressionStr = { String inStr ->

	StringBuilder sb = new StringBuilder()

	boolean nextLineHazard = false
	boolean nextLine95 = false

	Map<String, Map<String, String>> resultsItems = [:]

	sb << '<table class="AnalysisResults">'
	for (String line in inStr.readLines()) {

	    if (line.contains('n=')) {
		//This matches the lines in the Survival Cox Regression summary
		Matcher matcher = line =~ /\s*n\=\s*([0-9]+)\,\s*number of events\=\s*([0-9]+)\s*/
		if (matcher.matches()) {
		    //Add a table with overall number of subjects and events.
		    sb << '<tr><th>Number of Subjects</th><td>' << matcher[0][1] << '</td></tr>'
		    sb << '<tr><th>Number of Events</th><td>' << matcher[0][2] << '</td></tr>'
		}
	    }
	    else if (line.contains('se(coef)')) {
		//If we encounter the header for the hazard data, set a flag so we can pick it up on the next pass.
		nextLineHazard = true
		nextLine95 = false
		// To ensure that next records containing 'classList' are interpreted by the right piece of code
	    }
	    else if (line.contains('classList') && nextLineHazard) {
		String[] resultArray = line.split()

		String groupName = resultArray[0].replace('classList', '').replace('_', ' ')

		resultsItems[groupName] = [COX: resultArray[1], HAZARD: resultArray[2]]
	    }
	    else if (line.contains('lower')) {
		nextLine95 = true
		///In some cases (i.e. no significance codes) a line with '---' (indicator for the end of hazard data) is not present and therefore nextLineHazard was not set to false
		// If the header for the 95 data is encountered and nextLine95 is set, unconditionally reset nextLineHazard to avoid next records containing 'classList' are interpreted by the wrong piece of code
		nextLineHazard = false
	    }
	    else if (line.contains('classList') && nextLine95) {
		String[] resultArray = line.split()

		String groupName = resultArray[0].replace('classList', '').replace('_', ' ')

		resultsItems[groupName].UP = resultArray[3]
		resultsItems[groupName].DOWN = resultArray[4]
	    }
	    else if (line.contains('Likelihood ratio test')) {
		Matcher likTestMatcher = line =~ /\s*Likelihood\s*ratio\s*test\s*\=\s*(.*)/
		if (likTestMatcher.matches()) {
		    sb << '<tr><th>Likelihood ratio test </th><td>' << likTestMatcher[0][1] << '</td></tr>'
		}
	    }
	    else if (line.contains('Wald test')) {
		Matcher waldTestMatcher = line =~ /\s*Wald\s*test\s*\=\s*(.*)/
		if (waldTestMatcher.matches()) {
		    sb << '<tr><th>Wald test</th><td>' << waldTestMatcher[0][1] << '</td></tr>'
		}
	    }
	    else if (line.contains('Score')) {
		Matcher scoreTestMatcher = line =~ /\s*Score\s*\(logrank\)\s*test\s*\=(.*)/
		if (scoreTestMatcher.matches()) {
		    sb << '<tr><th>Score (logrank) test</th><td>' << scoreTestMatcher[0][1] << '</td></tr>'
		}
	    }
	}
	sb << '</table><br /><br />'

	sb << '<table class="AnalysisResults">'

	sb << '<tr>'
	sb << '<th>Subset</th>'
	sb << '<th>Cox Coefficient</th>'
	sb << '<th>Hazards Ratio</th>'
	sb << '<th>Lower Range of Hazards Ratio, 95% Confidence Interval</th>'
	sb << '<th>Upper Range of Hazards Ratio, 95% Confidence Interval</th>'
	sb << '</tr>'

	for (resultItem in resultsItems) {
	    sb << '<tr>'
	    sb << '<th>' << resultItem.key << '</th>'
	    sb << '<td>' << resultItem.value.COX << '/td>'
	    sb << '<td>' << resultItem.value.HAZARD << '</td>'
	    sb << '<td>' << resultItem.value.UP << '</td>'
	    sb << '<td>' << resultItem.value.DOWN << '</td>'
	    sb << '</tr>'
	}

	sb << '</table>'

	sb
    }

    private Closure parseSurvivalCurveSummary = { String inStr ->

	StringBuilder bufHeader = new StringBuilder()
	StringBuilder bufBody = new StringBuilder()

	//This tells us if the next line contains the actual records.
	boolean recordsLine = false

	bufHeader << '<table class="AnalysisResults"><tr><th>Subset</th>'
	bufHeader << '<th>Number of Subjects</th><th>Max Subjects</th>'
	bufHeader << '<th>Subjects at Start</th><th>Number of Events</th>'
	bufHeader << '<th>Median Time Value</th><th>Lower Range of Time Variable, 95% Confidence Interval</th>'
	bufHeader << '<th>Upper Range of Time Variable, 95% Confidence Interval</th></tr>'

	for (String line in inStr.readLines()) {

	    //Loop through the classes and get the information if we are past the records line.
	    if (recordsLine) {

		String[] strArray = line.split()
		int columnCount = strArray.size()
		int columnStart = 1
		int iskip

		//For each class, extract the name.
		if (strArray[0].contains('classList=')) {
		    bufBody << '<tr><th>' << strArray[0].replace('classList=', '').replace('_', ' ') << '</th>'
		    iskip = 8 - columnCount
		}
		else {
		    bufBody << '<tr><th>All Subjects</th>'
		    columnStart = 0
		    iskip = 7 - columnCount
		}

		for (int i = columnStart; i < columnCount; i++) {
		    String value = strArray[i]

		    if (value.contains('Inf')) {
			value = 'infinity'
		    }
		    bufBody << '<td>' << value << '</td>'
		    // Fill in duplicates for missing columns at start of values
		    while (iskip) {
			bufBody << '<td>' << value << '</td>'
			iskip--
		    }
		}

		bufBody << '</tr>'
	    }

	    //If we get records in the line, then we know the records are on the next line.
	    if (line.contains(' events ')) {
		recordsLine = true
	    }
	}

	bufHeader << bufBody
	bufHeader << '</table>'

	bufHeader
    }

    private String parseLegendTable = { String legendInStr ->

	StringBuilder sb = new StringBuilder()

	sb << '<span class="AnalysisHeader">Legend</span><br /><br />'
	sb << '<table class="AnalysisResults">'

	for (String line in legendInStr.readLines()) {
	    sb << '<tr>'

	    for (tableValue in line.split('\t')) {
		sb << '<th>' << tableValue << '</th>'
	    }

	    sb << '</tr>'
	}

	sb << '</table><br />'

	sb
    }
}
