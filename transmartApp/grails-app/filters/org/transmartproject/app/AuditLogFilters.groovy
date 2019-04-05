package org.transmartproject.app

import grails.converters.JSON
import org.transmart.audit.AuditLogService
import org.transmart.audit.StudyIdService
import org.transmartproject.core.log.AccessLogEntryResource
import org.transmartproject.core.users.User

class AuditLogFilters {

	AccessLogEntryResource accessLogService
	AuditLogService auditLogService
	StudyIdService studyIdService
	User currentUserBean

	def filters = {
		runDataExport(controller: 'dataExport', action: 'runDataExport') {
			after = { Map model ->
				String ip = request.getHeader('X-FORWARDED-FOR') ?: request.remoteAddr
				String fullUrl = request.forwardURI + (request.queryString ? '?' + request.queryString : '')
				String result_instance_id1 = params.result_instance_id1 ?: ''
				String result_instance_id2 = params.result_instance_id2 ?: ''
				String studies = studyIdService.getStudyIdsForQueries([result_instance_id1, result_instance_id2])
				String exportTypes = getExportTypes(params).join('+')

				accessLogService.report currentUserBean, 'Data Export',
						eventMessage: "User (IP: ${ip}) requested export of data. Http request parameters: ${params}",
						requestURL: fullUrl
				auditLogService.report "Clinical Data Exported - ${exportTypes}", request,
						study: studies, user: currentUserBean
			}
		}

		downloadFile(controller: 'dataExport', action: 'downloadFile') {
			after = { Map model ->
				String ip = request.getHeader('X-FORWARDED-FOR') ?: request.remoteAddr
				accessLogService.report currentUserBean, 'Data Export',
						eventMessage: "User (IP: ${ip}) downloaded an exported file.",
						requestURL: request.forwardURI + (request.queryString ? '?' + request.queryString : '')
			}
		}

		chart(controller: 'chart', action: '*', actionExclude: 'clearGrid|displayChart|childConceptPatientCounts') {
			before = {
				if (!auditLogService.enabled) {
					return
				}

				String result_instance_id1 = params.result_instance_id1 ?: ''
				String result_instance_id2 = params.result_instance_id2 ?: ''
				String studies = ''
				if (params.concept_key) {
					studies = studyIdService.getStudyIdForConceptKey(params.concept_key) ?: ''
				}
				if (!studies) {
					studies = studyIdService.getStudyIdsForQueries([result_instance_id1, result_instance_id2])
				}

				String task = actionName == 'childConceptPatientCounts' ?
						'Clinical Data Access' : 'Summary Statistics'
				auditLogService.report task, request,
						study: studies, user: currentUserBean,
						subset1: result_instance_id1, subset2: result_instance_id2
			}
		}

		concepts(controller: 'concepts', action: 'getChildren') {
			before = {
				if (!auditLogService.enabled) {
					return
				}

				String studies = null
				if (params.concept_key) {
					studies = studyIdService.getStudyIdForConceptKey(params.concept_key, studyConceptOnly: true)
				}
				if (studies == null) {
					return
				}

				auditLogService.report 'Clinical Data Access', request,
						study: studies, user: currentUserBean
			}
		}

		rwg(controller: 'RWG', action: 'getFacetResults') {
			before = {
				auditLogService.report 'Clinical Data Active Filter', request,
						query: params.searchTerms, user: currentUserBean
			}
		}

		userlanding(controller: 'userLanding', action: '*', actionExclude: 'checkHeartBeat') {
			before = {
				auditLogService.report 'User Access', request, user: currentUserBean
			}
		}

		oauth(controller: 'oauth', action: '*') {
			before = {
				auditLogService.report 'OAuth authentication', request, user: currentUserBean
			}
		}
	}

	/**
	 * Get export types from the <var>selectedSubsetDataTypeFiles</var>
	 * field in the parameters.
	 * Returns a list containing 'LDD' if the parameter contains data type 'CLINICAL'
	 * and 'HDD' if any other data type is in the parameter.
	 *
	 * @return a list containing 'LDD', 'HDD', or both.
	 */
	private List<String> getExportTypes(Map params) {
		def param = params.selectedSubsetDataTypeFiles
		def dataFiles = param instanceof String ?
				[JSON.parse(param)] :
				param.collect { JSON.parse(it) }
		Set<String> dataTypes = dataFiles*.dataTypeId as Set
		List<String> exportTypes = []
		if ('CLINICAL' in dataTypes) {
			exportTypes << 'LDD'
		}
		if (dataTypes - 'CLINICAL') {
			exportTypes << 'HDD'
		}
		exportTypes
	}
}
