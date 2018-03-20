package org.transmart.plugin.auth0

import com.auth0.jwt.algorithms.Algorithm
import grails.converters.JSON
import grails.gsp.PageRenderer
import grails.plugin.cache.Cacheable
import grails.transaction.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.request.RequestContextHolder
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.Role
import org.transmartproject.db.log.AccessLogService
import us.monoid.json.JSONObject
import us.monoid.web.Resty

import javax.servlet.http.HttpServletRequest

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j('logger')
class Auth0Service implements InitializingBean {

	private static final String CREDENTIALS_KEY = 'auth0Credentials'

	private Algorithm algorithm
	private String oauthTokenUrl
	private String userInfoUrl

	@Autowired private AccessLogService accessLogService
	@Autowired private AuthService authService
	@Autowired private Auth0Config auth0Config
	@Autowired private PageRenderer groovyPageRenderer
	@Autowired private UserService userService

	def mailService

	/**
	 * Handle the Auth0 callback.
	 * @param code the code to use to get an access token
	 * @return a 1-element map with either a redirect uri under the 'uri' key
	 *         or a redirect action under the 'action' key
	 */
	Map<String, String> callback(String code) {
		HttpServletRequest request = currentRequest()
		String port
		String scheme = request.scheme.toLowerCase()
		if ((scheme == 'http' && request.serverPort == 80) || (scheme == 'https' && request.serverPort == 443)) {
			port = ''
		}
		else {
			port = ':' + request.serverPort
		}
		String redirectUri = request.scheme + '://' + request.serverName + port + request.contextPath

		Credentials credentials = createCredentials(code, redirectUri)

		if (credentials.username && credentials.level > UserLevel.ZERO) {
			credentials.tosVerified = verifyTOSAccepted(credentials.id)
			if (credentials.tosVerified) {
				authService.authenticateAs credentials.username
				[uri: auth0Config.redirectOnSuccess]
			}
			else {
				[action: 'tos']
			}
		}
		else {
			[action: 'registration']
		}
	}

	/**
	 * @return the <code>Credentials</code> instance from the HTTP session
	 */
	Credentials credentials() {
		(Credentials) currentRequest().session.getAttribute(CREDENTIALS_KEY)
	}

	/**
	 * Creates an initial Credentials instance and stores it in the HTTP session.
	 * @param code the 'code' querystring parameter from the Auth0 callback
	 * @param redirectUri base of the callback url, e.g. https://server/contextPath
	 */
	@CompileDynamic
	Credentials createCredentials(String code, String redirectUri) {

		Credentials credentials

		try {
			JSONObject json = new JSONObject(
					client_id: auth0Config.auth0ClientId,
					client_secret: auth0Config.auth0ClientSecret,
					code: code,
					grant_type: 'authorization_code',
					redirect_uri: redirectUri)

			Resty resty = new Resty()
			JSONObject tokenInfo = resty.json(oauthTokenUrl, Resty.content(json)).toObject()
			// {
			//   "access_token":"...",
			//   "expires_in":86400,
			//   "id_token":"...",
			//   "token_type":"Bearer"
			// }

			String accessToken = tokenInfo.getString('access_token')
			String idToken = tokenInfo.getString('id_token')

			JSONObject userInfo = resty.json(userInfoUrl + accessToken).toObject()
			// {
			// 	"app_metadata":
			// 		{
			// 			"roles":["ROLE_CITI_USER"]
			// 		},
			// 		"clientID":"...",
			// 		"created_at":"2017-11-21T15:19:50.683Z",
			// 		"email":"burtbeckwith@gmail.com",
			// 		"email_verified":true,
			// 		"family_name":"Beckwith",
			// 		"gender":"male",
			// 		"given_name":"Burt",
			// 		"identities":[
			// 			{
			// 				"connection":"google-oauth2",
			// 				"isSocial":true,
			// 				"provider":"google-oauth2",
			// 				"user_id":"..."
			// 			}
			// 		],
			// 		"locale":"en",
			// 		"name":"Burt Beckwith",
			// 		"nickname":"burtbeckwith",
			// 		"picture":"https://lh3.googleusercontent.com/-rG-S66wU1LI/AAAAAAAAAAI/AAAAAAAAAfE/ijUU6rz8j3I/photo.jpg",
			// 		"roles":["ROLE_CITI_USER"],
			// 		"sub":"google-oauth2|...",
			// 		"updated_at":"2018-02-20T13:25:20.721Z",
			// 		"user_id":"google-oauth2|..."
			// }

			String email = userInfo.getString('email')
			credentials = new Credentials(
					accessToken: accessToken,
					connection: userInfo.getJSONArray('identities').getJSONObject(0).getString('connection'),
					email: email,
					idToken: idToken,
					name: 'unregistered',
					nickname: 'unregistered',
					picture: '',
					username: email)

			currentRequest().session.setAttribute CREDENTIALS_KEY, credentials

			AuthUser existingUser = authService.authUser(credentials.username)
			if (existingUser) {
				credentials.id = existingUser.id
				credentials.level = authService.userLevel(existingUser)
			}
			else {
				String uniqueId = userInfo.getString('sub')
				createUser credentials, uniqueId
				credentials.level = UserLevel.UNREGISTERED
			}
		}
		catch (e) {
			logger.error e.message
		}

		credentials
	}

