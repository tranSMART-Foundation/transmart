package org.transmart.plugin.auth0

import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.web.WebAttributes
import org.transmart.plugin.custom.CustomizationConfig
import org.transmart.plugin.custom.CustomizationService
import org.transmart.plugin.custom.Settings
import org.transmart.plugin.custom.UserLevel
import org.transmart.plugin.shared.SecurityService
import org.transmart.searchapp.AuthUser
import org.transmartproject.db.log.AccessLogService

import java.text.SimpleDateFormat

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@Slf4j('logger')
class Auth0Controller implements InitializingBean {

	static allowedMethods = [checkTOS: 'POST']

	private Map authModel

	@Autowired private AccessLogService accessLogService
	@Autowired private AuthService authService
	@Autowired private Auth0Service auth0Service
	@Autowired private Auth0Config auth0Config
	@Autowired private CustomizationConfig customizationConfig
	@Autowired private CustomizationService customizationService
	@Autowired private SecurityService securityService
	@Autowired private UserService userService

	// can't be initialized in afterPropertiesSet() since GORM isn't available yet
	@Lazy private String access1DetailsMessage = { ->
		String suffix = customizationConfig.instanceType == 'baseline' || customizationConfig.instanceType == '' ? '' :
				'.' + customizationConfig.instanceType
		message(code: 'edu.harvard.transmart.access1.details' + suffix)
	}()
	@Lazy private String authViewName = { ->
		String loginTemplatesValue = customizationService.setting('login-template')?.fieldvalue ?: ''
		if (loginTemplatesValue && !loginTemplatesValue.equalsIgnoreCase('default')) {
			loginTemplatesValue
		}
		else {
			'auth'
		}
	}()
	@Lazy private String notAuthorizedTemplate = { -> customizationService.setting('notAuthorizedTemplate')?.fieldvalue ?: '' }()
	@Lazy private String userGuideLink = { ->
		String userGuidePropertyId = 'edu.harvard.transmart.UserGuideMessage' +
				(customizationConfig.instanceType ? '.' + customizationConfig.instanceType : '')
		message(code: userGuidePropertyId, args: [customizationConfig.userGuideUrl, "User's Guide"])
	}()

	def auth() {
		nocache response

		boolean forcedFormLogin = request.queryString
		if (customizationConfig.guestAutoLogin && !forcedFormLogin) {
			logger.info 'Automatic login with userid {}', customizationConfig.guestUserName
			AuthUser authUser = authService.authUser(customizationConfig.guestUserName)
			if (authUser) {
				securityService.authenticateAs authUser.username
				if (authUser.authorities) {
					logger.debug 'User has roles, meaning has access to the system. Redirecting to the standard URL.'
					redirect uri: auth0Config.redirectOnSuccess
				}
				else {
					logger.debug 'User does not have roles. Redirecting to the waiting pattern.'
					redirect action: 'notyet'
				}
				return
			}
		}

		if (securityService.loggedIn()) {
			redirect uri: auth0Config.redirectOnSuccess
		}
		else {
			render view: authViewName, model: buildAuthModel()
		}
	}

	def adminLogin() {
		forward controller: 'login', action: 'auth'
	}

	def authfail() {
		String username = session[SpringSecurityUtils.SPRING_SECURITY_LAST_USERNAME_KEY] // TODO set apf.storeLastUsername=true
		String msg = params.error_message ?: ''
		// If the user was redirected here, we try to use the passed-in error message, otherwise, just use the empty string.

		def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]
		if (exception) {
			if (exception instanceof AccountExpiredException) {
				msg = message(code: 'springSecurity.errors.login.expired')
				accessLog username, 'Account Expired', msg
			}
			else if (exception instanceof CredentialsExpiredException) {
				msg = message(code: 'springSecurity.errors.login.passwordExpired')
				accessLog username, 'Password Expired', msg
			}
			else if (exception instanceof DisabledException) {
				msg = message(code: 'springSecurity.errors.login.disabled')
				accessLog username, 'Login Disabled', msg
			}
			else if (exception instanceof LockedException) {
				msg = message(code: 'springSecurity.errors.login.locked')
				accessLog username, 'Login Locked', msg
			}
			else if (exception instanceof IllegalStateException) {
				msg = 'Too slow to login, please try again'
				accessLog username, msg
			}
			else {
				msg = message(code: 'springSecurity.errors.login.fail')
				accessLog username, 'Login Failed', msg
			}
		}

