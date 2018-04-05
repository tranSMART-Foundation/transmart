import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import org.apache.commons.codec.binary.Base64
import org.springframework.util.Assert
import org.transmart.plugin.auth0.Auth0AuthenticationEntryPoint
import org.transmart.plugin.auth0.Auth0AuthenticationFilter
import org.transmart.plugin.auth0.Auth0AuthenticationProvider
import org.transmart.plugin.auth0.Auth0Config

class SpringSecurityAuth0GrailsPlugin {
	String version = '18.1-SNAPSHOT'
	String grailsVersion = '2.3 > *'
	String title = 'Spring Security Auth0 Plugin'
	String author = 'Burt Beckwith'
	String authorEmail = 'burt_beckwith@hms.harvard.edu'
	String description = 'Auth0 security for Transmart.'
	String documentation = 'TODO'
	String license = 'APACHE'
	def loadAfter = ['springSecurityCore']
	def organization = [name: 'TODO', url: 'TODO']
	def issueManagement = [url: 'TODO']
	def scm = [url: 'TODO']

	def doWithSpring = {
		def conf = SpringSecurityUtils.securityConfig
		if (!conf || !conf.active) {
			return
		}

		SpringSecurityUtils.loadSecondaryConfig 'DefaultAuth0SecurityConfig'
		// have to get again after overlaying DefaultAuth0SecurityConfig
		conf = SpringSecurityUtils.securityConfig

		if (!conf.auth0.active) {
			return
		}

		boolean printStatusMessages = (conf.printStatusMessages instanceof Boolean) ? conf.printStatusMessages : true

		if (printStatusMessages) {
			println '\nConfiguring Transmart Spring Security Auth0 ...'
		}

		getRequiredConfigString conf, 'clientId'
		getRequiredConfigString conf, 'domain'
		String clientSecret = getRequiredConfigString(conf, 'clientSecret')

		if (conf.auth0.base64EncodedSecret) {
			clientSecret = new Base64(true).decodeBase64(clientSecret)
			conf.auth0.clientSecret = clientSecret
		}

		auth0AuthenticationFilter(Auth0AuthenticationFilter) {
			authenticationManager = ref('authenticationManager')
			entryPoint = ref('auth0EntryPoint')
		}

		auth0AuthenticationProvider(Auth0AuthenticationProvider) {
			authService = ref('authService')
		}

		auth0Config(Auth0Config)

		auth0EntryPoint(Auth0AuthenticationEntryPoint)

		SpringSecurityUtils.registerProvider 'auth0AuthenticationProvider'
		SpringSecurityUtils.registerFilter 'auth0AuthenticationFilter',
				SecurityFilterPosition.OPENID_FILTER

		if (printStatusMessages) {
			println '... finished configuring Transmart Spring Security Auth0\n'
		}
	}

	private String getRequiredConfigString(ConfigObject conf, String propertyName) {
		String value = conf.auth0[propertyName] ?: ''
		Assert.hasText value, 'grails.plugin.springsecurity.auth0.' + propertyName + ' must be set'
		value
	}
}
