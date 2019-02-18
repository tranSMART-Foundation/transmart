import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.plugin.auth0.Auth0Service
import org.transmart.plugin.auth0.UserService
import org.transmart.plugin.custom.CustomizationConfig
import org.transmart.plugin.custom.CustomizationService
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.UtilService
import org.transmart.searchapp.AuthUser
import org.transmartproject.db.log.AccessLogService

@Slf4j('logger')
class UserProfileController {

	// non-recoverable error
	private static final String severeMessage = 'Unable to update user information. Contact administrator.'

	@Autowired private AccessLogService accessLogService
	@Autowired private Auth0Service auth0Service
	@Autowired private CustomizationConfig customizationConfig
	@Autowired private CustomizationService customizationService
	@Autowired private SecurityService securityService
	@Autowired private UserService userService
	@Autowired private UtilService utilService

	private boolean auth0Enabled = SpringSecurityUtils.securityConfig.auth0.active

	def index() {
		try {
			[user        : userService.currentUserInfo(),
			 token       : jwtToken(),
			 instanceType: customizationConfig.instanceType,
			 instanceName: customizationConfig.instanceName,
			 level       : customizationService.currentUserLevel()]
		}
		catch (e) {
			logger.error 'Caught error "{}" in UserProfile plugin.', e.message, e
			redirect action: 'basic'
		}
	}

	def save(String email, String firstname, String lastname) {
		try {
			AuthUser authUser = userService.updateUser(email, firstname, lastname, params)
			if (authUser.hasErrors()) {
				logger.error 'UserProfile.save() errors: {}', utilService.errorStrings(authUser)
				flash.error = 'Error occurred while updating user profile. Please try again later or contact administrator if error persists.'
			}
			else {
				flash.message = 'Profile successfully updated.'
			}
		}
		catch (e) {
			logger.error 'Error occurred while saving user info: {}', e.message, e
			flash.error = severeMessage
		}

		redirect action: 'index'
	}

	/**
	 * Basic user profile view if auth0Service not available
	 */
	def basic() {
		AuthUser user = AuthUser.findByUsername(securityService.currentUsername())

		if (request.post) {
			bindData user, params, [include: [
					'enabled', 'username', 'userRealName', 'email',
					'description', 'emailShow', 'authorities', 'changePassword'
			]]
			user.name = user.userRealName
			user.save(flush: true)
			if (user.hasErrors()) {
				logger.error 'UserProfile.basic() save errors: {}', utilService.errorStrings(user)
				flash.error = 'Error occurred while updating user profile. Please try again later or contact administrator if error persists.'
			}
			else {
				accessLogService.report user.username, 'Profile-update', "User profile $user.email has been updated"
				flash.message = 'Profile successfully updated.'
			}
		}

		[user: user, level: customizationService.currentUserLevel()]
	}

	private String jwtToken() {
		(auth0Enabled ? auth0Service.jwtToken() : securityService.jwtToken()) ?: 'Unable to retrieve token.'
	}
}
