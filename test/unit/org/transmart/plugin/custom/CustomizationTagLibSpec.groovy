package org.transmart.plugin.custom

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.TestFor
import org.springframework.security.authentication.AuthenticationTrustResolverImpl
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.security.AuthUserDetails
import org.transmart.plugin.shared.security.Roles
import spock.lang.Specification

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@TestFor(CustomizationTagLib)
class CustomizationTagLibSpec extends Specification {

	void setupSpec() {
		defineBeans {
			customizationService(CustomizationService)
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
		applyTemplate('<transmart:ifLevelZero>foo</transmart:ifLevelZero>') == ''

		when:
		authenticate Roles.PUBLIC_USER

		then:
		applyTemplate('<transmart:ifLevelZero>foo</transmart:ifLevelZero>') == 'foo'

		when:
		authenticate Roles.ADMIN

		then:
		applyTemplate('<transmart:ifLevelZero>foo</transmart:ifLevelZero>') == ''
	}

	void 'test ifLevelOne'() {
		expect:
		applyTemplate('<transmart:ifLevelOne>foo</transmart:ifLevelOne>') == ''

		when:
		authenticate Roles.PUBLIC_USER

		then:
		applyTemplate('<transmart:ifLevelOne>foo</transmart:ifLevelOne>') == ''

		when:
		authenticate Roles.STUDY_OWNER

		then:
		applyTemplate('<transmart:ifLevelOne>foo</transmart:ifLevelOne>') == 'foo'

		when:
		authenticate Roles.ADMIN

		then:
		applyTemplate('<transmart:ifLevelOne>foo</transmart:ifLevelOne>') == ''
	}

	void 'test ifLevelTwo'() {
		expect:
		applyTemplate('<transmart:ifLevelTwo>foo</transmart:ifLevelTwo>') == ''

		when:
		authenticate Roles.PUBLIC_USER

		then:
		applyTemplate('<transmart:ifLevelTwo>foo</transmart:ifLevelTwo>') == ''

		when:
		authenticate Roles.STUDY_OWNER

		then:
		applyTemplate('<transmart:ifLevelTwo>foo</transmart:ifLevelTwo>') == ''

		when:
		authenticate Roles.DATASET_EXPLORER_ADMIN

		then:
		applyTemplate('<transmart:ifLevelTwo>foo</transmart:ifLevelTwo>') == 'foo'

		when:
		authenticate Roles.ADMIN

		then:
		applyTemplate('<transmart:ifLevelTwo>foo</transmart:ifLevelTwo>') == ''
	}

	void 'test ifLevelAdmin'() {
		expect:
		applyTemplate('<transmart:ifLevelAdmin>foo</transmart:ifLevelAdmin>') == ''

		when:
		authenticate Roles.PUBLIC_USER

		then:
		applyTemplate('<transmart:ifLevelAdmin>foo</transmart:ifLevelAdmin>') == ''

		when:
		authenticate Roles.DATASET_EXPLORER_ADMIN

		then:
		applyTemplate('<transmart:ifLevelAdmin>foo</transmart:ifLevelAdmin>') == ''

		when:
		authenticate Roles.ADMIN

		then:
		applyTemplate('<transmart:ifLevelAdmin>foo</transmart:ifLevelAdmin>') == 'foo'
	}

	void 'test ifLevelUnregistered'() {
		expect:
		applyTemplate('<transmart:ifLevelUnregistered>foo</transmart:ifLevelUnregistered>') == 'foo'

		when:
		authenticate Roles.PUBLIC_USER

		then:
		applyTemplate('<transmart:ifLevelUnregistered>foo</transmart:ifLevelUnregistered>') == ''

		when:
		authenticate Roles.ADMIN

		then:
		applyTemplate('<transmart:ifLevelUnregistered>foo</transmart:ifLevelUnregistered>') == ''
	}

	void 'test userLevel'() {
		expect:
		applyTemplate('<transmart:userLevel />').toString() == UserLevel.UNREGISTERED.toString()

		when:
		authenticate Roles.PUBLIC_USER

		then:
		applyTemplate('<transmart:userLevel />').toString() == UserLevel.ZERO.toString()

		when:
		authenticate Roles.STUDY_OWNER

		then:
		applyTemplate('<transmart:userLevel />').toString() == UserLevel.ONE.toString()

		when:
		authenticate Roles.DATASET_EXPLORER_ADMIN

		then:
		applyTemplate('<transmart:userLevel />').toString() == UserLevel.TWO.toString()

		when:
		authenticate Roles.ADMIN

		then:
		applyTemplate('<transmart:userLevel />').toString() == UserLevel.ADMIN.toString()
	}

	private void authenticate(Roles... roles) {
		List<GrantedAuthority> authorities = []
		for (Roles role in roles) {
			authorities << new SimpleGrantedAuthority(role.authority)
		}
		SecurityContextHolder.context.authentication = new UsernamePasswordAuthenticationToken(
				new AuthUserDetails('username', 'password',
						true, true, true, true,
						authorities, 1, 'userRealName'), 'password', authorities)
	}
}
