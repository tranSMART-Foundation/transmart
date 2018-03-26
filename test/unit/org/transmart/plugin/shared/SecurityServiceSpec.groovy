package org.transmart.plugin.shared

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.test.mixin.TestFor
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.cache.NullUserCache
import spock.lang.Specification

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@TestFor(SecurityService)
class SecurityServiceSpec extends Specification {

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
		User principal = new User('username', 'password', [])
		SecurityContextHolder.context.authentication =
				new UsernamePasswordAuthenticationToken(principal, 'credentials')

		expect:
		principal.is service.principal()
	}

	void 'test currentUsername'() {
		given:
		String username = 'username!'
		SecurityContextHolder.context.authentication =
				new UsernamePasswordAuthenticationToken(
						new User(username, 'password', []), 'credentials')

		expect:
		username == service.currentUsername()
	}

	void 'test currentUserId'() {
		given:
		long id = 42
		SecurityContextHolder.context.authentication =
				new UsernamePasswordAuthenticationToken(
						new GrailsUser('username', 'password', true,
								true, true, true, [], id),
						'credentials')

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
		String username = 'username!!'
		SpringSecurityUtils.application = grailsApplication
		service.authenticateAs username

		then:
		service.loggedIn()
		username == service.currentUsername()
	}
}
