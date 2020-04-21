package com.recomdata.transmart.data.association

import groovy.util.logging.Slf4j
import java.util.regex.Matcher

@Slf4j('logger')
class PCAController {

    RModulesOutputRenderService RModulesOutputRenderService

    def pcaOut(String jobName) {
	List<String> imageLinks = []
	RModulesOutputRenderService.initializeAttributes jobName, 'PCA', imageLinks

	File tempDirectory = new File(RModulesOutputRenderService.tempDirectory)

	Map<Integer, File> componentsFileMap = constructComponentFileMap(tempDirectory)

	String geneListTable = createGeneListTable(componentsFileMap)

	String summaryTable = RModulesOutputRenderService.fileParseLoop(tempDirectory,
									/.*COMPONENTS_SUMMARY.*\.TXT/,
									/.*COMPONENTS_SUMMARY(.*)\.TXT/, parseComponentsSummaryStr)

	render template: '/plugin/pca_out', contextPath: pluginContextPath, model: [
	    imageLocations: imageLinks,
	    zipLink: RModulesOutputRenderService.zipLink,
	    summaryTable: summaryTable,
	    geneListTable: geneListTable]
    }

    /**
     * @param tempDirectoryFile - directory which contains components text files
     * @return map with component number as key and file as value
     */
    private Map<Integer, File> constructComponentFileMap(File tempDirectoryFile) {
	String fileNamePattern = ~/^GENELIST(?<component>[0-9]+)\.TXT$/

	tempDirectoryFile.listFiles().toList().collectEntries { File file ->
	    Matcher fileNameMatcher = file.name =~ fileNamePattern
	    fileNameMatcher ? [(fileNameMatcher.group('component').toInteger()): file] : [:]
	}.sort()
    }

    /**
     * @param componentsFileMap - components map to render
     * @return String with html representation of components
     */
    private String createGeneListTable(Map<Integer, File> componentsFileMap) {
	StringBuilder geneListTableHtml = new StringBuilder('<table><tr>')
	componentsFileMap.eachWithIndex { int component, File file, int ord ->
	    if (ord.mod(4) == 0) {
		geneListTableHtml << '</tr><tr><td>&nbsp;</td></tr><tr>'
	    }
	    geneListTableHtml << parseGeneList(file.text, componentsFileMap.size() > 1 ? component.toString() : '')
	}
	geneListTableHtml << '</tr></table><br /><br />'
	geneListTableHtml
    }

    private Closure parseComponentsSummaryStr = { String inStr ->

	StringBuilder sb = new StringBuilder()

	boolean firstLine = true

	sb << '<table class="AnalysisResults">'
	sb << '<tr><th>Primary Component</th><th>Eigen Value</th><th>Percent Variance</th></tr>'

	for (String line in inStr.readLines()) {

	    //Every line but the first in the file gets written to the table.
	    if (!firstLine) {
		String[] resultArray = line.split()
		sb << '<tr><td>' << resultArray[0] << '</td><td>' << resultArray[1] << '</td><td>' << resultArray[2] << '</td></tr>'
	    }

	    firstLine = false
	}

	sb << '</table><br /><br />'

	sb
    }

    private Closure parseGeneList = { String inStr, String currentComponent ->

	logger.info 'parseGeneList inStr "{}"', inStr
	logger.info 'currentComponent {}', currentComponent
	
	StringBuilder sb = new StringBuilder()

	sb << '<td valign="top"><table class="AnalysisResults"><tr><th colspan="2">Component ' << currentComponent << '</th></tr>'

	inStr.eachLine() { line ->
	    String[] resultArray = line.split('\t')
	    sb << '<tr><td>' << resultArray[0] << '</td><td>' << resultArray[1] << '</td></tr>'
	}

	sb << '</table></td>'

	sb
    }
}
