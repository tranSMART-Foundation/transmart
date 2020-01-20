package org.transmart.plugin.auth0

import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.util.logging.Slf4j
import org.apache.commons.validator.routines.EmailValidator
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
import org.transmart.plugin.shared.security.Roles
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.AuthUserSecureAccess
import org.transmart.searchapp.Role
import org.transmart.searchapp.SecureObjectAccess
import org.transmartproject.db.log.AccessLogService

import java.text.SimpleDateFormat

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@Slf4j('logger')
class Auth0Controller implements InitializingBean {

    private static final List<UserLevel> USER_LEVELS
    static {
	List<UserLevel> userLevels = UserLevel.values() as List
	userLevels.remove UserLevel.UNREGISTERED
	USER_LEVELS = userLevels.asImmutable()
    }

    static allowedMethods = [adminUserSave: 'POST', adminUserUpdate: 'POST', checkTOS: 'POST']

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

    def auth() {
	nocache response

	logger.debug 'auth request {}', request
	logger.debug 'auth request querystring {}', request.queryString

	boolean forcedFormLogin = request.queryString
	if (customizationConfig.guestAutoLogin && !forcedFormLogin) {
	    logger.info 'Automatic login with userid {}', customizationConfig.guestUserName
	    AuthUser authUser = userService.authUser(customizationConfig.guestUserName)
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
	    logger.debug 'securityService.loggedIn() true: redirect auth0Config.redirectOnSuccess {}', auth0Config.redirectOnSuccess
	    redirect uri: auth0Config.redirectOnSuccess
	}
	else {
	    logger.debug 'securityService.loggedIn() false: view: authViewName {} buildAuthModel {}', authViewName, buildAuthModel()
	    render view: authViewName, model: buildAuthModel()
	}
    }

    def passwordLogin() {
	if (auth0AdminExists()) {
	    render status: 404
	}
	else {
	    forward controller: 'login', action: 'auth'
	}
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
									    firstname, lastname, auth0Service.credentials(),
									    params, loginUrl, appUrl)

