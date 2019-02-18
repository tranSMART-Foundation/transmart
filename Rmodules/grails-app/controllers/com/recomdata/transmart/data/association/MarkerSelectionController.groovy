
package com.recomdata.transmart.data.association

class MarkerSelectionController {

    RModulesOutputRenderService RModulesOutputRenderService

    def markerSelectionOut(String jobName) {
	List<String> imageLinks = []
	RModulesOutputRenderService.initializeAttributes jobName, 'Heatmap', imageLinks

	File tempDirectory = new File(RModulesOutputRenderService.tempDirectory)
	String markerSelectionTable = RModulesOutputRenderService.fileParseLoop(tempDirectory,
										/.*CMS.*\.TXT/, /.*CMS(.*)\.TXT/, parseMarkerSelectionStr)

	render template: '/plugin/markerSelection_out', contextPath: pluginContextPath, model: [
	    imageLocations      : imageLinks,
	    markerSelectionTable: markerSelectionTable,
	    zipLink             : RModulesOutputRenderService.zipLink]
    }

    private Closure parseMarkerSelectionStr = { String inStr ->

	StringBuilder sb = new StringBuilder()

	boolean firstLine = true

	String tableHeader = '''\
			<thead>
			<tr>
				<th>Gene Symbol&nbsp&nbsp&nbsp&nbsp</th>	
				<th>Probe ID&nbsp&nbsp&nbsp&nbsp</th>
				<th>Log2(fold change) S2 vs S1&nbsp&nbsp&nbsp&nbsp</th>
				<th>t&nbsp&nbsp&nbsp&nbsp</th>
				<th>P-value&nbsp&nbsp&nbsp&nbsp</th>
				<th>Adjusted P-value&nbsp&nbsp&nbsp&nbsp</th>
				<th>B&nbsp&nbsp&nbsp&nbsp</th>
			</tr>
			</thead>
			'''

	//Start the table and add headers.
	sb << '<table id="markerSelectionTable" class="tablesorterAnalysisResults">'
	sb << tableHeader
	sb << '<tbody>'

	for (String line in inStr.readLines()) {

	    //Every line but the first in the file gets written to the table.
	    if (!firstLine) {
		//Split the current line (tabs) and trim the entries
		String[] resultArray = line.split('\t').collect { it.trim() }

		String tableRow = """\
			<tr>
				<td>${resultArray[0]}</td>
				<td>${resultArray[1]}</td>
				<td>${resultArray[2]}</td>
				<td>${resultArray[3]}</td>
				<td>${resultArray[4]}</td>
				<td>${resultArray[5]}</td>
				<td>${resultArray[6]}</td>
			</tr>
			"""

		sb << tableRow
	    }

	    firstLine = false
	}

	sb << '</tbody>'
	sb << '</table><br /><br />'

	sb
    }
}
