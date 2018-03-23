import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.context.ApplicationContext
import org.springframework.security.access.AccessDeniedException

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class Auth0UrlMappings {
	static mappings = { ApplicationContext ctx ->
		if (SpringSecurityUtils.securityConfig.auth0.active) {
			"/admin/userlist"(controller: 'auth0', action: 'userlist')
			"/login/auth"(controller: 'auth0', action: 'auth')
			"/login/authfail"(controller: 'auth0', action: 'authfail')
			"/login/callback"(controller: 'auth0', action: 'callback')
			"/login/cannotregister"(controller: 'auth0', action: 'cannotregister')
			"/login/forceAuth"(controller: 'auth0', action: 'forceAuth')
			"/login/tos"(controller: 'auth0', action: 'tos')
			"/login/checkTOS"(controller: 'auth0', action: 'checkTOS')
			"/logout"(controller: 'auth0', action: 'logout')
			"/registration/index"(controller: 'auth0', action: 'registration')
			"/registration/confirm"(controller: 'auth0', action: 'confirm')
			"/registration/notauthorized"(controller: 'auth0', action: 'notauthorized')
			"/registration/notyet"(controller: 'auth0', action: 'notyet')
			"/registration/thankyou"(controller: 'auth0', action: 'thankyou')

			"403"(view: '/auth0/denied')
			"500"(view: '/auth0/denied', exception: AccessDeniedException)
		}
	}
}
