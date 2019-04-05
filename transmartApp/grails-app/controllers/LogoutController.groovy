import grails.plugin.springsecurity.SpringSecurityUtils
import org.transmartproject.db.log.AccessLogService

/**
 * Logout Controller just writes an entry to the log and redirects to the login page (Identity Vault or form based)
 */
class LogoutController {

    AccessLogService accessLogService

    /**
     * Index action. Redirects to the Spring security logout uri.
     */
    def index() {
	accessLogService.report 'Logout', null
        redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl
    }
}
