package org.transmart.plugin.auth0

import grails.converters.JSON
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError
import org.transmart.searchapp.AuthUser

@CompileStatic
class UserService {

	static transactional = false

	@Autowired private AuthService authService
	@Autowired private MessageSource messageSource

	Map currentUserInfo(String username = null) {
		AuthUser user
		UserLevel level
		if (!username) {
			user = authService.currentAuthUser()
			level = authService.currentUserLevel()
		}
		else {
			user = authService.authUser(username)
			level = authService.userLevel(user)
		}

		if (!user) {
			// TODO
		}

		Map details = user.description ? (Map) JSON.parse(user.description) : [:]
		details + [
				email   : user.email ?: user.username ?: '',
				id      : user.id,
				level   : level,
				type    : user.type,
				username: user.username]
	}

	/**
	 * @param o a domain class instance
	 * @return resolved error strings
	 */
	@CompileDynamic
	Map<String, List<String>> errorStrings(o) {
		Locale locale = Locale.getDefault()
		Map<String, List<String>> stringsByField = [:].withDefault { [] }
		for (fieldErrors in o.errors) {
			for (error in fieldErrors.allErrors) {
				String message = messageSource.getMessage(error, locale)
				stringsByField[((FieldError) error).field] << message
			}
		}
		[:] + stringsByField
	}

	List<Map> buildUserListUserInfo() {
		List<Map> userData = []
		for (AuthUser authUser in AuthUser.list()) {
			Map description = (Map) JSON.parse(authUser.description ?: '{}')
			userData << [
					connection: description.connection ?: 'no connection data',
							email: authUser.email ?: 'unknown',
					firstName: description.firstname ?: 'UNKNOWN',
							id: authUser.id,
					institution: description.institution ?: 'UNKNOWN',
					lastName: description.lastname ?: 'UNKNOWN',
					lastUpdated: authUser.lastUpdated,
					level: authService.userLevel(authUser)]
		}
		userData
	}
}
