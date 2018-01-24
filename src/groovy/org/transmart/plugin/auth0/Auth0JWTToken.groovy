package org.transmart.plugin.auth0

import com.auth0.jwt.JWT
import com.auth0.jwt.interfaces.DecodedJWT
import groovy.transform.CompileStatic
import org.spockframework.util.Assert
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

/**
 * Based on com.auth0.spring.security.api.Auth0JWTToken from auth0-spring-security-api.
 *
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class Auth0JWTToken extends AbstractAuthenticationToken {
	private static final long serialVersionUID = 1

	AuthUserDetails principal

	Auth0JWTToken(String jwt) {
		super(null)
		Assert.notNull jwt, 'JWT token is required for authentication'
		details = JWT.decode(jwt)
	}

	String getCredentials() { decodedJWT.token }

	Collection<GrantedAuthority> getAuthorities() {
		(Collection<GrantedAuthority>) principal?.authorities
	}

	DecodedJWT getDecodedJWT() {
		(DecodedJWT) details
	}

	boolean hasExpired() {
		decodedJWT?.expiresAt?.before new Date()
	}

	boolean isAuthenticated() {
		super.authenticated && !hasExpired()
	}
}
