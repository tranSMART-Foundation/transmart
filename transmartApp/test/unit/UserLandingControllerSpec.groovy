import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.TestFor
import org.transmart.plugin.shared.SecurityService
import org.transmart.plugin.shared.UtilService
import org.transmartproject.db.log.AccessLogService
import spock.lang.Specification

@TestFor(UserLandingController)
class UserLandingControllerSpec extends Specification {

	void setupSpec() {
		defineBeans {
			accessLogService(AccessLogService)
			securityService(SecurityService)
			sprintSecurityService(SpringSecurityService)
			utilService(UtilService)
		}
	}

	void setup() {
		grailsApplication.config.clear()
	}

	void 'test default landing page'() {
		expect:
		'/RWG' == controller.userLandingPath
	}

	void 'test hide browse tab'() {
		when:
		grailsApplication.config.ui.tabs.browse.hide = true

		then:
		'/datasetExplorer' == controller.userLandingPath
	}

	void 'test preset landing page'() {
		when:
		String expectedPath = '/custom-path'
		grailsApplication.config.com.recomdata.defaults.landing = expectedPath

		then:
		expectedPath == controller.userLandingPath
	}
}
