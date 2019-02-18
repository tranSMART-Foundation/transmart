package com.recomdata.transmart.data.association

import grails.converters.JSON
import org.springframework.beans.factory.annotation.Value
import org.transmartproject.utils.FileUtils

class RNASeqgroupTestController {

    private static final Set<String> DEFAULT_FIELDS = ['regionname', 'genesymbol', 'logFC', 'logCPM', 'PValue', 'FDR'].asImmutable()
    private static final Set<String> DEFAULT_NUMBER_FIELDS = ['logFC', 'logCPM', 'PValue', 'FDR'].asImmutable()

    RModulesOutputRenderService RModulesOutputRenderService

    @Value('${RModules.tempFolderDirectory:}')
    private String tempFolderDirectory

    def RNASeqgroupTestOutput(String jobName) {
	List<String> imageLinks = []
	RModulesOutputRenderService.initializeAttributes jobName, 'RNASeqgroupTest', imageLinks

	render template: '/plugin/RNASeqgroupTest_out', model: [
	    zipLink: RModulesOutputRenderService.zipLink,
	    imageLinks: imageLinks]
    }

    def imagePath(String jobName) {
	render RModulesOutputRenderService.relativeImageURL + jobName + '/workingDirectory/rnaseq-groups-test.png'
    }

    def resultTable(String jobName, String fields, Integer start, Integer limit, String sort, String dir) {
	response.contentType = 'text/json'
	if (!(jobName ==~ /(?i)[-a-z0-9]+/)) {
	    render new JSON([error: 'jobName parameter is required. It should contains just alphanumeric characters and dashes.'])
	    return
	}

	File file = new File(tempFolderDirectory, jobName + '/workingDirectory/probability.txt')
	if (file.exists()) {
	    Map obj = FileUtils.parseTable(file,
					   start: start,
					   limit: limit,
					   fields: (fields?.split('\\s*,\\s*') as Set) ?: DEFAULT_FIELDS,
					   sort: sort,
					   dir: dir,
					   numberFields: DEFAULT_NUMBER_FIELDS,
					   separator: '\t')

	    JSON json = new JSON(obj)
	    json.prettyPrint = false
	    render json
	}
	else {
	    response.status = 404
	    render '[]'
	}
    }
}
