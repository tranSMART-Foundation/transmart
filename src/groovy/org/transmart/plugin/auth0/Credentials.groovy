package org.transmart.plugin.auth0

import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.transmart.plugin.custom.UserLevel

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
@ToString(includeNames = true)
class Credentials implements Serializable {
	private static final long serialVersionUID = 1

	String accessToken
	String connection
	String email
	Long id
	String idToken
	UserLevel level
	String name
	String nickname
	String picture = ''
	Boolean tosVerified
	String username
	String uniqueId
}
