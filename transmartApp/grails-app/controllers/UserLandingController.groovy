import grails.plugin.springsecurity.SpringSecurityService
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.web.servlet.support.RequestContextUtils
import org.transmartproject.db.log.AccessLogService

class UserLandingController implements InitializingBean {

    @Autowired private AccessLogService accessLogService
    @Autowired private MessageSource messageSource
    @Autowired private SpringSecurityService springSecurityService

    @Value('${com.recomdata.skipdisclaimer:false}')
    private boolean skipdisclaimer

    private String userLandingPath

    def index() {
	accessLogService.report 'Login', request.getHeader('user-agent')
	if (skipdisclaimer) {
	    checkChangePassword()
        }
        else {
	    redirect uri: '/userLanding/disclaimer.gsp'
        }
    }

    def agree() {
	accessLogService.report 'Disclaimer accepted', null
	checkChangePassword()
    }

    private checkChangePassword() {
	if (springSecurityService.currentUser?.changePassword) {
	    flash.message = messageSource.getMessage('changePassword', null, RequestContextUtils.getLocale(request))
	    redirect controller: 'changeMyPassword'
        }
        else {
	    redirect uri: userLandingPath
        }
    }

    def disagree() {
	accessLogService.report 'Disclaimer not accepted', null
	redirect uri: '/logout'
    }

    def checkHeartBeat() {
	render  'OK'
    }

    void afterPropertiesSet() {
	userLandingPath = grailsApplication.config.com.recomdata.defaults.landing ?:
	    grailsApplication.config.ui.tabs.browse.hide ?
	    '/datasetExplorer' :
	    '/RWG'
    }
}
