package org.transmart.plugin.auth0

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.TestFor
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.transmart.plugin.shared.SecurityService
import spock.lang.Specification

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@TestFor(AuthTagLib)
class AuthTagLibSpec extends Specification {

	void setupSpec() {
		defineBeans {
			authService(AuthService)
			securityContextLogoutHandler(SecurityContextLogoutHandler)
			securityService(SecurityService)
			springSecurityService(SpringSecurityService)
		}
		applicationContext.springSecurityService.authenticationTrustResolver = new AuthenticationTrustResolverImpl()
	}

	void cleanup() {
		SecurityContextHolder.context.authentication = null
	}

	void 'test ifLevelZero'() {
		expect:
		applyTemplate('<auth:ifLevelZero>foo</auth:ifLevelZero>') == ''

		when:
		authenticate Roles.PUBLIC_USER

		then:
		applyTemplate('<auth:ifLevelZero>foo</auth:ifLevelZero>') == 'foo'

		when:
		authenticate Roles.ADMIN

		then:
		applyTemplate('<auth:ifLevelZero>foo</auth:ifLevelZero>') == ''
	}

	void 'test ifLevelOne'() {
		expect:
		applyTemplate('<auth:ifLevelOne>foo</auth:ifLevelOne>') == ''

		when:
		authenticate Roles.PUBLIC_USER

		then:
		applyTemplate('<auth:ifLevelOne>foo</auth:ifLevelOne>') == ''

		when:
		authenticate Roles.STUDY_OWNER

		then:
		applyTemplate('<auth:ifLevelOne>foo</auth:ifLevelOne>') == 'foo'

		when:
		authenticate Roles.ADMIN

		then:
		applyTemplate('<auth:ifLevelOne>foo</auth:ifLevelOne>') == ''
	}

	void 'test ifLevelTwo'() {
		expect:
		applyTemplate('<auth:ifLevelTwo>foo</auth:ifLevelTwo>') == ''

		when:
		authenticate Roles.PUBLIC_USER

		then:
		applyTemplate('<auth:ifLevelTwo>foo</auth:ifLevelTwo>') == ''

		when:
		authenticate Roles.STUDY_OWNER

		then:
		applyTemplate('<auth:ifLevelTwo>foo</auth:ifLevelTwo>') == ''

		when:
		authenticate Roles.DATASET_EXPLORER_ADMIN

		then:
		applyTemplate('<auth:ifLevelTwo>foo</auth:ifLevelTwo>') == 'foo'

		when:
		authenticate Roles.ADMIN

		then:
		applyTemplate('<auth:ifLevelTwo>foo</auth:ifLevelTwo>') == ''
	}

	void 'test ifLevelAdmin'() {
		expect:
		applyTemplate('<auth:ifLevelAdmin>foo</auth:ifLevelAdmin>') == ''

		when:
		authenticate Roles.PUBLIC_USER

		then:
		applyTemplate('<auth:ifLevelAdmin>foo</auth:ifLevelAdmin>') == ''

		when:
		authenticate Roles.DATASET_EXPLORER_ADMIN

		then:
		applyTemplate('<auth:ifLevelAdmin>foo</auth:ifLevelAdmin>') == ''

		when:
		authenticate Roles.ADMIN

		then:
		applyTemplate('<auth:ifLevelAdmin>foo</auth:ifLevelAdmin>') == 'foo'
	}

	void 'test ifLevelUnregistered'() {
		expect:
		applyTemplate('<auth:ifLevelUnregistered>foo</auth:ifLevelUnregistered>') == 'foo'

		when:
		authenticate Roles.PUBLIC_USER

		then:
		applyTemplate('<auth:ifLevelUnregistered>foo</auth:ifLevelUnregistered>') == ''

		when:
		authenticate Roles.ADMIN

		then:
		applyTemplate('<auth:ifLevelUnregistered>foo</auth:ifLevelUnregistered>') == ''
	}

	void 'test userLevel'() {
		expect:
		applyTemplate('<auth:userLevel />').toString() == UserLevel.UNREGISTERED.toString()

		when:
		authenticate Roles.PUBLIC_USER

		then:
		applyTemplate('<auth:userLevel />').toString() == UserLevel.ZERO.toString()

		when:
		authenticate Roles.STUDY_OWNER

		then:
		applyTemplate('<auth:userLevel />').toString() == UserLevel.ONE.toString()

		when:
		authenticate Roles.DATASET_EXPLORER_ADMIN

		then:
		applyTemplate('<auth:userLevel />').toString() == UserLevel.TWO.toString()

		when:
		authenticate Roles.ADMIN

		then:
		applyTemplate('<auth:userLevel />').toString() == UserLevel.ADMIN.toString()
	}

	private void authenticate(Roles... roles) {
		List<GrantedAuthority> authorities = []
		for (Roles role in roles) {
			authorities << new SimpleGrantedAuthority(role.authority)
		}
		AuthUserDetails principal = new AuthUserDetails('username', 'password', true,
				true, true, true, authorities, 123, 'userRealName')
		SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken(principal, 'password', authorities)
	}
}
