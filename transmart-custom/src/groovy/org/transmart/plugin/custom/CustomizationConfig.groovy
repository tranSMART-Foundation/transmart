package org.transmart.plugin.custom

import groovy.transform.CompileStatic
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import javax.servlet.ServletContext

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
@CompileStatic
class CustomizationConfig implements InitializingBean {

	boolean isTOS
	String uiHeroImageUrl

	@Value('${edu.harvard.transmart.access.level1:}')
	String accessLevel1

	@Value('${com.recomdata.appTitle:}')
	String appTitle

	@Value('${edu.harvard.transmart.email.logo:}')
	String emailLogo

	@Value('${edu.harvard.transmart.email.notify:}')
	String emailNotify

	@Value('${edu.harvard.transmart.googleanalytics.tracking:}')
	String googleAnalyticsTracking

	@Value('${com.recomdata.guestAutoLogin:false}')
	boolean guestAutoLogin

	@Value('${com.recomdata.guestUserName:}')
	String guestUserName

	@Value('${edu.harvard.transmart.instance.name:i2b2/tranSMART}')
	String instanceName

	@Value('${edu.harvard.transmart.instance.type:}')
	String instanceType

	@Value('${loginBannerMessage:Please Login Below.}')
	String loginBannerMessage

	@Value('${com.recomdata.contactUs:}')
	String supportEmail

	@Value('${com.recomdata.userSignupEnabled:true}')
	boolean userSignupEnabled

	@Value('${edu.harvard.transmart.instance.quickstarturl:}')
	String quickStartUrl

	@Value('${edu.harvard.transmart.instance.userguideurl:}')
	String userGuideUrl

	@Autowired private CustomizationService customizationService
	@Autowired private ServletContext servletContext

	void afterPropertiesSet() {
		uiHeroImageUrl = servletContext.contextPath + '/images/transmartlogoHMS.jpg'
	}

	/**
	 * Initialization that cannot happen in <code>afterPropertiesSet()</code>
	 * (e.g. GORM calls) because not everything is ready yet.
	 */
	void init() {
		isTOS = customizationService.setting('tos.text') != null
	}
}
