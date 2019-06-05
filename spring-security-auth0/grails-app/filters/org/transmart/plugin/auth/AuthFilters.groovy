package org.transmart.plugin.auth

import grails.plugin.springsecurity.SpringSecurityUtils
import org.transmart.plugin.auth0.Auth0Config
import org.transmart.plugin.custom.CustomizationConfig
import org.transmart.plugin.shared.SecurityService

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class AuthFilters {

    Auth0Config auth0Config
    CustomizationConfig customizationConfig
    SecurityService securityService

    boolean active = SpringSecurityUtils.securityConfig.auth0.active

    def filters = {

	/**
	 * Automatically store commonly used config vars in the GSP model for /auth0/** urls.
	 */
	auth0CommonModel(controller: 'auth0', action: '*') {
	    after = { Map model ->
		if (active && model != null) {
		    model.appTitle = customizationConfig.appTitle
		    model.captchaSitekey = auth0Config.captchaSitekey
		    model.emailLogo = customizationConfig.emailLogo
		    model.googleAnalyticsTracking = customizationConfig.googleAnalyticsTracking
		    model.guestAutoLogin = customizationConfig.guestAutoLogin
		    model.instanceName = customizationConfig.instanceName
		    model.instanceType = customizationConfig.instanceType
		    model.isTOS = customizationConfig.isTOS
		    model.principal = securityService.principal()
		    model.supportEmail = customizationConfig.supportEmail
		    model.useRecaptcha = auth0Config.useRecaptcha
		    model.userGuideUrl = customizationConfig.userGuideUrl
		}
		true
	    }
	}

	/**
	 * Automatically store some config vars in the GSP model for all urls.
	 */
	commonModel(controller: '*', action: '*') {
	    after = { Map model ->
		if (model != null) {
		    model.auth0Active = active
		}
		true
	    }
	}
    }
}
