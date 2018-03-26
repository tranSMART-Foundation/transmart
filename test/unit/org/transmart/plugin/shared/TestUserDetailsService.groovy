package org.transmart.plugin.shared

import groovy.transform.CompileStatic
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class TestUserDetailsService implements UserDetailsService {
	UserDetails loadUserByUsername(String username) {
		new User(username, username, [])
	}
}
