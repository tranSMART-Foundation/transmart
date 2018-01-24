package org.transmart.plugin.auth0

import com.auth0.jwt.exceptions.JWTVerificationException
import groovy.transform.CompileStatic
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException

import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SignatureException

/**
 * Based on com.auth0.spring.security.api.Auth0AuthenticationProvider from auth0-spring-security-api.
 *
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class Auth0AuthenticationProvider implements AuthenticationProvider {

	AuthService authService

	Authentication authenticate(Authentication authentication) throws AuthenticationException {
		try {
			Auth0JWTToken tokenAuth = ((Auth0JWTToken) authentication)
			tokenAuth.principal = authService.loadAuthUserDetailsByUniqueId(tokenAuth.decodedJWT.subject)
			tokenAuth.authenticated = true
			authentication
		}
		catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException |
		       SignatureException | IOException | JWTVerificationException e) {
			throw new Auth0TokenException(e)
		}
	}

	boolean supports(Class<?> authentication) {
		Auth0JWTToken.isAssignableFrom authentication
	}
}
