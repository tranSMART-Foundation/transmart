package com.recomdata.transmart.data.association

import grails.converters.JSON
import org.transmartproject.utils.FileUtils

class aCGHgroupTestController {

    private static final Set<String> DEFAULT_FIELDS = ['chromosome', 'cytoband', 'start', 'end', 'pvalue', 'fdr'].asImmutable()
    private static final Set<String>  DEFAULT_NUMBER_FIELDS = ['start', 'end', 'pvalue', 'fdr'].asImmutable()

    RModulesOutputRenderService RModulesOutputRenderService

    def aCGHgroupTestOutput(String jobName) {
 	List<String> imageLinks = []
	RModulesOutputRenderService.initializeAttributes jobName, 'aCGHgroupTest', imageLinks

        render template: '/plugin/aCGHgroupTest_out', model: [
	    zipLink: RModulesOutputRenderService.zipLink,
	    imageLinks: imageLinks]
    }

    def imagePath(String jobName) {
	render RModulesOutputRenderService.relativeImageURL + jobName + '/workingDirectory/groups-test.png'
    }

    def resultTable(String jobName, String fields, String sort, Integer start, Integer limit, String dir) {
        response.contentType = 'text/json'
        if (!(jobName ==~ /(?i)[-a-z0-9]+/)) {
            render new JSON([error: 'jobName parameter is required. It should contains just alphanumeric characters and dashes.'])
            return
        }

        File file = new File(RModulesOutputRenderService.tempFolderDirectory, jobName + '/workingDirectory/groups-test.txt')
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
