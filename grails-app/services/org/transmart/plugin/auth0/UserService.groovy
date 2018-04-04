package org.transmart.plugin.auth0

import grails.converters.JSON
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.plugin.custom.CustomizationService
import org.transmart.plugin.custom.UserLevel
import org.transmart.searchapp.AuthUser

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class UserService {

	static transactional = false

	@Autowired private AuthService authService
	@Autowired private CustomizationService customizationService

	Map currentUserInfo(String username = null) {
		AuthUser user = username ? authService.authUser(username) : authService.currentAuthUser()
		if (!user) {
			// TODO
		}
		currentUserInfo user
	}

	List<Map> buildUserListUserInfo() {
		List<Map> userData = []
		for (AuthUser authUser in AuthUser.list()) {
			Map description = (Map) JSON.parse(authUser.description ?: '{}')
			userData << [
					connection : description.connection ?: 'no connection data',
					email      : authUser.email ?: 'unknown',
					firstName  : description.firstname ?: 'UNKNOWN',
					id         : authUser.id,
					institution: description.institution ?: 'UNKNOWN',
					lastName   : description.lastname ?: 'UNKNOWN',
					lastUpdated: authUser.lastUpdated,
					level      : customizationService.userLevel(authUser)]
		}
		userData
	}

	Map currentUserInfo(AuthUser user) {
		UserLevel level = customizationService.userLevel(user)
		Map details = user.description ? (Map) JSON.parse(user.description) : [:]
		details + [
				email   : user.email ?: user.username ?: '',
				id      : user.id,
				level   : level,
				type    : user.type,
				username: user.username]
	}
}
