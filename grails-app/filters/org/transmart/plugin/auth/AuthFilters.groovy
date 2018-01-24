package org.transmart.plugin.auth

import org.transmart.plugin.auth0.Auth0Config
import org.transmart.plugin.auth0.AuthService

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class AuthFilters {

	Auth0Config auth0Config
	AuthService authService

	def filters = {

		/**
		 * Intercepts all requests to enforce minimum <code>UserLevel</code> for controller
		 * actions annotated with <code>@RequiresLevel</code>.
		 */
		checkUserLevelAccess(controller: '*', action: '*') {
			before = {
				authService.checkUserLevelAccess controllerName, actionName
				true
			}
		}

		/**
		 * Automatically store commonly used config vars in the GSP model.
		 */
		commonModel(controller: 'auth0', action: '*') {
			after = { Map model ->
				if (model != null) {
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
					model.principal = authService.principal()
					model.useRecaptcha = auth0Config.useRecaptcha
				}
				true
			}
		}
	}
}
