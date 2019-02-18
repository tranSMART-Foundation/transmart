/*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************/

package com.recomdata.transmart.data.association

import grails.converters.JSON
import org.transmartproject.utils.FileUtils

class SurvivalAnalysisResultController {

    private static final Set<String> DEFAULT_FIELDS = ['chromosome', 'cytoband', 'start', 'end', 'pvalue', 'fdr'].asImmutable()
    private static final Set<String> DEFAULT_NUMBER_FIELDS = ['start', 'end', 'pvalue', 'fdr'].asImmutable()

    RModulesOutputRenderService RModulesOutputRenderService

    def list(String jobName, String fields, Integer start, Integer limit, String sort, String dir) {
	response.contentType = 'text/json'
	if (!(jobName ==~ /(?i)[-a-z0-9]+/)) {
	    render new JSON([error: 'jobName parameter is required. It should contains just alphanumeric characters and dashes.'])
	    return
	}

	File file = new File(RModulesOutputRenderService.tempFolderDirectory, jobName + '/workingDirectory/survival-test.txt')
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

    def imagePath(String jobName, String jobType, String chromosome, String start, String end) {
	render RModulesOutputRenderService.relativeImageURL + jobName + '/workingDirectory/' +
	    jobType + '_' + chromosome + '_' + start + '_' + end + '.png'
    }
}
