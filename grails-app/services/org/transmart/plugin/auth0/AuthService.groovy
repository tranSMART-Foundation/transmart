package org.transmart.plugin.auth0

import grails.plugin.springsecurity.userdetails.NoStackUsernameNotFoundException
import grails.transaction.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.web.context.request.RequestContextHolder
import org.transmart.plugin.custom.Roles
import org.transmart.plugin.shared.SecurityService
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.Role

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j('logger')
class AuthService {

	protected static final GrantedAuthority PUBLIC_USER = new SimpleGrantedAuthority(Roles.PUBLIC_USER.authority)

	@Autowired private List<LogoutHandler> logoutHandlers
	@Autowired private SecurityService securityService

	/**
	 * Find the <code>AuthUser</code> with the specified unique id and build
	 * an <code>AuthUserDetails</code> instance from it.
	 */
	@Transactional(readOnly = true, noRollbackFor = [IllegalArgumentException, UsernameNotFoundException])
	AuthUserDetails loadAuthUserDetailsByUniqueId(String uniqueId) throws UsernameNotFoundException {
		AuthUser authUser = findBy('uniqueId', uniqueId)
		if (authUser) {
			userDetails authUser
		}
		else {
			logger.warn 'No AuthUser found with uniqueId/subject "{}"', uniqueId
			throw new NoStackUsernameNotFoundException()
		}
	}

	/**
	 * Find the <code>AuthUser</code> with the specified username and build
	 * an <code>AuthUserDetails</code> instance from it.
	 */
	@Transactional(readOnly = true, noRollbackFor = [IllegalArgumentException, UsernameNotFoundException])
	AuthUserDetails loadAuthUserDetails(String username) throws UsernameNotFoundException {
		AuthUser authUser = authUser(username)
		if (authUser) {
			userDetails authUser
		}
		else {
			logger.warn 'No AuthUser found with username "{}"', username
			throw new NoStackUsernameNotFoundException()
		}
	}

	@CompileDynamic // TODO use GrailsCompileStatic
	protected AuthUser findBy(String name, String value) {
		AuthUser.createCriteria().get {
			eq name, value
			cache true
		}
	}

	protected AuthUserDetails userDetails(AuthUser authUser) {
		List<GrantedAuthority> authorities = []
		for (Role role in authUser.authorities) {
			authorities << new SimpleGrantedAuthority(role.authority)
		}
		if (!authorities) {
			authorities << PUBLIC_USER // TODO
		}

		new AuthUserDetails(authUser.username, authUser.passwd, authUser.enabled, true /*!user.accountExpired*/,
				true, true /*!user.accountLocked*/, authorities,
				authUser.id, authUser.userRealName)
	}

	/**
	 * @return the <code>AuthUser</code> instance for the currently authenticated user.
	 */
	AuthUser currentAuthUser() {
		authUser securityService.currentUsername()
	}

	/**
	 * Retrieve the <code>AuthUser</code> for the specified username.
	 * The query cache is used to reduce unnecessary database calls.
	 */
	AuthUser authUser(String username) {
		findBy 'username', username
	}

	void logout() {
		Authentication authentication = SecurityContextHolder.context.authentication
		if (authentication) {
			HttpServletRequest request = currentRequest()
			HttpServletResponse response = currentResponse()
			for (LogoutHandler handler in logoutHandlers) {
				handler.logout request, response, authentication
			}
		}
	}

	@Transactional
	void grantRoles(AuthUser user, Roles... roles) {
		for (Roles role in roles) {
			grantRole user, role
		}
	}

	@CompileDynamic // TODO use GrailsCompileStatic
	protected void grantRole(AuthUser user, Roles role) {
		Role.createCriteria().get {
			eq 'authority', role.authority
			cache true
			lock true
		}.addToPeople user
	}

	protected HttpServletRequest currentRequest() {
		currentRequestAttributes().request
	}

	protected HttpServletResponse currentResponse() {
		currentRequestAttributes().response
	}

	protected GrailsWebRequest currentRequestAttributes() {
		(GrailsWebRequest) RequestContextHolder.currentRequestAttributes()
	}
}
