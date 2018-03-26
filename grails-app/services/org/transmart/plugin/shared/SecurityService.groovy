package org.transmart.plugin.shared

import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class SecurityService {

	static transactional = false

	@Autowired private SpringSecurityService springSecurityService

	/**
	 * @return the current auth if authenticated
	 */
	Authentication authentication() {
		springSecurityService.getAuthentication()
	}

	/**
	 * @return the current auth principal if authenticated
	 */
	UserDetails principal() {
		loggedIn() ? (UserDetails) springSecurityService.getPrincipal() : null
	}

	/**
	 * @return the current auth username if authenticated
	 */
	String currentUsername() {
		principal()?.username
	}

	/**
	 * @return the id of the AuthUser instance for the currently authenticated user.
	 */
	long currentUserId() {
		(long) springSecurityService.getCurrentUserId()
	}

	/**
	 * @return true if authenticated and not anonymous.
	 */
	boolean loggedIn() {
		springSecurityService.isLoggedIn()
	}

	/**
	 * Build an <code>Authentication</code> for the given username and register
	 * it in the security context.
	 */
	void authenticateAs(String username) {
		springSecurityService.reauthenticate username
	}
}
