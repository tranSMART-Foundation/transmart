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

import java.util.regex.Matcher

class BoxPlotController {

	RModulesOutputRenderService RModulesOutputRenderService
	
	def boxPlotOut(String jobName) {
		List<String> imageLinks = []
		RModulesOutputRenderService.initializeAttributes jobName, 'BoxPlot', imageLinks
		
		File tempDirectoryFile = new File(RModulesOutputRenderService.tempDirectory)

		String anovaData = RModulesOutputRenderService.fileParseLoop(tempDirectoryFile,
				/.*ANOVA_RESULTS.*\.txt/, /.*ANOVA_RESULTS(.*)\.txt/, parseANOVAStr)
		anovaData += RModulesOutputRenderService.fileParseLoop(tempDirectoryFile,
				/.*ANOVA_PAIRWISE.*\.txt/, /.*ANOVA_PAIRWISE(.*)\.txt/, parseMatrixString)
		String legendText = RModulesOutputRenderService.fileParseLoop(tempDirectoryFile,
				/.*legend.*\.txt/, /.*legend(.*)\.txt/, parseLegendTable)

		render template: '/plugin/boxPlot_out', contextPath: pluginContextPath, model: [
				legendText    : legendText,
				imageLocations: imageLinks,
				ANOVAData     : anovaData,
				zipLink       : RModulesOutputRenderService.zipLink]
	}
	
	private Closure parseANOVAStr = { String statsInStr ->
		
		StringBuilder sb = new StringBuilder()

		// the name of the group when we have multiple probes.
		String nameValue = ''
		
		// differentiates the data from different groups.
		boolean firstGroup = true
		boolean writeHeader = true
		
		boolean groupedData = false
		
		boolean pvalueExtraction = false
		boolean summaryExtraction = false
		
		//################################
		//Summary table.
		//################################
		sb << '<span class="AnalysisHeader">ANOVA Result</span><br /><br />'
		
		for (String line in statsInStr.readLines()) {
			//If we find the name line, we can add it to the buffer.
			if (line.contains('name=')) {
				//Extract the name from the text file.
				nameValue = line.substring(line.indexOf('name=') + 5).trim()
				
				//If this isn't our first name field, we need to end the previous HTML table.
				if(!firstGroup) {
				    sb << '</table><br /><br />'
				}

				sb << '<table class="AnalysisResults">'
				
				sb << '<tr><th>Group</th><td>' << nameValue << '</td></tr>'
				
				groupedData = true
				firstGroup = false
			}
			
			//Set the boolean indicating the next few lines are pvalue text.
			if (line.contains('||PVALUES||')) {
				pvalueExtraction = true
				summaryExtraction = false
			}
			
			//Set the boolean indicating the next few lines are summary text.
			if (line.contains('||SUMMARY||')) {
				//Add the heading for the summary table.
				sb << '</table><br /><br />'
				
				if(groupedData) {
					sb << '<table class="AnalysisResults"><tr><th colspan="3">' << nameValue
					sb << '</th></tr><tr><th>Group</th><th>Mean</th><th>n</th></tr>'
				}
				
				pvalueExtraction = false
				summaryExtraction = true
			}
			
			//If we hit the p value extraction marker, pull the values out.
			if (line.contains('p=') && pvalueExtraction) {
				String pvalue = line.substring(line.indexOf('p=') + 2).trim()
				
				if(!groupedData) {
				    sb << '<table class="AnalysisResults">'
				}
				sb << '<tr><th>p-value</th><td>' << pvalue << '</td></tr>'
			}
			
			if (line.contains('f=') && pvalueExtraction) {
				String fvalue = line.substring(line.indexOf('f=') + 2).trim()
				sb << '<tr><th>F value</th><td>' << fvalue << '</td></tr>'
			}
			
			//If we don't have grouped data and haven't written our header, write it now.
			if(!groupedData && writeHeader && summaryExtraction) {
				sb << '<table class="AnalysisResults"><tr><th>Group</th><th>Mean</th><th>n</th></tr>'
				writeHeader = false
			}
			
			//If we are extracting summary stuff, process the line.
			if(summaryExtraction) {
				//This matches the lines in the ANOVA summary
				Matcher matcher = line =~ /'[0-9]+'\s+'(.*)'\s+'\s*(-*[0-9]*\.*[0-9]*)\s*'\s+([0-9]+)/
				if (matcher.matches()) {
					sb << '<tr>'
					sb << '<td>' << matcher[0][1] << '</td>'
					sb << '<td>' << matcher[0][2] << '</td>'
					sb << '<td>' << matcher[0][3] << '</td>'
					sb << '</tr>'
				}
			}
		}
		
		sb << '</table><br /><br />'
		
		sb
	}

	private Closure parseMatrixString = { String matrixInStr ->
		
		//################################
		//Matrix.
		//################################
		boolean firstLine = true
		boolean hasGroups = false
		
		StringBuilder sb = new StringBuilder()
		
		//Reset the flag that helps us draw the separation between groups.
		boolean firstGroup = true
		
		sb << '<span class="AnalysisHeader">Pairwise t-Test p-Values</span><br /><br />'

		for (String line in matrixInStr.readLines) {
			if (line.contains('name=')) {
				//Extract the name from the text file.
				String nameValue = line.substring(line.indexOf('name=') + 5).trim()
				
				if(!firstGroup) {
				    sb << '</table><br /><br />'
				}

				sb << '<span style="font: 12px tahoma,arial,helvetica,sans-serif;font-weight:bold;">' << nameValue
				sb << '</span><br /><br /><table class="AnalysisResults">'
				
				firstLine = true
				firstGroup = false
				hasGroups = true
			}
			else {
				if(firstLine && !hasGroups) {
				    sb << '<table class="AnalysisResults">'
				}

				sb << '<tr>'

				//The first line should have a blank cell first. All others we can do one cell per entry.
				if(firstLine) {
				    sb << '<td>&nbsp;</td>'
				}
				
				int cellCounter = 0
				
				for (tableValue in line.split('\t')) {

					if (firstLine || cellCounter == 0) { 
						sb << '<th>' << tableValue << '</th>'
					}
					else {
						sb << '<td>' << tableValue << '</td>'
					}
					
					cellCounter++
				}
				
				sb << '</tr>'
				
				firstLine = false
			}
		}
		
		sb << '</table><br />'
		
		sb
	}

	private Closure parseLegendTable = { String legendInStr ->

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
