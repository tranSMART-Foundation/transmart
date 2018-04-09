package org.transmart.plugin.shared.security

import grails.plugin.springsecurity.userdetails.GrailsUser
import groovy.transform.CompileStatic
import org.springframework.security.core.GrantedAuthority

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class AuthUserDetails extends GrailsUser {

	final String userRealName

	AuthUserDetails(String username, String password, boolean enabled,
	                boolean accountNonExpired, boolean credentialsNonExpired,
	                boolean accountNonLocked, Collection<GrantedAuthority> authorities,
	                long id, String userRealName) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired,
				accountNonLocked, authorities, id)
		this.userRealName = userRealName
	}

	/**
	 * @return true if ROLE_ADMIN is granted
	 */
	boolean isAdmin() {
		hasRole Roles.ADMIN
	}

	/**
	 * @return true if ROLE_DATASET_EXPLORER_ADMIN is granted
	 */
	boolean isDseAdmin() {
		hasRole Roles.DATASET_EXPLORER_ADMIN
	}

	boolean isAdminOrDseAdmin() {
		isAdmin() || isDseAdmin()
	}

	/**
	 * @return the id of the AuthUser domain class instance that was used to populate this.
	 */
	long getAuthUserId() {
		(long) id
	}

	protected boolean hasRole(Roles role) {
		for (GrantedAuthority auth in (Collection<GrantedAuthority>) authorities) {
			if (auth.authority == role.authority) {
				return true
			}
		}
		false
	}
}
