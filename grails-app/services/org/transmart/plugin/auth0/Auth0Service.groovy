package org.transmart.plugin.auth0

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.gsp.PageRenderer
import grails.plugin.cache.Cacheable
import grails.plugin.mail.MailService
import grails.plugin.springsecurity.SpringSecurityService
import grails.transaction.Transactional
import groovy.transform.CompileDynamic
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.TransactionStatus
import org.springframework.web.context.request.RequestContextHolder
import org.transmart.plugin.custom.CustomizationConfig
import org.transmart.plugin.custom.CustomizationService
import org.transmart.plugin.custom.Settings
import org.transmart.plugin.custom.UserLevel
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.UtilService
import org.transmart.plugin.shared.security.Roles
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.Role
import org.transmartproject.db.log.AccessLogService
import us.monoid.json.JSONObject
import us.monoid.web.Resty

import javax.servlet.http.HttpServletRequest

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@GrailsCompileStatic
@Slf4j('logger')
class Auth0Service implements InitializingBean {

	private static final String CREDENTIALS_KEY = 'auth0Credentials'

	private Algorithm algorithm
	private String oauthTokenUrl
	private String userInfoUrl

	@Autowired private AccessLogService accessLogService
	@Autowired private AuthService authService
	@Autowired private CustomizationConfig customizationConfig
	@Autowired private CustomizationService customizationService
	@Autowired private MailService mailService
	@Autowired private PageRenderer groovyPageRenderer
	@Autowired private SecurityService securityService
	@Autowired private SpringSecurityService springSecurityService
	@Autowired private UserService userService
	@Autowired private UtilService utilService

	@Autowired(required = false)
	private Auth0Config auth0Config

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
				authenticateAs credentials
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
	Credentials createCredentials(String code, String redirectUri) {

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
		String uniqueId = userInfo.getString('sub')

		idToken = rebuildJwt(idToken, email)

		Credentials credentials = new Credentials(
				accessToken: accessToken,
				connection: userInfo.getJSONArray('identities').getJSONObject(0).getString('connection'),
				email: email,
				idToken: idToken,
				name: 'unregistered',
				nickname: 'unregistered',
				picture: '')

		AuthUser existingUser
		List<AuthUser> uninitialized = AuthUser.findAllByEmailAndPasswdAndEnabledAndUniqueIdLikeAndDescriptionIsNull(
				credentials.email, 'auth0', false, '%UNINITIALIZED')
		if (uninitialized.size() > 1) {
			// TODO
		}
		else if (uninitialized.size() == 1) {
			existingUser = uninitialized[0]
			credentials.username = existingUser.username = UUID.randomUUID().toString()
			finishUninitializedUser existingUser, credentials, uniqueId
		}
		else {
			existingUser = userService.findBy('uniqueId', uniqueId)
		}

		if (existingUser) {
			credentials.id = existingUser.id
			credentials.level = customizationService.userLevel(existingUser)
			credentials.username = existingUser.username
		}
		else {
			credentials.username = UUID.randomUUID().toString()
			createUser credentials, uniqueId
			credentials.level = UserLevel.UNREGISTERED
		}

		currentRequest().session.setAttribute CREDENTIALS_KEY, credentials

		credentials
	}

	private String rebuildJwt(String idToken, String email) {
		DecodedJWT decodedJwt = JWT.decode(idToken)
		JWT.create()
				.withAudience(decodedJwt.audience as String[]) // 'aud', e.g. 'ywAq4Xu4Kl3uYNdm3m05Cc5ow0OibvXt'
				.withExpiresAt(decodedJwt.expiresAt) // 'exp'
				.withIssuedAt(decodedJwt.issuedAt) // 'iat'
				.withIssuer(decodedJwt.issuer) // 'iss', e.g. 'https://avillachlab.auth0.com/'
				.withKeyId(decodedJwt.keyId) // 'kid', e.g. 'RkNBQjE5OUNENzY3NjIwN0VCMTgwNjE3MDUwRTJDMUZFNDg4NkFERg'
				.withSubject(decodedJwt.subject) // 'sub', e.g. 'google-oauth2|...'
				.withClaim('email', email)
				.sign(algorithm)
	}