	redirect redirectArgs
    }

    def forceAuth() {
	authService.logout()
	render view: authViewName, model: buildAuthModel()
    }

    def logout() {
	accessLog securityService.currentUsername(), 'Logout'

	boolean auth0Login = securityService.authentication() instanceof Auth0JWTToken

	authService.logout()

	if (customizationConfig.guestAutoLogin || !auth0Login) {
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

    def adminUserList(String sort, String order, Integer max, Integer offset) {
	accessLog securityService.currentUsername(), 'View userlist'

	render view: 'userlist', model: [
	    users: userService.buildUserListUserInfo(sort, order ?: 'asc', max ?: 10, offset ?: 0),
	    userCount: AuthUser.count()]
    }

    def adminUserShow(AuthUser authUser) {
	if (!authUser) {
	    flash.message = 'AuthUser not found with id ' + params.id
	    redirect action: 'adminUserList'
	    return
	}

	Map person = userService.buildUserInfo(authUser)
	person.groups = authUser.groups
	person.changePassword = authUser.changePassword

	[person   : person,
	 roleNames: authUser.authorities*.authority.sort(),
	 soas     : SecureObjectAccess.findAllByPrincipal(authUser, [sort: 'accessLevel']),
	 ausas    : AuthUserSecureAccess.findAllByAuthUser(authUser, [sort: 'accessLevel'])]
    }

    def adminUserEdit(AuthUser authUser) {
	if (authUser) {
	    buildPersonModel authUser
	}
	else {
	    flash.message = 'AuthUser not found with id ' + params.id
	    redirect action: 'list'
	}
    }

    def adminUserUpdate(AuthUser authUser) {
	saveOrUpdate authUser
    }

    def adminUserCreate() {
	buildPersonModel new AuthUser(), UserLevel.UNREGISTERED
    }

    def adminUserSave() {
	saveOrUpdate()
    }

    private saveOrUpdate(AuthUser authUser) {

	boolean create = params.id == null
	if (create) {
	    authUser = new AuthUser(enabled: false, passwd: 'auth0',
				    username: UUID.randomUUID().toString())
	}

	authUser.userRealName = params.userRealName ?: ''
	authUser.name = authUser.userRealName

	authUser.email = params.email ?: ''

	authUser.validate()

	if (authUser.email) {
	    if (!EmailValidator.instance.isValid(authUser.email)) {
		authUser.errors.rejectValue 'email', 'valid', null,
		    'Please enter a valid email address'
	    }
	}
	else {
	    authUser.errors.rejectValue 'email', 'blank', null,
		'Email address is required'
	}

	authUser.uniqueId = Auth0Service.auth0Providers[params.auth0Provider]
	String providerId = params.uniqueId ?: ''
	if (providerId) {
	    authUser.uniqueId += providerId
	}
	if (create) {
	    authUser.uniqueId += '_UNINITIALIZED'
	}

	String message
	if (create) {
	    message = 'User: ' + authUser.username + ' for ' + authUser.userRealName + ' created'
	}
	else {
	    message = '' + authUser.username + ' has been updated. Changed fields include: '
	    message += authUser.dirtyPropertyNames.collect { String field ->
		def newValue = authUser[field]
		def oldValue = authUser.getPersistentValue(field)
		if (newValue != oldValue) {
		    '' + field + ' (' + oldValue + ' -> ' + newValue + ')'
		}
	    }.findAll().join(', ')
	}

	UserLevel userLevel = UserLevel.valueOf(params.userLevel)
	boolean ok = !authUser.hasErrors() && auth0Service.createOrUpdate(authUser, create, userLevel, message,
									  createLink(uri: '/', absolute: true).toString())

	if (ok) {
	    redirect action: 'adminUserShow', id: authUser.id
	}
	else {
	    flash.message = 'An error occurred, cannot save user'
	    render view: create ? 'adminUserCreate' : 'adminUserEdit', model: buildPersonModel(authUser, userLevel)
	}
    }

    private Map buildPersonModel(AuthUser authUser, UserLevel userLevel = null) {
	Map description = (Map) JSON.parse(authUser.description ?: '{}')

	Map.Entry<String, String> auth0ProviderEntry = Auth0Service.auth0Providers.entrySet().find { Map.Entry<String, String> entry ->
	    authUser.uniqueId?.startsWith entry.value
	}
	String providerId = (auth0ProviderEntry ? authUser.uniqueId - auth0ProviderEntry.value : '') - '_UNINITIALIZED'

	[person        : authUser,
	 userLevel     : userLevel ?: customizationService.userLevel(authUser),
	 connection    : description.connection,
	 auth0Providers: Auth0Service.auth0Providers.keySet(),
	 auth0Provider : auth0ProviderEntry?.key,
	 uniqueId      : providerId,
	 userLevels    : USER_LEVELS]
    }

    protected Map buildAuthModel() {
	String webtaskCSS = auth0Config.webtaskBaseUrl ? auth0Service.webtaskCSS() : ''
	String webtaskJavaScript = auth0Config.webtaskBaseUrl ? auth0Service.webtaskJavaScript() : ''
	logger.debug 'buildAuthModel webtaskCSS {} webtaskJavaScript {}', webtaskCSS, webtaskJavaScript
	[auth0ConnectionCss: webtaskCSS,
	 auth0ConnectionJs : webtaskJavaScript,
	 auth0AdminExists: auth0AdminExists()] + authModel
    }

    private boolean auth0AdminExists() {
	Role.findByAuthority(Roles.ADMIN.authority).people.any { AuthUser u -> u.passwd == 'auth0' }
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
		     // not referenced in any other page
		     //		     uiHeroImageUrl  : customizationConfig.uiHeroImageUrl,
		     auth0Domain     : auth0Config.auth0Domain]
    }
}
