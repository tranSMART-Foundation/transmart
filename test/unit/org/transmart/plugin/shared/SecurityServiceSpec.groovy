package org.transmart.plugin.shared

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.mixin.TestFor
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.cache.NullUserCache
import org.transmart.plugin.shared.security.AuthUserDetails
import spock.lang.Specification

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@TestFor(SecurityService)
class SecurityServiceSpec extends Specification {

	private static final String username = 'username!'
	private static final long id = 42

	void setupSpec() {
		defineBeans {
			authenticationTrustResolver AuthenticationTrustResolverImpl
			springSecurityService SpringSecurityService
			userCache NullUserCache
			userDetailsService TestUserDetailsService
		}
	}

	void setup() {
		service.springSecurityService = applicationContext.springSecurityService
		service.springSecurityService.authenticationTrustResolver = applicationContext.authenticationTrustResolver
	}

	void cleanup() {
		SecurityContextHolder.context.authentication = null
	}

	void 'test authentication'() {
		given:
		Authentication auth = new UsernamePasswordAuthenticationToken('principal', 'credentials')
		SecurityContextHolder.context.authentication = auth

		expect:
		auth.is service.authentication()
	}

	void 'test principal'() {
		given:
		def principal = newAuthUserDetails()
		SecurityContextHolder.context.authentication =
				new UsernamePasswordAuthenticationToken(principal, 'credentials')

		expect:
		principal.is service.principal()
	}

	void 'test currentUsername'() {
		given:
		SecurityContextHolder.context.authentication =
				new UsernamePasswordAuthenticationToken(newAuthUserDetails(), 'credentials')

		expect:
		username == service.currentUsername()
	}

	void 'test currentUserId'() {
		given:
		SecurityContextHolder.context.authentication =
				new UsernamePasswordAuthenticationToken(newAuthUserDetails(), 'credentials')

		expect:
		id == service.currentUserId()
	}

	void 'test loggedIn'() {
		expect:
		!service.loggedIn()

		when:
		SecurityContextHolder.context.authentication =
				new UsernamePasswordAuthenticationToken('principal', 'credentials')

		then:
		service.loggedIn()
	}

	void 'test authenticateAs'() {
		when:
		SpringSecurityUtils.application = grailsApplication
		service.authenticateAs username

		then:
		service.loggedIn()
		username == applicationContext.springSecurityService.principal.username
	}

	private AuthUserDetails newAuthUserDetails() {
		new AuthUserDetails(username, 'password',
				true, true, true, true,
				[], id, 'userRealName')

	}
}