		flash.message = msg
		redirect action: 'auth', params: params
	}

	def callback(String code) {
		redirect auth0Service.callback(code)
	}

	/**
	 * Displays a new TermsOfService, and forces the user to accept it.
	 *
	 * If the user accepts, a Settings record will be updated for the user,
	 * signaling that the user has accepted. On subsequent logins, while the
	 * terms of service is older than the acceptance record, the user will not
	 * be forced to update it.
	 *
	 * If the default tos record is newer than the user's own acceptance, the user will
	 * need to re-accept the 'presumably' new Terms of Service.
	 */
	def checkTOS() {
		Credentials credentials = auth0Service.credentials()
		try {
			auth0Service.checkTOS credentials
			accessLog credentials.username, 'audit', 'User has accepted the new TOS document.'
			redirect uri: auth0Config.redirectOnSuccess
		}
		catch (e) {
			logger.error 'Error while accepting TOS:{}', e.message
			render([error: e, scred: credentials] as JSON)
		}
	}

	def confirm(String username, String email, String firstname, String lastname) {

		String recaptchaResponse = params.'g-recaptcha-response'
		if (auth0Config.useRecaptcha && !recaptchaResponse) {
			accessLog username ?: email ?: 'unknown', 'ERROR',
					'Did not receive reCaptcha information from registration confirmation form. Details:' + params
			throw new RuntimeException('Captcha information is not received. Cannot validate other information until it is provided.')
		}

		Map tagArgs = [controller: 'login', action: 'auth', absolute: 'true']
		String loginUrl = link(tagArgs) { createLink(tagArgs) }
		String appUrl = createLink(uri: '/', absolute: true)

		Map<String, String> redirectArgs = auth0Service.confirmRegistration(recaptchaResponse, username, email,
				firstname, lastname, auth0Service.credentials(), params, loginUrl, appUrl)

		redirect redirectArgs
	}

	def forceAuth() {
		authService.logout()
		render view: authViewName, model: buildAuthModel()
	}

	def logout() {
		accessLog securityService.currentUsername(), 'Logout'

		authService.logout()

		if (customizationConfig.guestAutoLogin) {
			redirect action: 'forceAuth'
		}
		else {
			redirect url: 'https://' + auth0Config.auth0Domain + '/v2/logout?federated&returnTo=' +
					createLink(absolute: true, uri: auth0Config.redirectOnLogout)
		}
	}

	def notauthorized() {
		authService.logout()
		[notAuthorizedTemplate: notAuthorizedTemplate]
	}

	def notyet() {
		authService.logout()
	}

	def registration() {
		Credentials credentials = auth0Service.credentials()
		if (!credentials) {
			session.error_message = 'Could not find credentials for registration.' // TODO flash
			redirect action: 'auth'
			return
		}

		Map userInfo = userService.currentUserInfo(credentials.username)

		logger.info 'Registration starting for {}', credentials.username

		// After saving the credentials, logout from the session
		if (userInfo.level < UserLevel.ONE && userInfo.firstname) {
			logger.info 'User has already registered as {} {}', userInfo.firstname, userInfo.lastname

			authService.logout()

			logger.info 'User is now logged out, and redirect to NOTYET page.'
			redirect action: 'notyet'
			return
		}

		if (!customizationConfig.userSignupEnabled) {
			redirect action: 'notauthorized'
			return
		}

		[user: userInfo]
	}

	def thankyou() {
		authService.logout()
	}

	def tos() {
		Settings settings = customizationService.setting('tos.text')
		if (!settings) {
			// If no TOS has been configured in the database, display just an informational message
			return [tosEffectiveDate: 'N/A', tosValue: 'No Terms of Service has been set up, yet.']
		}

		boolean needAgreement = false

		Credentials credentials = auth0Service.credentials()
		if (credentials) {
			if (credentials.tosVerified == null) {
				logger.debug 'TermsOfService is not set. Will need to agree to it.'
			}
			else {
				logger.debug 'TermsOfService verified:{}', credentials.tosVerified
				needAgreement = !credentials.tosVerified
			}
		}
		else {
			logger.debug 'No credentials for user in session. This should never happen.'
		}

		[needAgreement   : needAgreement,
		 tosEffectiveDate: new SimpleDateFormat('MMM dd, yyyy').format(settings.lastUpdated),
		 tosValue        : settings.fieldvalue + (customizationService.setting('tos.text_cont')?.fieldvalue ?: '')]
	}

	def userlist(AuthUser user, String state) {
		if (state) {
			// Admin is changing state of a user
			auth0Service.changeUserLevel(user, UserLevel.valueOf(state),
					createLink(uri: '/', absolute: true).toString(), userGuideLink, access1DetailsMessage)
			redirect action: 'userlist'
			return
		}

		accessLog securityService.currentUsername(), 'View userlist'
		[users: userService.buildUserListUserInfo(), userSignupEnabled: customizationConfig.userSignupEnabled]
	}

	protected Map buildAuthModel() {
		[auth0ConnectionCss: auth0Service.webtaskCSS(),
		 auth0ConnectionJs : auth0Service.webtaskJavaScript()] + authModel
	}

	private void accessLog(String username, String event, String message = null) {
		accessLogService.report username, event, message
	}

	private void nocache(response) {
		response.setHeader('Cache-Control', 'no-cache') // HTTP 1.1
		response.addDateHeader('Expires', 0)
		response.setDateHeader('max-age', 0)
		response.setIntHeader('Expires', -1) //prevents caching at the proxy server
		response.addHeader('cache-Control', 'private') //IE5.x only
	}

	void afterPropertiesSet() {
		authModel = [auth0CallbackUrl: auth0Config.auth0CallbackUrl,
		             auth0ClientId   : auth0Config.auth0ClientId,
		             auth0Domain     : auth0Config.auth0Domain,
		             uiHeroImageUrl  : customizationConfig.uiHeroImageUrl]
	}
}
