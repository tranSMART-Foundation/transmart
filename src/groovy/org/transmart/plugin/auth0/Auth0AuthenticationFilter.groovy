package org.transmart.plugin.auth0

import groovy.transform.CompileStatic
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.web.filter.GenericFilterBean

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.regex.Pattern

/**
 * Intercepts the JWT in the HTTP header and attempts authentication (delegates to the authentication manager).
 * Based on com.auth0.spring.security.api.Auth0AuthenticationFilter from auth0-spring-security-api.
 *
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class Auth0AuthenticationFilter extends GenericFilterBean {

	protected static final Pattern bearerPattern = Pattern.compile('^Bearer$', Pattern.CASE_INSENSITIVE)

	AuthenticationManager authenticationManager
	AuthenticationEntryPoint entryPoint

	void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req
		HttpServletResponse response = (HttpServletResponse) res
		if (request.method == 'OPTIONS') {
			// CORS / X-domain pre-flight request
			chain.doFilter request, response
			return
		}

		if (!SecurityContextHolder.context.authentication) {
			String jwt = getToken(request)
			if (jwt) {
				try {
					SecurityContextHolder.context.authentication = authenticationManager.authenticate(
							new Auth0JWTToken(jwt))
				}
				catch (AuthenticationException e) {
					SecurityContextHolder.clearContext()
					entryPoint.commence request, response, e
					return
				}
			}
		}

		chain.doFilter request, response
	}

	/**
	 * Looks at the authorization bearer http header and extracts the JWT.
	 */
	protected String getToken(HttpServletRequest request) {
		String authorizationHeader = request.getHeader('authorization')
		if (!authorizationHeader) {
			return null
		}

		String[] parts = authorizationHeader.split(' ')
		if (parts.length != 2) {
			return null
		}

		String scheme = parts[0]
		String credentials = parts[1]

		bearerPattern.matcher(scheme).matches() ? credentials : null
	}
}
