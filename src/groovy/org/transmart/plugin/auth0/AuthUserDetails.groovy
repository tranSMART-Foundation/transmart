package org.transmart.plugin.auth0

import grails.plugin.springsecurity.userdetails.GrailsUser
import groovy.transform.CompileStatic
import org.springframework.security.core.GrantedAuthority

@CompileStatic
class AuthUserDetails extends GrailsUser {

	final String userRealName

	AuthUserDetails(String username, String password, boolean enabled, boolean accountNonExpired,
	                boolean credentialsNonExpired, boolean accountNonLocked, Collection<GrantedAuthority> authorities,
	                long id, String userRealName) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, id)

		this.userRealName = userRealName
	}
}
