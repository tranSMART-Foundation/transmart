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

import java.util.regex.Matcher

class TableWithFisherController {

    RModulesOutputRenderService RModulesOutputRenderService

    def fisherTableOut(String jobName) {

	RModulesOutputRenderService.initializeAttributes jobName, null, null

	File tempDirectory = new File(RModulesOutputRenderService.tempDirectory)

	StringBuilder fisherTableCountData = new StringBuilder()
	StringBuilder fisherTableTestData = new StringBuilder()

	List<String> txtFiles = []
	tempDirectory.traverse(nameFilter: ~/.*Count.*\.txt/) { File currentTextFile ->
	    txtFiles << currentTextFile.path
	}

	//Loop through the file path array and parse each of the files. We do this to make different tables if there are multiple files.
	for (String path in txtFiles) {
	    //Parse out the name of the group from the name of the text file.
	    Matcher matcher = path =~ /.*Count(.*)\.txt/
	    if (matcher.matches() && txtFiles.size() > 1) {
		//Add the HTML that will separate the different files.
		fisherTableCountData << '<br /><br /><span class="AnalysisHeader">' << matcher[0][1] << '</span><hr />'
	    }

	    fisherTableCountData << parseCountStr(new File(path).text)
	}

	txtFiles.clear()
	tempDirectory.traverse(nameFilter: ~/.*statisticalTests.*\.txt/) { File currentTextFile ->
	    txtFiles << currentTextFile.path
	}

	//Loop through the file path array and parse each of the files. We do this to make different tables if there are multiple files.
	for (String path in txtFiles) {
	    //Parse out the name of the group from the name of the text file.
	    Matcher matcher = path =~ /.*statisticalTests(.*)\.txt/
	    if (matcher.matches() && txtFiles.size() > 1) {
		//Add the HTML that will separate the different files.
		fisherTableTestData << '<br /><br /><span class="AnalysisHeader">' << matcher[0][1] << '</span><hr />'
	    }

	    fisherTableTestData << parseStatisticsString(new File(path).text)
	}

	render template: '/plugin/tableWithFisher_out', contextPath: pluginContextPath, model: [
	    countData     : fisherTableCountData.toString(),
	    statisticsData: fisherTableTestData.toString(),
	    zipLink       : RModulesOutputRenderService.zipLink]
    }

    private String parseCountStr(String inStr) {
	StringBuilder sb = new StringBuilder()

	sb << '<table class="AnalysisResults">'

	//The topleft cell needs to be empty, this flag tells us if we filled it our not.
	boolean fillInBlank = true
	boolean firstRecord = true

	for (String line in inStr.readLines()) {

	    if (line.contains('name=')) {
		String nameValue = line.substring(line.indexOf('name=') + 5).trim()

		if (!firstRecord) {
		    sb << '</table><br /><br />'
		}

		sb << '<table class="AnalysisResults">'
		sb << '<tr><th colspan="100">' << nameValue << '</th></tr>'

		fillInBlank = true
	    }
	    else {
		firstRecord = false

		sb << '<tr>'

		//Check to see if we need to fill in the blank cell.
		if (fillInBlank) {
		    sb << '<td class="blankCell">&nbsp;</td>'
		}

		int rowCounter = 0

		//Write the variable names across the top.
		for (currentText in line.split('\t')) {

		    if (fillInBlank || rowCounter == 0) {
			sb << '<th>' << currentText << '</th>'
		    }
		    else {
			sb << '<td>' << currentText << '</td>'
		    }

		    rowCounter++
		}

		if (fillInBlank) {
		    fillInBlank = false
		}

		sb << '</tr>'
	    }
	}

	sb << '</table>'

	sb
    }

    private String parseStatisticsString(String inStr) {
	StringBuilder sb = new StringBuilder()

	boolean firstRecord = true

	sb << '<table class="AnalysisResults">'

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
		String fisherPValue = line.substring(line.indexOf('fishp=') + 6).trim()
		sb << '<tr><th>Fisher test p-value</th><td>' << fisherPValue << '</td></tr>'
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
}