	@Transactional
	AuthUser createUser(Credentials credentials, String uniqueId) {
		AuthUser user = new AuthUser(
				description: buildDescription(credentials),
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
			logger.error 'Could not create user {} because {}', credentials.username, utilService.errorStrings(user)
		}
		logger.info 'getCredentials() New user record has been created: {}', credentials.username
		user
	}

	@Transactional
	void finishUninitializedUser(AuthUser user, Credentials credentials, String uniqueId) {
		user.description = buildDescription(credentials)
		user.enabled = true
		user.uniqueId = uniqueId
		user.save()
		if (user.hasErrors()) {
			logger.error 'Could not finish uninitialized user {} because {}', credentials.username, utilService.errorStrings(user)
		}
		else {
			logger.info 'finishUninitializedUser() updated uninitialized user {}', credentials.username
		}
		user
	}

	private String buildDescription(Credentials credentials) {
		([about     : '',
		  connection: credentials.connection,
		  firstname : '',
		  lastname  : '',
		  phone     : '',
		  picture   : credentials.picture] as JSON).toString()
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
		AuthUser authUser = userService.updateAuthUser(null, username, email, firstname, lastname, credentials, params)
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

	private void sendSignupEmails(String username, String email, AuthUser authUser, String loginUrl, String appUrl) {
		Map personDescription = (Map) JSON.parse(authUser.description)
		String emailLogo = customizationConfig.instanceType == 'pmsdn' ? customizationConfig.pmsdnLogo : customizationConfig.emailLogo
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
				template: '/auth0/email_signup' + customizationConfig.instanceTypeSuffix, model: [
				appUrl      : appUrl,
				emailLogo   : emailLogo,
				instanceName: customizationConfig.instanceName,
				person      : personDescription])
		sendEmail customizationConfig.emailNotify, 'Registration Request', body

		logger.debug 'Sent `Registration Request` e-mail to administrator(s)'

		accessLog username ?: email ?: 'unknown', 'user_registration-INFO',
				"New user $email has been registered"

		// Send registration confirmation e-mail to the user, once the form has been submitted.
		body = groovyPageRenderer.render(template: '/auth0/email_thankyou', model: [
				email       : personDescription.email ?: 'E-mailAddress',
				emailLogo   : emailLogo,
				firstName   : personDescription.firstname ?: 'FirstName',
				instanceName: customizationConfig.instanceName,
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
		if ('auto'.equalsIgnoreCase(customizationConfig.accessLevel1)) {
			// If configuration is set to auto-approve, go ahead and assign the basic roles.
			authService.grantRoles authUser, Roles.STUDY_OWNER, Roles.PUBLIC_USER
			logger.debug 'Assigned basic, Level1 access to new user'

			// If configuration is set to auto-approve, after filling out the registration form
			// the user will be redirected to the default internal page of the applications.
			securityService.authenticateAs username
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
		logger.info alertMsg
		accessLog securityService.currentUsername(), 'GrantAccess', alertMsg

		UserLevel userLevel = customizationService.userLevel(user)
		String levelName
		switch (userLevel) {
			case UserLevel.ONE:   levelName = customizationConfig.instanceType == 'grdr' ? 'Open Data' : 'Level 1'; break
			case UserLevel.TWO:   levelName = customizationConfig.instanceType == 'grdr' ? 'Controlled Data' : 'Level 2'; break
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

	private void updateRoles(UserLevel level, AuthUser user) {
		removeRoles user

		switch (level) {
			case UserLevel.ZERO:  authService.grantRoles user, Roles.PUBLIC_USER; break
			case UserLevel.ONE:   authService.grantRoles user, Roles.STUDY_OWNER; break
			case UserLevel.TWO:   authService.grantRoles user, Roles.DATASET_EXPLORER_ADMIN; break
			case UserLevel.ADMIN: authService.grantRoles user, Roles.ADMIN; break
		}
	}

	@CompileDynamic
	private void removeRoles(AuthUser user) {
		for (Role role in Role.list()) {
			role.removeFromPeople user
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

		Settings defaultTosSettings = customizationService.setting('tos.text')
		if (!defaultTosSettings) {
			logger.debug 'There is no TOS set up.'
			// There is no TOS settings. Verify by default. If the Settings does
			// not exist, it means we do not have TOS for anybody to re-agree to.
			return true
		}

		Settings userTosSettings = customizationService.userSetting('tos.text', potentialUserId)
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
		Settings tosTextSettings = customizationService.userSetting('tos.text', credentials.id)
		if (!tosTextSettings) {
			// This is the first time accepting. Create new settings record
			new Settings(userid: credentials.id, fieldname: 'tos.text', fieldvalue: 'Accepted').save()
		}
		else {
			tosTextSettings.lastUpdated = new Date()
			tosTextSettings.save()
		}

		credentials.tosVerified = null

		authenticateAs credentials
	}

	/**
	 * Build an <code>Authentication</code> for the given credentials and register
	 * it in the security context.
	 */
	void authenticateAs(Credentials credentials) {
		Auth0JWTToken tokenAuth = new Auth0JWTToken(credentials.idToken)
		tokenAuth.principal = authService.loadAuthUserDetailsByUniqueId(tokenAuth.decodedJWT.subject)
		tokenAuth.authenticated = true
		SecurityContextHolder.context.authentication = tokenAuth
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
	 * Convenience method to get the JWT token from the current authentication.
	 */
	String jwtToken() {
		if (securityService.loggedIn()) {
			Authentication auth = securityService.authentication()
			if (auth instanceof Auth0JWTToken) {
				((Auth0JWTToken) auth).jwtToken
			}
		}
	}

	@Transactional
	void autoCreateAdmin() {
		if (!auth0Config?.autoCreateAdmin) {
			logger.info 'Auth0 is disabled, or admin auto-create is disabled, not creating admin user'
			return
		}

		if (Role.findByAuthority(Roles.ADMIN.authority).people) {
			logger.info 'admin auto-create is enabled but an admin user exists, not creating admin user'
			return
		}

		String username = auth0Config.autoCreateAdminUsername
		if (!username) {
			logger.error 'admin auto-create is enabled but no username is specified, cannot create admin user'
			return
		}

		if (AuthUser.countByUsername(username)) {
			logger.error 'admin auto-create is enabled but non-admin user "{}" exists, not creating admin user', username
			return
		}

		String password = auth0Config.autoCreateAdminPassword
		if (!password) {
			logger.error 'admin auto-create is enabled but no password is specified, cannot create admin user'
			return
		}

		String email = auth0Config.autoCreateAdminEmail // can be null

		// don't double-hash
		boolean hashed = (password.length() == 59 || password.length() == 60) &&
				(password.startsWith('$2a$') || password.startsWith('$2b$') || password.startsWith('$2y$'))

		AuthUser admin = new AuthUser(description: 'System admin', email: email ?: null, enabled: true,
				name: 'System admin', passwd: hashed ? password : springSecurityService.encodePassword(password),
				uniqueId: username, userRealName: 'System admin', username: username)

		String errorMessage = createAdmin(admin, transactionStatus)
		if (errorMessage) {
			accessLogService.report 'BootStrap', 'admin auto-create', errorMessage
		}
	}

	private String createAdmin(AuthUser admin, TransactionStatus transactionStatus) {
		if (admin.save(flush: true)) {
			Role.findByAuthority(Roles.ADMIN.authority).addToPeople admin
			logger.info 'auto-created admin user'
			accessLogService.report 'BootStrap', 'admin auto-create', 'created admin user'
			null
		}
		else {
			transactionStatus.setRollbackOnly()
			String message = 'auto-create admin user failed: ' + utilService.errorStrings(admin)
			logger.error message
			message
		}
	}

	private String webtask(String urlMethod) {
		(auth0Config.webtaskBaseUrl + '/connection_details_base64?webtask_no_cache=1&' + urlMethod).toURL().text
	}

	void afterPropertiesSet() {
		if (auth0Config) { // not injected if active=false
			algorithm = Algorithm.HMAC256(auth0Config.auth0ClientSecret)
			oauthTokenUrl = 'https://' + auth0Config.auth0Domain + '/oauth/token'
			userInfoUrl = 'https://' + auth0Config.auth0Domain + '/userinfo?access_token='
		}
	}

	protected HttpServletRequest currentRequest() {
		((GrailsWebRequest) RequestContextHolder.currentRequestAttributes()).request
	}
}
