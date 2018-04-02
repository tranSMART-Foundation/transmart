package org.transmart.plugin.auth

import grails.plugin.springsecurity.SpringSecurityUtils
import org.transmart.plugin.auth0.Auth0Config
import org.transmart.plugin.auth0.AuthService
import org.transmart.plugin.shared.SecurityService

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class AuthFilters {

	Auth0Config auth0Config
	AuthService authService
	SecurityService securityService

	boolean active = SpringSecurityUtils.securityConfig.auth0.active

	def filters = {

		/**
		 * Intercepts all requests to enforce minimum <code>UserLevel</code> for controller
		 * actions annotated with <code>@RequiresLevel</code>.
		 */
		checkUserLevelAccess(controller: '*', action: '*') {
			before = {
				if (active) {
					authService.checkUserLevelAccess controllerName, actionName
				}
				true
			}
		}

		/**
		 * Automatically store commonly used config vars in the GSP model.
		 */
		commonModel(controller: 'auth0', action: '*') {
			after = { Map model ->
				if (active && model != null) {
					model.appTitle = auth0Config.appTitle
					model.captchaSitekey = auth0Config.captchaSitekey
					model.emailLogo = auth0Config.emailLogo
					model.googleAnalyticsTracking = auth0Config.googleAnalyticsTracking
					model.guestAutoLogin = auth0Config.guestAutoLogin
					model.instanceName = auth0Config.instanceName
					model.instanceType = auth0Config.instanceType
					model.instanceTypeSuffix = auth0Config.instanceTypeSuffix
					model.isTOS = auth0Config.isTOS
					model.pmsdnLogo = auth0Config.pmsdnLogo
					model.principal = securityService.principal()
					model.supportEmail = auth0Config.supportEmail
					model.useRecaptcha = auth0Config.useRecaptcha
					model.userGuideUrl = auth0Config.userGuideUrl
				}
				true
			}
		}
	}
}
