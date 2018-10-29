package org.transmartproject.db.log

import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.UtilService
import org.transmartproject.core.log.AccessLogEntryResource
import org.transmartproject.core.users.User

@Slf4j('logger')
class AccessLogService implements AccessLogEntryResource {

    @Autowired private SecurityService securityService
    @Autowired private UtilService utilService

    @Transactional
    AccessLogEntry report(Map<String, Object> additionalParams = [:], User user, String event) {
        save user.username, event, additionalParams.eventMessage,
				additionalParams.requestURL, additionalParams.accessTime ?: new Date()
    }

    @Transactional
    AccessLogEntry report(String username = securityService.currentUsername(), String event, String message) {
        save username, event, message, null, new Date()
    }

    private AccessLogEntry save(String username, String event, String message, String requestURL, Date accessTime) {
    	AccessLogEntry entry = new AccessLogEntry(
			username: username,
			event: event,
			eventMessage: message,
			requestURL: requestURL,
			accessTime: accessTime)
	entry.save()
	if (entry.hasErrors()) {
	    logger.error 'Problem(s) saving AccessLogEntry: {}', utilService.errorStrings(entry)
	}
	entry
    }

    List<AccessLogEntry> listEvents(Map<String, Object> paginationParams = [:], Date startDate, Date endDate) {
        AccessLogEntry.createCriteria().list(
                max:    paginationParams?.max,
                offset: paginationParams?.offset,
                sort:   paginationParams?.sort,
                order:  paginationParams?.order) {

	if (startDate && endDate) {
            between 'accessTime', startDate, endDate
            }
	    else if (startDate) {
                gte 'accessTime', startDate
            }
	    else if (endDate) {
                lte 'accessTime', endDate
            }
        }
    }
}
