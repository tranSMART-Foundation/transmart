package org.transmart.plugin.auth0

import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.plugin.custom.CustomizationService
import org.transmart.plugin.custom.UserLevel
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.UtilService
import org.transmart.searchapp.AuthUser
import org.transmartproject.db.log.AccessLogService

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@GrailsCompileStatic
@Slf4j('logger')
class UserService {

	static transactional = false

	@Autowired private AuthService authService
	@Autowired private CustomizationService customizationService
	@Autowired private UtilService utilService
	@Autowired private AccessLogService accessLogService
	@Autowired private SecurityService securityService

	Map currentUserInfo(String username = null) {
		AuthUser user = username ? authUser(username) : currentAuthUser()
		if (!user) {
			// TODO
		}
		currentUserInfo user
	}

	List<Map> buildUserListUserInfo(String sort, String order, int max, int offset) {
		List<Map> userData = []
		for (AuthUser authUser in AuthUser.list(sort: sort, order: order, max: max, offset: offset)) {
			userData << buildUserInfo(authUser)
		}
		userData
	}

	Map buildUserInfo(AuthUser authUser) {
		Map description = (Map) JSON.parse(authUser.description ?: '{}')
		[connection : description.connection,
		 email      : authUser.email ?: 'unknown',
		 fullName   : authUser.userRealName,
		 id         : authUser.id,
		 lastUpdated: authUser.lastUpdated,
		 level      : customizationService.userLevel(authUser).description,
		 username   : authUser.username,
		 uniqueId   : authUser.uniqueId]
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

	/**
	 * Update existing user details information.
	 *
	 * @param params The params from request
	 * @return Updated user instance
	 */
	@Transactional
	AuthUser updateUser(String email, String firstname, String lastname, Map params) {
		AuthUser user = currentAuthUser()
		updateAuthUser user, null, email, firstname, lastname, null,
				currentUserInfo(user) + params
	}

	/**
	 * Update user information for existed or newly registered user.
	 *
	 * @param user If NULL then update info for new user.
	 * @return
	 */
	AuthUser updateAuthUser(AuthUser user, String username, String email, String firstname,
	                        String lastname, Credentials credentials, Map params) {

		boolean existingUser
		if (user) {
			username = user.username
			existingUser = true
		}
		else {
			user = authUser(username)
			existingUser = false
		}

		logger.info 'Searching for user account:{}', username

		if (!user) {
			logger.error 'The registration/update information for username:{} and e-mail:{} could not be recorded.',
					username ?: 'N/A', email ?: 'N/A'
			throw new RuntimeException('The username ' + username +
					' was not authenticated previously. Cannot record registration information.')
		}

		try {
			if (firstname || lastname) {
				user.userRealName = ((firstname ?: '') + ' ' + (lastname ?: '')).trim()
			}
			user.name = user.userRealName
			if (email) {
				user.email = email
			}
			Map description = [:] + params
			description.remove 'g-recaptcha-response'
			description.remove 'action'
			description.remove 'controller'

			if (credentials) {
				description.access_token = credentials.accessToken
				description.id_token = credentials.idToken
				description.connection = credentials.connection
				description.picture = credentials.picture ?: ''
			}

			user.description = (description as JSON).toString()
			user.save(flush: true)
			if (user.hasErrors()) {
				logger.error 'Error updating user{}: {}', username, utilService.errorStrings(user)
			}
			else {
				logger.info 'Saved/Updated user registration information for {}', email
				if (existingUser) {
					accessLog username ?: email, 'Profile-update', "User profile $email has been updated"
				}
			}
			user
		}
		catch (e) {
			logger.error 'Could not save record:{}/{}', e.getClass().name, e.message
			throw new RuntimeException('Could not save record. ' + e.getClass().name + '/' + e.message)
		}
	}

	private void accessLog(String username, String event, String message = null) {
		accessLogService.report username, event, message
	}

	/**
	 * @return the <code>AuthUser</code> instance for the currently authenticated user.
	 */
	AuthUser currentAuthUser() {
		authUser securityService.currentUsername()
	}

	/**
	 * Retrieve the <code>AuthUser</code> for the specified username.
	 * The query cache is used to reduce unnecessary database calls.
	 */
	AuthUser authUser(String username) {
		findBy 'username', username
	}

	AuthUser findBy(String propertyName, String propertyValue) {
		(AuthUser) AuthUser.createCriteria().get {
			eq propertyName, propertyValue
			cache true
		}
	}
}
