package org.transmart.plugin.auth0

import grails.plugin.springsecurity.userdetails.NoStackUsernameNotFoundException
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class Auth0AuthenticationEntryPointSpec extends Specification {

    private Auth0AuthenticationEntryPoint entryPoint = new Auth0AuthenticationEntryPoint()
    private MockHttpServletRequest request = new MockHttpServletRequest()
    private MockHttpServletResponse response = new MockHttpServletResponse()

    void 'test commence for options request'() {
	when:
	    request.method = 'OPTIONS'
	entryPoint.commence request, response, null

	then:
	    response.status == 204
    }

    void 'test commence for Auth0TokenException'() {
	when:
	    entryPoint.commence request, response, new Auth0TokenException(new NullPointerException())

	then:
	    response.status == 401
    }

    void 'test commence for other exception'() {
	when:
	    entryPoint.commence request, response, new NoStackUsernameNotFoundException()

	then:
	    response.status == 403
    }
}
