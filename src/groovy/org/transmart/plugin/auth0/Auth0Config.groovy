package org.transmart.plugin.auth0

import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.transmart.plugin.custom.CustomizationConfig

import javax.servlet.ServletContext

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class Auth0Config implements InitializingBean {

	String auth0CallbackUrl
	String auth0ClientId
	String auth0ClientSecret
	String auth0Domain
	String redirectOnLogout
	String redirectOnSuccess
	boolean useRecaptcha

	String webtaskBaseUrl

	@Value('${edu.harvard.transmart.captcha.secret:}')
	String captchaSecret

	@Value('${edu.harvard.transmart.captcha.sitekey:MissingCaptchaSiteKey}')
	String captchaSitekey

	@Value('${edu.harvard.transmart.captcha.verifyurl:}')
	String captchaVerifyUrl

	@Autowired private AuthService authService
	@Autowired private CustomizationConfig customizationConfig
	@Autowired private ServletContext servletContext

	@CompileDynamic
	void afterPropertiesSet() {
		def conf = SpringSecurityUtils.securityConfig
		redirectOnLogout = conf.logout.filterProcessesUrl
		redirectOnSuccess = conf.successHandler.defaultTargetUrl

		conf = conf.auth0
		auth0CallbackUrl = conf.loginCallback
		auth0ClientId = conf.clientId
		auth0ClientSecret = conf.clientSecret
		auth0Domain = conf.domain
		useRecaptcha = conf.useRecaptcha
		webtaskBaseUrl = conf.webtaskBaseUrl
	}
}