	@Transactional
	AuthUser createUser(Credentials credentials, String uniqueId) {
		String description = ([about     : '',
		                       connection: credentials.connection,
		                       firstname : '',
		                       lastname  : '',
		                       phone     : '',
		                       picture   : credentials.picture] as JSON).toString()

		AuthUser user = new AuthUser(
				description: description,
				email: credentials.email,
				emailShow: true,
				enabled: true,
				name: credentials.name,
				passwd: 'auth0', // need a non-blank value for validation
				uniqueId: uniqueId,
				username: credentials.username,
				userRealName: credentials.name)
		if (!user.name) {
			user.name = user.userRealName
		}

		user.save()
		if (user.hasErrors()) {
			logger.error 'Could not create user {} because {}', credentials.username, userService.errorStrings(user)
		}
		logger.info 'getCredentials() New user record has been created: {}', credentials.username
		user
	}

	/**
	 * Registration confirmation.
	 * Steps: Validate reCaptcha, via Google
	 *        Find user, based on hidden variable on registration form
	 *        Update user record with registration form information
	 *        Set the basic (Level1) roles for the user
	 *        Send notification e-mail to admin
	 *        Send confirmation e-mail to user
	 *        Set authentication in SpringSecurity
	 *        Redirect to initial page
	 *
	 * @return a 1-element map with either a redirect uri under the 'uri' key
	 *         or a redirect action under the 'action' key
	 */
	@Transactional
	Map<String, String> confirmRegistration(String recaptchaResponse, String username, String email,
	                                        String firstname, String lastname, Credentials credentials,
	                                        Map params, String loginUrl, String appUrl) {
		if (auth0Config.useRecaptcha) {
			verifyRecaptchaResponse recaptchaResponse, username ?: email ?: 'unknown'
		}
		AuthUser authUser = updateAuthUser(null, username, email, firstname, lastname, credentials, params)
		sendSignupEmails username, email, authUser, loginUrl, appUrl
		grantRolesAndStoreAuth authUser, username
	}

	private void verifyRecaptchaResponse(String recaptchaResponse, String username) {
		// Verification parameters, per Googly's information  https://developers.google.com/recaptcha/docs/verify
		Resty resty = new Resty()
		JSONObject confirmation = resty.json(auth0Config.captchaVerifyUrl, resty.form(
				resty.data('secret', auth0Config.captchaSecret),
				resty.data('response', recaptchaResponse))).toObject()

		if (confirmation.getBoolean('success')) {
			accessLog username, 'captcha_verify-INFO',
					'Registration process has been allowed, per reCAPTCHAverification from ' + currentRequest().remoteHost
		}
		else {
			// If Google does not return a success message, log the error response and throw description exception back to the user
			accessLog username, 'captcha_verify-ERROR',
					confirmation.toString() + ' from host ' + currentRequest().remoteHost
			throw new RuntimeException('Captcha verification step has failed.')
		}
	}

