package org.transmartproject.app.user

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.springframework.security.authentication.encoding.PasswordEncoder
import org.springframework.validation.FieldError
import spock.lang.Specification

@TestMixin(ControllerUnitTestMixin)
class ChangeMyPasswordCommandSpec extends Specification {

	private ChangePasswordCommand testee = mockCommandObject(ChangePasswordCommand)
	private boolean passwordValid = true

	void setup() {
		testee.springSecurityService = [getCurrentUser: [getPersistentValue: { 'test' }]] as SpringSecurityService
		testee.passwordEncoder = [isPasswordValid: { String encPass, String rawPass, salt -> passwordValid }] as PasswordEncoder
		grailsApplication.config.clear()
	}

	void 'test old password does not match'() {
		when:
		passwordValid = false
		testee.oldPassword = 'old'
		testee.newPassword = 'new'
		testee.newPasswordRepeated = 'new'

		then:
		!testee.validate()
		testee.errors.allErrors.size() == 1
		testee.errors.fieldErrors.find { FieldError error -> error.field == 'oldPassword' }
	}

	void 'test new password is the same as old one'() {
		when:
		testee.oldPassword = 'test'
		testee.newPassword = 'test'
		testee.newPasswordRepeated = 'test'

		then:
		!testee.validate()
		testee.errors.allErrors.size() == 1
		testee.errors.fieldErrors.find { FieldError error -> error.field == 'newPassword' }
	}

	void 'test low password strength'() {
		when:
		grailsApplication.config.user.password.strength.regex = '^TEST.*$'
		testee.oldPassword = 'old'
		testee.newPassword = 'new'
		testee.newPasswordRepeated = 'new'

		then:
		!testee.validate()
		testee.errors.allErrors.size() == 1
		testee.errors.fieldErrors.find { FieldError error -> error.field == 'newPassword' }
	}

	void 'test repeat does not match'() {
		when:
		testee.oldPassword = 'old'
		testee.newPassword = 'new'
		testee.newPasswordRepeated = 'new2'

		then:
		!testee.validate()
		testee.errors.allErrors.size() == 1
		testee.errors.fieldErrors.find { FieldError error -> error.field == 'newPasswordRepeated' }
	}

	void 'test success'() {
		when:
		grailsApplication.config.user.password.strength.regex = '^n.*w$'
		testee.oldPassword = 'old'
		testee.newPassword = 'new'
		testee.newPasswordRepeated = 'new'

		then:
		testee.validate()
	}
}
