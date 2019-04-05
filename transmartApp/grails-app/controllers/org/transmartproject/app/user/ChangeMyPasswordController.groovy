package org.transmartproject.app.user

import grails.plugin.springsecurity.SpringSecurityService
import grails.validation.Validateable
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.web.servlet.support.RequestContextUtils
import org.transmart.plugin.shared.SecurityService
import org.transmart.searchapp.AuthUser
import org.springframework.security.authentication.encoding.PasswordEncoder

class ChangeMyPasswordController {

    static allowedMethods = [save: 'POST']
    static defaultAction = 'show'

    @Autowired private MessageSource messageSource
    @Autowired private SecurityService securityService
    @Autowired private SpringSecurityService springSecurityService

    def show() {}

    def save(ChangePasswordCommand command) {
        if (command.hasErrors()) {
            render(view: 'show', model: [command: command])
        }
        else {
	    AuthUser currentUser = AuthUser.get(securityService.currentUserId())
            currentUser.passwd = springSecurityService.encodePassword(command.newPassword)
            currentUser.changePassword = false
            currentUser.save(flush: true)

            if (currentUser.hasErrors()) {
		command.errors.reject 'ChangePassword.couldNotSave'
		render view: 'show', model: [command: command]
            }
            else {
                flash.message = messageSource.getMessage('ChangePassword.savedSuccessfully',
							 null, RequestContextUtils.getLocale(request))
		redirect action: 'show'
            }
        }
    }
}

@Validateable
class ChangePasswordCommand {

    GrailsApplication grailsApplication
    PasswordEncoder passwordEncoder
    SpringSecurityService springSecurityService

    String oldPassword
    String newPassword
    String newPasswordRepeated

    static constraints = {
	oldPassword blank: false, validator: { String oldPassword, ChangePasswordCommand command ->
	    if (!command.passwordEncoder.isPasswordValid(
		command.springSecurityService.currentUser.getPersistentValue('passwd'), oldPassword, null)) {
                'doesNotMatch'
            }
	}

	newPassword blank: false, validator: { String newPassword, ChangePasswordCommand command ->
	    if (newPassword == command.oldPassword) {
                'hasToBeChanged'
	    }
	    else if (command.grailsApplication.config.user.password.strength.regex.with { it && !(newPassword ==~ it) }) {
                'lowPasswordStrength'
            }
	}

	newPasswordRepeated blank: false, validator: { String newPassword2, ChangePasswordCommand command ->
	    if (newPassword2 != command.newPassword) {
                'doesNotEqual'
            }
	}
    }
}
