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

class ForestPlotController {

	RModulesOutputRenderService RModulesOutputRenderService
	
	def forestPlotOut(String jobName) {

	List<String> imageLinks = []
	RModulesOutputRenderService.initializeAttributes(jobName, 'ForestPlot', imageLinks)

	//Traverse the temporary directory for the generated image files.
	File tempDirectoryFile = new File(RModulesOutputRenderService.tempDirectory)

	//These strings represent the HTML of the data and formatting we pull from the R output files.
	String forestPlotCountData = RModulesOutputRenderService.fileParseLoop(tempDirectoryFile,
									       /.*Count.*\.txt/, /.*Count(.*)\.txt/, parseCountStr)
	String forestPlotTestData = RModulesOutputRenderService.fileParseLoop(tempDirectoryFile,
									      /.*statisticalTests.*\.txt/, /.*statisticalTests(.*)\.txt/,
									      parseStatisticsString)
	String legendText = RModulesOutputRenderService.fileParseLoop(tempDirectoryFile,
								      /.*legend.*\.txt/, /.*legend(.*)\.txt/, parseLegendTable)
	String rVersionInfo = RModulesOutputRenderService.parseVersionFile()
	String statisticByStratificationTable = RModulesOutputRenderService.fileParseLoop(tempDirectoryFile,
											  /.*statisticByStratificationTable.*\.txt/,
											  /.*statisticByStratificationTable(.*)\.txt/,
											  parseStatisticByStratificationTable)

	render template: '/plugin/forestPlot_out', contextPath: pluginContextPath, model: [
	    imageLocations                : imageLinks,
	    countData                     : forestPlotCountData,
	    statisticsData                : forestPlotTestData,
	    zipLink                       : RModulesOutputRenderService.zipLink,
	    statisticByStratificationTable: statisticByStratificationTable,
	    legendText                    : legendText,
	    rVersionInfo                  : rVersionInfo]
    }
	
    private Closure parseCountStr = { String inStr ->
	StringBuilder sb = new StringBuilder()

	//The topleft cell needs to be empty, this flag tells us if we filled it our not.
	boolean fillInBlank = true
	boolean firstRecord = true

	for (String line in inStr.readLines()) {

	    if (line.contains('stratificationName=')) {
		String nameValue = line.substring(line.indexOf('stratificationName=') + 19).trim()

		if (!firstRecord) {
		    sb << '</table><br /><br />'
		}

		sb << '<table class="AnalysisResults">'
		if (nameValue != 'NA') { //account for dummy stratification
		    sb << '<tr><th colspan="100">' << nameValue << '</th></tr>'
		}

		fillInBlank = true
	    }
	    else if (line.contains('NULL')) {
		//skip NULL
	    }
	    else {
		firstRecord = false
		sb << '<tr>'

		//Check to see if we need to fill in the blank cell.
		if (fillInBlank) {
		    sb << '<td class="blankCell">&nbsp;</td>'
		}

		int rowCounter = 0
		boolean statflag = false
		//Write the variable names across the top.
		for (currentText in line.split('\t')) {

		    if (fillInBlank || rowCounter == 0) {
			if (currentText.contains('fisherResults.p.Value')) {
			    currentText = 'Fisher test p-value'
			    statflag = true
			}
			else if (currentText.contains('fisherResults.oddsratio')) {
			    currentText = 'Odds Ratio'
			    statflag = true
			}
			else if (currentText.contains('chiResults.p.value')) {
			    currentText = '&chi;<sup>2</sup> p-value'
			    statflag = true
			}
			else if (currentText.contains('chiResults.statistic')) {
			    currentText = '&chi;<sup>2</sup>'
			    statflag = true
			}

			sb << '<th>' << currentText << '</th>'
		    }
		    else {

			if (statflag) {
			    sb << '<td colspan="3">' << currentText << '</td>'
			    statflag = false
			}
			else if (currentText.contains('NA')) {
			    //skip NA
			}
			else {
			    sb << '<td>' << currentText << '</td>'
			}
		    }

		    rowCounter++
		}

		fillInBlank = false

		sb << '</tr>'
	    }

	}

	sb << '</table>'
	sb
    }

    //TODO: Carried over from Fisher Test. Remove when we can determine it's not needed.
    private Closure parseStatisticsString = { String inStr ->

	StringBuilder sb = new StringBuilder()

	boolean firstRecord = true

	for (String line in inStr.readLines()) {
	    if (line.contains('name=')) {
		String nameValue = line.substring(line.indexOf('name=') + 5).trim()

		if (!firstRecord) {
		    sb << '</table><br /><br />'
		}
		sb << '<table class="AnalysisResults">'
		sb << '<tr><th colspan="2">' << nameValue << '</th></tr>'
	    }
	    else if (line.contains('fishp=')) {
		String forestPValue = line.substring(line.indexOf('forestp=') + 6).trim()
		sb << '<tr><th>ForestPlot test p-value</th><td>' << forestPValue << '</td></tr>'
	    }
	    else if (line.contains('chis=')) {
		String chiSquare = line.substring(line.indexOf('chis=') + 5).trim()
		sb << '<tr><th>&chi;<sup>2</sup></th><td>' << chiSquare << '</td></tr>'
	    }
	    else if (line.contains('chip=')) {
		String chiPValue = line.substring(line.indexOf('chip=') + 5).trim()
		sb << '<tr><th>&chi;<sup>2</sup> p-value</th><td>' << chiPValue << '</td></tr>'
		firstRecord = false
	    }
	}

	sb << '</table>'
	sb
    }

    private Closure parseStatisticByStratificationTable = { String inStr ->

	StringBuilder sb = new StringBuilder('<table class="AnalysisResults"')

	int rowCounter = 0

	for (String line in inStr.readLines()) {

	    sb << '<tr>'

	    for (tableValue in line.split('\t')) {

		if (rowCounter == 0) {
		    sb << "<th style='font-family:\"Arial\";font-size:16px;'>" << tableValue << '</th>'
		}
		else {
		    sb << "<td style='font-family:\"Arial\";font-size:16px;'>" << tableValue << '</td>'
		}
	    }

	    sb << '</tr>'

	    rowCounter++
	}

	sb << '</table><br />'

	sb
    }
	
    private Closure parseLegendTable = { String legendInStr ->

	StringBuilder sb = new StringBuilder()

	sb << "<span class='AnalysisHeader'>Legend</span><br /><br />"
	sb << "<table class='AnalysisResults'>"

	for (String line in legendInStr.readLines()) {

	    sb << '<tr>'

	    for (tableValue in line.split('\t')) {
		sb << '<th>' << tableValue.replace('|', '<br />') << '</th>'
	    }

	    sb << '</tr>'
	}

	sb << '</table><br />'

	sb
    }
}
