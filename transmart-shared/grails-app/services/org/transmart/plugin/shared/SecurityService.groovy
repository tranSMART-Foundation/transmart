package org.transmart.plugin.shared

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileStatic
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.transmart.plugin.shared.security.AuthUserDetails

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class SecurityService implements InitializingBean {

    static transactional = false

    @Autowired private SpringSecurityService springSecurityService

    @Value('${grails.plugin.springsecurity.auth0.clientId:}')
    private String clientId

    @Value('${grails.plugin.springsecurity.auth0.clientSecret:}')
    private String clientSecret

    private Algorithm algorithm

    /**
     * @return the current auth if authenticated
     */
    Authentication authentication() {
	springSecurityService.getAuthentication()
    }

    /**
     * @return the current auth principal if authenticated
     */
    AuthUserDetails principal() {
	loggedIn() ? (AuthUserDetails) springSecurityService.getPrincipal() : null
    }

    /**
     * @return the current auth username if authenticated
     */
    String currentUsername() {
	principal()?.username
    }

    /**
     * @return the id of the AuthUser instance for the currently authenticated user.
     */
    long currentUserId() {
	principal()?.authUserId ?: -1
    }

    /**
     * @return true if authenticated and not anonymous.
     */
    boolean loggedIn() {
	springSecurityService.isLoggedIn()
    }

    /**
     * Build an <code>Authentication</code> for the given username and register
     * it in the security context.
     */
    void authenticateAs(String username) {
	springSecurityService.reauthenticate username
    }

    String jwtToken() {
	String email = principal().email
	Date now = new Date()
	JWT.create()
	    .withAudience(clientId)      // 'aud'
	    .withExpiresAt(now + 1)      // 'exp'
	    .withIssuedAt(now)           // 'iat'
	    .withIssuer(getClass().name) // 'iss'
	    .withSubject(email)          // 'sub'
	    .withClaim('email', email)
	    .sign(algorithm)
    }

    void afterPropertiesSet() {
	algorithm = Algorithm.HMAC256(clientSecret)
    }
}
