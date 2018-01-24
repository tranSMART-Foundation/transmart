package org.transmart.plugin.auth0

import org.springframework.beans.factory.annotation.Value

/**
 * @author <a href='mailto:burt_beckwith@hms.harvard.edu'>Burt Beckwith</a>
 */
class AuthTagLib {

	static namespace = 'auth'
	static returnObjectForTags = ['setting', 'settingValue', 'userLevel',
	                              'userSetting', 'userSettingValue']

	AuthService authService
	Auth0Config auth0Config

	@Value('${edu.harvard.transmart.instance.userguideurl:http://s3.amazon.com/dbmi-public-docs/i2b2_transmart_userguide.pdf}')
	private String userGuideUrl

	@Value('${edu.harvard.transmart.instance.userguideurl:https://s3.amazonaws.com/hms-dbmi-docs/GRDR_User_Guide.pdf}')
	private String userGuideUrlGRDR

	/**
	 * Renders the body if the current user has level 0.
	 */
	def ifLevelZero = { attrs, body ->
		ifLevel UserLevel.ZERO, body
	}

	/**
	 * Renders the body if the current user has level 1.
	 */
	def ifLevelOne = { attrs, body ->
		ifLevel UserLevel.ONE, body
	}

	/**
	 * Renders the body if the current user has level 2.
	 */
	def ifLevelTwo = { attrs, body ->
		ifLevel UserLevel.TWO, body
	}

	/**
	 * Renders the body if the current user has level admin.
	 */
	def ifLevelAdmin = { attrs, body ->
		ifLevel UserLevel.ADMIN, body
	}

	/**
	 * Renders the body if the current user is unregistered.
	 */
	def ifLevelUnregistered = { attrs, body ->
		ifLevel UserLevel.UNREGISTERED, body
	}

	/**
	 * @return the <code>UserLevel</code> for the currently authenticated user.
	 */
	def userLevel = {
		authService.currentUserLevel()
	}

	/**
	 * @attr name REQUIRED the setting name
	 * @return the <code>Settings</code> instance for the currently authenticated user.
	 */
	def userSetting = { attrs ->
		String name = assertAttribute('name', attrs, 'userSetting')
		authService.userSetting name
	}

	/**
	 * @attr name REQUIRED the setting name
	 * @return the <code>Settings</code> instance <code>fieldvalue</code> (or an
	 *         empty String if not found) for the currently authenticated user.
	 */
	def userSettingValue = { attrs ->
		userSetting(attrs)?.fieldvalue ?: ''
	}

	/**
	 * @attr name REQUIRED the setting name
	 * @return the shared <code>Settings</code> instance.
	 */
	def setting = { attrs ->
		String name = assertAttribute('name', attrs, 'setting')
		authService.setting name
	}

	/**
	 * @attr name REQUIRED the setting name
	 * @return the shared <code>Settings</code> instance <code>fieldvalue</code>
	 *         (or an empty String if not found).
	 */
	def settingValue = { attrs ->
		setting(attrs)?.fieldvalue ?: ''
	}

	// UICustomizationTagLib start

	def useAccessLevelDescription = {
		out << (auth0Config.instanceType == 'grdr' ? 'Open Data Access' : 'Level 1 Access')
	}

	def useAccessLevelMemo = {
		if (auth0Config.instanceType == 'grdr') {
			out << '<div class="description"><p>Although <i>Open Data</i> access does not require prior permission, authorization or review, you are required to <br />register and provide the minimal information as requested in the form below.</p></div>'
		}
	}

	def useRegisterButton = {
		if (auth0Config.instanceType == 'pmsdn') {
			out << "I agree to the terms and register"
		}
		else {
			out << "Register"
		}
	}

	def useUserGuide = {
		if (auth0Config.instanceType == 'grdr') {
			out << '''<b>NIH NCATS User's Guide</b> is available here: <br /><br /><a href="''' + userGuideUrlGRDR + '">' + userGuideUrlGRDR + '</a><br />'
		}
		else {
			out << 'To learn more about the application, please visit <a href="' + userGuideUrl + '">' + userGuideUrl + '</a>.'
		}
	}

	// UICustomizationTagLib end

	protected void ifLevel(UserLevel level, Closure body) {
		if (authService.currentUserLevel() == level) {
			out << body()
		}
	}

	protected assertAttribute(String name, attrs, String tag) {
		if (!attrs.containsKey(name)) {
			throwTagError "Tag [$tag] is missing required attribute [$name]"
		}
		attrs.remove name
	}
}
