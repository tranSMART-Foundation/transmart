package org.transmart.plugin.auth0

import groovy.transform.CompileStatic
import org.springframework.security.core.AuthenticationException

/**
 * Based on com.auth0.spring.security.api.Auth0TokenException from auth0-spring-security-api.
 *
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class Auth0TokenException extends AuthenticationException {
	Auth0TokenException(Exception cause) {
		super(cause.getClass().simpleName + ' thrown while decoding JWT token: ' +
				cause.localizedMessage, cause)
	}
}