	private AuthUser updateAuthUser(AuthUser user, String username, String email, String firstname,
	                                String lastname, Credentials credentials, Map params) {

		boolean existingUser
		if (user) {
			username = user.username
			existingUser = true
		}
		else {
			user = authService.authUser(username)
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
				user.userRealName = (firstname ?: '') + ' ' + (lastname ?: '')
			}
			user.name = user.userRealName
			if (email) {
				user.email = email
			}
			Map description = [:] + params
			description.remove 'g-recaptcha-response'
			description.remove 'action'
			description.remove 'controller'

			description.access_token = credentials.accessToken
			description.id_token = credentials.idToken
			description.connection = credentials.connection
			description.picture = credentials.picture ?: ''

			user.description = (description as JSON).toString()
			user.save()
			if (user.hasErrors()) {
				logger.error 'Error updating user{}: {}', credentials.username, userService.errorStrings(user)
			}
			else{
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

	private void sendSignupEmails(String username, String email, AuthUser authUser, String loginUrl, String appUrl) {
		Map personDescription = (Map) JSON.parse(authUser.description)
		String emailLogo = auth0Config.instanceType == 'pmsdn' ? auth0Config.pmsdnLogo : auth0Config.emailLogo
		if (!emailLogo.startsWith('data:')) {
			if (appUrl.endsWith('/') && emailLogo.startsWith('/')) {
				emailLogo = appUrl + emailLogo.substring(1)
			}
			else {
				emailLogo = appUrl + emailLogo
			}
		}

		// Send notification to admin that a user has completed the sign-up form
		String body = groovyPageRenderer.render(
				template: '/auth0/email_signup' + auth0Config.instanceTypeSuffix, model: [
				appUrl      : appUrl,
				emailLogo   : emailLogo,
				instanceName: auth0Config.instanceName,
				person      : personDescription])
		sendEmail auth0Config.emailNotify, 'Registration Request', body

		logger.debug 'Sent `Registration Request` e-mail to administrator(s)'

		accessLog username ?: email ?: 'unknown', 'user_registration-INFO',
				"New user $email has been registered"

		// Send registration confirmation e-mail to the user, once the form has been submitted.
		body = groovyPageRenderer.render(template: '/auth0/email_thankyou', model: [
				email       : personDescription.email ?: 'E-mailAddress',
				emailLogo   : emailLogo,
				firstName   : personDescription.firstname ?: 'FirstName',
				instanceName: auth0Config.instanceName,
				lastName    : personDescription.lastname ?: 'LastName',
				loginUrl    : loginUrl,
				user        : authUser])
		sendEmail authUser.email, 'Registration Confirmation', body
		logger.debug 'Sent `Registration Confirmation` e-mail to user'
		accessLog email, 'user_registration-INFO', "Confirmation e-mails for $email has been sent"
	}

	/**
	 * @return a 1-element map with either a redirect uri under the 'uri' key
	 *         or a redirect action under the 'action' key
	 */
	private Map<String, String> grantRolesAndStoreAuth(AuthUser authUser, String username) {
		if ('auto'.equalsIgnoreCase(auth0Config.accessLevel1)) {
			// If configuration is set to auto-approve, go ahead and assign the basic roles.
			authService.grantRoles authUser, Roles.STUDY_OWNER, Roles.PUBLIC_USER
			logger.debug 'Assigned basic, Level1 access to new user'

			// If configuration is set to auto-approve, after filling out the registration form
			// the user will be redirected to the default internal page of the applications.
			authService.authenticateAs username
			logger.info 'Automated approval is set. User {} has roles assigned and logged into the app.', username

			[uri: auth0Config.redirectOnSuccess]
		}
		else {
			authService.grantRoles authUser, Roles.PUBLIC_USER
			logger.debug 'Assigned basic, Level 0 access to new user. Will have to wait for administrative approval.'
			logger.info 'Automated approval is NOT set. User {} needs to wait for administrator to approve.', username
			[action: 'thankyou']
		}
	}

	private void accessLog(String username, String event, String message = null) {
		accessLogService.report username, event, message
	}

	@CompileDynamic
	private void sendEmail(String recipient, String theSubject, String body) {
		mailService.sendMail {
			to recipient
			subject theSubject
			html body
		}
	}

	void changeUserLevel(AuthUser user, UserLevel newLevel, String appUrl,
	                     String userGuideLink, String access1DetailsMessage) {

		updateRoles newLevel, user

		String alertMsg = "User <b>$user.username</b> has been granted <b>$newLevel</b> access."
		log.info alertMsg
		accessLog authService.currentUsername(), 'GrantAccess', alertMsg

		UserLevel userLevel = authService.userLevel(user)
		String levelName
		switch (userLevel) {
			case UserLevel.ONE: levelName = auth0Config.instanceType == 'grdr' ? 'Open Data' : 'Level 1'; break
			case UserLevel.TWO: levelName = auth0Config.instanceType == 'grdr' ? 'Controlled Data' : 'Level 2'; break
			case UserLevel.ADMIN: levelName = 'Administrator'; break
			default: levelName = 'Unknown'
		}

		String body = groovyPageRenderer.render(template: '/auth0/email_accessgranted', model: [
				access1DetailsMessage: access1DetailsMessage,
				appUrl               : appUrl,
				levelName            : levelName,
				userGuideLink        : userGuideLink,
				userlevel            : userLevel])
		sendEmail user.email, 'Access Granted', body
	}

	@CompileDynamic
	private void updateRoles(UserLevel level, AuthUser user) {
		for (Role role in Role.list()) {
			role.removeFromPeople user
		}

		switch (level) {
			case UserLevel.ZERO:  authService.grantRoles user, Roles.PUBLIC_USER; break
			case UserLevel.ONE:   authService.grantRoles user, Roles.PUBLIC_USER, Roles.STUDY_OWNER; break
			case UserLevel.TWO:   authService.grantRoles user, Roles.PUBLIC_USER, Roles.DATASET_EXPLORER_ADMIN; break
			case UserLevel.ADMIN: authService.grantRoles user, Roles.PUBLIC_USER, Roles.ADMIN; break
		}
	}

	/**
	 * Check if the latest TOS has been re-agreed-to by the current user.
	 *
	 * The date the latest TOS has been created is stored in , and the
	 * flag storing the confirmation (or if flag missing, the NOT confirmation)
	 * by the user is in the user account.
	 */
	boolean verifyTOSAccepted(long potentialUserId) {
		logger.debug 'verifyTOSAccepted({})', potentialUserId

		Settings defaultTosSettings = authService.setting('tos.text')
		if (!defaultTosSettings) {
			logger.debug 'There is no TOS set up.'
			// There is no TOS settings. Verify by default. If the Settings does
			// not exist, it means we do not have TOS for anybody to re-agree to.
			return true
		}

		Settings userTosSettings = authService.userSetting('tos.text', potentialUserId)
		if (!userTosSettings) {
			logger.debug 'User never accepted TOS, before.'
			// User never approved TOS. Fail by default.
			return false
		}

		if (defaultTosSettings.lastUpdated < userTosSettings.lastUpdated) {
			// If the date of the latest TOS is less then the user's agreement date, it means the latest TOS has been accepted by the user.
			// As a side note, the date of the user's agreeing to the TOS, should have a corresponding AccessLog entry in the database.
			logger.debug 'User already accepted the latest TOS.{} < {}',
					defaultTosSettings.lastUpdated, userTosSettings.lastUpdated
			return true
		}

		logger.debug 'User has not accepted the latest TOS. Will be forced to. def: {} < usr: {}',
				defaultTosSettings.lastUpdated, userTosSettings.lastUpdated
		false
	}

	@Transactional
	void checkTOS(Credentials credentials) {
		Settings tosTextSettings = authService.userSetting('tos.text', credentials.id)
		if (!tosTextSettings) {
			// This is the first time accepting. Create new settings record
			new Settings(userid: credentials.id, fieldname: 'tos.text', fieldvalue: 'Accepted').save()
		}
		else {
			tosTextSettings.lastUpdated = new Date()
			tosTextSettings.save()
		}

		credentials.tosVerified = null

		authService.authenticateAs credentials.username
	}

	@Cacheable('webtask')
	String webtaskJavaScript() {
		webtask 'client_id=' + auth0Config.auth0ClientId
	}

	@Cacheable('webtask')
	String webtaskCSS() {
		webtask 'css=true'
	}

	/**
	 * Update existing user details information.
	 *
	 * @param params The params from request
	 * @return Updated user instance, that may contain errors
	 */
	@Transactional
	AuthUser updateUser(String email, String firstname, String lastname, Map params) {
		AuthUser user = authService.currentAuthUser()
		updateAuthUser user, null, email, firstname, lastname, credentials(),
				userService.currentUserInfo(user) + params
	}

	private String webtask(String urlMethod) {
		(auth0Config.webtaskBaseUrl + '/connection_details_base64?webtask_no_cache=1&' + urlMethod).toURL().text
	}

	void afterPropertiesSet() {
		algorithm = Algorithm.HMAC256(auth0Config.auth0ClientSecret)
		oauthTokenUrl = 'https://' + auth0Config.auth0Domain + '/oauth/token'
		userInfoUrl = 'https://' + auth0Config.auth0Domain + '/userinfo?access_token='
	}

	protected HttpServletRequest currentRequest() {
		((GrailsWebRequest) RequestContextHolder.currentRequestAttributes()).request
	}
}
