package org.transmart.plugin.auth0

import groovy.transform.CompileStatic
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED

/**
 * Based on com.auth0.spring.security.api.Auth0AuthenticationEntryPoint from auth0-spring-security-api.
 *
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class Auth0AuthenticationEntryPoint implements AuthenticationEntryPoint {

	void commence(HttpServletRequest request, HttpServletResponse response,
	              AuthenticationException e) throws IOException, ServletException {

		if ('OPTIONS' == request.method) {
			// X-domain pre-flight request
			response.status = SC_NO_CONTENT
		}
		else if (e instanceof Auth0TokenException) {
			response.sendError SC_UNAUTHORIZED, e.message
		}
		else {
			response.sendError SC_FORBIDDEN, e.message
		}
	}
}
