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

class ScatterPlotController {

    RModulesOutputRenderService RModulesOutputRenderService

    def scatterPlotOut(String jobName) {
	List<String> imageLinks = []
	RModulesOutputRenderService.initializeAttributes jobName, 'ScatterPlot', imageLinks

	File tempDirectory = new File(RModulesOutputRenderService.tempDirectory)

	List<String> txtFiles = []
	tempDirectory.traverse(nameFilter: ~/.*LinearRegression.*\.txt/) { File currentTextFile ->
	    txtFiles << currentTextFile.path
	}

	//Loop through the file path array and parse each of the files. We do this to make different tables if there are multiple files.
	StringBuilder linearRegressionData = new StringBuilder()
	for (String path in txtFiles) {
	    //Parse out the name of the group from the name of the text file.
	    Matcher matcher = path =~ /.*LinearRegression(.*)\.txt/
	    if (matcher.matches() && txtFiles.size() > 1) {
		//Add the HTML that will separate the different files.
		linearRegressionData << '<br /><br /><span class="AnalysisHeader">' << matcher[0][1] << '</span><hr />'
	    }

	    linearRegressionData << parseLinearRegressionStr(new File(path).text)
	}

	render template: '/plugin/scatterPlot_out', contextPath: pluginContextPath, model: [
	    imageLocations: imageLinks,
	    linearRegressionData: linearRegressionData.toString(),
	    zipLink: RModulesOutputRenderService.zipLink]
    }

    private String parseLinearRegressionStr(String inStr) {
	StringBuilder sb = new StringBuilder()

	//If we found the name line then we know there are multiple graphs to be displayed.
	boolean hasName = false

	for (String line in inStr.readLines()) {
	    if (line.contains('name=')) {
		String name = line.substring(line.indexOf('name=') + 5).trim()

		//If the string builder isn't empty we already have a table in there, end that one and start a new one.
		if (sb) {
		    sb << '</table><br /><br />'
		}

		sb << '<table class="AnalysisResults" width="30%"><tr><th>Group Name</th><td>' << name << '</td></tr>'
		hasName = true
	    }
	    else if (line.contains('n=')) {
		String numSubject = line.substring(line.indexOf('n=') + 2).trim()
		if (hasName) {
		    sb << '<tr><th>Number of Subjects</th><td>' << numSubject << '</td></tr>'
		}
		else {
		    sb << '<table class="AnalysisResults" width="30%"><tr><th>Number of Subjects</th><td>' << numSubject << '</td></tr>'
		}
	    }
	    else if (line.contains('intercept=')) {
		String intercept = line.substring(line.indexOf('intercept=') + 10).trim()
		sb << '<tr><th>Intercept</th><td>' << intercept << '</td></tr>'
	    }
	    else if (line.contains('slope=')) {
		String slope = line.substring(line.indexOf('slope=') + 6).trim()
		sb << '<tr><th>Slope</th><td>' << slope << '</td></tr>'
	    }
	    else if (line.contains('nr2=')) {
		String rSquared = line.substring(line.indexOf('nr2=') + 4).trim()
		sb << '<tr><th>r-squared</th><td>' << rSquared << '</td></tr>'
	    }
	    else if (line.contains('ar2=')) {
		String adjRSquared = line.substring(line.indexOf('ar2=') + 4).trim()
		sb << '<tr><th>adjusted r-squared</th><td>' << adjRSquared << '</td></tr>'
	    }
	    else if (line.contains('p=')) {
		String pValue = line.substring(line.indexOf('p=') + 2).trim()
		sb << '<tr><th>p-value</th><td>' << pValue << '</td></tr>'
	    }
	}

	sb << '</table>'

	sb
    }
}
